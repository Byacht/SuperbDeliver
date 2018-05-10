package com.byacht.superbdeliver.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.Utils.Constant;
import com.byacht.superbdeliver.Utils.NetworkUtil;
import com.byacht.superbdeliver.Utils.TimeUtil;
import com.byacht.superbdeliver.adapter.MyFragmentPagerAdapter;
import com.byacht.superbdeliver.fragment.MyFragment;
import com.byacht.superbdeliver.view.YearPickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class StatisticsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_statistics)
    Toolbar mToolbar;
    @BindView(R.id.showtime_tv)
    TextView mShowTimeTv;
    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    @BindView(R.id.chart_view)
    LineChartView mChart;
    @BindView(R.id.order_count_tv)
    TextView mOrderCountTv;

    private String mSearchTime;
    private String mSelectedYear;
    private String mSelectedMonth;
    private String mStartTime;
    private String mEndTime;

    private List<Integer> mDataList;

    private MyFragmentPagerAdapter adapter;
    private int viewHeight;  //选择年月视图的高度
    private boolean isSelected = false;  //是否处于选择时间的状态，若是，按下返回键将隐藏选择视图，否则退出Activity

    private int mIndex;
    private int mAccountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ButterKnife.bind(this);

        mAccountId = getIntent().getIntExtra("accountId", 0);

        initView();
        setupToolBar();
        setupTabLayout();
//        generateLineData(ChartUtils.COLOR_BLUE, 50);
//        showStatisticsView();

