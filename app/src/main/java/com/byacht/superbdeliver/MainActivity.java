package com.byacht.superbdeliver;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RideStep;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.byacht.superbdeliver.AntRouteSearch.ACS;
import com.byacht.superbdeliver.AntRouteSearch.MultiACS;
import com.byacht.superbdeliver.Receiver.PhoneCallReceiver;
import com.byacht.superbdeliver.Utils.AmapUtil;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.Utils.ToastUtil;
import com.byacht.superbdeliver.model.OrderInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, RouteSearch.OnRouteSearchListener,
        AMap.OnInfoWindowClickListener, AMap.OnMarkerClickListener, AMap.OnMyLocationChangeListener {

    @BindView(R.id.map_main)
    MapView mMapView;
    @BindView(R.id.btn_show_route)
    Button mBtnShowRoute;

    private AMap mAMap;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private RideRouteResult mRideRouteResult;


    //模拟的送餐点坐标
    private LatLonPoint[] mPoint;
    //模拟的各个送餐点的距离
    private double distance[][];

    private RidePath mRidePath[][][];
    private final int ROUTE_TYPE_RIDE = 4;

    public List<RideStep> customRidePaths = new ArrayList<RideStep>();
    //送餐地点数量
    private int pointNumber;
    private int[][] points;
    //放大地图到指定比例
    private int zoomTime = 3;
    //送餐地点数量
    private int addressCount = 0;
    private int numberCount = 0;
    //当前地点到下个送餐地点的距离
    private double currentDistance = 0;

    private static final int NOTIFY_DISTANCE = 50;

    //订单信息
    private List<OrderInfo> mOrderInfos;

    private ProgressDialog progDialog = null;// 搜索时进度条
    //当前正在配送第几个送餐地点
    private int mCurrentPosition = 0;

    private List<String> mNoAnswerNumberList;
    private int mUserId;

    //当前是否在通话
    private boolean mIsCalling;
    private int mSelectPath;

    private List<Integer> mArrivedTimeList;
    private List<Double> mTwoPointTimeList;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mContext = this.getApplicationContext();
        mMapView.onCreate(bundle);// 此方法必须重写

        mNoAnswerNumberList = new ArrayList<String>();

        SharedPreferences sharedPreferences = getSharedPreferences("Account ID", Context.MODE_PRIVATE);
        mUserId = sharedPreferences.getInt("id", 1);
        //注册拨号状态监听广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        mReceiver = new PhoneCallReceiver();
        registerReceiver(mReceiver, filter);
        //拨号结束回调
        mReceiver.setOnCallingListener(new PhoneCallReceiver.OnCalling() {
            @Override
            public void setCalling(boolean isCalling) {
                mIsCalling = isCalling;
                mNoAnswerNumberList.clear();
                //拨号结束，显示未接通号码
                showNoAnswerList();
                for (Integer id : mOrderInfos.get(points[mSelectPath][2 * mNextAddress + 1]).getOrderId()) {
                    Call call = NetworkUtil.getCallByGet(Constant.ORIGINAL_URL + "/" + mUserId + "/finishOrder/" + id);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                        }
                    });
                }
                int curAddress = mNextAddress;
                mTwoPointTimeList.remove(0);
                getArrivedTime();
                List<Integer> orderIdList = mOrderInfos.get(points[mSelectPath][2 * curAddress + 1]).getOrderId();
                List<String> jsonList = new ArrayList<>();
                for (int j = mNextAddress; j < mOrderInfos.size(); j++) {
                    int index = 0;
                    StringBuilder sb = new StringBuilder("[");
                    for (int k = 0; k < orderIdList.size(); k++) {
                        sb.append(orderIdList.get(k));
                        sb.append(",");
                    }
                    sb.append(mOrderInfos.get(orderIdList.get(orderIdList.size() - 1)));
                    sb.append("]");
                    String json = "{\"time\":" + mArrivedTimeList.get(index++) + ","
                            + "\"orderId\":" + sb.toString() + "}";
                    jsonList.add(json);
                    curAddress++;
                }
                StringBuilder jsonSb = new StringBuilder("[");
                for (int i = 0; i < jsonList.size() - 1; i++) {
                    jsonSb.append(jsonList.get(i));
                    jsonSb.append(",");
                }
                jsonSb.append(jsonList.get(jsonList.size()));
                jsonSb.append("]");
                    Log.d("htout", "add order:" + jsonSb.toString());
                Call call = NetworkUtil.getCallByPost(Constant.ORIGINAL_URL, jsonSb.toString());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
            }
        });
        //初始化地图
        init();
        simpleLocation();
        accurateLocation();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
        }
