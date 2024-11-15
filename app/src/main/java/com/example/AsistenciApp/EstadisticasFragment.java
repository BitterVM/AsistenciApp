package com.example.AsistenciApp;

import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.net.Uri;

import com.example.AsistenciApp.db.DbHelperPersonal;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;

public class EstadisticasFragment extends Fragment {

    Button btn_datos;
    DbHelperPersonal dbPersonal;
    TextView tv_total;

    public EstadisticasFragment() {
        super(R.layout.fragment_estadisticas);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla el layout para este fragmento
        View rootView = inflater.inflate(R.layout.fragment_estadisticas, container, false);

        dbPersonal = new DbHelperPersonal(getContext());
        tv_total = rootView.findViewById(R.id.tv_total);
        btn_datos = rootView.findViewById(R.id.btn_datos);

        Spinner spinnerEventos = rootView.findViewById(R.id.spinner_eventos);

        // Obtener todos los eventos desde la base de datos
        ArrayList<Evento> eventos = dbPersonal.obtenerEventos();

        ArrayAdapter<Evento> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, eventos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        // Manejar la selección del evento en el Spinner
        spinnerEventos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtener el evento seleccionado
                Evento eventoSeleccionado = (Evento) parentView.getItemAtPosition(position);

                // Obtener el ID del evento seleccionado
                String idEvento = eventoSeleccionado.getIdEvento();

                // Obtener las estadísticas para el evento seleccionado
                int acreditados = dbPersonal.cuentaAcreditadoPorEvento(idEvento);
                int total = dbPersonal.contarTotalPorEvento(idEvento);
                int porcentajeT = total > 0 ? (acreditados * 100) / total : 0;

                // Actualizar el texto de la vista (TextView) con las estadísticas
                tv_total.setText("Acreditados: " + acreditados + "/" + total + " ( " + porcentajeT + "% )");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Opcional: puedes poner un mensaje o una acción por defecto si no se selecciona ningún evento
                tv_total.setText("Seleccione un evento para ver las estadísticas");
            }
        });

        // Configurar el botón para exportar los datos a Excel y enviarlos por correo
        btn_datos.setOnClickListener(v -> {
            File filePath = dbPersonal.exportToExcel();
            enviarCorreoConAdjunto(filePath);
        });

        return rootView;
    }

    private void enviarCorreoConAdjunto(File filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Resumen de la Base de Datos en Excel");
        intent.putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás el archivo Excel con el resumen de los usuarios.");

        // Adjuntar el archivo Excel
        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", filePath);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(intent, "Enviar correo..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "No hay aplicaciones de correo instaladas.", Toast.LENGTH_SHORT).show();
        }
    }
}