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

import com.example.AsistenciApp.Evento;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


// Clase para crear base de datos de acreditados
public class DbHelperPersonal extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NOMBRE = "personal.db";
    public static final String TABLE_PERSONAL = "t_personal";
    public static final String TABLE_EVENTOS = "t_eventos";
    public static final String TABLE_PERSONA_EVENTO = "t_persona_evento";
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
                "ACTIVIDAD TEXT" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_EVENTOS + " (" +
                "ID_EVENTO TEXT PRIMARY KEY, " +
                "NOMBRE_EVENTO TEXT NOT NULL " +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_PERSONA_EVENTO + " (" +
                "ID_PERSONA TEXT, " +
                "ID_EVENTO TEXT, " +
                "ACREDITADO TEXT," +
                "HORARIO TEXT,"+
                "PRIMARY KEY (ID_PERSONA, ID_EVENTO), " +
                "FOREIGN KEY (ID_PERSONA) REFERENCES " + TABLE_PERSONAL + "(RUT) ON DELETE CASCADE, " +
                "FOREIGN KEY (ID_EVENTO) REFERENCES " + TABLE_EVENTOS + "(ID_EVENTO) ON DELETE CASCADE" +
                ")");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DELETE FROM " + "t_personal");
        db.execSQL("DROP TABLE IF EXISTS " + "t_personal");
        onCreate(db);
    }

    // Método para obtener los eventos para el rut seleccionado
    public ArrayList<Evento> obtenerEventosPorRut(String rut) {
        ArrayList<Evento> eventos = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Consulta con JOIN entre las tablas PERSONA_EVENTO y EVENTOS
            String query = "SELECT E.ID_EVENTO, E.NOMBRE_EVENTO " +
                    "FROM " + TABLE_PERSONA_EVENTO + " PE " +
                    "JOIN " + TABLE_EVENTOS + " E ON PE.ID_EVENTO = E.ID_EVENTO " +
                    "WHERE PE.ID_PERSONA = ?";

            cursor = db.rawQuery(query, new String[]{rut});

            // Si la consulta devuelve resultados, crea objetos Evento y los agrega a la lista
            if (cursor.moveToFirst()) {
                do {
                    String idEvento = cursor.getString(cursor.getColumnIndex("ID_EVENTO"));
                    String nombreEvento = cursor.getString(cursor.getColumnIndex("NOMBRE_EVENTO"));

                    if (idEvento != null && nombreEvento != null) {
                        eventos.add(new Evento(idEvento, nombreEvento));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return eventos;
    }

    //Obtiene todos los eventos disponibles
    public ArrayList<Evento> obtenerEventos() {
        ArrayList<Evento> eventos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT * FROM " + TABLE_EVENTOS; // Consulta todos los eventos
            cursor = db.rawQuery(query, null);

            // Recorrer los resultados
            if (cursor.moveToFirst()) {
                do {
                    String idEvento = cursor.getString(cursor.getColumnIndex("ID_EVENTO"));
                    String nombreEvento = cursor.getString(cursor.getColumnIndex("NOMBRE_EVENTO"));

                    // Crear un objeto Evento y agregarlo a la lista
                    Evento evento = new Evento(idEvento, nombreEvento);
                    eventos.add(evento);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return eventos;
    }

    // Método para almacenar el horario actual en la bd
    public void storeHorario(String rut, String idEvento) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Obtener la hora actual
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());

        // Actualizar la fila correspondiente al RUT
        ContentValues valores = new ContentValues();
        valores.put("HORARIO", currentTime);

        db.update(TABLE_PERSONA_EVENTO, valores, "ID_PERSONA = ? AND ID_EVENTO = ?", new String[]{rut, idEvento});
    }

    public String consultaHoraPorEvento(String rut, String idEvento) {
        SQLiteDatabase db = this.getReadableDatabase();
        String horario = null;
        Cursor cursor = null;

        String query = "SELECT HORARIO FROM " + TABLE_PERSONA_EVENTO + " WHERE ID_PERSONA = ? AND ID_EVENTO = ?";
        cursor = db.rawQuery(query, new String[]{rut, idEvento});

        if (cursor != null && cursor.moveToFirst()) {
            horario = cursor.getString(cursor.getColumnIndex("HORARIO"));
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return horario; // Devuelve el horario encontrado o null si no se encontró
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
    public boolean setAcreditacion(String rut, String idEvento) {
        SQLiteDatabase bd = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("ACREDITADO", "Si"); // Cambia la columna ACREDITADO a Si

        // Actualiza la fila que tiene el RUT especificado
        int filasAfectadas = bd.update(TABLE_PERSONA_EVENTO, values, "ID_PERSONA = ? AND ID_EVENTO = ?", new String[]{rut, idEvento});

        // Si filasAfectadas es Si, significa que el RUT fue encontrado y actualizado
        if (filasAfectadas > 0) {
            return true;
        }else return false;
    }

    // Método para consultar si un RUT ya está acreditado
    public boolean isAcreditado(String rut, String idEvento) {
        SQLiteDatabase bd = this.getReadableDatabase();
        Cursor cursor = null;
        boolean acreditado = false;

        try {
            // Consulta que verifica si la persona está acreditada para un evento específico
            String query = "SELECT ACREDITADO FROM t_persona_evento WHERE ID_PERSONA = ? AND ID_EVENTO = ?";
            cursor = bd.rawQuery(query, new String[]{rut, idEvento});

            if (cursor.moveToFirst()) {
                acreditado = "si".equalsIgnoreCase(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (bd != null) bd.close();
        }

        return acreditado;
    }

    //Metodo cuenta la cantidad de personas acreditadas
    public int cuentaAcreditadoPorEvento(String idEvento) {
        SQLiteDatabase bd = this.getReadableDatabase();
        Cursor cursor = null;
        int totalAcreditados = 0;

        try {
            // Consulta que cuenta los acreditados para un evento específico
            String query = "SELECT COUNT(*) FROM t_persona_evento WHERE TRIM(ACREDITADO)  = 'Si' AND ID_EVENTO = ?";
            cursor = bd.rawQuery(query, new String[]{idEvento});

            if (cursor.moveToFirst()) {
                totalAcreditados = cursor.getInt(0); // Obtiene el valor del COUNT
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (bd != null) bd.close();
        }

        return totalAcreditados;
    }

    //Metodo cuenta el total de personas por evento
    public int contarTotalPorEvento(String idEvento) {
        SQLiteDatabase bd = this.getReadableDatabase();
        Cursor cursor = null;
        int totalAcreditados = 0;

        try {
            // Consulta para contar las personas acreditadas en un evento específico
            String query = "SELECT COUNT(*) FROM t_persona_evento WHERE ID_EVENTO = ?";
            cursor = bd.rawQuery(query, new String[]{idEvento});

            // Si la consulta devuelve resultados, obtenemos el total
            if (cursor.moveToFirst()) {
                totalAcreditados = cursor.getInt(0); // Obtiene el valor del COUNT
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            if (bd != null) bd.close();
        }
        return totalAcreditados;
    }

    //Metodo que toma datos de la bd y exporta a excel
    public File exportToExcel() {
        SQLiteDatabase db = getReadableDatabase();

        // Crear el archivo Excel
        Workbook workbook = new XSSFWorkbook();

        // Consultar los eventos
        Cursor eventosCursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTOS, null);

        // Para cada evento, crear una hoja y agregar los asistentes
        while (eventosCursor.moveToNext()) {
            String eventId = eventosCursor.getString(eventosCursor.getColumnIndexOrThrow("ID_EVENTO"));
            String eventName = eventosCursor.getString(eventosCursor.getColumnIndexOrThrow("NOMBRE_EVENTO"));

            // Crear una hoja para cada evento
            Sheet eventoSheet = workbook.createSheet(eventName);

            // Crear encabezados para la hoja de evento
            Row eventoHeaderRow = eventoSheet.createRow(0);
            eventoHeaderRow.createCell(0).setCellValue("NOMBRE");
            eventoHeaderRow.createCell(1).setCellValue("RUT");
            eventoHeaderRow.createCell(2).setCellValue("EMPRESA");
            eventoHeaderRow.createCell(3).setCellValue("ACTIVIDAD");
            eventoHeaderRow.createCell(4).setCellValue("ACREDITADO");
            eventoHeaderRow.createCell(5).setCellValue("HORARIO");

            // Consultar los asistentes para el evento actual
            String query = "SELECT p.NOMBRE, p.RUT, p.EMPRESA, p.ACTIVIDAD, pe.ACREDITADO, pe.HORARIO " +
                    "FROM " + TABLE_PERSONAL + " p " +
                    "JOIN " + TABLE_PERSONA_EVENTO + " pe ON p.RUT = pe.ID_PERSONA " +
                    "WHERE pe.ID_EVENTO = ?";
            Cursor asistentesCursor = db.rawQuery(query, new String[]{eventId});

            // Rellenar con los datos de los asistentes
            int rowIndex = 1;
            while (asistentesCursor.moveToNext()) {
                Row row = eventoSheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(asistentesCursor.getString(asistentesCursor.getColumnIndexOrThrow("NOMBRE")));
                row.createCell(1).setCellValue(asistentesCursor.getString(asistentesCursor.getColumnIndexOrThrow("RUT")));
                row.createCell(2).setCellValue(asistentesCursor.getString(asistentesCursor.getColumnIndexOrThrow("EMPRESA")));
                row.createCell(3).setCellValue(asistentesCursor.getString(asistentesCursor.getColumnIndexOrThrow("ACTIVIDAD")));

                // Obtener el valor de "ACREDITADO" y manejar null o valores string
                String acreditado = asistentesCursor.getString(asistentesCursor.getColumnIndexOrThrow("ACREDITADO"));
                if (acreditado == null) {
                    row.createCell(4).setCellValue("No"); // Si es null, asigna "No"
                } else {
                    row.createCell(4).setCellValue(acreditado); // Si tiene valor, lo conserva
                }

                row.createCell(5).setCellValue(asistentesCursor.getString(asistentesCursor.getColumnIndexOrThrow("HORARIO")));
            }
            asistentesCursor.close();
        }
        eventosCursor.close();
        db.close();

        // Guardar el archivo en almacenamiento externo
        String fechaActual = new SimpleDateFormat("dd_MM").format(new Date());
        File filePath = new File(context.getExternalFilesDir(null), "resumen_asistencia_" + fechaActual + ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
            workbook.close();
            Toast.makeText(context, "Archivo Excel generado", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar el archivo", Toast.LENGTH_SHORT).show();
        }

        return filePath;
    }

    //Llena la lista con los datos de listaActivity
    public ArrayList<String> llenar_lv(String idEvento) {
        ArrayList<String> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Consulta para obtener los registros de las personas acreditadas en un evento específico
        String query = "SELECT p.NOMBRE, p.RUT, pe.ACREDITADO, pe.HORARIO " +
                "FROM t_personal p " +
                "LEFT JOIN t_persona_evento pe ON p.RUT = pe.ID_PERSONA " +
                "WHERE pe.ID_EVENTO = ?";

        Cursor registros = db.rawQuery(query, new String[]{idEvento});

        if (registros.moveToFirst()) {
            do {
                String nombre = registros.getString(0);
                String rut = registros.getString(1);
                String acreditado = registros.getString(2);
                String horario = registros.getString(3);

                String acred = "No Acreditado";
                if ("Si".equalsIgnoreCase(acreditado)) {
                    acred = "Acreditado";
                    if (horario != null && !horario.isEmpty()) {
                        acred += " - " + horario;
                    }
                }
                lista.add(nombre + " - " + rut + "\n" + acred);

            } while (registros.moveToNext());
        }
        registros.close();
        db.close();
        return lista;
    }
}
