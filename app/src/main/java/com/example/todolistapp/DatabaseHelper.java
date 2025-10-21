package com.example.todolistapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    public static final String COLUMN_LABEL = "label";       // Tetap ada untuk menyimpan label custom
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_FK_USER_ID = "user_id";

    private static final String CREATE_TABLE_TODOS = "CREATE TABLE " + TABLE_TODOS + "("
            + COLUMN_TODO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TASK_NAME + " TEXT,"
            + COLUMN_TASK_DESC + " TEXT,"
            + COLUMN_DUE_DATE + " TEXT,"
            + COLUMN_DUE_TIME + " TEXT,"
            + COLUMN_LABEL + " TEXT,"       // Label custom
            + COLUMN_STATUS + " INTEGER DEFAULT 0,"
            + COLUMN_FK_USER_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_FK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_ID + ")"
            + ")";

    // =====================================================================

    public DatabaseHelper(Context context) {
        // Menggunakan DATABASE_NAME dan DATABASE_VERSION yang sama
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // (Bagian Vita) Eksekusi query tabel user
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)");

        // (Bagian Hafiz) Eksekusi query tabel todos (sudah direvisi)
        db.execSQL(CREATE_TABLE_TODOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // (Bagian Vita) Drop tabel user
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // (Bagian Hafiz) Drop tabel todos
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
        onCreate(db);
    }

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

    // =====================================================================

    /**
     * CREATE: Menambahkan todo baru (parameter iconName dihapus)
     */
    public boolean addTodo(String taskName, String taskDesc, String dueDate, String dueTime, String label, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, taskName);
        values.put(COLUMN_TASK_DESC, taskDesc);
        values.put(COLUMN_DUE_DATE, dueDate);
        values.put(COLUMN_DUE_TIME, dueTime);
        values.put(COLUMN_LABEL, label); // Label custom
        values.put(COLUMN_FK_USER_ID, userId);
        values.put(COLUMN_STATUS, 0); // Status awal (0 = belum selesai)

        long result = db.insert(TABLE_TODOS, null, values);
        db.close();
        return result != -1; // Mengembalikan true jika insert berhasil
    }

    /**
     * READ: Mengambil semua todos milik user tertentu
     */
    public Cursor getTodosForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TODOS, null,
                COLUMN_FK_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_TODO_ID + " DESC");
    }

    /**
     * UPDATE: Mengubah status todo (selesai/belum selesai)
     */
    public boolean updateTodoStatus(int todoId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);

        int rowsAffected = db.update(TABLE_TODOS, values,
                COLUMN_TODO_ID + "=?",
                new String[]{String.valueOf(todoId)});
        db.close();
        return rowsAffected > 0; // Mengembalikan true jika update berhasil
    }

    /**
     * DELETE: Menghapus todo dari database
     */
    public boolean deleteTodo(int todoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TODOS,
                COLUMN_TODO_ID + "=?",
                new String[]{String.valueOf(todoId)});
        db.close();
        return rowsAffected > 0; // Mengembalikan true jika delete berhasil
    }
}