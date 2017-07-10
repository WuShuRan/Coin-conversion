package com.zucc.wushuran31401394.mycurrencies;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RateActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db ;
    private List list = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        dbHelper = new MyDatabaseHelper(this,"Currency.db",null,1);
        db = dbHelper.getWritableDatabase();
        Log.d("wushuran","mission start");
        Cursor cursor = db.query("Rate",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                long time = cursor.getLong(cursor.getColumnIndex("time"));
                Double rate = cursor.getDouble(cursor.getColumnIndex("rate"));
                list.add("\t"+new Date((time)) +"\t\t\t\t\t : \t\t\t\t\t"+rate);
            }while (cursor.moveToNext());
        }
        cursor.close();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                RateActivity.this,android.R.layout.simple_expandable_list_item_1,list);
        final ListView listView = (ListView)findViewById(R.id.ratelist);
        listView.setAdapter(adapter);
    }
}
