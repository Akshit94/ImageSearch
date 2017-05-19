package com.example.akshit.imagesearch.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PhotosDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;

    private static final String DATABASE_NAME = "photos_database.db";

    private static final String TEXT_NOT_NULL = " TEXT NOT NULL, ";

    public PhotosDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_PHOTOS_TABLE = "CREATE TABLE " + PhotosContract.PhotoEntry.TABLE_NAME + " (" +
                PhotosContract.PhotoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PhotosContract.PhotoEntry.QUERY_STRING + TEXT_NOT_NULL +
                PhotosContract.PhotoEntry.PHOTO_URL + TEXT_NOT_NULL +
                PhotosContract.PhotoEntry.PAGE_NUMBER + " INTEGER NOT NULL" + ");";

        db.execSQL(SQL_CREATE_PHOTOS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + PhotosContract.PhotoEntry.TABLE_NAME);
        onCreate(db);

    }
}
