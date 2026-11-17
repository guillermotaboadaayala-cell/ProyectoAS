package com.alsazon.movil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etUsuario, etPin;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsuario = findViewById(R.id.etUsuario);
        etPin = findViewById(R.id.etPin);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = etUsuario.getText().toString().trim();
                String pin = etPin.getText().toString().trim();

                if (pin.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Ingresa la contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pin.equals("0691")) {
                    etUsuario.setText("");
                    etPin.setText("");
                    String saludo = usuario.isEmpty() ? "Administrador" : usuario;
                    Toast.makeText(MainActivity.this, "Bienvenido " + saludo + " (Modo Consulta)", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                } else {
                    etPin.setText("");
                    Toast.makeText(MainActivity.this, "Contraseña incorrecta. Acceso denegado.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}