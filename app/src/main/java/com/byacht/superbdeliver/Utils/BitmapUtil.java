package com.byacht.superbdeliver.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by dn on 2018/5/3.
 */

public class BitmapUtil {

    public static String Bitmap2String(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        return new String(Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT));
    }

    public static Drawable String2Drawable(String string) {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(string.getBytes(), Base64.DEFAULT));
        return Drawable.createFromStream(bais, "");

    }
}
