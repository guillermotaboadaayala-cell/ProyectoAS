package com.alsazon.movil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class ReporteFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_reporte, container, false);

        TextView tvLabelVerde = vista.findViewById(R.id.tvLabelVerde);
        TextView tvLabelRojo = vista.findViewById(R.id.tvLabelRojo);
        ProgressBar barraVerde = vista.findViewById(R.id.barraVerdeVentas);
        ProgressBar barraRoja = vista.findViewById(R.id.barraRojaGastos);
        TextView tvResumenCorte = vista.findViewById(R.id.tvResumenCorte);

        double ventas = DatosAlSazon.ingresosTotales;
        double salidas = DatosAlSazon.gastosLotes + DatosAlSazon.retirosCaja;
        double diferencia = ventas - salidas;
        double maximo = Math.max(ventas, Math.max(salidas, 1.0));

        tvLabelVerde.setText("VENTAS / INGRESOS (VERDE): $" + String.format("%.2f", ventas));
        tvLabelRojo.setText("GASTOS LOTES + RETIROS (ROJO): $" + String.format("%.2f", salidas));

        barraVerde.setProgress((int) ((ventas / maximo) * 100));
        barraRoja.setProgress((int) ((salidas / maximo) * 100));

        String resumen = "COMPARATIVA DE CORTES Y REPORTE GENERAL:\n\n"
                + "• TOTAL DE VENTAS (INGRESOS): $" + String.format("%.2f", ventas) + "\n"
                + "• GASTOS EN INSUMOS (LOTES): $" + String.format("%.2f", DatosAlSazon.gastosLotes) + "\n"
                + "• RETIROS (GANANCIAS / SUELDOS): $" + String.format("%.2f", DatosAlSazon.retirosCaja) + "\n"
                + "--------------------------------------------------\n"
                + "• DIFERENCIA (INGRESOS VS GASTADO): $" + String.format("%.2f", diferencia) + "\n"
                + "• SALDO ESPERADO EN CAJA: $" + String.format("%.2f", DatosAlSazon.presupuestoEnCaja) + "\n"
                + "• DÍA DE MAYOR CONSUMO: " + DatosAlSazon.diaMayorConsumo + "\n"
                + "• MERMA EN LOTES CADUCADOS: " + DatosAlSazon.mermaCaducada + " unidades/Kg\n\n"
                + "TUS RESULTADOS INDICAN QUE EXISTE UNA GANANCIA DE " + DatosAlSazon.porcentajeGanancia + "%";

        tvResumenCorte.setText(resumen);

        return vista;
    }
}