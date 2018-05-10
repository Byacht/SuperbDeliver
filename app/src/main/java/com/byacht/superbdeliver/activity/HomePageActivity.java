package com.byacht.superbdeliver.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.Utils.BitmapUtil;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.Utils.ToastUtil;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomePageActivity extends AppCompatActivity {

    private String mAccountName;
    private int mAccountId;
    private String mPortrait;

    private TextView mLoginTv;
    private ImageView mPortraitImg;

    private boolean mHasLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mLoginTv = (TextView) findViewById(R.id.login_tv);
        mPortraitImg = (ImageView) findViewById(R.id.my_portrait_img);

        mLoginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        mPortraitImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasLogin) {
                    PictureSelector.create(HomePageActivity.this)
                            .openGallery(PictureMimeType.ofImage())
                            .maxSelectNum(1)
                            .forResult(PictureConfig.CHOOSE_REQUEST);
                } else {
                    Toast.makeText(HomePageActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                }

            }
        });
        TextView myDataTv =  (TextView) findViewById(R.id.my_data_tv);
        myDataTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasLogin) {
                    Intent intent = new Intent(HomePageActivity.this, StatisticsActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomePageActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                }

            }
        });
        TextView infoTv = (TextView) findViewById(R.id.order_info_tv);
        infoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasLogin) {
                    Intent intent = new Intent(HomePageActivity.this, OrderInfoActivity.class);
                    intent.putExtra("id", mAccountId);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomePageActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                }

            }
        });
        TextView inputInfoTv = (TextView) findViewById(R.id.input_order_info_tv);
        inputInfoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, AddOrderInfoActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("Account ID", Context.MODE_PRIVATE);
        mAccountName = sharedPreferences.getString("account name", "byacht");
        mAccountId = sharedPreferences.getInt("id", 1);
        mPortrait = sharedPreferences.getString("portrait", "null");
        if (mAccountId != 0) {
            mHasLogin = true;
        }
        mLoginTv.setText(mAccountName);
        if (!mPortrait.equals(null)) {
            mPortraitImg.setImageDrawable(BitmapUtil.String2Drawable(mPortrait));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    Bitmap bm = BitmapFactory.decodeFile(selectList.get(0).getPath());
                    mPortraitImg.setImageBitmap(bm);
                    SharedPreferences sharedPreferences = getSharedPreferences("Account ID", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("portrait", BitmapUtil.Bitmap2String(bm));
                    editor.commit();
                    Call call = NetworkUtil.getCallByPost(Constant.ORIGINAL_URL + mAccountId + "/setPhoto", "portrait", BitmapUtil.Bitmap2String(bm));
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            final int resultCode = Integer.valueOf(response.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (resultCode == 200) {
                                        ToastUtil.show(HomePageActivity.this, "设置头像成功");
                                    }
                                }
                            });
                        }
                    });
                    break;
            }
        }
    }
}