//        mAMap.setMapLanguage(AMap.ENGLISH);
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
    }

    /**
     * 注册监听
     */
    private void registerListener() {
        mAMap.setOnMapClickListener(MainActivity.this);
        mAMap.setOnMarkerClickListener(MainActivity.this);
        mAMap.setOnInfoWindowClickListener(MainActivity.this);
    }

    private void simpleLocation() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
        mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        mAMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
    }

    public void getDataAndSearchRoute() {
        mOrderInfos = getIntent().getParcelableArrayListExtra("info");
        if (mOrderInfos == null) {
            mOrderInfos = new ArrayList<>();
        }
        mArrivedTimeList = new ArrayList<>(mOrderInfos.size());
        mTwoPointTimeList = new ArrayList<>(mOrderInfos.size());
        //记录送餐地点数目
        pointNumber = mOrderInfos.size();
        addressCount = mOrderInfos.size();
        //初始化距离矩阵
        distance = new double[pointNumber][pointNumber];
        mPoint = new LatLonPoint[pointNumber];
        mRidePath = new RidePath[3][pointNumber][pointNumber];
        for (int i = 0; i < pointNumber; i++) {
            mPoint[i] = new LatLonPoint(mOrderInfos.get(i).getXPoint(), mOrderInfos.get(i).getYPoint());
        }

        searchRouteResult(ROUTE_TYPE_RIDE, RouteSearch.RidingDefault);

    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        showProgressDialog();

        RouteSearch.FromAndTo[][] fromAndTo = new RouteSearch.FromAndTo[pointNumber][pointNumber];
        for (int i = 0; i < fromAndTo.length; i++) {
            for (int j = 0; j < fromAndTo.length; j++) {
                if (i != j) {
                    fromAndTo[i][j] = new RouteSearch.FromAndTo(mPoint[i], mPoint[j]);
                    if (routeType == ROUTE_TYPE_RIDE) {// 骑行路径规划
                        RouteSearch.RideRouteQuery query = new RouteSearch.RideRouteQuery(fromAndTo[i][j], mode);
                        mRouteSearch.calculateRideRouteAsyn(query);// 异步路径规划骑行模式查询
                    }
                } else {
                    distance[i][j] = 0;
                }
            }
        }
    }

    private boolean isFirstSearchRoute;
    @OnClick(R.id.btn_show_route)
    public void showRoute() {
        if (isDataReady()) {
            if (!isFirstSearchRoute) {
                getRouteFromACS();
                getTime();
                getArrivedTime();
                isFirstSearchRoute = true;
            }
            new AlertDialog.Builder(this)
                    .setTitle("请选择一条路线")
                    .setItems(new String[]{"路线1", "路线2", "路线3"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int j) {
                            mSelectPath = j;
                            mAMap.clear();// 清理地图上的所有覆盖物
                            //根据蚁群算法计算的路径绘制路径

                            int[] color = {getResources().getColor(R.color.path_blue), getResources().getColor(R.color.path_pink), getResources().getColor(R.color.path_green)};
//                        for (int j = 0; j < 2; j++) {
                            customRidePaths.clear();
                            for (int i = 0; i < pointNumber - 1; i++) {
                                customRidePaths.addAll(mRidePath[j][points[j][2 * i]][points[j][2 * i + 1]].getSteps());
                                mAMap.addMarker(new MarkerOptions()
                                        .position(AmapUtil.convertToLatLng(mRidePath[j][points[j][2 * i]][points[j][2 * i + 1]].getSteps().get(0).getPolyline().get(0)))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_ride)));
//            Log.d("htout", points[2 * i] + "->" + points[2 * i + 1] + ":" + distance[points[0][2 * i]][points[0][2 * i + 1]]);
                            }
                            customRidePaths.addAll(mRidePath[j][points[j][2 * pointNumber - 2]][points[j][0]].getSteps());

                            RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                                    MainActivity.this, mAMap, mRidePath[j][0][1],
                                    mPoint[points[j][0]],
                                    mPoint[points[j][2 * pointNumber - 2]]);
                            rideRouteOverlay.setPathColor(color[j]);
                            rideRouteOverlay.customRidePaths = customRidePaths;
                            rideRouteOverlay.removeFromMap();
                            rideRouteOverlay.addToMap();
                            rideRouteOverlay.zoomToSpan();
                        }
                    })
                    .create()
                    .show();
        } else {
            ToastUtil.show(this, "路径规划中，请稍后再试");
        }
    }

    private boolean isDataReady() {
        for (int i = 0; i < distance.length; i++) {
            for (int j = 0; j < distance[0].length; j++) {
                if (i != j && distance[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void getRouteFromACS() {
        for (int i = 0; i < distance.length; i++) {
            for (int j = 0; j < distance[i].length; j++) {
                Log.d("htout", i + "<->" + j + ":" + distance[i][j]);
            }
        }
        MultiACS multiACS = new MultiACS();
        points = multiACS.acs(distance);
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[0].length; j++) {
                Log.d("htout", "points" + i + ":" + points[i][j]);
            }

        }
    }

    private void getTime() {
        int curIndex = 0;
        for (int i = 0; i < mOrderInfos.size() - 1; i++) {
            double twoPointDistance = distance[points[mSelectPath][2 * curIndex]][points[mSelectPath][2 * curIndex + 1]];
            Log.d("htout", "distance:" + distance[points[mSelectPath][2 * curIndex]][points[mSelectPath][2 * curIndex + 1]]);
            double time = twoPointDistance / 400;
            mTwoPointTimeList.add(time);
            Log.d("htout", "time:" + time);
            curIndex++;
        }
    }

    private void getArrivedTime() {
        mArrivedTimeList.clear();
        int arrivedTime = 0;
        for (int i = 0; i < mTwoPointTimeList.size(); i++) {
            arrivedTime += Math.round(mTwoPointTimeList.get(i));
            if (i != 0) {
                arrivedTime += 4;
            }
            mArrivedTimeList.add(arrivedTime);
            Log.d("htout", "arrivedtime:" + arrivedTime);
        }
//        updateArrivedTime();
    }

    private void updateArrivedTime() {
        int curAddress = 0;
        Log.d("htout", "aaaa");

        List<String> jsonList = new ArrayList<>();
        Log.d("htout", "bbbb");
        int index = 0;
        for (int j = curAddress; j < mOrderInfos.size() - 1; j++) {
            List<Integer> orderIdList = mOrderInfos.get(points[mSelectPath][2 * curAddress + 1]).getOrderId();

            StringBuilder sb = new StringBuilder("[");
            for (int k = 0; k < orderIdList.size() - 1; k++) {
                sb.append(orderIdList.get(k));
                sb.append(",");
            }
            sb.append(orderIdList.get(orderIdList.size() - 1));
            sb.append("]");
            String json = "{\"time\":" + mArrivedTimeList.get(index++) + ","
                    + "\"orderId\":" + sb.toString() + "}";
            jsonList.add(json);
            curAddress++;
        }
        StringBuilder jsonSb = new StringBuilder("[");
        for (int i = 0; i < jsonList.size() - 1; i++) {
            jsonSb.append(jsonList.get(i));
            jsonSb.append(",");
        }
        jsonSb.append(jsonList.get(jsonList.size() - 1));
        jsonSb.append("]");
        Log.d("htout", "add order:" + jsonSb.toString());
        Call call = NetworkUtil.getCallByPost(Constant.ORIGINAL_URL + "/2/setArriveTime", jsonSb.toString());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("htout", "update:" + response.body().string());
            }
        });
    }

    @Override
    public void onRideRouteSearched(RideRouteResult result, int errorCode) {
        dissmissProgressDialog();
        mAMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mRideRouteResult = result;
                    final RidePath ridePath = mRideRouteResult.getPaths().get(0);
                    LatLonPoint startPoint = result.getStartPos();
                    LatLonPoint endPoint = result.getTargetPos();
                    double startLatitude = startPoint.getLatitude();
                    double startLongitude = startPoint.getLongitude();
                    double endLatitude = endPoint.getLatitude();
                    double endLongitude = endPoint.getLongitude();
                    int flag = 0;
                    for (int i = 0; i < pointNumber; i++) {
                        for (int j = 0; j < pointNumber; j++) {
                            if (i != j) {
                                if (mPoint[i].getLatitude() == startLatitude && mPoint[i].getLongitude() == startLongitude
                                        && mPoint[j].getLatitude() == endLatitude && mPoint[j].getLongitude() == endLongitude) {
                                    distance[i][j] = ridePath.getDistance();
                                    for (int k = 0; k < 3; k++) {
                                        mRidePath[k][i][j] = ridePath;
                                    }
                                    flag++;
                                    break;
                                }
                            }
                        }
                        if (flag != 0) {
                            break;
                        }
                    }
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, "无结果");
                }
            } else {
                ToastUtil.show(mContext, "无结果");
            }
        } else {
//            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    private int mNextAddress;

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null) {
                if (location.getErrorCode() == 0) {

                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    ToastUtil.show(MainActivity.this, "ErrCode:"
                            + location.getErrorCode() + ", errInfo:"
                            + location.getErrorInfo());
                }
            }
            if (zoomTime > 0) {
                mAMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                zoomTime--;
            } else {
                MyLocationStyle myLocationStyle = new MyLocationStyle();
                myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
                mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
            }

            if (addressCount > 0) {
                //获取当前位置到下一个送餐地点的距离
                Log.d("htout", "point:" +  mOrderInfos.get(points[mSelectPath][2 * mNextAddress + 1]).getYPoint() + " " + mOrderInfos.get(points[mSelectPath][2 * mNextAddress + 1]).getXPoint());
                currentDistance = AmapUtil.getDistance(location.getLongitude(), location.getLatitude(),
                        mOrderInfos.get(points[mSelectPath][2 * mNextAddress + 1]).getYPoint(), mOrderInfos.get(points[mSelectPath][2 * mNextAddress + 1]).getXPoint());
                if (currentDistance < NOTIFY_DISTANCE && !mIsCalling) {
                    mReceiver.setCurrentIndex(1);
                    mReceiver.setPhoneNumberList(mOrderInfos.get(points[mSelectPath][2 * mNextAddress + 1]).getPhoneNumberList());
                    call(mOrderInfos.get(points[mSelectPath][2 * mNextAddress + 1]).getPhoneNumberList());
                    mIsCalling = true;
                    addressCount--;
                    mNextAddress++;
                }
                ToastUtil.show(MainActivity.this, "距离下个送餐地点还有" + (int)currentDistance + "m");
                location.getExtras().getInt(MyLocationStyle.LOCATION_TYPE);
            }
        }
    };

    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private void accurateLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //初始化AMapLocationClientOption对象
        mLocationOption = getDefaultOption();
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    /**
     * 默认的定位参数
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(true);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (zoomTime > 0) {
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            zoomTime--;
        }
        if (addressCount > 0) {
            currentDistance = AmapUtil.getDistance(location.getLongitude(), location.getLatitude(),
                    mOrderInfos.get(addressCount - 1).getYPoint(), mOrderInfos.get(addressCount - 1).getXPoint());
            if (currentDistance < 5 && !mIsCalling) {
//                mReceiver.phoneNumberList = mOrderInfos.get(addressCount - 1).getPhoneNumberList();
                call(mOrderInfos.get(addressCount - 1).getPhoneNumberList());
                mIsCalling = true;
                addressCount--;

            }
//            ToastUtil.show(MainActivity.this, "距离下个送餐地点还有" + currentDistance + "m");
            location.getExtras().getInt(MyLocationStyle.LOCATION_TYPE);
        }

    }

    private PhoneCallReceiver mReceiver;

    private void call(List<String> phoneNumberList) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + phoneNumberList.get(0)));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    public static final int READ_CALL_LOG_PERMISSION = 0;

    private void showNoAnswerList() {
        search();
        String[] noAnswerNumbers = new String[mNoAnswerNumberList.size()];
        for (int i = 0; i < mNoAnswerNumberList.size(); i++) {
            noAnswerNumbers[i] = mNoAnswerNumberList.get(i);
        }
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("是否重拨以下未接通的号码")
                .setItems(noAnswerNumbers, null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReceiver.setPhoneNumberList(mNoAnswerNumberList);
                        call(mNoAnswerNumberList);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void search() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(this, "you should allow this permission!", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, READ_CALL_LOG_PERMISSION);
            }
        } else {
            searchCallLog();
        }
    }

    /**
     *查询未接通号码
     **/
    private void searchCallLog() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls.CACHED_NAME,  //姓名
                        CallLog.Calls.NUMBER,    //号码
                        CallLog.Calls.TYPE,  //呼入/呼出(2)/未接
                        CallLog.Calls.DATE,  //拨打时间
                        CallLog.Calls.DURATION   //通话时长
                }, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
        int count = mOrderInfos.get(addressCount - 1).getPhoneNumberList().size();
        while (cursor.moveToNext()) {
            String number = cursor.getString(1);
            int callDuration = Integer.parseInt(cursor.getString(4));
            if (callDuration < 1) {
                mNoAnswerNumberList.add(number);
            }
            count--;
            if (count == 0) {
                break;
            }

        }
        cursor.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_CALL_LOG_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchCallLog();
                } else {

                }
                return;
            }
            default:
                break;
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null) {
            progDialog = new ProgressDialog(this);
        }
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        getDataAndSearchRoute();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
