package com.byacht.superbdeliver.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.Utils.ToastUtil;
import com.byacht.superbdeliver.adapter.SearchAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InputOrderInfoActivity extends Activity implements LocationSource, AMapLocationListener,
        TextWatcher, AdapterView.OnItemClickListener, Inputtips.InputtipsListener {


    @BindView(R.id.input_phone_et)
    EditText mInputPhoneEt;
    @BindView(R.id.submit_btn)
    Button mSubmitBtn;
    private EditText editText;
    private ListView listView;
    private TextView textView;
    private SearchAdapter searchAdapter;
    List<HashMap<String, String>> searchList = new ArrayList<HashMap<String, String>>();
    ;
    private String currentCity = "广州";

    private double mLatitude;
    private double mLongitude;
    private int mUserId;
    private String mAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_order_info);
        ButterKnife.bind(this);

        init();//初始化

        SharedPreferences sharedPreferences = getSharedPreferences("Account ID", Context.MODE_PRIVATE);
        mUserId = sharedPreferences.getInt("id", 1);

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(editText.getText()) || TextUtils.isEmpty(mInputPhoneEt.getText())) {
                    ToastUtil.show(InputOrderInfoActivity.this, "请输入订单信息");
                } else {
                    Pattern pattern = Pattern.compile("[0-9]*");
                    Matcher matcher = pattern.matcher(mInputPhoneEt.getText().toString().replace(" ", ""));
                    if (matcher.matches()) {
                        String[] phoneNumber = mInputPhoneEt.getText().toString().split(" ");
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < phoneNumber.length - 1; i++) {
                            sb.append("\"");
                            sb.append(phoneNumber[i]);
                            sb.append("\",");
                        }
                        sb.append("\"");
                        sb.append(phoneNumber[phoneNumber.length - 1]);
                        sb.append("\"]");
                        String json = "[{\"xPoint\":\"" + mLongitude + "\","
                                + "\"yPoint\":\"" + mLatitude + "\","
                                + "\"placeName\":\"" + mAddress + "\","
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
                                            mInputPhoneEt.setText("");
                                            editText.setText("");
                                            editText.requestFocus();
                                            ToastUtil.show(InputOrderInfoActivity.this, "提交成功");
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.show(InputOrderInfoActivity.this, "提交失败");
                                        }
                                    });

                                }

                            }
                        });
                    } else {
                        ToastUtil.show(InputOrderInfoActivity.this, "手机号码输入有误，请检查后再重新提交");
                    }
                }

            }
        });
//        getAdress(position[0], position[1]);
        //getAdress(position[2], position[3]);
        //getAdress(position[4],position[5]);

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //存放drawableLeft，Right，Top，Bottom四个图片资源对象
                //index=2 表示的是 drawableRight 图片资源对象
                Drawable drawable = editText.getCompoundDrawables()[2];
                if (drawable == null)
                    return false;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getX() > editText.getWidth() - editText.getPaddingRight() - drawable.getIntrinsicWidth()) {

//                        Log.e("TAG","为什么有进来了");
                        if (editText.getText().toString() != null) {
                            editText.clearFocus();
                            editText.setText("");
                            searchList.clear();
                            searchAdapter.notifyDataSetChanged();
                        }

                    }
                    return false;
                }
                return false;
            }
        });

    }


    private void init() {
        editText = (EditText) findViewById(R.id.input_order_et);
        listView = (ListView) findViewById(R.id.search_lv);
        editText.addTextChangedListener(this);
        listView.setOnItemClickListener(this);
    }

    /*
    解释指定坐标的地址
    @param x 经度
    @param y 纬度
     */
    public void getAdress(final double x, final double y) {
//        Log.e("TAG", "调用getAdress");
        //地址查询器
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        //设置查询参数,
        //三个参数依次为坐标，范围多少米，坐标系
        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(new LatLonPoint(x, y), 200, GeocodeSearch.AMAP);
        //设置查询结果监听
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            //根据坐标获取地址信息调用
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                String result = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                Log.e("TAG", "获得请求结果");
            }

            //根据地址获取坐标信息是调用
            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        //发起异步查询请求
        geocodeSearch.getFromLocationAsyn(regeocodeQuery);

    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.e("TAG", "editText的内容改变了");
        //获取自动提示输入框的内容
        String content = s.toString().trim();

        //初始化一个输入提示搜索对象，并传入参数
        InputtipsQuery inputtipsQuery = new InputtipsQuery(content, currentCity);
        //将获取到的结果进行城市限制筛选
        inputtipsQuery.setCityLimit(true);
        //定义一个输入提示对象，传入当前上下文和搜索对象
        Inputtips inputtips = new Inputtips(this, inputtipsQuery);
        //设置输入提示查询的监听，实现输入提示的监听方法onGetInputtips()
        inputtips.setInputtipsListener(this);
        //输入查询提示的异步接口实现
        inputtips.requestInputtipsAsyn();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onGetInputtips(List<Tip> list, int returnCode) {
        //如果输入提示搜索成功
        if (returnCode == AMapException.CODE_AMAP_SUCCESS) {
            //每次搜索时都先把原来的searchList内容清掉
            searchList.clear();
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("name", list.get(i).getName());
                //将地址信息取出放入HashMap中
                hashMap.put("address", list.get(i).getDistrict());
                Log.e("TAG", list.get(i).getPoint().toString());
                //解析返回的经纬度
                String latlonPoint = list.get(i).getPoint().toString();
                //经度
                String x = latlonPoint.substring(0, latlonPoint.indexOf(","));
                //纬度
                String y = latlonPoint.substring(latlonPoint.indexOf(",") + 1, latlonPoint.length());
                //详细地址
                String detailAddress = list.get(i).getAddress();
                hashMap.put("x", x);
                hashMap.put("y", y);
                hashMap.put("detailAddress", detailAddress);
                //将HashMap放入表中
                searchList.add(hashMap);

            }
            //新建一个适配器
            searchAdapter = new SearchAdapter(this, searchList);
            //为listview适配
            listView.setAdapter(searchAdapter);

        } else {
            //清空原来的所有item
            searchList.clear();
            searchAdapter.notifyDataSetChanged();
            Log.e("TAG", "editText内容为空时返回的错误返回码:" + returnCode);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        double x = Double.parseDouble(searchList.get(position).get("x"));
        double y = Double.parseDouble(searchList.get(position).get("y"));
        mAddress = searchList.get(position).get("name");
        Log.e("TAG", "点击listView地点的经度:" + x + "   纬度:" + y + " 地点：" + searchList.get(position).get("name"));
        mLongitude = x;
        mLatitude = y;
        editText.setText(mAddress);
        editText.setSelection(mAddress.length());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //把listView清空
                        searchList.clear();
                        searchAdapter.notifyDataSetChanged();
                        Log.d("htout", "size:" + searchList.size());
                    }
                });
            }
        }).start();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(null != mlocationClient){
//            mlocationClient.onDestroy();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        Log.e("TAG", "重新开始");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        Log.e("TAG", "进入下一个界面，暂停");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }
}