package com.byacht.superbdeliver.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.byacht.superbdeliver.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddOrderInfoActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener{

    @BindView(R.id.submit_btn)
    Button mSubmitBtn;

    PoiSearch.Query mQuery;
    PoiSearch mPoiSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order_info);
        ButterKnife.bind(this);

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        String keyWord = "华南理工大学北三";
        mQuery = new PoiSearch.Query(keyWord, "", "020");
//keyWord表示搜索字符串，
//第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
//cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        mQuery.setPageSize(10);// 设置每页最多返回多少条poiitem
//        mQuery.setPageNum(currentPage);//设置查询页码
        mPoiSearch = new PoiSearch(this, mQuery);
        mPoiSearch.setOnPoiSearchListener(this);
        mPoiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int code) {
        for (int i = 0; i < poiResult.getPois().size(); i++) {
            Log.d("htout", poiResult.getPois().get(i).getSnippet());
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
