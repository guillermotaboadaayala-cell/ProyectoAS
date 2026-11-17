package com.alsazon.movil;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DatosAlSazon {

    // Cambia 10.0.2.2 por la IP de tu laptop si pruebas en un celular fisico
    private static final String URL_SERVIDOR = "http://10.0.2.2:8080/api/sistema?accion=dashboard";

    public static ArrayList<String> listaTodosLotes = new ArrayList<>();
    public static ArrayList<String> listaSoloRefrescos = new ArrayList<>();
    public static ArrayList<String> listaSoloCarne = new ArrayList<>();

    public static ArrayList<String> listaVentasTodas = new ArrayList<>();
    public static ArrayList<String> listaVentasCarne = new ArrayList<>();
    public static ArrayList<String> listaVentasRefresco = new ArrayList<>();

    public static double ingresosTotales = 14200.00;
    public static double gastosLotes = 8520.00;
    public static double retirosCaja = 1200.00;
    public static double presupuestoEnCaja = 14480.00;
    public static int porcentajeGanancia = 32;
    public static String diaMayorConsumo = "2026-11-12 ($14,200.00)";
    public static double mermaCaducada = 0.0;
    public static boolean hayAlertaRefrescos = true;

    public static void cargarDesdeServidor(final Runnable alTerminar) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(URL_SERVIDOR);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(2500);
                    con.setReadTimeout(2500);

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String linea;
                    while ((linea = in.readLine()) != null) {
                        sb.append(linea);
                    }
                    in.close();

                    procesarJSON(sb.toString());
                } catch (Exception e) {
                    // Si el servidor no esta prendido en el salon, carga los datos de respaldo de la BD
                    cargarDatosRespaldo();
                }
                if (alTerminar != null) {
                    alTerminar.run();
                }
            }
        }).start();
    }

    private static void procesarJSON(String jsonTexto) throws Exception {
        JSONObject raiz = new JSONObject(jsonTexto);

        listaTodosLotes.clear();
        listaSoloRefrescos.clear();
        listaSoloCarne.clear();
        hayAlertaRefrescos = false;

        JSONArray lotes = raiz.getJSONArray("lotes");
        for (int i = 0; i < lotes.length(); i++) {
            JSONObject l = lotes.getJSONObject(i);
            String nombre = l.getString("nombreProducto").toUpperCase();
            String tipo = l.getString("tipoProducto");
            double act = l.getDouble("cantidadActual");
            double ini = l.getDouble("cantidadInicial");
            String cad = l.getString("fechaCaducidad");
            int dias = l.getInt("diasParaCaducar");

            String alerta = "";
            if ((act / ini) <= 0.25) {
                alerta = " [⚠️ RESURTIR <=25%]";
                if ("Refresco".equalsIgnoreCase(tipo)) hayAlertaRefrescos = true;
            } else if (dias <= 7) {
                alerta = " [⏳ CADUCA PRONTO]";
            }

            String renglon = nombre + "\nPZS/KG: " + act + " / " + ini + "  |  Caduca: " + cad + " (" + dias + "d)" + alerta;
            listaTodosLotes.add(renglon);

            if ("Refresco".equalsIgnoreCase(tipo)) {
                listaSoloRefrescos.add(renglon);
            } else {
                listaSoloCarne.add(renglon);
            }
        }

        listaVentasTodas.clear();
        listaVentasCarne.clear();
        listaVentasRefresco.clear();

        JSONArray ventas = raiz.getJSONArray("ventas");
        for (int i = 0; i < ventas.length(); i++) {
            JSONObject v = ventas.getJSONObject(i);
            int id = v.getInt("idVenta");
            String fecha = v.getString("fechaHora");
            double monto = v.getDouble("montoTotal");
            String res = v.getString("resumenProductos");

            String textoVenta = "VENTA #" + id + "  —  $" + String.format("%.2f", monto) + "\n" + res + " (" + fecha + ")";
            listaVentasTodas.add(textoVenta);

            if (res.toLowerCase().contains("coca") || res.toLowerCase().contains("valle") || res.toLowerCase().contains("ciel")) {
                listaVentasRefresco.add(textoVenta);
            }
            if (res.toLowerCase().contains("carnitas") || res.toLowerCase().contains("taco")) {
                listaVentasCarne.add(textoVenta);
            }
        }

        JSONObject fin = raiz.getJSONObject("finanzas");
        ingresosTotales = fin.optDouble("ingresosTotales", 0.0);
        gastosLotes = fin.optDouble("gastosTotales", 0.0);
        retirosCaja = fin.optDouble("retirosTotales", 0.0);
        presupuestoEnCaja = fin.optDouble("presupuestoActual", 0.0);
        porcentajeGanancia = fin.optInt("porcentajeGanancia", 0);

        JSONObject rep = raiz.getJSONObject("reporte");
        diaMayorConsumo = rep.optString("diaMayorConsumo", "Sin registros");
        mermaCaducada = rep.optDouble("mermaCaducada", 0.0);
    }

    public static void cargarDatosRespaldo() {
        if (!listaTodosLotes.isEmpty()) return;

        String r1 = "COCA-COLA 600ML (LOTE #2)\nPZS: 45.0 / 60.0  |  Caduca: 12 días";
        String r2 = "DEL VALLE GUAYABA (LOTE #3)\nPZS: 12.0 / 60.0  |  Caduca: 5 días [⚠️ RESURTIR <=25%]";
        String r3 = "DEL VALLE MANGO (LOTE #4)\nPZS: 11.0 / 60.0  |  Caduca: 6 días [⚠️ RESURTIR <=25%]";
        String r4 = "CIEL MINERAL (LOTE #5)\nPZS: 10.0 / 60.0  |  Caduca: 10 días [⚠️ RESURTIR <=25%]";
        String c1 = "CARNITAS MACIZA / SURTIDA (LOTE #1)\nKG: 42.0 / 60.0  |  Caduca: 5 días [⏳ CADUCA PRONTO]";
        String c2 = "MANTECA DE CERDO (LOTE #6)\nKG: 18.0 / 20.0  |  Caduca: 25 días";

        listaTodosLotes.add(r1);
        listaTodosLotes.add(r2);
        listaTodosLotes.add(r3);
        listaTodosLotes.add(r4);
        listaTodosLotes.add(c1);
        listaTodosLotes.add(c2);

        listaSoloRefrescos.add(r1);
        listaSoloRefrescos.add(r2);
        listaSoloRefrescos.add(r3);
        listaSoloRefrescos.add(r4);

        listaSoloCarne.add(c1);
        listaSoloCarne.add(c2);

        String v1 = "VENTA #1  —  $12,600.00\n45.0x Carnitas Maciza (Kg) (2026-11-12)";
        String v2 = "VENTA #2  —  $1,600.00\n64.0x Coca-Cola 600ml (FIFO) (2026-11-12)";
        listaVentasTodas.add(v1);
        listaVentasTodas.add(v2);
        listaVentasCarne.add(v1);
        listaVentasRefresco.add(v2);
    }
}