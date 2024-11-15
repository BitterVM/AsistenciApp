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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.AsistenciApp.db.DbHelperPersonal;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

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

    //Lee el excel y inserta los datos a la bdPersonal
    public void readExcelAndInsertData(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {

            // Obtener las tres hojas del archivo Excel
            XSSFSheet sheetPersonal = workbook.getSheetAt(0);  // Hoja para PERSONAL
            XSSFSheet sheetEventos = workbook.getSheetAt(1);   // Hoja para EVENTOS
            XSSFSheet sheetPersonaEvento = workbook.getSheetAt(2);  // Hoja para PERSONA_EVENTO

            DbHelperPersonal dbHelper = new DbHelperPersonal(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Limpiar las tablas antes de insertar los nuevos datos
            db.execSQL("DELETE FROM " + DbHelperPersonal.TABLE_PERSONAL);
            db.execSQL("DELETE FROM " + DbHelperPersonal.TABLE_EVENTOS);
            db.execSQL("DELETE FROM " + DbHelperPersonal.TABLE_PERSONA_EVENTO);

            db.beginTransaction();  // Inicia la transacción

            int rowCount = 0;

            // Insertar los datos en la tabla PERSONAL (Hoja 1)
            rowCount += insertPersonalData(sheetPersonal, db);

            // Insertar los datos en la tabla EVENTOS (Hoja 2)
            rowCount += insertEventosData(sheetEventos, db);

            // Insertar los datos en la tabla PERSONA_EVENTO (Hoja 3)
            rowCount += insertPersonaEventoData(sheetPersonaEvento, db);

            db.setTransactionSuccessful();  // Confirma la transacción
            db.endTransaction();  // Finaliza la transacción

            Toast.makeText(this, rowCount + " registros importados con éxito", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al leer el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int insertPersonalData(XSSFSheet sheet, SQLiteDatabase db) {
        int rowCount = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // Salta la primera fila (encabezado)
            Row row = sheet.getRow(i);
            if (row != null) {
                ContentValues values = new ContentValues();

                String rut = getCellValue(row.getCell(1));

                if (rut != null && !rut.trim().isEmpty()) {
                    values.put("NOMBRE", getCellValue(row.getCell(0)));
                    values.put("RUT", rut);
                    values.put("EMPRESA", getCellValue(row.getCell(2)));
                    values.put("ACTIVIDAD", getCellValue(row.getCell(3)));

                    // Insertar en la tabla PERSONAL
                    long result = db.insert(DbHelperPersonal.TABLE_PERSONAL, null, values);
                    if (result != -1) {
                        rowCount++;
                    }
                } else {
                    Log.w("Data Import", "Fila " + (i + 1) + " omitida debido a RUT vacío o nulo.");
                }
            }
        }
        return rowCount;
    }

    private int insertEventosData(XSSFSheet sheet, SQLiteDatabase db) {
        int rowCount = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // Salta la primera fila (encabezado)
            Row row = sheet.getRow(i);
            if (row != null) {
                ContentValues values = new ContentValues();

                String eventoId = getCellValue(row.getCell(0));  // ID_EVENTO
                String nombreEvento = getCellValue(row.getCell(1));  // NOMBRE_EVENTO

                if (eventoId != null && !eventoId.trim().isEmpty() && nombreEvento != null && !nombreEvento.trim().isEmpty()) {
                    values.put("ID_EVENTO", eventoId);
                    values.put("NOMBRE_EVENTO", nombreEvento);

                    // Insertar en la tabla EVENTOS
                    long result = db.insertWithOnConflict(DbHelperPersonal.TABLE_EVENTOS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    if (result != -1) {
                        rowCount++;
                    }
                }
            }
        }
        return rowCount;
    }

    private int insertPersonaEventoData(XSSFSheet sheet, SQLiteDatabase db) {
        int rowCount = 0;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {  // Salta la primera fila (encabezado)
            Row row = sheet.getRow(i);
            if (row != null) {
                ContentValues values = new ContentValues();

                String rut = getCellValue(row.getCell(0));  // ID_PERSONA (RUT)
                String eventoId = getCellValue(row.getCell(1));  // ID_EVENTO
                String acreditado = getAcreditadoValue(row);  // ACREDITADO
                String horario = getCellValueForHorario(row.getCell(2));  // HORARIO

                if (rut != null && eventoId != null && !rut.trim().isEmpty() && !eventoId.trim().isEmpty()) {
                    values.put("ID_PERSONA", rut);
                    values.put("ID_EVENTO", eventoId);
                    values.put("ACREDITADO", acreditado);
                    values.put("HORARIO", horario);

                    // Insertar en la tabla PERSONA_EVENTO
                    long result = db.insert(DbHelperPersonal.TABLE_PERSONA_EVENTO, null, values);
                    if (result != -1) {
                        rowCount++;
                    }
                }
            }
        }
        return rowCount;
    }

    // Método para obtener el valor de "ACREDITADO"
    private String getAcreditadoValue(Row row) {
        try {
            Cell accredCell = row.getCell(3);
            if (accredCell != null) {
                if (accredCell.getCellType() == CellType.NUMERIC) {
                    double accredValue = accredCell.getNumericCellValue();
                    return accredValue == 1.0 ? "Si" : "No";  // Devuelve "1" o "0" como texto
                } else if (accredCell.getCellType() == CellType.STRING) {
                    String accredValue = accredCell.getStringCellValue().trim();
                    return "1".equals(accredValue) ? "Si" : "No";  // Devuelve "1" o "0" como texto
                }
            }
        } catch (Exception e) {
            Log.w("Data Import", "Error al procesar la celda ACREDITADO: " + e.getMessage());
        }
        return "No";  // Valor predeterminado como texto
    }

    // Método para obtener el valor de "HORARIO" como texto
    private String getCellValueForHorario(Cell cell) {
        return cell != null ? cell.toString().trim() : "";
    }
    // Método para obtener el valor de una celda, manejando diferentes tipos de datos
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";  // Si la celda está vacía, devolver un string vacío
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();  // Si es texto, devolverlo como cadena
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Si la celda es una fecha, devolverla en formato de fecha
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                } else {
                    // Si es un número, devolverlo como texto
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());  // Si es booleano, devolver "true" o "false"
            default:
                return "";
        }
    }

    public void cerrarSesion(){
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }



}