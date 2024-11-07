package com.example.AsistenciApp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

//Clase para crear base de datos de usuarios
public class DbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NOMBRE = "users.db";
    public static final String TABLE_USERS = "t_users";

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + "(" +
                "usuario TEXT PRIMARY KEY," +
                "password TEXT," +
                "role TEXT)");

        ContentValues datosAdmin = new ContentValues();
        datosAdmin.put("usuario", "admin");
        datosAdmin.put("password", "12345");
        datosAdmin.put("role", "admin");
        db.insert(TABLE_USERS, null, datosAdmin);

        // Ejemplo de usuario normal
        ContentValues datosUsuario = new ContentValues();
        datosUsuario.put("usuario", "usuario");
        datosUsuario.put("password", "6789");
        datosUsuario.put("role", "user");
        db.insert(TABLE_USERS, null, datosUsuario);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //recibe usuario y contraseña y busca en la bd, retorna un boolean
    public String checkUser(String user, String pass) {
        SQLiteDatabase bd = this.getReadableDatabase();
        Cursor fila = bd.rawQuery("SELECT role FROM t_users WHERE usuario = ? AND password = ?", new String[]{user, pass});

        if (fila.moveToFirst()) {
            String role = fila.getString(0);
            fila.close();
            return role;
        } else {
            fila.close();
            return null;
        }
    }
}