//        findViewById(R.id.scrollView).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (isSelected) {
//                    hideChooseTimeLayout();
//                    isSelected = false;
//                }
//                return false;
//            }
//        });
    }

    private void initView() {
        viewHeight = 400;
        mSearchTime = TimeUtil.getCurrentTime().substring(0, 7);
        mSelectedYear = mSearchTime.substring(0, 4);
        mSelectedMonth = mSearchTime.substring(5, 7);
        mShowTimeTv.setText(mSelectedYear + "-" + mSelectedMonth);
        mDataList = new ArrayList<>();
        getData(MONTH_STATISTIC);
        generateInitialLineData(MONTH_STATISTIC);
        generateLineData(ChartUtils.COLOR_BLUE);
    }

    private void setupToolBar() {
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setTitle("个人资料");
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

    private void setupTabLayout() {
        adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorColor(Color.alpha(0));

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mIndex = mViewPager.getCurrentItem();
                /* 得到当前的fragment，通过接口回调响应fragment的点击事件 */
                MyFragment myFragment = (MyFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + mIndex);
                myFragment.setOnShowTimeListener(new MyFragment.OnShowTime() {
                    @Override
                    public void showTime(String time) {
                        mSelectedMonth = time;
                        mSearchTime = mSearchTime.substring(0, 5) + mSelectedMonth;
                        mShowTimeTv.setText(mSearchTime);
                        hideChooseTimeLayout();
                        getData(MONTH_STATISTIC);
                        generateInitialLineData(MONTH_STATISTIC);
                        generateLineData(ChartUtils.COLOR_BLUE);
                    }
                });
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    hideChooseTimeLayout();
                    showYearPickerDialog();
                }
                if (tab.getPosition() == 1) {
                    isSelected = true;
                    showChooseTimeLayout();
                }
                if (tab.getPosition() == 2) {
                    hideChooseTimeLayout();
                    showDoubleDatePicker();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showYearPickerDialog();
                }
                if (tab.getPosition() == 1) {
                    isSelected = true;
                    showChooseTimeLayout();
                }
                if (tab.getPosition() == 2) {
                    showDoubleDatePicker();
                }
            }
        });
    }

    private void showYearPickerDialog() {
        YearPickerDialog dialog = new YearPickerDialog(StatisticsActivity.this, AlertDialog.THEME_HOLO_LIGHT,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mShowTimeTv.setText(year + "年");
                        mSelectedYear = String.valueOf(year);
                        mSearchTime = mSelectedYear + "-";
                        getData(YEAR_STATISTIC);
                        generateInitialLineData(YEAR_STATISTIC);
                        generateLineData(ChartUtils.COLOR_BLUE);
                    }
                },
                Integer.valueOf(mSelectedYear), 1, 1);
        dialog.show();
    }

    private void showDoubleDatePicker() {
        View view = LayoutInflater.from(StatisticsActivity.this).inflate(R.layout.double_datepicker_layout, null);
        final DatePicker startDatePicker = (DatePicker) view.findViewById(R.id.StartDate_dp);
        final DatePicker endDatePicker = (DatePicker) view.findViewById(R.id.EndDate_dp);

        final AlertDialog dialog = new AlertDialog.Builder(StatisticsActivity.this)
                .setView(view)
                .setTitle("请选择查询时间段")
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int startYear = startDatePicker.getYear();
                int startMonth = startDatePicker.getMonth() + 1;
                int startDay = startDatePicker.getDayOfMonth();
                int endYear = endDatePicker.getYear();
                int endMonth = endDatePicker.getMonth() + 1;
                int endDay = endDatePicker.getDayOfMonth();
                if (endYear >= startYear && endMonth >= startMonth && endDay >= startDay){
                    String startM = formDate(startMonth);
                    String startD = formDate(startDay);
                    String endM = formDate(endMonth);
                    String endD = formDate(endDay);

                    mStartTime = startYear + "-" + startM + "-" + startD;
                    mEndTime = endYear + "-" + endM + "-" + endD;
                    mShowTimeTv.setText(mStartTime + " ~ " + mEndTime);
//                    notifyTimeChange(1);
                    dialog.dismiss();
                } else {
                    Toast.makeText(StatisticsActivity.this, "起始时间不能晚于终止时间", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private String formDate(int time) {
        if (time < 10) {
            return "0" + time;
        }
        return time + "";
    }

    private void showChooseTimeLayout() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mViewPager.getLayoutParams();
        layoutParams.height = viewHeight;
        mViewPager.setLayoutParams(layoutParams);
    }

    private void hideChooseTimeLayout() {
        isSelected = false;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mViewPager.getLayoutParams();
        layoutParams.height = 0;
        mViewPager.setLayoutParams(layoutParams);
    }

    private LineChartData lineData;
    public final static String[] days = new String[]{"1", "5", "10", "15", "20", "25", "28"};
    public final static String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};


    private void generateInitialLineData(int type) {

        List<AxisValue> axisValues = new ArrayList<AxisValue>();
        List<PointValue> values = new ArrayList<PointValue>();
        int curIndex = 0;
        if (type == MONTH_STATISTIC) {
            for (int i = 0; i < mDataList.size(); ++i) {
                values.add(new PointValue(i, 0));
                if (curIndex < days.length && i + 1 == Integer.valueOf(days[curIndex])) {
                    axisValues.add(new AxisValue(i).setLabel(days[curIndex]));
                    curIndex++;
                }

            }
        } else if (type == YEAR_STATISTIC){
            for (int i = 0; i < mDataList.size(); ++i) {
                values.add(new PointValue(i, 0));
                if (curIndex < months.length) {
                    axisValues.add(new AxisValue(i).setLabel(months[curIndex]));
                    curIndex++;
                }

            }
        }

        Line line = new Line(values);
        line.setColor(ChartUtils.COLOR_GREEN).setCubic(true).setPointRadius(4);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        lineData = new LineChartData(lines);
        lineData.setAxisXBottom(new Axis(axisValues).setHasLines(true));
        lineData.setAxisYLeft(new Axis().setHasLines(true).setMaxLabelChars(4));

        mChart.setLineChartData(lineData);

        // For build-up animation you have to disable viewport recalculation.
        mChart.setViewportCalculationEnabled(false);

        // And set initial max viewport and current viewport- remember to set viewports after data.
        Viewport v;
        if (type == MONTH_STATISTIC) {
            v = new Viewport(0, 80, 30, 0);
        } else {
            v = new Viewport(0, 2500, 12, 0);
        }

        mChart.setMaximumViewport(v);
        mChart.setCurrentViewport(v);

        mChart.setZoomType(ZoomType.HORIZONTAL);
    }

    private void generateLineData(int color) {
        // Cancel last animation if not finished.
        mChart.cancelDataAnimation();

        // Modify data targets
        Line line = lineData.getLines().get(0);// For this example there is always only one line.
        line.setColor(color);
        int count = 0;
        int index = 0;
        for (PointValue value : line.getValues()) {
            // Change target only for Y value.
            float num = mDataList.get(index);
            count += num;
            value.setTarget(value.getX(), num);
        }
        mOrderCountTv.setText("总配送数：" + count + "份");
        // Start new data animation with 300ms duration;
        mChart.startDataAnimation(300);
    }


    private void getData(int type) {
        Call call;
        if (type == YEAR_STATISTIC) {
            call = NetworkUtil.getCallByGet(Constant.ORIGINAL_URL + mAccountId + "/getYearData/" +  mSelectedYear + mSelectedMonth);
        } else {
            call = NetworkUtil.getCallByGet(Constant.ORIGINAL_URL + mAccountId + "/getMonthData/" + mSelectedYear + mSelectedMonth);
        }
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.toString();
                String[] data = result.split(";");
                mDataList.clear();
                for (int i = 0; i < data.length; i++) {
                    int num = Integer.valueOf(data[i].split(":")[1]);
                    mDataList.add(num);
                }
            }
        });
    }

    public static int MONTH_STATISTIC = 0;
    public static int YEAR_STATISTIC = 1;

//    private void notifyTimeChange(int searchWay) {
//        if (searchWay == 0) {
//            getData();
//        } else if (searchWay == 1) {
//            searchDataOfCustomTime();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isSelected) {
            hideChooseTimeLayout();
            isSelected = false;
        } else {
            super.onBackPressed();
        }
    }
}
