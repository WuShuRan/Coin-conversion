package com.zucc.wushuran31401394.mycurrencies;

/**
 * Created by wushu on 2017/7/4.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsMgr {

    private static SharedPreferences sSharedPreferences;

    //往文件中存数据
    public static void setString(Context context, String locale, String code ){
        sSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putString(locale, code);
        editor.commit();
    }
    //读取文件中存数据
    public static String getString(Context context, String locale){
        sSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sSharedPreferences.getString(locale, null);

    }
}
