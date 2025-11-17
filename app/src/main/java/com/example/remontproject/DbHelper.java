package com.example.remontproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "autofix.db";
    public static final int DB_VER = 1;

    public static final String T_REQUESTS = "requests";

    public DbHelper(Context c) {
        super(c, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + T_REQUESTS + " (" +
                        " id TEXT PRIMARY KEY," +
                        " owner TEXT NOT NULL," +
                        " phone TEXT NOT NULL," +
                        " model TEXT NOT NULL," +
                        " description TEXT," + // ДЛЯ РЕМОНТА
                        " type TEXT NOT NULL," + // REPAIR | PAINT
                        " status TEXT NOT NULL," + // IN_PROGRESS | DONE
                        " created_date TEXT NOT NULL," + // 'dd.MM.yyyy'
                        " created_time TEXT NOT NULL," + // 'HH:mm'
                        " done_date TEXT," +
                        " done_time TEXT" +
                        " )"
        );
        db.execSQL("CREATE INDEX idx_req_type ON " + T_REQUESTS + "(type)");
        db.execSQL("CREATE INDEX idx_req_status ON " + T_REQUESTS + "(status)");
        db.execSQL("CREATE INDEX idx_req_created ON " + T_REQUESTS + "(created_date, created_time)");
        db.execSQL("CREATE INDEX idx_req_done ON " + T_REQUESTS + "(done_date, done_time)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + T_REQUESTS);
        onCreate(db);
    }
}