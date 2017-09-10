package com.byacht.superbdeliver.Utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by dn on 2017/9/8.
 */

public class ToastUtil {
    public static void show(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
