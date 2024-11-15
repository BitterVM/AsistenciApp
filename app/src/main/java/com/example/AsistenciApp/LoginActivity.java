package com.example.AsistenciApp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.AsistenciApp.db.DbHelper;

public class LoginActivity extends AppCompatActivity {
    private EditText et_usuario, et_pass;
    private Button btn_login;
    DbHelper dbHelper;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        dbHelper = new DbHelper(this);
        et_usuario= (EditText) findViewById(R.id.et_usuario);
        et_pass= (EditText) findViewById(R.id.et_pass);
        btn_login= (Button) findViewById(R.id.btn_login);

        btn_login.setOnClickListener(view -> {
            String userRole = dbHelper.checkUser(et_usuario.getText().toString().trim(), et_pass.getText().toString().trim());

            if (userRole != null) {
                if (userRole.equals("admin")) {
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    startActivity(intent);
                    finish();
                } else if (userRole.equals("user")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
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