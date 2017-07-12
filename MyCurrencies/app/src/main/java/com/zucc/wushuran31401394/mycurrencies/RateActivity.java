package com.zucc.wushuran31401394.mycurrencies;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;


public class RateActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase db ;
    private List list = new ArrayList();
    private List<PointValue> values;
    private  List<PointValue> default_values_up;
    private  List<PointValue> default_values_down;
    private  ListView listView;
    private LineChartView chart;
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        chart = (LineChartView)findViewById(R.id.chart);
        listView = (ListView)findViewById(R.id.ratelist);
        init();
        initDataBase();
        initData();
        initListview();
        drawChart();
    }

    private void init(){
        values = new ArrayList<PointValue>();
        default_values_up = new ArrayList<PointValue>();
        default_values_down = new ArrayList<PointValue>();
    }

    private void initDataBase(){
        dbHelper = new MyDatabaseHelper(this,"Currency.db",null,1);
        db = dbHelper.getWritableDatabase();
    }

    private void initData(){
        Cursor cursor = db.query("Rate",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                long time = cursor.getLong(cursor.getColumnIndex("time"));
                Date date = new Date(time);
                String timelistString = (date.getYear()+1900)+"-"+(date.getMonth()+1)+"-"+(date.getDay()+10)+"\t\t"+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
                String timeString = date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
                Float rate = cursor.getFloat(cursor.getColumnIndex("rate"));
                list.add(timelistString+"\t\t\t\t :  \t\t\t\t"+rate);
                default_values_up.add(new PointValue(time/60000, (rate+0.02f)));
                default_values_down.add(new PointValue(time/60000,(rate-0.02f)) );
                mAxisXValues.add(new AxisValue(time/60000).setLabel(timeString));
                values.add(new PointValue(time/60000,rate*10000/10000));
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void initListview(){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                RateActivity.this,android.R.layout.simple_expandable_list_item_1,list);
        listView.setAdapter(adapter);
    }

    private void drawChart(){
        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.BLACK);  //设置字体颜色
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        axisX.setHasLines(true); //x 轴分割线

        //Y轴是根据数据的大小自动设置Y轴上限
        Axis axisY = new Axis();  //Y轴
        //axisY.setName("");//y轴标注
        axisY.setTextSize(10);//设置字体大小
        //data.setAxisYRight(axisY);  //y轴设置在右边

        Line line = new Line(values).setColor(Color.BLUE).setCubic(false);
        Line default_line_up = new Line(default_values_up).setColor(Color.alpha(0)).setCubic(true);
        Line default_line_down = new Line(default_values_down).setColor(Color.alpha(0)).setCubic(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        lines.add(default_line_up);
        lines.add(default_line_down);
        line.setHasPoints(false);// 是否显示节点
        LineChartData data = new LineChartData();
        data.setLines(lines);
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        data.setAxisXBottom(axisX); //x 轴在底部
        chart.setLineChartData(data);
    }
}
