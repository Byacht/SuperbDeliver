package com.byacht.superbdeliver.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.Utils.ToastUtil;
import com.byacht.superbdeliver.model.UserInfo;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends Activity {

    private EditText mAccountEt;
    private EditText mPasswordEt;
    private ImageView mDeleteAccountImg;
    private ImageView mDeletePasswordImg;
    private Button mLoginBtn;
    private TextView mForgetPasswordTv;
    private ImageView mBackImg;
    private TextView mRegisterTv;

    private boolean mIsPasswordSee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        mAccountEt = (EditText) findViewById(R.id.account_et);
        mPasswordEt = (EditText) findViewById(R.id.password_et);
        mDeleteAccountImg = (ImageView) findViewById(R.id.delete_account_img);
        mDeletePasswordImg = (ImageView) findViewById(R.id.delete_password_img);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mForgetPasswordTv = (TextView) findViewById(R.id.forget_password_tv);
        mBackImg = (ImageView) findViewById(R.id.title_back);
        mRegisterTv = (TextView) findViewById(R.id.register_title_tv);

        mBackImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mRegisterTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        mDeletePasswordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsPasswordSee) {
                    mPasswordEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    mPasswordEt.setSelection(mPasswordEt.getText().length());
                    mIsPasswordSee = true;
                } else {
                    mPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mPasswordEt.setSelection(mPasswordEt.getText().length());
                    mIsPasswordSee = false;
                }
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call call = NetworkUtil.getCallByPost(Constant.LOGIN_URL, mAccountEt.getText().toString(), mPasswordEt.getText().toString());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Gson gson = new Gson();
                        String data = response.body().string();
                        final UserInfo userInfo = gson.fromJson(data, UserInfo.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (userInfo.getId() == -1) {
                                    ToastUtil.show(LoginActivity.this, "账号不存在");
                                } else if (userInfo.getId() == -2) {
                                    ToastUtil.show(LoginActivity.this, "密码错误");
                                } else {
                                    SharedPreferences sharedPreferences = getSharedPreferences("Account ID", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("id", userInfo.getId());
                                    editor.putString("name", userInfo.getName());
                                    editor.putString("portrait", userInfo.getPortrait());
                                    editor.commit();
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        });

        mForgetPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}
