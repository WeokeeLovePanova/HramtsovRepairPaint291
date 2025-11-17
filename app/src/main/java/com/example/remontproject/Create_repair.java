package com.example.remontproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Create_repair extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    private EditText etOwner, etPhone, etModel, etDesc;

    private Button btnPickDate, btnPickTime, btnSubmit;

    private TextView tvPicked;

    private final Calendar cal = Calendar.getInstance();

    private String pickedDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(cal.getTime());

    private String pickedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.getTime());

    private RequestRepo repo;

    private String editId = null;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.create_repair);

        repo = new RequestRepo(this);

        etOwner = findViewById(R.id.etOwner);
        etPhone = findViewById(R.id.etPhone);
        etModel = findViewById(R.id.etModel);
        etDesc = findViewById(R.id.etDesc);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvPicked = findViewById(R.id.tvPicked);

        editId = getIntent().getStringExtra(EXTRA_ID);
        if (editId != null) {
            setTitle("Редактирование ремонта");
            btnSubmit.setText("Сохранить");
            ContentValues v = repo.getById(editId);
            if (v != null) {
                etOwner.setText(n(v.getAsString("owner")));
                etPhone.setText(n(v.getAsString("phone")));
                etModel.setText(n(v.getAsString("model")));
                etDesc.setText(n(v.getAsString("description")));
                pickedDate = n(v.getAsString("created_date"), pickedDate);
                pickedTime = n(v.getAsString("created_time"), pickedTime);
            }
        } else {
            setTitle("Новая заявка: ремонт");
        }

        btnPickDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, y, m, d) -> {
                cal.set(y, m, d);
                pickedDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(cal.getTime());
                tvPicked.setText("Дата: " + pickedDate + "  Время: " + pickedTime);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPickTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, h, min) -> {
                cal.set(Calendar.HOUR_OF_DAY, h);
                cal.set(Calendar.MINUTE, min);
                pickedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.getTime());
                tvPicked.setText("Дата: " + pickedDate + "  Время: " + pickedTime);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        btnSubmit.setOnClickListener(v -> {
            String owner = etOwner.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String model = etModel.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (owner.isEmpty() || phone.isEmpty() || model.isEmpty()) {
                Toast.makeText(this, "Заполните имя, телефон и модель", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editId == null) {
                repo.createRepair(owner, phone, model, desc, pickedDate, pickedTime);
                Toast.makeText(this, "Заявка создана", Toast.LENGTH_SHORT).show();
            } else {
                repo.updateRepair(editId, owner, phone, model, desc);
                Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
            }
            finish();
        });

        tvPicked.setText("Дата: " + pickedDate + "  Время: " + pickedTime);
    }

    private String n(String s){ return s==null? "" : s; }

    private String n(String s, String def){ return (s==null || s.isEmpty()) ? def : s; }
}