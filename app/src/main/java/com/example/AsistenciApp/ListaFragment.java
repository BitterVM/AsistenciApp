package com.example.AsistenciApp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.AsistenciApp.db.DbHelperPersonal;

import java.util.ArrayList;

public class ListaFragment extends Fragment {

    private Spinner spinnerEventos;
    private ListView lv;
    private ArrayList<String> lista;
    private ArrayAdapter<String> adaptador;
    private ArrayList<String> listaEventos;
    private ArrayList<String> listaIdsEventos;
    private DbHelperPersonal dbPersonal;

    public ListaFragment() {
        super(R.layout.fragment_lista);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el layout del fragmento
        View view = inflater.inflate(R.layout.fragment_lista, container, false);

        // Inicializar las vistas
        spinnerEventos = view.findViewById(R.id.SpinnerLista);
        lv = view.findViewById(R.id.lista);

        // Inicializar la base de datos
        dbPersonal = new DbHelperPersonal(getActivity());

        // Cargar los eventos en el spinner
        cargarEventosEnSpinner();

        // Configurar la acción al seleccionar un evento en el spinner
        spinnerEventos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Obtener el ID del evento seleccionado
                String idEventoSeleccionado = listaIdsEventos.get(position);

                // Llenar la lista con los participantes del evento seleccionado
                llenarListaParticipantes(idEventoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no hay selección
            }
        });

        return view;
    }

    private void cargarEventosEnSpinner() {
        listaEventos = new ArrayList<>();
        listaIdsEventos = new ArrayList<>();

        // Consultar los eventos desde la base de datos
        SQLiteDatabase db = dbPersonal.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ID_EVENTO, NOMBRE_EVENTO FROM t_eventos", null);

        if (cursor.moveToFirst()) {
            do {
                // Agregar el nombre del evento a la lista de eventos para mostrar en el Spinner
                listaEventos.add(cursor.getString(cursor.getColumnIndexOrThrow("NOMBRE_EVENTO")));
                // Agregar el ID del evento a la lista de IDs
                listaIdsEventos.add(cursor.getString(cursor.getColumnIndexOrThrow("ID_EVENTO")));
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Crear y configurar el adaptador para el Spinner
        ArrayAdapter<String> eventosAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listaEventos);
        eventosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventos.setAdapter(eventosAdapter);
    }

    private void llenarListaParticipantes(String idEvento) {
        // Consultar los participantes para el evento seleccionado
        lista = dbPersonal.llenar_lv(idEvento);

        // Configurar el adaptador para la lista
        if (lista != null && !lista.isEmpty()) {
            adaptador = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, lista);
            lv.setAdapter(adaptador);
        } else {
            // Limpiar la lista y mostrar un mensaje si no hay participantes
            lv.setAdapter(null);
            Toast.makeText(getActivity(), "No hay participantes para este evento", Toast.LENGTH_SHORT).show();
        }
    }
}
