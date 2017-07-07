package com.example.daniel.agoto.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 6/2/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper databaseHelper;
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "tasks";

    public synchronized static DatabaseHelper getInstance(Context context) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }
        return databaseHelper;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String TABLE_TASK_NAME = "zadaci";
    private static final String TABLE_USERS_NAME = "korisnici";

    private static final String KEY_SERVER_ID = "server_id";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_BODY = "body";
    private static final String KEY_SOLUTION = "solution";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_SCORE = "score";
    private static final String KEY_COMPLETED = "completed";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_ME = "is_me";


    private static final String CREATE_TABLE_ZADACI = "CREATE TABLE " + TABLE_TASK_NAME + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_SERVER_ID + " INTEGER, "
            + KEY_NAME + " TEXT, "
            + KEY_BODY + " TEXT, "
            + KEY_SOLUTION + " TEXT, "
            + KEY_COMPLETED + " INTEGER, "
            + KEY_LONGITUDE + " REAL, "
            + KEY_LATITUDE + " REAL, "
            + KEY_SCORE + " INTEGER)";

    private static final String CREATE_TABLE_KORISNICI = "CREATE TABLE " + TABLE_USERS_NAME + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_EMAIL + " TEXT, "
            + KEY_SCORE + " INTEGER, "
            + KEY_IS_ME + " INTEGER)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ZADACI);
        db.execSQL(CREATE_TABLE_KORISNICI);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS_NAME);
        onCreate(db);
    }

    public List<Task> getAllUnfinishedTasks() {
        List<Task> tasks = new ArrayList<Task>();
        String selectQuery = "SELECT * FROM " + TABLE_TASK_NAME +
                " WHERE " + KEY_COMPLETED + "=0";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                tasks.add(new Task(
                        c.getLong(0),
                        c.getLong(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4),
                        c.getInt(5) > 0,
                        c.getDouble(6),
                        c.getDouble(7),
                        c.getInt(8)
                ));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return tasks;
    }

    public Integer getNumberOfSolvedTasks() {
        Integer countOfSolvedTasks;

        SQLiteDatabase db = this.getReadableDatabase();
        /**
         * https://stackoverflow.com/questions/12436596/how-to-count-number-of-records-in-sqlite-in-android
         */
        countOfSolvedTasks = (int) DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " +
                TABLE_TASK_NAME + " WHERE "
                + KEY_COMPLETED + "=1", null);

        db.close();
        return countOfSolvedTasks;
    }

    public void solveTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (task.isCompleted()) {
            values.put(KEY_COMPLETED, 1);
        } else values.put(KEY_COMPLETED, 0);

        // updating row
        db.update(TABLE_TASK_NAME, values, KEY_ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SERVER_ID, task.getServer_id());
        values.put(KEY_NAME, task.getName());
        values.put(KEY_BODY, task.getBody());
        values.put(KEY_SOLUTION, task.getSolution());
        if (task.isCompleted()) {
            values.put(KEY_COMPLETED, 1);
        } else {
            values.put(KEY_COMPLETED, 0);
        }
        values.put(KEY_LONGITUDE, task.getLongitude());
        values.put(KEY_LATITUDE, task.getLatitude());
        values.put(KEY_SCORE, task.getScore());

        db.insert(TABLE_TASK_NAME, null, values);
        db.close();
    }

    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASK_NAME, null, null);
        db.close();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USERS_NAME + " ORDER BY " + KEY_SCORE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                if (c.getInt(3) == 1) {
                    users.add(new User(
                            c.getLong(0),
                            c.getString(1),
                            c.getInt(2),
                            true
                    ));
                } else {
                    users.add(new User(
                            c.getLong(0),
                            c.getString(1),
                            c.getInt(2),
                            false
                    ));
                }

            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return users;
    }

    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_SCORE, user.getScore());
        if (user.getMe()) {
            values.put(KEY_IS_ME, 1);
        } else {
            values.put(KEY_IS_ME, 0);
        }

        db.insert(TABLE_USERS_NAME, null, values);
        db.close();
    }

    public void updateUserScore(Integer score) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_USERS_NAME + " WHERE "
                + KEY_IS_ME + " =1";

        Cursor c = db.rawQuery(selectQuery, null);
        User user;
        if (c.moveToFirst()) {
            user = new User(
                    c.getLong(0),
                    c.getString(1),
                    c.getInt(2),
                    c.getInt(3) > 0
            );
            ContentValues values = new ContentValues();
            Log.e("USER", String.valueOf(user));
            Log.e("TASKS SCIRE", String.valueOf(score));
            Log.e("USER SCIRE", String.valueOf(user.getScore()));
            Log.e("USER SCIRE", String.valueOf(score + user.getScore()));
            values.put(KEY_SCORE, score + user.getScore());

            // updating row
            db.update(TABLE_USERS_NAME, values, KEY_IS_ME + " =?",
                    new String[]{String.valueOf(1)});
            db.close();
        }
    }

    public void deleteAllUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS_NAME, null, null);
        db.close();
    }
}
