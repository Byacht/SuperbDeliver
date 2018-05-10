package com.byacht.superbdeliver.Utils;

import android.util.Log;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by dn on 2018/5/3.
 */

public class NetworkUtil {

    private static volatile OkHttpClient okhttpClient;

    public static OkHttpClient getOkhttpClient() {
        if (okhttpClient == null) {
            synchronized (NetworkUtil.class) {
                if (okhttpClient == null) {
                    okhttpClient = new OkHttpClient();
                }
            }
        }
        return okhttpClient;
    }

    public static Call getCallByGet(String url) {
        Request request = new Request
                .Builder()
                .url(url)
                .build();
        return getOkhttpClient().newCall(request);
    }

    public static Call getCallByPost(String url, String key, String value) {
        FormBody formBody = new FormBody
                .Builder()
                .add(key,value)
                .build();
        Request request = new Request
                .Builder()
                .post(formBody)
                .url(url)
                .build();
        return getOkhttpClient().newCall(request);
    }

    public static Call getCallByPost(String url, String json) {
        Log.d("htout", "json:" + json);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request
                .Builder()
                .post(requestBody)
                .url(url)
                .build();
        return getOkhttpClient().newCall(request);
    }
}
