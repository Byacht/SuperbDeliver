package com.byacht.superbdeliver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
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
import com.byacht.superbdeliver.Receiver.PhoneCallReceiver;
import com.byacht.superbdeliver.Utils.AmapUtil;
import com.byacht.superbdeliver.Utils.ToastUtil;
import com.byacht.superbdeliver.model.OrderInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, RouteSearch.OnRouteSearchListener,
        AMap.OnInfoWindowClickListener, AMap.OnMarkerClickListener, AMap.OnMyLocationChangeListener {

    @BindView(R.id.map_main)
    MapView mMapView;
    @BindView(R.id.btn_get_data)
    Button mBtnGetData;
    @BindView(R.id.btn_show_route)
    Button mBtnShowRoute;

    private AMap mAMap;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private RideRouteResult mRideRouteResult;

    private double city[][] = new double[6][2];
    private LatLonPoint[] mPoint = new LatLonPoint[6];
    private double distance[][] = new double[6][6];
    private RidePath mRidePath[][] = new RidePath[6][6];
    private final int ROUTE_TYPE_RIDE = 4;
    public List<RideStep> customRidePaths = new ArrayList<RideStep>();
    private int pointNumber;
    private Button btnShowRoute;
    private int points[];
    private int zoomTime = 2;
    private int addressCount = 0;
    private int numberCount = 0;
    private double currentDistance = 0;

    private List<OrderInfo> mOrderInfos;


    private ProgressDialog progDialog = null;// 搜索时进度条

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mContext = this.getApplicationContext();
        mMapView.onCreate(bundle);// 此方法必须重写

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.intent.action.NEW_OUTGOING_CALL");
        mReceiver = new PhoneCallReceiver();
        registerReceiver(mReceiver, filter);
        mReceiver.setCallOverListener(new PhoneCallReceiver.CallOver() {
            @Override
            public void setCallOver(boolean isCallOver) {
                isCall = isCallOver;
                Log.d("htout", "isCall:" + isCall);
            }
        });
        init();
        initPoint();
        simpleLocation();

    }

    private void initPoint() {
        city[0][0] = 23.165808;
        city[0][1] = 113.340662;
        city[1][0] = 23.16565;
        city[1][1] = 113.341713;
        city[2][0] = 23.166124;
        city[2][1] = 113.339761;
        city[3][0] = 23.164681;
        city[3][1] = 113.339804;
        city[4][0] = 23.164156;
        city[4][1] = 113.341418;
        city[5][0] = 23.167776;
        city[5][1] = 113.346418;

        pointNumber = city.length;

        for (int i = 0; i < city.length; i++) {
            mPoint[i] = new LatLonPoint(city[i][0], city[i][1]);
        }
    }

    private void setfromandtoMarker() {
//        aMap.addMarker(new MarkerOptions()
//                .position(AMapUtil.convertToLatLng(mStartPoint))
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
//        aMap.addMarker(new MarkerOptions()
//                .position(AMapUtil.convertToLatLng(mEndPoint))
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
        }
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
//        aMap.setInfoWindowAdapter(MainActivity.this);
    }

    private void simpleLocation() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) ;//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
        mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        mAMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mAMap.setOnMyLocationChangeListener(this);
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
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//
//        }

    }

    private void getRouteFromACS(){
        int fakeCity[][] = new int[6][2];
        ACS acs = new ACS();
        for (int i = 0; i < distance.length; i++) {
            for (int j = 0; j < distance[i].length; j++) {
                Log.d("htout", i + "<->" + j + ":" + distance[i][j]);
            }
        }
        points = acs.ACSalgorithm(fakeCity, distance);
        for (int i = 0; i < points.length; i++) {
            Log.d("htout", "points" + i + ":" + points[i]);
        }
//        points = new int[12];
//        points[0] = 5;
//        points[1] = 1;
//        points[2] = 1;
//        points[3] = 0;
//        points[4] = 0;
//        points[5] = 2;
//        points[6] = 2;
//        points[7] = 3;
//        points[8] = 3;
//        points[9] = 4;
//        points[10] = 4;
//        points[11] = 5;
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
                                    mRidePath[i][j] = ridePath;
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

    @OnClick(R.id.btn_get_data)
    public void getDataAndSearchRoute() {
        searchRouteResult(ROUTE_TYPE_RIDE, RouteSearch.RidingDefault);
        mOrderInfos = new ArrayList<OrderInfo>();


        List<String> numberList = new ArrayList<String>();
        numberList.add("15913049468");
        numberList.add("15913049468");

        List<String> numberList1 = new ArrayList<String>();
        numberList1.add("15913049468");
        numberList1.add("15913049468");
        numberCount = numberList.size();

        LatLng lat1 = new LatLng(23.16565, 113.341713);
        LatLng lat2 = new LatLng(23.165808, 113.340662);

        OrderInfo orderInfo1 = new OrderInfo(lat1, numberList);
        OrderInfo orderInfo2 = new OrderInfo(lat2, numberList1);
        mOrderInfos.add(orderInfo1);
        mOrderInfos.add(orderInfo2);

        addressCount = mOrderInfos.size();
    }

    @OnClick(R.id.btn_show_route)
    public void showRoute() {
        getRouteFromACS();

        //根据蚁群算法计算的路径绘制路径
        for (int i = 0; i < pointNumber - 1; i++) {
            customRidePaths.addAll(mRidePath[points[2 * i]][points[2 * i + 1]].getSteps());
            mAMap.addMarker(new MarkerOptions()
                    .position(AmapUtil.convertToLatLng(mRidePath[points[2 * i]][points[2 * i + 1]].getSteps().get(0).getPolyline().get(0)))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_ride)));
            Log.d("htout", points[2 * i] + "->" + points[2 * i + 1] + ":" + distance[points[2 * i]][points[2 * i + 1]]);
        }
        RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                MainActivity.this, mAMap, mRidePath[0][1],
                mPoint[points[0]],
                mPoint[points[2 * pointNumber - 2]]);
        rideRouteOverlay.customRidePaths = customRidePaths;
        rideRouteOverlay.removeFromMap();
        rideRouteOverlay.addToMap();
        rideRouteOverlay.zoomToSpan();
    }

    private boolean isCall = true;

    @Override
    public void onMyLocationChange(Location location) {
        if (zoomTime > 0){
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            zoomTime--;
        }
        if (addressCount > 0) {
            currentDistance = AmapUtil.getDistance(location.getLongitude(), location.getLatitude(),
                    mOrderInfos.get(addressCount - 1).getLatLng().longitude, mOrderInfos.get(addressCount - 1).getLatLng().latitude);
            if (currentDistance < 50 && isCall) {
                mReceiver.phoneNumberList = mOrderInfos.get(addressCount - 1).getPhoneNumberList();
                call(mOrderInfos.get(addressCount - 1).getPhoneNumberList());
                isCall = false;
                addressCount--;

            }
            ToastUtil.show(MainActivity.this, "距离下个送餐地点还有" + currentDistance + "m");
            location.getExtras().getInt(MyLocationStyle.LOCATION_TYPE);
        }

    }

    private PhoneCallReceiver mReceiver;

    private void call(List<String> phoneNumberList) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + phoneNumberList.get(0)));
        startActivity(intent);
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
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
        unregisterReceiver(mReceiver);
    }


}
