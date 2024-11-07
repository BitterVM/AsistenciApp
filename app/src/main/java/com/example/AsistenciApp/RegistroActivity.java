package com.example.AsistenciApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.net.Uri;
import android.util.Log;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.AsistenciApp.db.DbHelperPersonal;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class RegistroActivity extends AppCompatActivity {
    DbHelperPersonal dbPersonal;
    private EditText et_rut;
    private Button btn_verificar, btn_scan, btn_estadisticas, btn_cerrar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        dbPersonal = new DbHelperPersonal(this);
        et_rut = findViewById(R.id.et_RUT);
        btn_verificar = findViewById(R.id.btn_verificar);
        btn_scan = findViewById(R.id.btn_scan);
        //btn_estadisticas = findViewById(R.id.btn_estadisticas);
        btn_cerrar = findViewById(R.id.btn_cerrar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_registro);

        btn_verificar.setOnClickListener(v ->  {
            String rut = et_rut.getText().toString().trim();
            buscarRUT(rut);
        });

        btn_scan.setOnClickListener(v -> {
            scanCode();
        });

        /*
        btn_estadisticas.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroActivity.this, EstadisticasActivity.class);
            startActivity(intent);
        });
         */

        btn_cerrar.setOnClickListener(v ->{
            cerrarSesion();
        });

        //Para comprobar el rut
        et_rut.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // puede quedar vacio
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Convertir "k" a "K" en el texto ingresado
                String input = s.toString();

                // Si contiene una "k" minúscula, cambiarla por "K"
                if (input.contains("k")) {
                    String newText = input.replace("k", "K");
                    et_rut.setText(newText);
                    et_rut.setSelection(newText.length());  // Mover cursor al final
                }

                // Validar el formato y el dígito verificador
                if (!isValidRutFormat(input)) {
                    et_rut.setError("Formato incorrecto. Debe ser 12345678-K");
                } else if (!isValidRut(input)) {
                    et_rut.setError("RUT inválido.");
                } else {
                    et_rut.setError(null); // Si es válido, no mostrar error
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesitas hacer nada aquí
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_estadisticas) {
                    Intent homeIntent = new Intent(RegistroActivity.this, EstadisticasActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (itemId == R.id.menu_registro) {
                    Toast.makeText(RegistroActivity.this, "Menu Actual", Toast.LENGTH_LONG).show();
                    return true;
                } else if (itemId == R.id.menu_lista) {
                    Intent listaIntent = new Intent(RegistroActivity.this, ListaActivity.class);
                    startActivity(listaIntent);
                    return true;
                }
                return false;
            }
        });

    }

    public void cerrarSesion(){
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // Función para validar el formato del RUT
    private boolean isValidRutFormat(String rut) {
        //(ejemplo: 12345678-K)
        String regex = "^[0-9]{7,8}-[0-9Kk]$";
        return rut.matches(regex);
    }

    // Función para validar el RUT
    private boolean isValidRut(String rut) {
        try {
            // Separar número y dígito verificador
            String[] parts = rut.split("-");
            String numberPart = parts[0];
            char dvInput = parts[1].toUpperCase().charAt(0); // Convertir dígito verificador a mayúscula

            // Calcular dígito verificador real
            char dvCalculated = calculateRutDv(Integer.parseInt(numberPart));

            return dvInput == dvCalculated;
        } catch (Exception e) {
            return false; // Retornar falso si hay un error
        }
    }

    // Función para calcular el dígito verificador
    private char calculateRutDv(int rut) {
        int m = 0, s = 1;
        while (rut != 0) {
            s = (s + rut % 10 * (9 - m++ % 6)) % 11;
            rut /= 10;
        }
        return (char) (s != 0 ? s + 47 : 75); // Retorna 'K' o el número como dígito verificador
    }

    //Funciona al presionar "escanear"
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    //Analisa el resultado del scanCode y extrae el rut
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String url = result.getContents();
            Uri uri = Uri.parse(url);
            String RUN = uri.getQueryParameter("RUN");

            if (RUN != null) {
                Log.d("RUT", RUN);
            } else {
                Log.d("RUT", "No se encontró RUT");
            }
            buscarRUT(RUN);
        }
    });

    //Recibe rut y busca en la bd de personal, si existe pasa a acreditacion
    public void buscarRUT(String rut) {
        if (rut != null) {
            try {
                boolean existe = dbPersonal.checkTrabajador(rut);

                if (existe) {
                    Bundle enviaRut = new Bundle();
                    enviaRut.putString("keyDatos", rut);

                    Intent intent = new Intent(RegistroActivity.this, AcreditarActivity.class);
                    intent.putExtras(enviaRut);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegistroActivity.this, "RUT no encontrado", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(RegistroActivity.this, "Error al consultar la BD", Toast.LENGTH_LONG).show();
                Log.e("BuscarRUT", "Error querying database: " + e.getMessage(), e);
            }
        } else {
            Toast.makeText(RegistroActivity.this, "RUN es nulo", Toast.LENGTH_LONG).show();
            Log.d("BuscarRUT", "RUT es nulo");
        }
    }


}