package com.zucc.wushuran31401394.mycurrencies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wushu on 2017/7/6.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_COIN = "create table Currency (id integer primary key autoincrement,before text," +
            "before_currency text,after text,after_currency text)";

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:
                db.execSQL(CREATE_COIN);
            default:
        }
    }
}
