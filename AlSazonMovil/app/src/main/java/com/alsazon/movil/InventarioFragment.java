package com.alsazon.movil;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

public class InventarioFragment extends Fragment {

    private RadioGroup rgFiltro;
    private ListView lvInventario;
    private TextView tvAlertaRefrescos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_inventario, container, false);

        rgFiltro = vista.findViewById(R.id.rgFiltroInventario);
        lvInventario = vista.findViewById(R.id.lvInventario);
        tvAlertaRefrescos = vista.findViewById(R.id.tvAlertaRefrescos);

        DatosAlSazon.cargarDatosRespaldo();

        if (DatosAlSazon.hayAlertaRefrescos) {
            tvAlertaRefrescos.setText("⚠️ FALTAN REFRESCOS (STOCK AL 25% O MENOS)");
        } else {
            tvAlertaRefrescos.setText("✅ STOCK DE LOTES EN NIVEL ÓPTIMO");
        }

        llenarLista(DatosAlSazon.listaTodosLotes);

        rgFiltro.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbTodos) {
                    llenarLista(DatosAlSazon.listaTodosLotes);
                } else if (checkedId == R.id.rbRefrescos) {
                    llenarLista(DatosAlSazon.listaSoloRefrescos);
                } else if (checkedId == R.id.rbCarne) {
                    llenarLista(DatosAlSazon.listaSoloCarne);
                }
            }
        });

        lvInventario.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                Toast.makeText(getContext(), "Modo Consulta:\n" + item, Toast.LENGTH_SHORT).show();
            }
        });

        return vista;
    }

    private void llenarLista(ArrayList<String> datos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, datos) {
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
        lvInventario.setAdapter(adapter);
    }
}