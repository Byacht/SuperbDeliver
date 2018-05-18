package com.byacht.superbdeliver.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.Utils.ToastUtil;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgetPasswordActivity extends AppCompatActivity {

    @BindView(R.id.title_back)
    ImageView mTitleBack;
    @BindView(R.id.title_name_tv)
    TextView mTitleTv;
    @BindView(R.id.account_layout)
    RelativeLayout mAccoutLayout;
    @BindView(R.id.mine_add_sub_title)
    RelativeLayout mMineAddSubTitle;
    @BindView(R.id.register_account_et)
    EditText mRegisterAccountEt;
    @BindView(R.id.delete_register_account_img)
    ImageView mDeleteRegisterAccountImg;
    @BindView(R.id.register_phone_et)
    EditText mRegisterPhoneEt;
    @BindView(R.id.delete_register_phone_img)
    ImageView mDeleteRegisterPhoneImg;
    @BindView(R.id.register_enter_confirm_et)
    EditText mRegisterEnterConfirmEt;
    @BindView(R.id.register_get_confirm_tv)
    TextView mRegisterGetConfirmTv;
    @BindView(R.id.register_password_et)
    EditText mRegisterPasswordEt;
    @BindView(R.id.delete_register_password_img)
    ImageView mDeleteRegisterPasswordImg;
    @BindView(R.id.notice_img)
    ImageView mNoticeImg;
    @BindView(R.id.register_confirm_password_et)
    EditText mRegisterConfirmPasswordEt;
    @BindView(R.id.delete_register_confirm_password_img)
    ImageView mDeleteRegisterConfirmPasswordImg;
    @BindView(R.id.register_btn)
    Button mRegisterBtn;
    @BindView(R.id.alert_password_layout)
    RelativeLayout mAlertLayout;

    private boolean mPasswordVisitable;
    private boolean mConfirmPasswordVisitable;
    private String mCookies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initView();
    }

    private void initView() {
        mTitleTv.setText("找回密码");
        mAccoutLayout.setVisibility(View.GONE);
        mTitleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mDeleteRegisterPasswordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPasswordVisitable(mRegisterPasswordEt, mPasswordVisitable);
                mPasswordVisitable = !mPasswordVisitable;
            }
        });

        mDeleteRegisterConfirmPasswordImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPasswordVisitable(mRegisterConfirmPasswordEt, mConfirmPasswordVisitable);
                mConfirmPasswordVisitable = !mConfirmPasswordVisitable;
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mRegisterPasswordEt.getText().toString();
                if (password.equals(mRegisterConfirmPasswordEt.getText().toString())) {
                    Toast.makeText(ForgetPasswordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    mAlertLayout.setVisibility(View.GONE);
                    finish();
                } else {
                    mAlertLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mRegisterGetConfirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String json = "{\"phoneNumber\":\"" + mRegisterPhoneEt.getText().toString() + "\"}";
                Call call = NetworkUtil.getCallByPost(Constant.GET_CODE_URL, json);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Headers responseHeaders = response.headers();
                        mCookies = responseHeaders.get("Set-Cookie").split(";")[0];
                        Log.d("htout", "cookies:" + mCookies);
                    }
                });
            }
        });

        mRegisterBtn.setText("提交");
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = mRegisterPasswordEt.getText().toString();
                Pattern numPattern = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,100}$");
                Matcher numMatch = numPattern.matcher(password);
                Log.d("htout", "num:" + numMatch.matches());
                if (numMatch.matches()) {
                    if (password.equals(mRegisterConfirmPasswordEt.getText().toString())) {
                        String json = "{\"code\":\"" + mRegisterEnterConfirmEt.getText().toString() + "\","
                                + "\"phoneNumber\":\"" + mRegisterPhoneEt.getText().toString() + "\","
                                + "\"password\":\"" + mRegisterPasswordEt.getText().toString() + "\"}";
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                        Request request = new Request
                                .Builder()
                                .post(requestBody)
                                .addHeader("Cookie", mCookies)
                                .url(Constant.RESET_PASSWORD_URL)
                                .build();
                        Call call = NetworkUtil.getOkhttpClient().newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

//                            final int resultCode = Integer.valueOf(response.body().string());
                                final String resultCode = response.body().string();
                                Log.d("htout", "code:" + resultCode);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (resultCode.equals("200")) {
                                            Toast.makeText(ForgetPasswordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                                            mAlertLayout.setVisibility(View.GONE);
                                            finish();
                                        } else if (resultCode.equals("-1")) {
                                            ToastUtil.show(ForgetPasswordActivity.this, "验证码错误");
                                        } else if (resultCode.equals("-2")) {
                                            ToastUtil.show(ForgetPasswordActivity.this, "账号不存在");
                                        }
                                    }
                                });
                            }
                        });

                    } else {
                        mAlertLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    ToastUtil.show(ForgetPasswordActivity.this, "密码过于简单");
                }
            }
        });
    }

    private void setPasswordVisitable(EditText editText, boolean visiable) {
        if (!visiable) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setSelection(editText.getText().length());
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setSelection(editText.getText().length());
        }
    }
}
