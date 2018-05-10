package com.byacht.superbdeliver.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byacht.superbdeliver.R;


/**
 * Created by dn on 2016/11/17.
 */

public class MyFragment extends Fragment implements View.OnClickListener{
    private static final String ARGS_PAGE = "page";
    private int mPage;

    public interface OnShowTime{
        void showTime(String time);
    }

    private OnShowTime onShowTimeListener;

    public void setOnShowTimeListener(OnShowTime onShowTimeListener){
        this.onShowTimeListener = onShowTimeListener;
    }

    public static MyFragment newInstance(int page){
        Bundle args = new Bundle();
        args.putInt(ARGS_PAGE, page);
        MyFragment fragment = new MyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARGS_PAGE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (mPage == 1){
            view = null;
        } else if (mPage == 2){
            view = inflater.inflate(R.layout.choose_month_layout,container,false);
            view.findViewById(R.id.Jan).setOnClickListener(this);
            view.findViewById(R.id.Feb).setOnClickListener(this);
            view.findViewById(R.id.Mar).setOnClickListener(this);
            view.findViewById(R.id.Apr).setOnClickListener(this);
            view.findViewById(R.id.May).setOnClickListener(this);
            view.findViewById(R.id.Jun).setOnClickListener(this);
            view.findViewById(R.id.Jul).setOnClickListener(this);
            view.findViewById(R.id.Aug).setOnClickListener(this);
            view.findViewById(R.id.Sept).setOnClickListener(this);
            view.findViewById(R.id.Obt).setOnClickListener(this);
            view.findViewById(R.id.Nov).setOnClickListener(this);
            view.findViewById(R.id.Dec).setOnClickListener(this);
        } else {
            view = null;
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Jan:
                onShowTimeListener.showTime("01");
                break;
            case R.id.Feb:
                onShowTimeListener.showTime("02");
                break;
            case R.id.Mar:
                onShowTimeListener.showTime("03");
                break;
            case R.id.Apr:
                onShowTimeListener.showTime("04");
                break;
            case R.id.May:
                onShowTimeListener.showTime("05");
                break;
            case R.id.Jun:
                onShowTimeListener.showTime("06");
                break;
            case R.id.Jul:
                onShowTimeListener.showTime("07");
                break;
            case R.id.Aug:
                onShowTimeListener.showTime("08");
                break;
            case R.id.Sept:
                onShowTimeListener.showTime("09");
                break;
            case R.id.Obt:
                onShowTimeListener.showTime("10");
                break;
            case R.id.Nov:
                onShowTimeListener.showTime("11");
                break;
            case R.id.Dec:
                onShowTimeListener.showTime("12");
                break;
            default: break;
        }
    }
}
