package edu.stanford.cs108.bunnyworld;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper {
    //Variable for single instance of DBSingleton
    private static DatabaseHelper single_instance = null;

    private static final String DATABASE_NAME = "BunnyWorldDB";
    public SQLiteDatabase db;

    private DatabaseHelper(Context context) {
        db = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type ='table' AND name = 'games';", null);
        if (cursor.getCount() == 0) {
            String cmd = "CREATE TABLE games (gameName Text, _id INTEGER PRIMARY KEY AUTOINCREMENT);";
            db.execSQL(cmd);
        }
    }

    public static DatabaseHelper getInstance(Context context) {
        if (single_instance == null) {
            single_instance = new DatabaseHelper(context.getApplicationContext());
        }
        return single_instance;
    }

    public void addGameToTable(String gameName) {
        String cmd = "INSERT INTO games VALUES ('" + gameName + "', NULL);";
        db.execSQL(cmd);
    }

    public boolean gameExists(String gameName) {
        Cursor cursor = db.rawQuery("SELECT * FROM games WHERE gameName = '" + gameName + "';", null);
        if ((cursor != null) && (cursor.getCount() != 0)) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public long getGameId(String gameName) {
        Cursor cursor = db.rawQuery("SELECT * FROM games WHERE gameName = '" + gameName + "';", null);
        cursor.moveToLast();
        int colIndex = cursor.getColumnIndex("_id");
        int id = cursor.getInt(colIndex);
        cursor.close();
        return id;
    }


}
