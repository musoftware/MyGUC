package com.lzmouse.myguc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ahmed Ali on 4/2/2018.
 */

public class MyGucDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "myguc";
    private static final int DB_VERSION = 1;


    public static final String ACCOUNT_TABLE = "account";
    public static final String USER_NAME_COL = "USER_NAME";
    public static final String PASSWORD_COL = "PASSWORD";

    public static final String FAVOURITES_TABLE = "favourites";
    public static final String PATH_COL = "PATH";
    public static final String NAME_COL = "NAME";

    public static final String SUBJECTS_TABLE = "subjects";
    public static final String DATE_COL = "DATE";
    public static final String COLOR_COL = "COLOR";

    public static final String SOURCES_TABLE = "lectures";
    public static final String SUBJECT_ID_COL = "subject_id";
    public static final String TYPE_COL = "type";

    public static final String ENTRIES_TABLE = "entries";
    public static final String SOURCE_ID_COL = "source_id";
    public static final String DURATION_COL = "duration";
    public static final String NOTE_COL = "note";
    public MyGucDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format("CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT) ;"
                ,ACCOUNT_TABLE,USER_NAME_COL,PASSWORD_COL));
        db.execSQL(String.format("CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT) ;"
                ,FAVOURITES_TABLE,PATH_COL,NAME_COL));
        db.execSQL(String.format("CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER, %s INTEGER) ;"
                ,SUBJECTS_TABLE,NAME_COL,DATE_COL,COLOR_COL));
        db.execSQL(String.format("CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER, %s INTEGER, %s INTEGER) ;"
                ,SOURCES_TABLE,NAME_COL,DATE_COL,SUBJECT_ID_COL,TYPE_COL));
        db.execSQL(String.format("CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER) ;"
                ,ENTRIES_TABLE,NAME_COL,PATH_COL,NOTE_COL,DATE_COL, SOURCE_ID_COL,TYPE_COL, DURATION_COL));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
