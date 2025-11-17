package com.example.remontproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestRepo {
    private final DbHelper helper;

    public RequestRepo(Context c) {
        helper = new DbHelper(c.getApplicationContext());
    }

    // --- CREATE ---

    public void createRepair(String owner, String phone, String model, String desc,
                             String date, String time) {
        ContentValues v = base(owner, phone, model, date, time);
        v.put("description", desc);
        v.put("type", "REPAIR");
        insert(v);
    }

    public void createPaint(String owner, String phone, String model, String color,
                            String date, String time) {
        ContentValues v = base(owner, phone, model, date, time);
        v.put("color", color);
        v.put("type", "PAINT");
        insert(v);
    }

    private ContentValues base(String owner, String phone, String model, String date, String time) {
        ContentValues v = new ContentValues();
        v.put("id", UUID.randomUUID().toString());
        v.put("owner", owner);
        v.put("phone", phone);
        v.put("model", model);
        v.put("status", "IN_PROGRESS");
        v.put("created_date", date); // "16.11.2024"
        v.put("created_time", time); // "18:24"
        return v;
    }

    private void insert(ContentValues v) {
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            db.insertOrThrow(DbHelper.T_REQUESTS, null, v);
        }
    }

    // --- READ ---
    public List<ContentValues> listByType(String type, String orderBy) {
        String ob = (orderBy == null || orderBy.trim().isEmpty())
                ? "created_date DESC, created_time DESC"
                : orderBy;
        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.query(DbHelper.T_REQUESTS, null, "type=?",
                     new String[]{type}, null, null, ob)) {
            return copyCursor(c);
        }
    }

    public List<ContentValues> listByTypeAndStatus(String type, String status, String orderBy) {
        String ob = (orderBy == null || orderBy.trim().isEmpty())
                ? ("DONE".equals(status) ? "done_date DESC, done_time DESC"
                : "created_date DESC, created_time DESC")
                : orderBy;
        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.query(DbHelper.T_REQUESTS, null, "type=? AND status=?",
                     new String[]{type, status}, null, null, ob)) {
            return copyCursor(c);
        }
    }

    public List<ContentValues> listWhere(String where, String[] args, String orderBy) {
        String ob = (orderBy == null || orderBy.trim().isEmpty())
                ? "created_date DESC, created_time DESC"
                : orderBy;
        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.query(DbHelper.T_REQUESTS, null, where, args, null, null, ob)) {
            return copyCursor(c);
        }
    }

    /**
     * Прочитать одну запись по id (для редактирования)
     */
    public ContentValues getById(String id) {
        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.query(DbHelper.T_REQUESTS, null, "id=?",
                     new String[]{id}, null, null, null)) {
            if (c.moveToFirst()) {
                ContentValues v = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(c, v);
                return v;
            }
            return null;
        }
    }

// --- UPDATE ---

    /**
     * ОТМЕТИТЬ КАК ВЫПОЛНЕНО + ЗАФИКСИРОВАТЬ ДАТУ/ВРЕМЯ ВЫПОЛНЕНИЯ
     */
    public void markDone(String id, String doneDate, String doneTime) {
        ContentValues v = new ContentValues();
        v.put("status", "DONE");
        v.put("done_date", doneDate);
        v.put("done_time", doneTime);
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            db.update(DbHelper.T_REQUESTS, v, "id=?", new String[]{id});
        }
    }

    /**
     * Редактирование записи ремонта
     */
    public void updateRepair(String id, String owner, String phone, String model, String desc) {
        ContentValues v = new ContentValues();
        v.put("owner", owner);
        v.put("phone", phone);
        v.put("model", model);
        v.put("description", desc);
        updateById(id, v);
    }

    /**
     * Редактирование записи покраски
     */
    public void updatePaint(String id, String owner, String phone, String model, String color) {
        ContentValues v = new ContentValues();
        v.put("owner", owner);
        v.put("phone", phone);
        v.put("model", model);
        v.put("color", color);
        updateById(id, v);
    }

    private void updateById(String id, ContentValues v) {
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            db.update(DbHelper.T_REQUESTS, v, "id=?", new String[]{id});
        }
    }

// --- DELETE ---

    public void delete(String id) {
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            db.delete(DbHelper.T_REQUESTS, "id=?", new String[]{id});
        }
    }

    public int count(String type) {
        return count(type, null);
    }

    public int count(String type, String status) {
        String sel = status == null ? "type=?" : "type=? AND status=?";
        String[] args = status == null ? new String[]{type} : new String[]{type, status};
        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbHelper.T_REQUESTS + " WHERE " + sel, args)) {
            c.moveToFirst();
            return c.getInt(0);
        }
    }

    /**
     * Добавить демо-данные, если таблица пустая
     */
    public void seedIfEmpty() {
        try (SQLiteDatabase db = helper.getReadableDatabase();
             Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbHelper.T_REQUESTS, null)) {
            c.moveToFirst();
            if (c.getInt(0) > 0) return;
        }
        createRepair("Иван", "+7 900 000-00-00", "Audi A6", "СТУК В ДВИГАТЕЛЕ", "16.11.2024", "18:22");
        createRepair("Олег", "+7 901 111-11-11", "Audi TT", "", "16.11.2024", "18:24");
        createRepair("Лена", "+7 902 222-22-22", "Polo", "", "16.11.2024", "18:37");
        createPaint("Рита", "+7 903 333-33-33", "Niva", "Красный", "16.11.2024", "18:40");
    }

    private List<ContentValues> copyCursor(Cursor c) {
        ArrayList<ContentValues> list = new ArrayList<>();
        while (c.moveToNext()) {
            ContentValues v = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(c, v);
            list.add(v);
        }
        return list;
    }
}
