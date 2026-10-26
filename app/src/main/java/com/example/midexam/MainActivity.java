package com.example.midexam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etStudentName;
    private EditText etStudentId;
    private CheckBox cbEnrolled;
    private LinearLayout recordsContainer;

    private StudentDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etStudentName = findViewById(R.id.etStudentName);
        etStudentId = findViewById(R.id.etStudentId);
        cbEnrolled = findViewById(R.id.cbEnrolled);
        recordsContainer = findViewById(R.id.recordsContainer);

        Button btnAdd = findViewById(R.id.btnAdd);

        database = new StudentDatabase(this);

        btnAdd.setOnClickListener(v -> addStudent());

        displayStudents();
    }
    private void addStudent() {
        String name = etStudentName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();

        String status = cbEnrolled.isChecked()
                ? "Enrolled"
                : "Not Enrolled";

        if (name.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("student_name", name);
        values.put("student_id", studentId);
        values.put("enrollment_status", status);

        long result = database.getWritableDatabase().insert(
                "students",
                null,
                values
        );

        if (result == -1) {
            Toast.makeText(this,
                    "Student ID already exists",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this,
                "Student added successfully",
                Toast.LENGTH_SHORT).show();

        etStudentName.setText("");
        etStudentId.setText("");
        cbEnrolled.setChecked(false);

        displayStudents();
    }
    private void displayStudents() {
        recordsContainer.removeAllViews();

        Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT _id, student_name, student_id, enrollment_status "
                        + "FROM students ORDER BY _id DESC",
                null
        );

        if (cursor.getCount() == 0) {
            TextView emptyMessage = new TextView(this);
            emptyMessage.setText("No student records yet.");
            emptyMessage.setTextColor(0xFF667085);
            emptyMessage.setTextSize(14);
            emptyMessage.setPadding(4, 16, 4, 16);

            recordsContainer.addView(emptyMessage);
        }

        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            String name = cursor.getString(1);
            String studentId = cursor.getString(2);
            String status = cursor.getString(3);

            addStudentCard(id, name, studentId, status);
        }

        cursor.close();
    }
    private void addStudentCard(
            long id,
            String name,
            String studentId,
            String status) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 14, 16, 14);
        card.setBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 12);
        card.setLayoutParams(cardParams);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(0xFF17243A);
        nameView.setTextSize(17);
        nameView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        card.addView(nameView);

        TextView idView = new TextView(this);
        idView.setText("Student ID: " + studentId);
        idView.setTextColor(0xFF475467);
        idView.setTextSize(14);
        idView.setPadding(0, 5, 0, 0);

        card.addView(idView);

        TextView statusView = new TextView(this);
        statusView.setText("Enrollment: " + status);
        statusView.setTextColor(
                status.equals("Enrolled")
                        ? 0xFF16803D
                        : 0xFFB42318
        );
        statusView.setTextSize(14);
        statusView.setPadding(0, 4, 0, 8);

        card.addView(statusView);

        Button btnDelete = new Button(this);
        btnDelete.setText("DELETE");

        btnDelete.setOnClickListener(
                v -> deleteStudent(id)
        );

        card.addView(btnDelete);

        recordsContainer.addView(card);
    }
    private void deleteStudent(long id) {
        int result = database.getWritableDatabase().delete(
                "students",
                "_id = ?",
                new String[]{String.valueOf(id)}
        );

        if (result > 0) {
            Toast.makeText(this,
                    "Student deleted successfully",
                    Toast.LENGTH_SHORT).show();

            displayStudents();
        } else {
            Toast.makeText(this,
                    "Could not delete student",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private static class StudentDatabase
            extends SQLiteOpenHelper {

        private static final String DATABASE_NAME =
                "student_enrollment.db";

        private static final int DATABASE_VERSION = 1;

        StudentDatabase(Context context) {
            super(
                    context,
                    DATABASE_NAME,
                    null,
                    DATABASE_VERSION
            );
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = "CREATE TABLE students ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "student_name TEXT NOT NULL, "
                    + "student_id TEXT NOT NULL UNIQUE, "
                    + "enrollment_status TEXT NOT NULL"
                    + ")";

            db.execSQL(query);
        }

        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int oldVersion,
                int newVersion) {
        }
    }
}
