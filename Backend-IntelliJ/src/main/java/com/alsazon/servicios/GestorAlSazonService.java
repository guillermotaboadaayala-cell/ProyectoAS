package com.alsazon.servicios;

import com.alsazon.entidades.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class GestorAlSazonService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ApplicationContext context;

    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        try {
            context.getBean(GestorAlSazonService.class).asegurarDatosIniciales();
        } catch (Exception ignored) {}
    }

    @Transactional
    public void asegurarDatosIniciales() {
        try {
            em.createNativeQuery("ALTER TABLE Flujo_Caja MODIFY COLUMN tipo_movimiento VARCHAR(50) NOT NULL").executeUpdate();
            em.createNativeQuery("ALTER TABLE Productos MODIFY COLUMN tipo VARCHAR(30) NOT NULL").executeUpdate();
            em.createNativeQuery("ALTER TABLE Productos MODIFY COLUMN unidad_medida VARCHAR(30) NOT NULL").executeUpdate();
        } catch (Exception ignored) {}

        Long admins = em.createQuery("SELECT COUNT(a) FROM Administrador a WHERE a.pinAcceso = '0691'", Long.class).getSingleResult();
        if (admins == 0) {
            Administrador a = new Administrador();
            a.setNombre("Administrador Al Sazon");
            a.setCorreo("admin@alsazon.mx");
            a.setPinAcceso("0691");
            em.persist(a);
        }

        Long prods = em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class).getSingleResult();
        if (prods == 0) {
            crearProdSemilla("Carnitas Maciza / Surtida (Kg)", "Carne", "Kg", 280.0);
            crearProdSemilla("Orden de Tacos de Carnitas", "Carne", "Unidad", 75.0);
            Producto pCoca = crearProdSemilla("Coca-Cola 600ml", "Refresco", "Unidad", 25.0);
            Producto pGuay = crearProdSemilla("Del Valle Guayaba", "Refresco", "Unidad", 25.0);
            Producto pMang = crearProdSemilla("Del Valle Mango", "Refresco", "Unidad", 25.0);
            Producto pCiel = crearProdSemilla("Ciel Mineral", "Refresco", "Unidad", 22.0);
            crearProdSemilla("Manteca de Cerdo (Kg)", "Insumo", "Kg", 65.0);

            crearLoteSemilla(pCoca, 60.0, 45.0, LocalDate.now().plusDays(12), 900.0);
            crearLoteSemilla(pGuay, 60.0, 12.0, LocalDate.now().plusDays(5), 850.0);
            crearLoteSemilla(pMang, 60.0, 11.0, LocalDate.now().plusDays(6), 850.0);
            crearLoteSemilla(pCiel, 60.0, 10.0, LocalDate.now().plusDays(10), 720.0);
        }
    }

    private Producto crearProdSemilla(String nombre, String tipo, String unidad, Double precio) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setTipo(tipo);
        p.setUnidadMedida(unidad);
        p.setPrecioVenta(precio);
        em.persist(p);
        return p;
    }

    private void crearLoteSemilla(Producto p, Double ini, Double act, LocalDate cad, Double costo) {
        Lote l = new Lote();
        l.setProducto(p);
        l.setCantidadInicial(ini);
        l.setCantidadActual(act);
        l.setFechaIngreso(LocalDate.now());
        l.setFechaCaducidad(cad);
        l.setCostoTotal(costo);
        em.persist(l);
    }

    @Transactional(readOnly = true)
    public boolean validarPin(String pin) {
        if ("0691".equals(pin)) return true;
        Long count = em.createQuery("SELECT COUNT(a) FROM Administrador a WHERE a.pinAcceso = :pin", Long.class)
                .setParameter("pin", pin)
                .getSingleResult();
        return count > 0;
    }

    @Transactional(readOnly = true)
    public List obtenerProductosCatalogo() {
        List<Producto> lista = em.createQuery("SELECT p FROM Producto p ORDER BY p.idProducto ASC", Producto.class).getResultList();
        List resultado = new ArrayList();
        for (Producto p : lista) {
            Map m = new HashMap();
            m.put("idProducto", p.getIdProducto());
            m.put("nombre", p.getNombre());
            m.put("tipo", p.getTipo());
            m.put("unidad", p.getUnidadMedida());
            m.put("precio", p.getPrecioVenta());
            resultado.add(m);
        }
        return resultado;
    }

    @Transactional
    public List obtenerLotes() {
        em.createQuery("DELETE FROM Lote l WHERE l.cantidadActual <= 0").executeUpdate();

        List<Lote> lotes = em.createQuery("SELECT l FROM Lote l JOIN FETCH l.producto WHERE l.cantidadActual > 0 ORDER BY l.fechaCaducidad ASC", Lote.class).getResultList();
        List salida = new ArrayList();
        LocalDate hoy = LocalDate.now();

        for (Lote l : lotes) {
            Map m = new HashMap();
            m.put("idLote", l.getIdLote());
            m.put("idProducto", l.getProducto().getIdProducto());
            m.put("nombreProducto", l.getProducto().getNombre());
            m.put("tipoProducto", l.getProducto().getTipo());
            m.put("cantidadInicial", l.getCantidadInicial());
            m.put("cantidadActual", l.getCantidadActual());
            m.put("fechaIngreso", l.getFechaIngreso().toString());
            m.put("fechaCaducidad", l.getFechaCaducidad().toString());
            m.put("costoTotal", l.getCostoTotal());
            long dias = ChronoUnit.DAYS.between(hoy, l.getFechaCaducidad());
            m.put("diasParaCaducar", (int) dias);
            salida.add(m);
        }
        return salida;
    }

    @Transactional
    public void registrarNuevoProductoYLote(String nombre, double cantidad, String categoria, double precio, String fechaCad, double costo) {
        if (nombre == null || nombre.trim().isEmpty() || cantidad <= 0 || costo < 0) {
            throw new IllegalArgumentException("Verifica los datos: nombre obligatorio y cantidades mayores a cero.");
        }
        String unidad = "Refresco".equalsIgnoreCase(categoria) ? "Unidad" : "Kg";
        List<Producto> existentes = em.createQuery("SELECT p FROM Producto p WHERE LOWER(p.nombre) = LOWER(:nom)", Producto.class)
                .setParameter("nom", nombre.trim())
                .getResultList();

        Producto prod;
        if (existentes.isEmpty()) {
            prod = new Producto();
            prod.setNombre(nombre.trim());
            prod.setTipo(categoria);
            prod.setUnidadMedida(unidad);
            prod.setPrecioVenta(precio > 0 ? precio : 50.0);
            em.persist(prod);
        } else {
            prod = existentes.get(0);
        }

        Lote lote = new Lote();
        lote.setProducto(prod);
        lote.setCantidadInicial(cantidad);
        lote.setCantidadActual(cantidad);
        lote.setFechaIngreso(LocalDate.now());
        lote.setFechaCaducidad(LocalDate.parse(fechaCad));
        lote.setCostoTotal(costo);
        em.persist(lote);

        if (costo > 0) {
            FlujoCaja fc = new FlujoCaja();
            fc.setTipoMovimiento("Gasto_Insumo");
            fc.setMonto(costo);
            fc.setFechaHora(LocalDateTime.now());
            fc.setConcepto("Alta de lote: " + prod.getNombre());
            em.persist(fc);
        }
    }

    @Transactional
    public void descontarManualOArbitrario(int idLote, double cantidadARestar) {
        if (cantidadARestar <= 0) throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a 0.");
        Lote lote = em.find(Lote.class, idLote);
        if (lote == null) throw new IllegalArgumentException("El lote #" + idLote + " no existe.");
        if (cantidadARestar > lote.getCantidadActual()) {
            throw new IllegalArgumentException("No puedes descontar mas de lo que tiene el lote (" + lote.getCantidadActual() + ").");
        }
        double nuevoStock = lote.getCantidadActual() - cantidadARestar;
        if (nuevoStock <= 0.0001) {
            em.remove(lote);
        } else {
            lote.setCantidadActual(nuevoStock);
            em.merge(lote);
        }
    }

    @Transactional
    public void modificarLote(int idLote, double cantActual, String nuevaCaducidad, double nuevoCosto) {
        if (cantActual < 0 || nuevoCosto < 0) throw new IllegalArgumentException("No se permiten cantidades negativas.");
        Lote lote = em.find(Lote.class, idLote);
        if (lote == null) throw new IllegalArgumentException("El lote #" + idLote + " no existe.");
        if (cantActual == 0) {
            em.remove(lote);
        } else {
            lote.setCantidadActual(cantActual);
            lote.setFechaCaducidad(LocalDate.parse(nuevaCaducidad));
            lote.setCostoTotal(nuevoCosto);
            em.merge(lote);
        }
    }

    @Transactional
    public void procesarVentaConFIFO(JsonObject body) {
        double montoTotal = body.get("montoTotal").getAsDouble();
        if (montoTotal <= 0) throw new IllegalArgumentException("El monto de la venta debe ser mayor a cero.");

        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setMontoTotal(montoTotal);
        em.persist(venta);

        if (body.has("detalles") && body.get("detalles").isJsonArray()) {
            JsonArray detalles = body.getAsJsonArray("detalles");
            for (JsonElement elem : detalles) {
                JsonObject detObj = elem.getAsJsonObject();
                int idProd = detObj.get("idProducto").getAsInt();
                double cantVendida = detObj.get("cantidadVendida").getAsDouble();
                double subtotal = detObj.get("subtotal").getAsDouble();

                Producto prod = em.find(Producto.class, idProd);
                if (prod == null) throw new IllegalArgumentException("Producto no encontrado: " + idProd);

                if ("Refresco".equalsIgnoreCase(prod.getTipo())) {
                    descontarFIFOHibernate(idProd, cantVendida);
                }

                DetalleVenta dv = new DetalleVenta();
                dv.setVenta(venta);
                dv.setProducto(prod);
                dv.setCantidadVendida(cantVendida);
                dv.setSubtotal(subtotal);
                em.persist(dv);
            }
        }

        FlujoCaja fc = new FlujoCaja();
        fc.setTipoMovimiento("Ingreso_Venta");
        fc.setMonto(montoTotal);
        fc.setFechaHora(LocalDateTime.now());
        fc.setConcepto("Venta #" + venta.getIdVenta());
        em.persist(fc);
    }

    private void descontarFIFOHibernate(int idProducto, double cantidadVendida) {
        List<Lote> lotes = em.createQuery(
                "SELECT l FROM Lote l WHERE l.producto.idProducto = :idProd AND l.cantidadActual > 0 ORDER BY l.fechaCaducidad ASC", Lote.class)
                .setParameter("idProd", idProducto)
                .getResultList();

        double restante = cantidadVendida;
        for (Lote lote : lotes) {
            if (restante <= 0) break;
            double disp = lote.getCantidadActual();
            if (disp > restante) {
                lote.setCantidadActual(disp - restante);
                em.merge(lote);
                restante = 0;
            } else {
                restante -= disp;
                em.remove(lote);
            }
        }
        if (restante > 0.0001) {
            throw new IllegalArgumentException("Inventario insuficiente en lotes de refresco para cubrir la venta.");
        }
    }

    @Transactional(readOnly = true)
    public List obtenerHistorialVentas() {
        List<Venta> ventas = em.createQuery("SELECT v FROM Venta v ORDER BY v.idVenta DESC", Venta.class).getResultList();
        List salida = new ArrayList();
        for (Venta v : ventas) {
            List<DetalleVenta> dets = em.createQuery("SELECT d FROM DetalleVenta d JOIN FETCH d.producto WHERE d.venta.idVenta = :idV", DetalleVenta.class)
                    .setParameter("idV", v.getIdVenta())
                    .getResultList();
            List<String> partes = new ArrayList<>();
            for (DetalleVenta d : dets) {
                partes.add(d.getCantidadVendida() + "x " + d.getProducto().getNombre());
            }
            Map m = new HashMap();
            m.put("idVenta", v.getIdVenta());
            m.put("fechaHora", v.getFechaHora() != null ? v.getFechaHora().format(FMT_FECHA_HORA) : LocalDate.now().toString());
            m.put("montoTotal", v.getMontoTotal());
            m.put("resumenProductos", partes.isEmpty() ? "Venta general" : String.join(", ", partes));
            salida.add(m);
        }
        return salida;
    }

    @Transactional
    public void modificarVenta(int idVenta, double nuevoMonto) {
        if (nuevoMonto < 0) throw new IllegalArgumentException("El monto no puede ser negativo.");
        Venta v = em.find(Venta.class, idVenta);
        if (v != null) {
            v.setMontoTotal(nuevoMonto);
            em.merge(v);
        }
    }

    @Transactional
    public void registrarGastoYCrearLote(int idProducto, double cantidad, String fechaCaducidad, double costoTotal, String concepto) {
        if (cantidad <= 0 || costoTotal <= 0) throw new IllegalArgumentException("Cantidad y costo deben ser mayores a 0.");
        Producto prod = em.find(Producto.class, idProducto);
        if (prod == null) throw new IllegalArgumentException("Producto no encontrado.");

        Lote l = new Lote();
        l.setProducto(prod);
        l.setCantidadInicial(cantidad);
        l.setCantidadActual(cantidad);
        l.setFechaIngreso(LocalDate.now());
        l.setFechaCaducidad(LocalDate.parse(fechaCaducidad));
        l.setCostoTotal(costoTotal);
        em.persist(l);

        FlujoCaja fc = new FlujoCaja();
        fc.setTipoMovimiento("Gasto_Insumo");
        fc.setMonto(costoTotal);
        fc.setFechaHora(LocalDateTime.now());
        fc.setConcepto(concepto);
        em.persist(fc);
    }

    @Transactional
    public void registrarRetiroCaja(double monto, String motivo) {
        if (monto <= 0) throw new IllegalArgumentException("El monto a retirar de caja debe ser mayor a 0.");
        if (motivo == null || motivo.trim().isEmpty()) throw new IllegalArgumentException("Especifica el motivo del retiro.");

        FlujoCaja fc = new FlujoCaja();
        fc.setTipoMovimiento("Retiro_Caja");
        fc.setMonto(monto);
        fc.setFechaHora(LocalDateTime.now());
        fc.setConcepto(motivo.trim());
        em.persist(fc);
    }

    @Transactional
    public void ajustarPresupuestoBase(double nuevoMontoBase) {
        if (nuevoMontoBase < 0) throw new IllegalArgumentException("El presupuesto no puede ser negativo.");
        em.createQuery("DELETE FROM FlujoCaja f WHERE f.tipoMovimiento = 'Ajuste'").executeUpdate();
        FlujoCaja fc = new FlujoCaja();
        fc.setTipoMovimiento("Ajuste");
        fc.setMonto(nuevoMontoBase);
        fc.setFechaHora(LocalDateTime.now());
        fc.setConcepto("Presupuesto Inicial / Base");
        em.persist(fc);
    }

    @Transactional
    public Map obtenerFinanzasYCorte(Double efectivoDeclarado, Double retiroEnCorte, String motivoRetiro) {
        if (retiroEnCorte != null && retiroEnCorte > 0) {
            registrarRetiroCaja(retiroEnCorte, motivoRetiro != null && !motivoRetiro.isEmpty() ? motivoRetiro : "Retiro en Corte");
        }

        Double base = em.createQuery("SELECT COALESCE(SUM(f.monto), 0.0) FROM FlujoCaja f WHERE f.tipoMovimiento = 'Ajuste'", Double.class).getSingleResult();
        Double ingresos = em.createQuery("SELECT COALESCE(SUM(v.montoTotal), 0.0) FROM Venta v", Double.class).getSingleResult();
        Double gastosLotes = em.createQuery("SELECT COALESCE(SUM(f.monto), 0.0) FROM FlujoCaja f WHERE f.tipoMovimiento = 'Gasto_Insumo'", Double.class).getSingleResult();
        if (gastosLotes == 0) {
            gastosLotes = em.createQuery("SELECT COALESCE(SUM(l.costoTotal), 0.0) FROM Lote l", Double.class).getSingleResult();
        }
        Double retiros = em.createQuery("SELECT COALESCE(SUM(f.monto), 0.0) FROM FlujoCaja f WHERE f.tipoMovimiento = 'Retiro_Caja'", Double.class).getSingleResult();

        double salidasTotales = gastosLotes + retiros;
        double presupuestoActual = base + ingresos - salidasTotales;
        double diferenciaIngresosVsGastos = ingresos - salidasTotales;
        double porcentajeGanancia = ingresos > 0 ? ((ingresos - salidasTotales) / ingresos) * 100.0 : 0.0;

        Map data = new HashMap();
        data.put("presupuestoBase", base);
        data.put("ingresosTotales", ingresos);
        data.put("gastosTotales", gastosLotes);
        data.put("retirosTotales", retiros);
        data.put("salidasTotales", salidasTotales);
        data.put("presupuestoActual", presupuestoActual);
        data.put("diferenciaIngresosVsGastos", diferenciaIngresosVsGastos);
        data.put("porcentajeGanancia", Math.round(porcentajeGanancia));

        if (efectivoDeclarado != null) {
            double descuadre = efectivoDeclarado - presupuestoActual;
            data.put("efectivoDeclarado", efectivoDeclarado);
            data.put("descuadre", descuadre);

            FlujoCaja corte = new FlujoCaja();
            corte.setTipoMovimiento("Corte_Z");
            corte.setMonto(presupuestoActual);
            corte.setFechaHora(LocalDateTime.now());
            corte.setConcepto("Cierre de Caja X/Z");
            corte.setEfectivoDeclarado(efectivoDeclarado);
            corte.setDescuadre(descuadre);
            em.persist(corte);
        }
        return data;
    }

    @Transactional(readOnly = true)
    public Map obtenerReporteEjecutivo() {
        Map rep = new HashMap();
        List<Producto> prods = em.createQuery("SELECT p FROM Producto p ORDER BY p.idProducto ASC", Producto.class).getResultList();
        List porProducto = new ArrayList();

        for (Producto p : prods) {
            Object[] sums = (Object[]) em.createQuery(
                    "SELECT COALESCE(SUM(d.cantidadVendida), 0.0), COALESCE(SUM(d.subtotal), 0.0) FROM DetalleVenta d WHERE d.producto.idProducto = :idP")
                    .setParameter("idP", p.getIdProducto())
                    .getSingleResult();
            Map m = new HashMap();
            m.put("producto", p.getNombre());
            m.put("cantidad", sums[0]);
            m.put("total", sums[1]);
            porProducto.add(m);
        }
        rep.put("ventasPorProducto", porProducto);

        List<Object[]> dias = em.createNativeQuery(
                "SELECT DATE(fecha_hora) as dia, SUM(monto_total) as total FROM Ventas GROUP BY DATE(fecha_hora) ORDER BY total DESC LIMIT 1")
                .getResultList();
        if (!dias.isEmpty() && dias.get(0)[0] != null) {
            rep.put("diaMayorConsumo", dias.get(0)[0].toString() + " ($" + dias.get(0)[1].toString() + ")");
        } else {
            rep.put("diaMayorConsumo", "Sin registros");
        }

        Double merma = em.createQuery("SELECT COALESCE(SUM(l.cantidadActual), 0.0) FROM Lote l WHERE l.fechaCaducidad < :hoy", Double.class)
                .setParameter("hoy", LocalDate.now())
                .getSingleResult();
        rep.put("mermaCaducada", merma);

        return rep;
    }
}