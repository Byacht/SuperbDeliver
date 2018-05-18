package com.byacht.superbdeliver.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.Utils.ToastUtil;
import com.byacht.superbdeliver.adapter.AddOrderInfoAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddOrderInfoActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener {

    @BindView(R.id.toolbar_add_order)
    Toolbar mToolbar;
    @BindView(R.id.submit_btn)
    Button mSubmitBtn;
    PoiSearch.Query mQuery;
    PoiSearch mPoiSearch;
    @BindView(R.id.longitude_et)
    EditText mLongitudeEt;
    @BindView(R.id.latitude_et)
    EditText mLatitudeEt;
    @BindView(R.id.add_phone_et)
    EditText mAddPhoneEt;

    private int mUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order_info);
        ButterKnife.bind(this);

        setupToolBar();
        SharedPreferences sharedPreferences = getSharedPreferences("Account ID", Context.MODE_PRIVATE);
        mUserId = sharedPreferences.getInt("id", 1);

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pattern pattern = Pattern.compile("[0-9]*");
                Matcher matcher = pattern.matcher(mAddPhoneEt.getText().toString().replace(" ",""));
                if (matcher.matches()) {
                    String[] phoneNumber = mAddPhoneEt.getText().toString().split(" ");
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < phoneNumber.length - 1; i++) {
                        sb.append("\"");
                        sb.append(phoneNumber[i]);
                        sb.append("\",");
                    }
                    sb.append("\"");
                    sb.append(phoneNumber[phoneNumber.length - 1]);
                    sb.append("\"]");
                    String json = "[{\"xPoint\":\"" + mLongitudeEt.getText().toString() + "\","
                            + "\"yPoint\":\"" + mLatitudeEt.getText().toString() + "\","
                            + "\"phoneNumber\":" + sb.toString() + "}]";
                    Log.d("htout", "add order:" + json);
                    Call call = NetworkUtil.getCallByPost(Constant.ORIGINAL_URL + "/" + mUserId + "/submit", json);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String code = response.body().string();
                            if (code.equals("200")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLatitudeEt.setText("");
                                        mLongitudeEt.setText("");
                                        mAddPhoneEt.setText("");
                                        mLongitudeEt.requestFocus();
                                        ToastUtil.show(AddOrderInfoActivity.this, "提交成功");
                                    }
                                });
                            } else {
                                ToastUtil.show(AddOrderInfoActivity.this, "提交失败");
                            }

                        }
                    });
                } else {
                    ToastUtil.show(AddOrderInfoActivity.this, "手机号码输入有误，请检查后再重新提交");
                }

            }
        });
    }

    private void setupToolBar() {
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setTitle("输入订单");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int code) {
        for (int i = 0; i < poiResult.getPois().size(); i++) {
            Log.d("htout", poiResult.getPois().get(i).getSnippet());
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
