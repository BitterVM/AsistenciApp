package com.example.AsistenciApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.AsistenciApp.db.DbHelperPersonal;

import java.util.ArrayList;

public class AcreditarActivity extends AppCompatActivity {
    Button btn_acreditar, btn_volver;
    TextView tv_rut, tv_nombre, tv_empresa, tv_actividad, tv_hora;
    DbHelperPersonal dbPersonal;
    private String opcionSeleccionada, nombreEventoSeleccionado;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acreditar);
        dbPersonal = new DbHelperPersonal(this);
        tv_rut = findViewById(R.id.tv_rut);
        tv_nombre = findViewById(R.id.tv_nombre);
        tv_empresa = findViewById(R.id.tv_empresa);
        tv_actividad = findViewById(R.id.tv_actividad);
        tv_hora = findViewById(R.id.tv_hora);
        Spinner miSpinner = findViewById(R.id.miSpinner);
        btn_acreditar = findViewById(R.id.btn_acreditar);
        btn_volver = findViewById(R.id.btn_volver);

        Bundle recibeRut = getIntent().getExtras();
        String rut = recibeRut.getString("keyDatos");

        ArrayList<Evento> eventos = dbPersonal.obtenerEventosPorRut(rut);

        String nombre = dbPersonal.obtenerDatoPorColumna(rut, "NOMBRE");
        String empresa = dbPersonal.obtenerDatoPorColumna(rut, "EMPRESA");
        String actividad = dbPersonal.obtenerDatoPorColumna(rut, "ACTIVIDAD");


        tv_rut.setText("RUT: "+ rut);
        tv_nombre.setText("Nombre: " + nombre);
        tv_empresa.setText("Empresa: " + empresa);
        tv_actividad.setText("Actividad: " + actividad);


        // Si no hay eventos, muestra un mensaje
        if (eventos.isEmpty()) {
            eventos.add(new Evento("0", "No hay eventos para este RUT"));
        }

        // Crea el ArrayAdapter usando las opciones dinámicas
        ArrayAdapter<Evento> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asigna el adaptador al Spinner
        miSpinner.setAdapter(adapter);

        // Configura un listener para manejar la selección de opciones
        miSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Evento eventoSeleccionado = (Evento) parent.getItemAtPosition(position);
                opcionSeleccionada = eventoSeleccionado.getIdEvento(); // Ahora opcionSeleccionada almacena el ID del evento
                nombreEventoSeleccionado = eventoSeleccionado.getNombreEvento();
                Toast.makeText(AcreditarActivity.this, "Seleccionaste ID: " + nombreEventoSeleccionado, Toast.LENGTH_SHORT).show();
                String hora = dbPersonal.consultaHoraPorEvento(rut, opcionSeleccionada);
                if(hora != null && !hora.isEmpty()) {
                    tv_hora.setText("Hora de Acreditación " + nombreEventoSeleccionado +" : "+ hora);
                }else {
                    tv_hora.setText("No se ha acreditado este evento");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btn_acreditar.setOnClickListener(v -> {
            if (opcionSeleccionada != null) {
                boolean acreditado = dbPersonal.isAcreditado(rut, opcionSeleccionada);
                if (acreditado) {
                    Toast.makeText(AcreditarActivity.this, "Persona ya fue acreditada", Toast.LENGTH_LONG).show();
                } else {
                    boolean setAcreditado = dbPersonal.setAcreditacion(rut, opcionSeleccionada);
                    if (setAcreditado) {
                        dbPersonal.storeHorario(rut, opcionSeleccionada);
                        Toast.makeText(AcreditarActivity.this, "Persona acreditada", Toast.LENGTH_LONG).show();

                        String hora = dbPersonal.consultaHoraPorEvento(rut, opcionSeleccionada);
                        if(hora != null && !hora.isEmpty()) {
                            tv_hora.setText("Hora de Acreditación " + nombreEventoSeleccionado +" : "+ hora);
                        }else {
                            tv_hora.setText("No se ha acreditado este evento");
                        }
                    } else {
                        Toast.makeText(AcreditarActivity.this, "Fallo Acreditación", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(AcreditarActivity.this, "Por favor selecciona un evento", Toast.LENGTH_SHORT).show();
            }
        });

        btn_volver.setOnClickListener(v -> {
            cambioPagina();
        });
    }


    private void cambioPagina(){
        Intent intent = new Intent(AcreditarActivity.this, MainActivity.class);
        startActivity(intent);
    }
}