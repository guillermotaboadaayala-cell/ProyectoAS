package com.alsazon.movil;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MenuActivity extends AppCompatActivity {

    private Button btnFragInventario, btnFragVentas, btnFragReportes, btnSalir;
    private TextView tvTituloTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnFragInventario = findViewById(R.id.btnFragInventario);
        btnFragVentas = findViewById(R.id.btnFragVentas);
        btnFragReportes = findViewById(R.id.btnFragReportes);
        btnSalir = findViewById(R.id.btnSalir);
        tvTituloTop = findViewById(R.id.tvTituloTop);

        // Cargar datos del servidor (o respaldo local) y mostrar el primer Fragment
        DatosAlSazon.cargarDesdeServidor(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cambiarFragment(new InventarioFragment());
                    }
                });
            }
        });

        btnFragInventario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarFragment(new InventarioFragment());
            }
        });

        btnFragVentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarFragment(new VentasFragment());
            }
        });

        btnFragReportes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarFragment(new ReporteFragment());
            }
        });

        tvTituloTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarFragment(new InventarioFragment());
                Toast.makeText(MenuActivity.this, "Actualizando consulta...", Toast.LENGTH_SHORT).show();
            }
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cambiarFragment(Fragment fragmento) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragments, fragmento)
                .commit();
    }
}