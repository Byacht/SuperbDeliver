package com.byacht.superbdeliver.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RouteSearch;
import com.byacht.superbdeliver.MainActivity;
import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.adapter.OrderInfoAdapter;
import com.byacht.superbdeliver.model.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OrderInfoActivity extends AppCompatActivity implements GeocodeSearch.OnGeocodeSearchListener {

    private int mAccountId;
    private ArrayList<OrderInfo> mOrderInfos;
    private ArrayList<String> mAddressList;
    private boolean mFinished;
    private RecyclerView mOrderInfoRv;
    private OrderInfoAdapter mAdapter;
    private CountDownLatch mLatch;
    private GeocodeSearch mGeocodeSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);

        mAccountId = getIntent().getIntExtra("id", 0);
        mOrderInfos = new ArrayList<OrderInfo>();
        mOrderInfoRv = (RecyclerView) findViewById(R.id.order_info_rv);
        mGeocodeSearch = new GeocodeSearch(OrderInfoActivity.this);
        mGeocodeSearch.setOnGeocodeSearchListener(OrderInfoActivity.this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        getOrderInfo();
    }

    private void getOrderInfo() {
        List<String> numList = new ArrayList<String>();
        numList.add("111");
        numList.add("111");
        OrderInfo orderInfo10 = new OrderInfo(numList,23.164574, 113.342431);
        mOrderInfos.add(orderInfo10);
        OrderInfo orderInfo9 = new OrderInfo(numList,23.164141, 113.342497);
        mOrderInfos.add(0, orderInfo9);
        OrderInfo orderInfo = new OrderInfo(numList,23.168874, 113.341259);
        OrderInfo orderInfo1 = new OrderInfo(numList,23.166911, 113.341013);
        OrderInfo orderInfo2 = new OrderInfo(numList,23.167207, 113.343137);
        OrderInfo orderInfo3 = new OrderInfo(numList,23.162758, 113.342118);
        OrderInfo orderInfo4 = new OrderInfo(numList,23.162374, 113.340229);
        OrderInfo orderInfo5 = new OrderInfo(numList,23.161486, 113.34156);
//                OrderInfo orderInfo6 = new OrderInfo(numList,23.165609, 113.344092);
//                OrderInfo orderInfo7 = new OrderInfo(numList,23.165609, 113.344092);
        OrderInfo orderInfo8 = new OrderInfo(numList,23.162936, 113.344843);
        OrderInfo orderInfo11 = new OrderInfo(numList,23.165808, 113.340662);
        OrderInfo orderInfo12 = new OrderInfo(numList,23.16565, 113.341713);
        OrderInfo orderInfo13 = new OrderInfo(numList,23.166124, 113.339761);
        OrderInfo orderInfo14 = new OrderInfo(numList,23.165039, 113.339879);
        OrderInfo orderInfo15 = new OrderInfo(numList,23.164156, 113.341418);
        OrderInfo orderInfo16 = new OrderInfo(numList,23.167776, 113.346418);
        OrderInfo orderInfo17 = new OrderInfo(numList,23.165601, 113.343655);
        OrderInfo orderInfo18 = new OrderInfo(numList,23.166331, 113.343022);
        OrderInfo orderInfo19 = new OrderInfo(numList,23.16642, 113.344288);
        OrderInfo orderInfo20 = new OrderInfo(numList,23.164171, 113.343623);
        OrderInfo orderInfo21 = new OrderInfo(numList,23.164092, 113.339782);
        OrderInfo orderInfo22 = new OrderInfo(numList,23.16426, 113.340512);
        mOrderInfos.add(orderInfo);
        mOrderInfos.add(orderInfo1);
        mOrderInfos.add(orderInfo2);
        mOrderInfos.add(orderInfo3);
        mOrderInfos.add(orderInfo4);
        mOrderInfos.add(orderInfo5);
        mOrderInfos.add(orderInfo11);
        mOrderInfos.add(orderInfo12);
        mOrderInfos.add(orderInfo13);
        mOrderInfos.add(orderInfo14);
        mOrderInfos.add(orderInfo15);
        mOrderInfos.add(orderInfo16);
        mOrderInfos.add(orderInfo17);
        mOrderInfos.add(orderInfo19);
        mOrderInfos.add(orderInfo20);
        mOrderInfos.add(orderInfo18);
        mOrderInfos.add(orderInfo21);
        mOrderInfos.add(orderInfo22);

//                mOrderInfos.add(orderInfo6);
//                mOrderInfos.add(orderInfo7);
        mOrderInfos.add(orderInfo8);
        mFinished = true;
//                mGeocodeSearch = new GeocodeSearch(OrderInfoActivity.this);
//                mGeocodeSearch.setOnGeocodeSearchListener(OrderInfoActivity.this);
//                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.AMAP);
//
//                geocoderSearch.getFromLocationAsyn(query);
        mAddressList = new ArrayList<>();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter = new OrderInfoAdapter(mOrderInfos, mAddressList);
                LinearLayoutManager layoutManager = new LinearLayoutManager(OrderInfoActivity.this);
                mOrderInfoRv.setLayoutManager(layoutManager);
                mOrderInfoRv.setAdapter(mAdapter);
            }
        });
