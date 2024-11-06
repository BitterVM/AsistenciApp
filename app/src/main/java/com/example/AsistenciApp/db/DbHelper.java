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
                "password TEXT)");
        String user = "admin";
        String pass = "12345";
        ContentValues datosAdmin = new ContentValues();
        datosAdmin.put("usuario", user);
        datosAdmin.put("password", pass);
        db.insert("t_users", "(usuario, password)", datosAdmin);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //recibe usuario y contraseña y busca en la bd, retorna un boolean
    public boolean checkUser(String user, String pass){
        SQLiteDatabase bd = this.getWritableDatabase();
        Cursor fila = bd.rawQuery("SELECT * FROM t_users WHERE usuario= ? AND password= ?", new String[]{user, pass});

        if (fila.getCount() > 0)
            return true;
        else return false;
    }
}
