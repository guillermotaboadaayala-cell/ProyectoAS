package com.alsazon.controladores;

import com.alsazon.servicios.GestorAlSazonService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SistemaController {

    @Autowired
    private GestorAlSazonService service;

    private final Gson gson = new Gson();

    @GetMapping(value = "/api/sistema", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> obtenerDatos(@RequestParam(required = false) String accion) {
        try {
            if ("dashboard".equals(accion)) {
                Map<String, Object> todo = new HashMap<>();
                todo.put("productos", service.obtenerProductosCatalogo());
                todo.put("lotes", service.obtenerLotes());
                todo.put("ventas", service.obtenerHistorialVentas());
                todo.put("finanzas", service.obtenerFinanzasYCorte(null, null, null));
                todo.put("reporte", service.obtenerReporteEjecutivo());
                return ResponseEntity.ok(gson.toJson(todo));
            } else {
                return ResponseEntity.ok(gson.toJson(service.obtenerLotes()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping(value = "/api/inventario/alertas", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> obtenerAlertas() {
        try {
            return ResponseEntity.ok(gson.toJson(service.obtenerLotes()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping(value = "/api/ventas/procesar", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> procesarVenta(@RequestBody String rawJson) {
        try {
            JsonObject body = gson.fromJson(rawJson, JsonObject.class);
            service.procesarVentaConFIFO(body);
            return ResponseEntity.ok("{\"mensaje\":\"Venta registrada exitosamente. Refrescos descontados por FIFO.\"}");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping(value = "/api/sistema", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> ejecutarAccion(@RequestBody String rawJson) {
        try {
            JsonObject body = gson.fromJson(rawJson, JsonObject.class);
            String accion = body.get("accion").getAsString();

            switch (accion) {
                case "login":
                    boolean ok = service.validarPin(body.get("pin").getAsString());
                    if (ok) return ResponseEntity.ok("{\"acceso\":true}");
                    else return ResponseEntity.status(401).body("{\"error\":\"Credenciales incorrectas.\"}");

                case "registrarProductoYLote":
                    service.registrarNuevoProductoYLote(
                            body.get("nombre").getAsString(),
                            body.get("cantidad").getAsDouble(),
                            body.get("categoria").getAsString(),
                            body.get("precio").getAsDouble(),
                            body.get("fechaCaducidad").getAsString(),
                            body.get("costo").getAsDouble()
                    );
                    return ResponseEntity.ok("{\"mensaje\":\"Producto y lote registrados exitosamente.\"}");

                case "descontarLote":
                    service.descontarManualOArbitrario(body.get("idLote").getAsInt(), body.get("cantidad").getAsDouble());
                    return ResponseEntity.ok("{\"mensaje\":\"Consumo / Merma descontado del lote correctamente.\"}");

                case "editarLote":
                    service.modificarLote(
                            body.get("idLote").getAsInt(),
                            body.get("cantidadActual").getAsDouble(),
                            body.get("fechaCaducidad").getAsString(),
                            body.get("costoTotal").getAsDouble()
                    );
                    return ResponseEntity.ok("{\"mensaje\":\"Lote actualizado correctamente.\"}");

                case "gastoLote":
                    service.registrarGastoYCrearLote(
                            body.get("idProducto").getAsInt(),
                            body.get("cantidad").getAsDouble(),
                            body.get("fechaCaducidad").getAsString(),
                            body.get("costoTotal").getAsDouble(),
                            body.get("concepto").getAsString()
                    );
                    return ResponseEntity.ok("{\"mensaje\":\"Gasto registrado y nuevo Lote ingresado a Inventario.\"}");

                case "retiroCaja":
                    service.registrarRetiroCaja(
                            body.get("monto").getAsDouble(),
                            body.get("motivo").getAsString()
                    );
                    return ResponseEntity.ok("{\"mensaje\":\"Retiro de caja (Ganancias/Sueldos) registrado correctamente.\"}");

                case "ajustarPresupuesto":
                    service.ajustarPresupuestoBase(body.get("monto").getAsDouble());
                    return ResponseEntity.ok("{\"mensaje\":\"Presupuesto inicial actualizado.\"}");

                case "corteCaja":
                    Double retiro = body.has("retiroMonto") && !body.get("retiroMonto").isJsonNull() ? body.get("retiroMonto").getAsDouble() : 0.0;
                    String motivo = body.has("retiroMotivo") && !body.get("retiroMotivo").isJsonNull() ? body.get("retiroMotivo").getAsString() : "";
                    Map<String, Object> corte = service.obtenerFinanzasYCorte(body.get("efectivoDeclarado").getAsDouble(), retiro, motivo);
                    return ResponseEntity.ok(gson.toJson(corte));

                case "editarVenta":
                    service.modificarVenta(body.get("idVenta").getAsInt(), body.get("nuevoMonto").getAsDouble());
                    return ResponseEntity.ok("{\"mensaje\":\"Monto de venta corregido.\"}");

                default:
                    return ResponseEntity.status(400).body("{\"error\":\"Accion no reconocida\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}