//        Call call = NetworkUtil.getCallByGet(Constant.GET_ORDER_INFO_URL);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//
//                List<String> numList = new ArrayList<String>();
//                numList.add("111");
//                numList.add("111");
//                OrderInfo orderInfo10 = new OrderInfo(numList,23.164574, 113.342431);
//
//                Gson gson = new Gson();
//                String data = response.body().string();
//                mOrderInfos = gson.fromJson(data, new TypeToken<List<OrderInfo>>(){}.getType());
//                mOrderInfos.add(orderInfo10);
//                OrderInfo orderInfo9 = new OrderInfo(numList,23.164141, 113.342497);
//                mOrderInfos.add(0, orderInfo9);
//                OrderInfo orderInfo = new OrderInfo(numList,23.168874, 113.341259);
//                OrderInfo orderInfo1 = new OrderInfo(numList,23.166911, 113.341013);
//                OrderInfo orderInfo2 = new OrderInfo(numList,23.167207, 113.343137);
//                OrderInfo orderInfo3 = new OrderInfo(numList,23.162758, 113.342118);
//                OrderInfo orderInfo4 = new OrderInfo(numList,23.162374, 113.340229);
//                OrderInfo orderInfo5 = new OrderInfo(numList,23.161486, 113.34156);
////                OrderInfo orderInfo6 = new OrderInfo(numList,23.165609, 113.344092);
////                OrderInfo orderInfo7 = new OrderInfo(numList,23.165609, 113.344092);
//                OrderInfo orderInfo8 = new OrderInfo(numList,23.162936, 113.344843);
//                mOrderInfos.add(orderInfo);
//                mOrderInfos.add(orderInfo1);
//                mOrderInfos.add(orderInfo2);
//                mOrderInfos.add(orderInfo3);
//                mOrderInfos.add(orderInfo4);
//                mOrderInfos.add(orderInfo5);
////                mOrderInfos.add(orderInfo6);
////                mOrderInfos.add(orderInfo7);
//                mOrderInfos.add(orderInfo8);
//                mFinished = true;
////                mGeocodeSearch = new GeocodeSearch(OrderInfoActivity.this);
////                mGeocodeSearch.setOnGeocodeSearchListener(OrderInfoActivity.this);
////                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.AMAP);
////
////                geocoderSearch.getFromLocationAsyn(query);
//                mAddressList = new ArrayList<>();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mAdapter = new OrderInfoAdapter(mOrderInfos, mAddressList);
//                        LinearLayoutManager layoutManager = new LinearLayoutManager(OrderInfoActivity.this);
//                        mOrderInfoRv.setLayoutManager(layoutManager);
//                        mOrderInfoRv.setAdapter(mAdapter);
//                    }
//                });
//
//            }
//        });
        for (OrderInfo info : mOrderInfos) {
//            mLatch = new CountDownLatch(1);
            LatLonPoint latLonPoint = new LatLonPoint(info.getXPoint(), info.getYPoint());
            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.AMAP);
            mGeocodeSearch.getFromLocationAsyn(query);
//            try {
//                mLatch.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
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
        Log.d("htout", "address:" + regeocodeResult.getRegeocodeAddress().getRoads().get(0).getName());
        Log.d("htout", "address:" + regeocodeResult.getRegeocodeAddress().getPois().get(0).getSnippet());
        Log.d("htout", "address:" + regeocodeResult.getRegeocodeAddress().getBuilding());
        Log.d("htout", "address:" + regeocodeResult.getRegeocodeAddress().getFormatAddress());
//        mLatch.countDown();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        Log.d("htout", "address:" + geocodeResult.toString());
//        mLatch.countDown();
    }
}
