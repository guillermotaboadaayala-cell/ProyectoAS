package com.alsazon.movil;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

public class VentasFragment extends Fragment {

    private Spinner spFiltroVentas;
    private ListView lvVentas;
    private TextView tvTotalVentasConsulta;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_ventas, container, false);

        spFiltroVentas = vista.findViewById(R.id.spFiltroVentas);
        lvVentas = vista.findViewById(R.id.lvVentas);
        tvTotalVentasConsulta = vista.findViewById(R.id.tvTotalVentasConsulta);

        DatosAlSazon.cargarDatosRespaldo();
        tvTotalVentasConsulta.setText("TOTAL DEL DÍA / ACUMULADO: $" + String.format("%.2f", DatosAlSazon.ingresosTotales));

        String[] opciones = {"Todas las ventas registradas", "Solo ventas de Carnitas / Tacos", "Solo ventas de Refrescos (FIFO)"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, opciones);
        spFiltroVentas.setAdapter(spAdapter);

        spFiltroVentas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    mostrarVentas(DatosAlSazon.listaVentasTodas);
                } else if (position == 1) {
                    mostrarVentas(DatosAlSazon.listaVentasCarne);
                } else {
                    mostrarVentas(DatosAlSazon.listaVentasRefresco);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return vista;
    }

    private void mostrarVentas(ArrayList<String> lista) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, lista) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = v.findViewById(android.R.id.text1);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(14f);
                tv.setPadding(16, 18, 16, 18);
                return v;
            }
        };
        lvVentas.setAdapter(adapter);
    }
}