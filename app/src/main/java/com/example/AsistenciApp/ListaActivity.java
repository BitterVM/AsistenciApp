package com.example.AsistenciApp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.AsistenciApp.db.DbHelperPersonal;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class ListaActivity extends AppCompatActivity {
    ListView lv;
    ArrayList<String> lista;
    ArrayAdapter adaptador;
    DbHelperPersonal dbPersonal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista);
        lv = (ListView) findViewById(R.id.lista);
        dbPersonal = new DbHelperPersonal(this);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_lista);

        lista = dbPersonal.llenar_lv();
        adaptador = new ArrayAdapter(this, android.R.layout.simple_list_item_1, lista);
        lv.setAdapter(adaptador);


        //Barra de navegacion inferior
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_estadisticas) {
                    Intent homeIntent = new Intent(ListaActivity.this, EstadisticasActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (itemId == R.id.menu_registro) {
                    Intent listaIntent = new Intent(ListaActivity.this, RegistroActivity.class);
                    startActivity(listaIntent);
                    return true;

                } else if (itemId == R.id.menu_lista) {
                    Toast.makeText(ListaActivity.this, "Menu Actual", Toast.LENGTH_LONG).show();
                    return true;
                }
                return false;
            }
        });
    }
}