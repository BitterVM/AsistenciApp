package com.example.AsistenciApp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.AsistenciApp.db.DbHelperPersonal;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;

public class AdminActivity extends AppCompatActivity {

    Button btn_cerrarAdmin, btn_excel;
    DbHelperPersonal dbPersonal;
    private static final int PICK_EXCEL_FILE = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        dbPersonal = new DbHelperPersonal(this);
        btn_cerrarAdmin = findViewById(R.id.btn_cerrarAdmin);
        btn_excel = findViewById(R.id.btn_excel);

        btn_cerrarAdmin.setOnClickListener(v -> {
            cerrarSesion();
        });

        btn_excel.setOnClickListener(v -> openFilePicker());
    }

    // Abre el selector de archivos
    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_EXCEL_FILE);
    }

    // Recibe el archivo seleccionado
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_EXCEL_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                readExcelAndInsertData(uri);  // Procesa el archivo Excel
            } else {
                Toast.makeText(this, "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void readExcelAndInsertData(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            XSSFSheet sheet = workbook.getSheetAt(0);  // Primera hoja del archivo Excel

            DbHelperPersonal dbHelper = new DbHelperPersonal(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();  // Inicia la transacción

            int rowCount = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // Salta la primera fila (encabezado)
                Row row = sheet.getRow(i);
                if (row != null) {
                    ContentValues values = new ContentValues();

                    // Verifica las celdas y asigna valores predeterminados si están vacías
                    values.put("NOMBRE", getCellValue(row.getCell(0)));
                    values.put("RUT", getCellValue(row.getCell(1)));
                    values.put("EMPRESA", getCellValue(row.getCell(2)));
                    values.put("ACTIVIDAD", getCellValue(row.getCell(3)));

                    // "ACREDITADO" no se necesita modificar, se mantiene como está

                    // Para "HORARIO" (tipo TEXT), si está vacío, asignamos null o "" (lo que prefieras)
                    values.put("HORARIO", getCellValueForHorario(row.getCell(5)));

                    // Insertar el registro en la base de datos
                    long result = db.insert(DbHelperPersonal.TABLE_PERSONAL, null, values);
                    if (result == -1) {
                        Log.e("Database Error", "Error al insertar la fila " + (i + 1));
                    } else {
                        rowCount++;
                    }
                }
            }

            db.setTransactionSuccessful();  // Confirma la transacción
            db.endTransaction();  // Finaliza la transacción

            Toast.makeText(this, rowCount + " registros importados con éxito", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al leer el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Método auxiliar para obtener el valor de las celdas
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";  // Si la celda está vacía, devuelve una cadena vacía
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";  // Devuelve vacío si el tipo de celda es inesperado
        }
    }

    // Método específico para manejar "HORARIO" (tipo TEXT) y verificar si está vacío
    private String getCellValueForHorario(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;  // Si la celda está vacía o en blanco, devuelve null (o "" si prefieres cadena vacía)
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();  // Retorna el texto si es una cadena
            default:
                return null;  // En caso de que sea cualquier otro tipo (por si acaso), retorna null
        }
    }


    public void cerrarSesion(){
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }



}