package com.example.remontproject;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class stats extends AppCompatActivity {

    private TextView tvRepairTotal, tvRepairWork, tvRepairDone;
    private TextView tvPaintTotal, tvPaintWork, tvPaintDone;
    private TableLayout tableDone, tableInWork;
    private ImageButton btnBack;

    private RequestRepo repo;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_stats);

        repo = new RequestRepo(this);

        // счетчики
        tvRepairTotal = findViewById(R.id.tvRepairTotal);
        tvRepairWork = findViewById(R.id.tvRepairWork);
        tvRepairDone = findViewById(R.id.tvRepairDone);


        // таблицы
        tableDone = findViewById(R.id.tableDone);
        tableInWork = findViewById(R.id.tableInWork);

        refresh();
    }

    private void refresh() {
        // счетчики
        tvRepairTotal.setText(String.valueOf(repo.count("REPAIR")));
        tvRepairWork.setText(String.valueOf(repo.count("REPAIR", "IN_PROGRESS")));
        tvRepairDone.setText(String.valueOf(repo.count("REPAIR", "DONE")));

        tvPaintTotal.setText(String.valueOf(repo.count("PAINT")));
        tvPaintWork.setText(String.valueOf(repo.count("PAINT", "IN_PROGRESS")));
        tvPaintDone.setText(String.valueOf(repo.count("PAINT", "DONE")));

        renderDone();
        renderInWork();
    }

    private void renderDone() {
        tableDone.removeAllViews();

        List<ContentValues> rows = new ArrayList<>();
        rows.addAll(repo.listByTypeAndStatus("REPAIR", "DONE", "done_date DESC, done_time DESC"));
        rows.addAll(repo.listByTypeAndStatus("PAINT", "DONE", "done_date DESC, done_time DESC"));
        if (rows.isEmpty()) {
            tableDone.addView(oneLine("ПУСТО"));
            return;
        }

        // заголовок
        tableDone.addView(header("Модель", "Дата (готово)", "Тип"));

        // до 20 последних
        int max = Math.min(20, rows.size());
        for (int i = 0; i < max; i++) {
            ContentValues v = rows.get(i);
            String model = n(v.getAsString("model"));
            String date = n(v.getAsString("done_date"));
            String type = "REPAIR".equals(v.getAsString("type")) ? "ремонт" : "покраска";
            tableDone.addView(row(model, date, type));
        }
    }

    private void renderInWork() {
        tableInWork.removeAllViews();
        List<ContentValues> rows = new ArrayList<>();
        rows.addAll(repo.listByTypeAndStatus("REPAIR", "IN_PROGRESS", "created_date DESC, created_time DESC"));
        rows.addAll(repo.listByTypeAndStatus("PAINT", "IN_PROGRESS", "created_date DESC, created_time DESC"));
        if (rows.isEmpty()) {
            tableInWork.addView(oneLine("Пусто"));
            return;
        }

        // заголовок
        tableInWork.addView(header("Модель", "Дата создания", "Тип"));
        int max = Math.min(20, rows.size());
        for (int i = 0; i < max; i++) {
            ContentValues v = rows.get(i);
            String model = n(v.getAsString("model"));
            String date = n(v.getAsString("created_date"));
            String type = "REPAIR".equals(v.getAsString("type")) ? "ремонт" : "покраска";
            tableInWork.addView(row(model, date, type));
        }
    }

    private TableRow header(String c1, String c2, String c3) {
        TableRow tr = new TableRow(this);
        tr.setPadding(dp(4), dp(4), dp(4), dp(4));

        tr.addView(cell(c1, true, 1f, Gravity.START));
        tr.addView(cell(c2, true, 1f, Gravity.CENTER));
        tr.addView(cell(c3, true, 1f, Gravity.END));

        // тонкая разделительная линия
        View divider = new View(this);
        divider.setBackgroundColor(0xFF202020);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dp(1));
        lp.span = 3;
        divider.setLayoutParams(lp);
        tr.addView(divider);

        return tr;
    }

    private TableRow row(String c1, String c2, String c3) {
        TableRow tr = new TableRow(this);
        tr.setPadding(dp(4), dp(8), dp(4), dp(8));
        tr.addView(cell(c1, false, 1f, Gravity.START));
        tr.addView(cell(c2, false, 1f, Gravity.CENTER));
        tr.addView(cell(c3, false, 1f, Gravity.END));

        return tr;
    }

    private TableRow oneLine(String text) {
        TableRow tr = new TableRow(this);
        tr.setPadding(dp(8), dp(12), dp(8), dp(12));
        tr.addView(cell(text, false, 1f, Gravity.START));
        return tr;
    }

    private TextView cell(String text, boolean header, float weight, int gravity) {
        TextView tv = new TextView(this);
        TableRow.LayoutParams p = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        tv.setLayoutParams(p);
        tv.setText(n(text));
        tv.setGravity(gravity);
        tv.setTextSize(header ? 14 : 14);
        tv.setTextColor(header ? 0xFFCCCCCC : 0xFFECECEC);
        if (header) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        return tv;
    }

    private int dp(int v){ return Math.round(getResources().getDisplayMetrics().density * v); }
    private String n(String s){ return s == null ? "-" : s; }
}