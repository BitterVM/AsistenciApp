package com.example.AsistenciApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.AsistenciApp.db.DbHelperPersonal;

public class AcreditarActivity extends AppCompatActivity {
    Button btn_acreditar, btn_volver;
    TextView tv_rut, tv_nombre, tv_empresa, tv_actividad, tv_hora;
    DbHelperPersonal dbPersonal;

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
        btn_acreditar = findViewById(R.id.btn_acreditar);
        btn_volver = findViewById(R.id.btn_volver);

        Bundle recibeRut = getIntent().getExtras();
        String rut = recibeRut.getString("keyDatos");

        String nombre = dbPersonal.obtenerDatoPorColumna(rut, "NOMBRE");
        String empresa = dbPersonal.obtenerDatoPorColumna(rut, "EMPRESA");
        String actividad = dbPersonal.obtenerDatoPorColumna(rut, "ACTIVIDAD");

        String hora = dbPersonal.obtenerDatoPorColumna(rut, "HORARIO");


        tv_rut.setText("RUT: "+ rut);
        tv_nombre.setText("Nombre: " + nombre);
        tv_empresa.setText("Empresa: " + empresa);
        tv_actividad.setText("Actividad: " + actividad);

        if (hora != null){
            tv_hora.setText("Hora de Acreditación: " + hora);
        }


        btn_acreditar.setOnClickListener(v->{
            boolean acreditado = dbPersonal.isAcreditado(rut);
            if (acreditado) {
                Toast.makeText(AcreditarActivity.this, "Persona ya fue acreditada", Toast.LENGTH_LONG).show();
            } else {
                boolean setAcreditado = dbPersonal.setAcreditacion(rut);
                if (setAcreditado){
                    dbPersonal.storeHorario(rut);
                    Toast.makeText(AcreditarActivity.this, "Persona acreditada", Toast.LENGTH_LONG).show();
                    cambioPagina();
                }
                else{
                    Toast.makeText(AcreditarActivity.this, "Fallo Acreditación", Toast.LENGTH_LONG).show();
                }
            }


        });

        btn_volver.setOnClickListener(v -> {
            cambioPagina();
        });
    }

    private void cambioPagina(){
        Intent intent = new Intent(AcreditarActivity.this, RegistroActivity.class);
        startActivity(intent);
    }
}