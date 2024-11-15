package com.example.AsistenciApp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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