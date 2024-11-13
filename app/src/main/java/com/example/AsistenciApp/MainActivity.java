package com.example.AsistenciApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.net.Uri;
import android.util.Log;
import com.example.AsistenciApp.R;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.AsistenciApp.db.DbHelperPersonal;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

public class RegistroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegistroFragment())
                .commit();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_registro);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.menu_estadisticas) {
                    selectedFragment = new EstadisticasFragment();
                }else if (item.getItemId() == R.id.menu_lista) {
                    selectedFragment = new ListaFragment();
                } else if (item.getItemId() == R.id.menu_registro) {
                    selectedFragment = new RegistroFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container, selectedFragment)
                            .addToBackStack(null)
                            .commit();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        // Crear un cuadro de diálogo de alerta
        new AlertDialog.Builder(this)
                .setMessage("¿Estás seguro de que quieres salir de la aplicación?")
                .setCancelable(false)
                .setPositiveButton("Sí", (dialog, id) -> {
                    // Cerrar la aplicación
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    dialog.dismiss();
                })
                .show();
    }

}