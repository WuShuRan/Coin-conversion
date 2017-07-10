package com.zucc.wushuran31401394.mycurrencies;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    //define members that correspond to Views in our layout
    private Button mCalcButton;
    private TextView mConvertedTextView;
    private EditText mAmountEditText;
    private Spinner mForSpinner,mHomSpinner;
    private String[] mCurrencies;

    private MyDatabaseHelper dbHelper;
    private  SQLiteDatabase db ;


    public static final String FOR = "FOR_CURRENCY";
    public static final String HOM = "HOM_CURRENCY";

    //this will contain my developers key
    private String mKey;
    //used to fetch the 'rates' json object from openexchangerates.org
    public static final String RATES = "rates";
    public static final String URL_BASE =
            "http://openexchangerates.org/api/latest.json?app_id=";
    //used to format data from openexchangerates.org
    private static final DecimalFormat DECIMAL_FORMAT = new
            DecimalFormat("#,##0.00000");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建数据库
        dbHelper = new MyDatabaseHelper(this,"Currency.db",null,1);
        db = dbHelper.getWritableDatabase();



        //unpack ArrayList from the bundle and convert to array
        ArrayList<String> arrayList = ((ArrayList<String>)
                getIntent().getSerializableExtra(SplashActivity.KEY_ARRAYLIST));
        Collections.sort(arrayList);
        mCurrencies = arrayList.toArray(new String[arrayList.size()]);

        //assign references to our Views;
        mConvertedTextView = (TextView) findViewById(R.id.txt_converted);
        mAmountEditText = (EditText) findViewById(R.id.edt_amount);
        mCalcButton = (Button) findViewById(R.id.btn_calc);
        mForSpinner = (Spinner) findViewById(R.id.spn_for);
        mHomSpinner = (Spinner) findViewById(R.id.spn_hom);

        //controller:mediates model and view
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                //context
                this,
                //view:layout you see when the spinner is closed
                R.layout.spinner_closed,
                //model:the array of Strings
                mCurrencies
        );
        //view:layout you see when the spinner is open
        arrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        //assign adapters to spinners
        mHomSpinner.setAdapter(arrayAdapter);
        mForSpinner.setAdapter(arrayAdapter);

        mHomSpinner.setOnItemSelectedListener(this);
        mForSpinner.setOnItemSelectedListener(this);

        //set to shared-preferences or pull from shared-preferences on restart
        if(savedInstanceState == null && (PrefsMgr.getString(this,FOR) == null && PrefsMgr.getString(this,HOM) == null)){
            mForSpinner.setSelection(findPositionGivenCode("USD",mCurrencies));
            mHomSpinner.setSelection(findPositionGivenCode("CNY",mCurrencies));

            PrefsMgr.setString(this,FOR,"USD");
            PrefsMgr.setString(this,HOM,"CNY");
        }else {
            mForSpinner.setSelection(findPositionGivenCode(PrefsMgr.getString(this,FOR),mCurrencies));
            mHomSpinner.setSelection(findPositionGivenCode(PrefsMgr.getString(this,HOM),mCurrencies));
        }
        mCalcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CurrencyConverterTask().execute(URL_BASE+mKey);
            }
        });
        mKey = getKey("open_key");
        new RateTask().execute(URL_BASE+mKey);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.mnu_invert:
                //TODO define behavior here
                invertCurrencies();
                break;
            case R.id.mnu_codes:
                //TODO define behavior here
                launchBrowser(SplashActivity.URL_CODES);
                break;
            
            case R.id.mnu_history:
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;

            case R.id.mnu_rate:
                Intent intent1 = new Intent(MainActivity.this, RateActivity.class);
                startActivity(intent1);
                break;

            case R.id.mnu_exit:
                finish();
                break;
        }
        return true;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
    private void launchBrowser(String strUri) {
        if (isOnline()) {
            Uri uri = Uri.parse(strUri);
            //称为隐含意图
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
    private void invertCurrencies() {
        int nFor = mForSpinner.getSelectedItemPosition();
        int nHom = mHomSpinner.getSelectedItemPosition();
        mForSpinner.setSelection(nHom);
        mHomSpinner.setSelection(nFor);
        mConvertedTextView.setText("");

        PrefsMgr.setString(this, FOR, extractCodeFromCurrency((String)
                mForSpinner.getSelectedItem()));
        PrefsMgr.setString(this, HOM, extractCodeFromCurrency((String)
                mHomSpinner.getSelectedItem()));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {

            case R.id.spn_for:
                PrefsMgr.setString(this, FOR,
                        extractCodeFromCurrency((String)mForSpinner.getSelectedItem()));
                break;

            case R.id.spn_hom:
                PrefsMgr.setString(this, HOM,
                        extractCodeFromCurrency((String)mHomSpinner.getSelectedItem()));
                break;

            default:
                break;
        }

        mConvertedTextView.setText("");

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private int findPositionGivenCode(String code, String[] currencies) {

        for (int i = 0; i < currencies.length; i++) {
            if (extractCodeFromCurrency(currencies[i]).equalsIgnoreCase(code)) {
                return i;
            }
        }
        //default
        return 0;
    }

    private String extractCodeFromCurrency(String currency){
        return (currency).substring(0,3);
    }

    private String getKey(String keyName){
        AssetManager assetManager = this.getResources().getAssets();
        Properties properties = new Properties();
        try {
            InputStream inputStream = assetManager.open("keys.properties");
            properties.load(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return  properties.getProperty(keyName);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // 充气菜单; 如果存在，则会将该项目添加到操作栏。
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    private class RateTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
//            Toast.makeText(mContext,"Y",Toast.LENGTH_SHORT).show();
        }
        @Override
        protected JSONObject doInBackground(String... params) {
            while(true){
                JSONObject jsonObject =  new JSONParser().getJSONFromUrl(params[0]);
                try {
                    if (jsonObject == null){
                        throw new JSONException("no data available.");
                    }
                    JSONObject jsonRates = jsonObject.getJSONObject(RATES);
                    //把数据存储到数据库中
                    ContentValues values = new ContentValues();
                    values.put("time",System.currentTimeMillis());
                    values.put("rate",jsonRates.getDouble("CNY"));
                    db.insert("Rate",null,values);
                    Thread.sleep(20000);
                } catch (JSONException e) {
                    Log.d("error", "There's been a JSON exception: " + e.getMessage());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {

        }
    }


    private class CurrencyConverterTask extends AsyncTask<String, Void, JSONObject> {
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Calculating Result...");
            progressDialog.setMessage("One moment please...");
            progressDialog.setCancelable(true);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CurrencyConverterTask.this.cancel(true);
                            progressDialog.dismiss();
                        }
                    });
            progressDialog.show();
        }
        @Override
        protected JSONObject doInBackground(String... params) {
            return new JSONParser().getJSONFromUrl(params[0]);
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            double dCalculated = 0.0;
            String strForCode =
                    extractCodeFromCurrency(mCurrencies[mForSpinner.getSelectedItemPosition()]);
            Log.d("strForCode",strForCode);
            String strHomCode = extractCodeFromCurrency(mCurrencies[mHomSpinner.
                    getSelectedItemPosition()]);
            Log.d("strHomCode",strHomCode);
            String strAmount = mAmountEditText.getText().toString();
            try {
                if (jsonObject == null){
                    throw new JSONException("no data available.");
                }
                JSONObject jsonRates = jsonObject.getJSONObject(RATES);
                if (strHomCode.equalsIgnoreCase("USD")){
                    dCalculated = Double.parseDouble(strAmount) / jsonRates.getDouble(strForCode);
                    Log.d("aaa", String.valueOf(jsonRates.getDouble(strForCode)));
                } else if (strForCode.equalsIgnoreCase("USD")) {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode) ;
                }
                else {
                    dCalculated = Double.parseDouble(strAmount) * jsonRates.getDouble(strHomCode)
                            / jsonRates.getDouble(strForCode) ;
                }
            } catch (JSONException e) {
                Toast.makeText(
                        MainActivity.this,
                        "There's been a JSON exception: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                mConvertedTextView.setText("");
                e.printStackTrace();
            }
            mConvertedTextView.setText(DECIMAL_FORMAT.format(dCalculated) + " " + strHomCode);

            //把数据存储到数据库中
            ContentValues values = new ContentValues();
            values.put("before",mAmountEditText.getText().toString());
            values.put("before_currency",strForCode);
            values.put("after",DECIMAL_FORMAT.format(dCalculated));
            values.put("after_currency",strHomCode);
            db.insert("Currency",null,values);
            progressDialog.dismiss();
        }
    }

}
