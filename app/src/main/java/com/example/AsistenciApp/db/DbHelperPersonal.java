package com.example.AsistenciApp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


// Clase para crear base de datos de acreditados
public class DbHelperPersonal extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NOMBRE = "personal.db";
    public static final String TABLE_PERSONAL = "t_personal";
    private Context context;

    public DbHelperPersonal(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
        this.context = context;

    }

    //Crea los campos de la bd y el tipo
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PERSONAL + "(" +
                "NOMBRE TEXT, " +
                "RUT TEXT PRIMARY KEY," +
                "EMPRESA TEXT," +
                "ACTIVIDAD TEXT," +
                "ACREDITADO INTEGER," +
                "HORARIO TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "t_personal");
        onCreate(db);
    }

    // Método para almacenar el horario actual en la bd
    public void storeHorario(String rut) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Obtener la hora actual
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        // Actualizar la fila correspondiente al RUT
        ContentValues valores = new ContentValues();
        valores.put("HORARIO", currentTime);

        db.update(TABLE_PERSONAL, valores, "RUT = ?", new String[]{rut});
    }

    //Recibe el rut y busca en la bd, retorna un boolean
    public boolean checkTrabajador(String rut){
        SQLiteDatabase bd = this.getWritableDatabase();
        Cursor fila = bd.rawQuery("SELECT * FROM t_personal WHERE RUT= ?", new String[]{rut});

        if (fila.getCount() > 0)
            return true;
        else return false;
    }

    //Recibe rut y nombre de columna, devuelve dato que se solicite
    public String obtenerDatoPorColumna(String rut, String nombreColumna) {
        String valor = null;
        Cursor cursor = this.getWritableDatabase().query("t_personal", null, "RUT=?", new String[]{rut}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                valor = cursor.getString(cursor.getColumnIndexOrThrow(nombreColumna));
            }
            cursor.close();
        }
        return valor;
    }

    //Recibe rut y  cambia valor a acreditar en bd
    public boolean setAcreditacion(String rut) {
        SQLiteDatabase bd = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ACREDITADO", 1); // Cambia la columna ACREDITADO a 1

        // Actualiza la fila que tiene el RUT especificado
        int filasAfectadas = bd.update("t_personal", values, "RUT = ?", new String[]{rut});

        // Si filasAfectadas es 1, significa que el RUT fue encontrado y actualizado
        if (filasAfectadas > 0) {
            return true;
        }else return false;
    }

    // Método para consultar si un RUT ya está acreditado
    public boolean isAcreditado(String rut) {
        SQLiteDatabase bd = this.getReadableDatabase();

        Cursor cursor = bd.rawQuery("SELECT ACREDITADO FROM t_personal WHERE RUT = ?", new String[]{rut});

        boolean acreditado = false;

        if (cursor.moveToFirst()) {
            acreditado = cursor.getInt(0) == 1;
        }
        cursor.close();
        return acreditado;
    }

    //Metodo cuenta la cantidad de personas acreditadas
    public int contarAcreditados() {
        SQLiteDatabase bd = this.getReadableDatabase();
        Cursor cursor = bd.rawQuery("SELECT COUNT(*) FROM t_personal WHERE ACREDITADO = 1", null);

        int totalAcreditados = 0;
        if (cursor.moveToFirst()) {
            totalAcreditados = cursor.getInt(0); // Obtiene el valor del COUNT
        }
        cursor.close();
        return totalAcreditados;
    }

    //Metodo cuenta el total de personas en la bd
    public int contarTotal() {
        SQLiteDatabase bd = this.getReadableDatabase();
        Cursor cursor = bd.rawQuery("SELECT COUNT(*) FROM t_personal", null);

        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0); // Obtiene el valor del COUNT
        }
        cursor.close();
        return total;
    }

    //Metodo que toma datos de la bd y exporta a excel
    public void exportToExcel() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM t_personal", null);

        // Crear el archivo Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Resumen de Asistencia");

        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("NOMBRE");
        headerRow.createCell(1).setCellValue("RUT");
        headerRow.createCell(2).setCellValue("EMPRESA");
        headerRow.createCell(3).setCellValue("ACTIVIDAD");
        headerRow.createCell(4).setCellValue("ACREDITADO");
        headerRow.createCell(5).setCellValue("HORARIO");

        // Rellenar el archivo con datos
        int rowIndex = 1;
        while (cursor.moveToNext()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow("NOMBRE")));
            row.createCell(1).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow("RUT")));
            row.createCell(2).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow("EMPRESA")));
            row.createCell(3).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow("ACTIVIDAD")));
            row.createCell(4).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow("ACREDITADO")));
            row.createCell(5).setCellValue(cursor.getString(cursor.getColumnIndexOrThrow("HORARIO")));
        }

        cursor.close();
        db.close();
        // Guardar el archivo en almacenamiento externo

        File filePath = new File(context.getExternalFilesDir(null), "resumen_asistencia.xlsx");
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
            workbook.close();
            Toast.makeText(context, "Archivo Excel generado", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar el archivo", Toast.LENGTH_SHORT).show();
        }

    }

    //Llena la lista con los datos
    public ArrayList llenar_lv(){
        ArrayList<String> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor registros = db.rawQuery("SELECT * FROM t_personal", null);

        if (registros.moveToFirst()){
            do{
                String nombre = registros.getString(0);
                String rut = registros.getString(1);
                int acreditado = registros.getInt(4);
                String acred = null;

                if (acreditado == 1){
                    acred = "Acreditado";
                }else {
                    acred = "No Acreditado";
                }

                String registro = nombre +" - " +acred + "\n(" + rut + ")  ";

                lista.add(registro);
            }while(registros.moveToNext());
        }

        registros.close();
        db.close();
        return lista;
    }

}
