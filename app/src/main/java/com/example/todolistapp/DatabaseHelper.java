
package com.example.todolistapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Imports baru untuk logika filter
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {

    // =====================================================================

    public static final String DATABASE_NAME = "folderly.db";
    public static final String TABLE_USERS = "users";
    public static final String COL_ID = "id"; // Primary Key untuk Users
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    private static final int DATABASE_VERSION = 1; // Pastikan ini sama

    // =====================================================================

    // Konstanta untuk Tabel Todos
    public static final String TABLE_TODOS = "todos";
    public static final String COLUMN_TODO_ID = "todo_id";
    public static final String COLUMN_TASK_NAME = "task_name";
    public static final String COLUMN_TASK_DESC = "task_desc";
    public static final String COLUMN_DUE_DATE = "due_date";
    public static final String COLUMN_DUE_TIME = "due_time";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_STATUS = "status"; // 0=Ongoing, 1=Completed
    public static final String COLUMN_FK_USER_ID = "user_id";

    private static final String CREATE_TABLE_TODOS = "CREATE TABLE " + TABLE_TODOS + "("
            + COLUMN_TODO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TASK_NAME + " TEXT,"
            + COLUMN_TASK_DESC + " TEXT,"
            + COLUMN_DUE_DATE + " TEXT," // Format: "dd-MM-yyyy"
            + COLUMN_DUE_TIME + " TEXT," // Format: "HH:mm"
            + COLUMN_LABEL + " TEXT,"
            + COLUMN_STATUS + " INTEGER DEFAULT 0,"
            + COLUMN_FK_USER_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_FK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + ")"
            + ")";

    // =====================================================================

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)");
        db.execSQL(CREATE_TABLE_TODOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
        onCreate(db);
    }

    // =====================================================================
    // == FUNGSI UNTUK USERS ==
    // =====================================================================

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username=? AND password=?",
                new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    
    // (Tambahan) Ambil user ID berdasarkan username
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_ID}, COL_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            cursor.close();
            return userId;
        }
        return -1; // atau 0, tergantung desain Anda
    }

    // =====================================================================
    // == FUNGSI CRUD UNTUK TODOS ==
    // =====================================================================

    public boolean addTodo(String taskName, String taskDesc, String dueDate, String dueTime, String label, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, taskName);
        values.put(COLUMN_TASK_DESC, taskDesc);
        values.put(COLUMN_DUE_DATE, dueDate);
        values.put(COLUMN_DUE_TIME, dueTime);
        values.put(COLUMN_LABEL, label);
        values.put(COLUMN_FK_USER_ID, userId);
        values.put(COLUMN_STATUS, 0);

        long result = db.insert(TABLE_TODOS, null, values);
        db.close();
        return result != -1;
    }

    public Cursor getTodosForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TODOS, null,
                COLUMN_FK_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_TODO_ID + " DESC");
    }

    /**
     * (BARU) READ: Mengambil todos berdasarkan user dan filter status.
     */
    public Cursor getTodosFiltered(int userId, String filter) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_FK_USER_ID + " = ?";
        
        // Menggunakan List agar dinamis
        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(String.valueOf(userId));

        // Dapatkan tanggal hari ini dalam format YYYY-MM-DD untuk perbandingan SQL
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Ekspresi SQL untuk mengubah format tanggal 'dd-MM-yyyy' menjadi 'yyyy-MM-dd'
        String storedDateAsISO = "substr(" + COLUMN_DUE_DATE + ", 7, 4) || '-' || substr(" + COLUMN_DUE_DATE + ", 4, 2) || '-' || substr(" + COLUMN_DUE_DATE + ", 1, 2)";

        switch (filter) {
            case "Completed":
                selection += " AND " + COLUMN_STATUS + " = 1";
                break;
            case "Missed":
                // Status 0 (ongoing) DAN tanggalnya sudah lewat
                selection += " AND " + COLUMN_STATUS + " = 0 AND " + storedDateAsISO + " < ?";
                selectionArgsList.add(today);
                break;
            case "Ongoing":
            default: // Default filter adalah Ongoing
                // Status 0 (ongoing) DAN (tanggalnya belum lewat ATAU tidak ada tanggal)
                selection += " AND " + COLUMN_STATUS + " = 0 AND (" + storedDateAsISO + " >= ? OR " + COLUMN_DUE_DATE + " IS NULL OR " + COLUMN_DUE_DATE + " = '')";
                selectionArgsList.add(today);
                break;
        }

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);
        return db.query(TABLE_TODOS, null, selection, selectionArgs, null, null, COLUMN_TODO_ID + " DESC");
    }

    public boolean updateTodoStatus(int todoId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);

        int rowsAffected = db.update(TABLE_TODOS, values,
                COLUMN_TODO_ID + "=?",
                new String[]{String.valueOf(todoId)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * (BARU) UPDATE: Mengubah detail dari sebuah todo.
     */
    public boolean updateTodoDetails(int todoId, String taskName, String description, String dueDate, String dueTime, String label) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, taskName);
        values.put(COLUMN_TASK_DESC, description);
        values.put(COLUMN_DUE_DATE, dueDate);
        values.put(COLUMN_DUE_TIME, dueTime);
        values.put(COLUMN_LABEL, label);

        int rowsAffected = db.update(TABLE_TODOS, values, COLUMN_TODO_ID + " = ?", new String[]{String.valueOf(todoId)});
        db.close();
        return rowsAffected > 0;
    }


    public boolean deleteTodo(int todoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TODOS,
                COLUMN_TODO_ID + "=?",
                new String[]{String.valueOf(todoId)});
        db.close();
        return rowsAffected > 0;
    }
}
