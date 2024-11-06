package com.example.AsistenciApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.AsistenciApp.db.DbHelperPersonal;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class EstadisticasActivity extends AppCompatActivity {

    Button btn_datos;
    DbHelperPersonal dbPersonal;
    TextView tv_total;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadisticas);

        dbPersonal = new DbHelperPersonal(this);
        int acreditados = dbPersonal.contarAcreditados();
        int total = dbPersonal.contarTotal();
        tv_total = findViewById(R.id.tv_total);
        btn_datos = findViewById(R.id.btn_datos);
        int porcentajeT = (acreditados*100)/total;
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_estadisticas);

        tv_total.setText( "Acreditados: " + acreditados + "/" + total + " ( "+ porcentajeT +"% )");

        btn_datos.setOnClickListener(v -> {
            File filePath = new File(getExternalFilesDir(null), "resumen_asistencia.xlsx");
            dbPersonal.exportToExcel();
            enviarCorreoConAdjunto(filePath);
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_estadisticas) {
                    Toast.makeText(EstadisticasActivity.this, "Menu Actual", Toast.LENGTH_LONG).show();
                    return true;
                } else if (itemId == R.id.menu_registro) {
                    Intent listaIntent = new Intent(EstadisticasActivity.this, RegistroActivity.class);
                    startActivity(listaIntent);
                    return true;

                } else if (itemId == R.id.menu_lista) {
                    Intent homeIntent = new Intent(EstadisticasActivity.this, ListaActivity.class);
                    startActivity(homeIntent);
                    return true;
                }
                return false;
            }
        });
    }

    private void enviarCorreoConAdjunto(File filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        //intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"vmedinaa@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Resumen de la Base de Datos en Excel");
        intent.putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás el archivo Excel con el resumen de los usuarios.");

        // Adjuntar el archivo Excel
        Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", filePath);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(intent, "Enviar correo..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No hay aplicaciones de correo instaladas.", Toast.LENGTH_SHORT).show();
        }
    }
}