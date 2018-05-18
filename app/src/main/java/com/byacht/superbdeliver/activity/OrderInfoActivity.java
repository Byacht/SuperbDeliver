package com.byacht.superbdeliver.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.byacht.superbdeliver.MainActivity;
import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.adapter.MyOrderInfoAdapter;
import com.byacht.superbdeliver.adapter.OrderInfoAdapter;
import com.byacht.superbdeliver.model.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vivian.timelineitemdecoration.itemdecoration.DotItemDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OrderInfoActivity extends AppCompatActivity implements GeocodeSearch.OnGeocodeSearchListener {

    @BindView(R.id.toolbar_order)
    Toolbar mToolbar;
    private int mAccountId;
    private ArrayList<OrderInfo> mOrderInfos;
    private ArrayList<String> mAddressList;
    private boolean mFinished;
    private RecyclerView mOrderInfoRv;
    private OrderInfoAdapter mAdapter;
    private List<CountDownLatch> mLatch;
    private GeocodeSearch mGeocodeSearch;

    private MyOrderInfoAdapter mOrderInfoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);
        ButterKnife.bind(this);

        setupToolBar();
        mAccountId = getIntent().getIntExtra("id", 0);
        mOrderInfos = new ArrayList<OrderInfo>();
        mOrderInfoRv = (RecyclerView) findViewById(R.id.order_info_rv);
        mGeocodeSearch = new GeocodeSearch(OrderInfoActivity.this);
        mGeocodeSearch.setOnGeocodeSearchListener(OrderInfoActivity.this);
        mAddressList = new ArrayList<>();

        LatLonPoint latLonPoint = new LatLonPoint(23.165961, 113.340624);
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.AMAP);

        mGeocodeSearch.getFromLocationAsyn(query);
    }

    private void setupToolBar() {
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setTitle("我的订单");
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
    protected void onResume() {
        super.onResume();
        getOrderInfo();
    }

    private void getOrderInfo() {
        mOrderInfos.clear();
//        List<String> numList = new ArrayList<String>();
//        numList.add("15622256731");
//        numList.add("15622256731");
//        OrderInfo orderInfo10 = new OrderInfo(numList, 23.164574, 113.342431);
//        mOrderInfos.add(orderInfo10);
//        OrderInfo orderInfo9 = new OrderInfo(numList, 23.164141, 113.342497);
//        mOrderInfos.add(0, orderInfo9);
//        OrderInfo orderInfo = new OrderInfo(numList, 23.168874, 113.341259);
//        OrderInfo orderInfo1 = new OrderInfo(numList, 23.166911, 113.341013);
//        OrderInfo orderInfo2 = new OrderInfo(numList, 23.167207, 113.343137);
//        OrderInfo orderInfo3 = new OrderInfo(numList, 23.162758, 113.342118);
//        OrderInfo orderInfo4 = new OrderInfo(numList, 23.162374, 113.340229);
//        OrderInfo orderInfo5 = new OrderInfo(numList, 23.161486, 113.34156);
//        OrderInfo orderInfo8 = new OrderInfo(numList, 23.162936, 113.344843);
//        OrderInfo orderInfo11 = new OrderInfo(numList, 23.165808, 113.340662);
//        OrderInfo orderInfo13 = new OrderInfo(numList, 23.166124, 113.339761);
//        OrderInfo orderInfo14 = new OrderInfo(numList, 23.165039, 113.339879);
//        OrderInfo orderInfo15 = new OrderInfo(numList, 23.164156, 113.341418);
//        OrderInfo orderInfo16 = new OrderInfo(numList, 23.167776, 113.346418);
//        OrderInfo orderInfo12 = new OrderInfo(numList, 23.16565, 113.341713);
//        OrderInfo orderInfo17 = new OrderInfo(numList,23.165601, 113.343655);
//        OrderInfo orderInfo18 = new OrderInfo(numList,23.166331, 113.343022);
//        OrderInfo orderInfo19 = new OrderInfo(numList,23.16642, 113.344288);
//        OrderInfo orderInfo20 = new OrderInfo(numList,23.164171, 113.343623);
//        OrderInfo orderInfo21 = new OrderInfo(numList,23.164092, 113.339782);
////        OrderInfo orderInfo22 = new OrderInfo(numList,23.16426, 113.340512);
//        mOrderInfos.add(orderInfo);
//        mOrderInfos.add(orderInfo1);
//        mOrderInfos.add(orderInfo2);
//        mOrderInfos.add(orderInfo3);
//        mOrderInfos.add(orderInfo4);
//        mOrderInfos.add(orderInfo5);
//        mOrderInfos.add(orderInfo8);
//        mOrderInfos.add(orderInfo11);
////        mOrderInfos.add(orderInfo12);
//        mOrderInfos.add(orderInfo13);
//        mOrderInfos.add(orderInfo14);
//        mOrderInfos.add(orderInfo15);
//        mOrderInfos.add(orderInfo16);
//        mOrderInfos.add(orderInfo17);
////        mOrderInfos.add(orderInfo19);
////        mOrderInfos.add(orderInfo20);
//        mOrderInfos.add(orderInfo18);
////        mOrderInfos.add(orderInfo21);
////        mOrderInfos.add(orderInfo22);

//                mOrderInfos.add(orderInfo6);
//                mOrderInfos.add(orderInfo7);
//        mOrderInfos.add(orderInfo8);
        mFinished = true;
//                mGeocodeSearch = new GeocodeSearch(OrderInfoActivity.this);
//                mGeocodeSearch.setOnGeocodeSearchListener(OrderInfoActivity.this);
//                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.AMAP);
//
//                geocoderSearch.getFromLocationAsyn(query);


//        for (OrderInfo info : mOrderInfos) {
////            mLatch.add(new CountDownLatch(1));
//            LatLonPoint latLonPoint = new LatLonPoint(info.getXPoint(), info.getYPoint());
//            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
//            mGeocodeSearch.getFromLocationAsyn(query);
//        }

        Call call = NetworkUtil.getCallByGet(Constant.ORIGINAL_URL + "/" + mAccountId + "/getOrderMessage");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

//                List<String> numList = new ArrayList<String>();
//                numList.add("111");
//                numList.add("111");
//                OrderInfo orderInfo10 = new OrderInfo(numList,23.164574, 113.342431);

                Gson gson = new Gson();
                String data = response.body().string();
                Log.d("htout", "orderinfo:" + data);
                mOrderInfos = gson.fromJson(data, new TypeToken<List<OrderInfo>>(){}.getType());
                mFinished = true;
                mAddressList.clear();
                for (OrderInfo orderInfo : mOrderInfos) {
                    String address = orderInfo.getXPoint() + "," + orderInfo.getYPoint();
                    mAddressList.add(address);
                    Log.d("htout", "address:" + address);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        mAdapter = new OrderInfoAdapter(mOrderInfos, mAddressList);
//                        LinearLayoutManager layoutManager = new LinearLayoutManager(OrderInfoActivity.this);
//                        mOrderInfoRv.setLayoutManager(layoutManager);
//                        mOrderInfoRv.setAdapter(mAdapter);
                        mOrderInfoAdapter = new MyOrderInfoAdapter(OrderInfoActivity.this, mOrderInfos);
                        mOrderInfoRv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                        DotItemDecoration mItemDecoration = new DotItemDecoration
                                .Builder(OrderInfoActivity.this)
                                .setOrientation(DotItemDecoration.VERTICAL)//如果LayoutManager设置了横向，那么这里也要设置成横向
                                .setItemStyle(DotItemDecoration.STYLE_DRAW)//选择dot使用图片资源或者用canvas画
                                .setTopDistance(20)//单位dp
                                .setItemInterVal(10)//单位dp
                                .setItemPaddingLeft(10)//如果不设置，默认和item间距一样
                                .setItemPaddingRight(10)//如果不设置，默认和item间距一样
                                .setDotColor(Color.WHITE)
                                .setDotRadius(2)//单位dp
                                .setDotPaddingTop(0)
                                .setDotInItemOrientationCenter(false)//设置dot居中
                                .setLineColor(Color.WHITE)//设置线的颜色
                                .setLineWidth(1)//单位dp
                                .setEndText("END")//设置结束的文字
                                .setTextColor(Color.WHITE)
                                .setTextSize(10)//单位sp
                                .setDotPaddingText(2)//单位dp.设置最后一个点和文字之间的距离
                                .setBottomDistance(40)//设置底部距离，可以延长底部线的长度
                                .create();
                        mOrderInfoRv.addItemDecoration(mItemDecoration);
                        mOrderInfoRv.setAdapter(mOrderInfoAdapter);
                    }
                });

            }
        });
    }

    public void startDeliver(View view) {
        if (mFinished) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putParcelableArrayListExtra("info", mOrderInfos);
            startActivity(intent);
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        Log.d("htout", "address:" + regeocodeResult.getRegeocodeAddress().getBuilding());
        Log.d("htout", "address:" + regeocodeResult.getRegeocodeAddress());
        mAddressList.add(regeocodeResult.getRegeocodeAddress().getFormatAddress());
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        Log.d("htout", "address:" + geocodeResult.toString());
//        mLatch.countDown();
    }
}
