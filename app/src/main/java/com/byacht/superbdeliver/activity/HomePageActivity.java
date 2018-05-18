package com.byacht.superbdeliver.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;

public class HomePageActivity extends AppCompatActivity {

    @BindView(R.id.my_data_layout)
    RelativeLayout mMyDataLayout;
    @BindView(R.id.my_order_info_layout)
    RelativeLayout mMyOrderInfoLayout;
    @BindView(R.id.input_order_layout)
    RelativeLayout mInputOrderLayout;
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
        ButterKnife.bind(this);

        mLoginTv = (TextView) findViewById(R.id.login_tv);
        mPortraitImg = (ImageView) findViewById(R.id.my_portrait_img);

        checkNeededPermission();
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
        mMyDataLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasLogin) {
                    Intent intent = new Intent(HomePageActivity.this, StatisticsActivity.class);
                    intent.putExtra("id", mAccountId);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomePageActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mMyOrderInfoLayout.setOnClickListener(new View.OnClickListener() {
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
        mInputOrderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHasLogin) {
                    Intent intent = new Intent(HomePageActivity.this, InputOrderInfoActivity.class);
                    intent.putExtra("id", mAccountId);
                    startActivity(intent);
                } else {
                    Toast.makeText(HomePageActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private static int MY_PERMISSION_REQUEST_CODE = 1;

    private void checkNeededPermission() {
        boolean isAllGranted = checkPermissionAllGranted(
                new String[] {
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }
        );

        if (isAllGranted) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[] {
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                MY_PERMISSION_REQUEST_CODE
        );
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (!isAllGranted) {
                ToastUtil.show(this, "部分权限未授予，可能导致应用某些功能无法使用");
            }
        }
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
        Log.d("htout", "onStart:" + BitmapUtil.String2Drawable(mPortrait));
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
                    String json = "{\"photo\":\"" + BitmapUtil.Bitmap2String(bm);
                    json = json + "\"}";
                    Call call = NetworkUtil.getCallByPost(Constant.ORIGINAL_URL + "/" + mAccountId + "/setPhoto", json);
//                    call.enqueue(new Callback() {
//                        @Override
//                        public void onFailure(Call call, IOException e) {
//
//                        }
//
//                        @Override
//                        public void onResponse(Call call, Response response) throws IOException {
//                            String code = response.body().string();
//                            Log.d("htout", "code:" + code);
//                            final int resultCode = Integer.valueOf(code);
//
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (resultCode == 200) {
//                                        ToastUtil.show(HomePageActivity.this, "设置头像成功");
//                                    }
//                                }
//                            });
//                        }
//                    });
                    break;
            }
        }
    }
}
