package com.example.multi_notes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.JsonWriter;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBHelper extends SQLiteOpenHelper {

    // Tên DB và version bất kì
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    // Tên bảng
    private static final String TABLE_NAME = "notes";

    // Các thuộc tính của bảng
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_REMINDER = "reminder";
    private static final String COLUMN_IMAGES = "images";

    // Khởi tạo
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Tạo bảng tự động khi khởi tạo DBHelper
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_REMINDER + " TEXT, " +
                COLUMN_IMAGES + " TEXT, " +
                COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createTableQuery);
    }

    // Xóa bảng tự động khi có bảng trùng
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Thêm một "note" mới
    public void addNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_TIMESTAMP, note.getTimestamp());
        values.put(COLUMN_REMINDER, note.getReminder());
        values.put(COLUMN_IMAGES, note.getImages());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Lấy ra danh sách các note
    public ArrayList<Note> getAllNotes() {
        ArrayList<Note> noteList = new ArrayList<>();

        // Thực hiện truy vấn để lấy dữ liệu từ cơ sở dữ liệu SQLite
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notes", null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String content = cursor.getString(2);
                String reminder = cursor.getString(3);
                String images = cursor.getString(4);
                String timestamp = cursor.getString(5);

                Note note = new Note(id, title, content, timestamp, reminder, images);
                noteList.add(note);
            }
            while (cursor.moveToNext());
        }

        // Đóng con trỏ và cơ sở dữ liệu
        cursor.close();
        db.close();

        // Trả về danh sách các đối tượng Note
        return noteList;
    }

    // Lấy ra Note theo id
    public Note getNoteById(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        Note note = null;

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
            @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
            @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
            @SuppressLint("Range") String images = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGES));
            @SuppressLint("Range") String reminder = cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER));

            note = new Note(id, title, content, timestamp, reminder, images);
        }

        cursor.close();
        db.close();

        return note;
    }

    // Cập nhật note
    public void updateNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_REMINDER, note.getReminder());
        values.put(COLUMN_IMAGES, note.getImages());

        db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }


    // Xóa note theo id
    public void deleteNote(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Tìm kiếm note
    public List<Note> searchNotes(String query) {
        List<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String selection = COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};

        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                @SuppressLint("Range") String content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
                @SuppressLint("Range") String timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                @SuppressLint("Range") String reminder = cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER));
                @SuppressLint("Range") String images = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGES));

                Note note = new Note(id, title, content, timestamp, reminder, images);
                notes.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return notes;
    }

    // Hàm lấy ra thời gian hiện tại
    public String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        return timestamp;
    }


    // Hàm kiểm tra DB có tồn tại hay ko
    public boolean isSQLiteDatabaseEmpty(String databasePath) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = SQLiteDatabase.openDatabase(databasePath, null, SQLiteDatabase.OPEN_READONLY);
            String tableName = TABLE_NAME;

            cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);

            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                return count == 0; // Trả về true nếu bảng không có dữ liệu
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return true; // Trả về true nếu xảy ra lỗi hoặc không thể truy cập vào tệp SQLite
    }
}
