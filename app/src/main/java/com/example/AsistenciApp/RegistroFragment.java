package com.example.AsistenciApp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.net.Uri;

import com.example.AsistenciApp.db.DbHelperPersonal;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class RegistroFragment extends Fragment {

    DbHelperPersonal dbPersonal;
    private EditText et_rut;
    private Button btn_verificar, btn_scan, btn_cerrar;

    public RegistroFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_registro, container, false);

        dbPersonal = new DbHelperPersonal(getContext());
        et_rut = rootView.findViewById(R.id.et_RUT);
        btn_verificar = rootView.findViewById(R.id.btn_verificar);
        btn_scan = rootView.findViewById(R.id.btn_scan);
        btn_cerrar = rootView.findViewById(R.id.btn_cerrar);

        btn_verificar.setOnClickListener(v -> {
            String rut = et_rut.getText().toString().trim();
            buscarRUT(rut);
        });

        btn_scan.setOnClickListener(v -> {
            scanCode();
        });

        btn_cerrar.setOnClickListener(v -> {
            cerrarSesion();
        });

        // Validar RUT
        et_rut.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.contains("k")) {
                    String newText = input.replace("k", "K");
                    et_rut.setText(newText);
                    et_rut.setSelection(newText.length());
                }

                // Validar el formato y el dígito verificador
                if (!isValidRutFormat(input)) {
                    et_rut.setError("Formato incorrecto. Debe ser 12345678-K");
                } else if (!isValidRut(input)) {
                    et_rut.setError("RUT inválido.");
                } else {
                    et_rut.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return rootView;
    }

    // Función para validar el formato del RUT
    private boolean isValidRutFormat(String rut) {
        String regex = "^[0-9]{7,8}-[0-9Kk]$";
        return rut.matches(regex);
    }

    // Función para validar el RUT
    private boolean isValidRut(String rut) {
        try {
            String[] parts = rut.split("-");
            String numberPart = parts[0];
            char dvInput = parts[1].toUpperCase().charAt(0);

            char dvCalculated = calculateRutDv(Integer.parseInt(numberPart));

            return dvInput == dvCalculated;
        } catch (Exception e) {
            return false;
        }
    }

    // Función para calcular el dígito verificador
    private char calculateRutDv(int rut) {
        int m = 0, s = 1;
        while (rut != 0) {
            s = (s + rut % 10 * (9 - m++ % 6)) % 11;
            rut /= 10;
        }
        return (char) (s != 0 ? s + 47 : 75);
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    // Análisis del resultado del scanCode y extrae el RUT
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

    public void buscarRUT(String rut) {
        if (rut != null) {
            try {
                boolean existe = dbPersonal.checkTrabajador(rut);
                if (existe) {
                    Bundle enviaRut = new Bundle();
                    enviaRut.putString("keyDatos", rut);
                    Intent intent = new Intent(getActivity(), AcreditarActivity.class);
                    intent.putExtras(enviaRut);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "RUT no encontrado", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Error al consultar la BD", Toast.LENGTH_LONG).show();
                Log.e("BuscarRUT", "Error querying database: " + e.getMessage(), e);
            }
        } else {
            Toast.makeText(getActivity(), "RUN es nulo", Toast.LENGTH_LONG).show();
            Log.d("BuscarRUT", "RUT es nulo");
        }
    }

    // Cerrar sesión y volver al login
    private void cerrarSesion() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}