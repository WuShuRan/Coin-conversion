package com.zucc.wushuran31401394.mycurrencies;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    private  SQLiteDatabase db ;
    private List list = new ArrayList();
    private EditText mfindbeforeEditText;
    private EditText mFindafterEditText;
    private Button mFindButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mfindbeforeEditText = (EditText)findViewById(R.id.find_before);
        mFindafterEditText = (EditText) findViewById(R.id.find_after);
        mFindButton = (Button)findViewById(R.id.find);

        dbHelper = new MyDatabaseHelper(this,"Currency.db",null,1);

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("Currency",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String before = cursor.getString(cursor.getColumnIndex("before"));
                String before_currency = cursor.getString(cursor.getColumnIndex("before_currency"));
                String after = cursor.getString(cursor.getColumnIndex("after"));
                String after_currency = cursor.getString(cursor.getColumnIndex("after_currency"));
                list.add("\t"+before +"\t\t\t"+ before_currency +"\t\t\t\t\t=>\t\t\t\t\t"+ after +"\t\t\t"+ after_currency);
                Log.d("db",before);
                Log.d("db",before_currency);
                Log.d("db",after);
                Log.d("db",after_currency);
            }while (cursor.moveToNext());
        }
        cursor.close();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                HistoryActivity.this,android.R.layout.simple_expandable_list_item_1,list);
        final ListView listView = (ListView)findViewById(R.id.historylist);
        listView.setAdapter(adapter);

        mFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = db.query("Currency",null,"before_currency like ? and after_currency like ?",new String[]{"%"+mfindbeforeEditText.getText().toString()+"%","%"+mFindafterEditText.getText().toString()+"%"},null,null,null);
                list.clear();
                if(cursor.moveToFirst()){
                    do{
                        String before = cursor.getString(cursor.getColumnIndex("before"));
                        String before_currency = cursor.getString(cursor.getColumnIndex("before_currency"));
                        String after = cursor.getString(cursor.getColumnIndex("after"));
                        String after_currency = cursor.getString(cursor.getColumnIndex("after_currency"));
                        list.add("\t"+before +"\t\t\t"+ before_currency +"\t\t\t\t\t=>\t\t\t\t\t"+ after +"\t\t\t"+ after_currency);
                        Log.d("db",before);
                        Log.d("db",before_currency);
                        Log.d("db",after);
                        Log.d("db",after_currency);
                    }while (cursor.moveToNext());
                }
                cursor.close();
                adapter.notifyDataSetChanged();
                Toast.makeText(HistoryActivity.this,mfindbeforeEditText.getText().toString()+"  "+mFindafterEditText.getText().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
