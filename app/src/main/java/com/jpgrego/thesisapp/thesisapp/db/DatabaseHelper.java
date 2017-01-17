package com.jpgrego.thesisapp.thesisapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jpgrego on 14/12/16.
 */

public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION    = 3;
    private static final String DATABASE_NAME    = "database.db";

    public DatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DatabaseContract.CellEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(DatabaseContract.WifiAPEntry.SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(DatabaseContract.BluetoothEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch(oldVersion) {
            case 1:
            case 2:
                sqLiteDatabase.execSQL(DatabaseContract.BluetoothEntry.SQL_CREATE_TABLE);
                break;
            default:
                sqLiteDatabase.execSQL(DatabaseContract.CellEntry.SQL_DELETE_TABLE);
                sqLiteDatabase.execSQL(DatabaseContract.WifiAPEntry.SQL_DELETE_TABLE);
                sqLiteDatabase.execSQL(DatabaseContract.BluetoothEntry.SQL_DELETE_TABLE);
                onCreate(sqLiteDatabase);
                break;
        }
    }
}
