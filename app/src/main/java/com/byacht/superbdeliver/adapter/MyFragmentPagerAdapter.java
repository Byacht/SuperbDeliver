package com.byacht.superbdeliver.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.byacht.superbdeliver.fragment.MyFragment;


/**
 * Created by dn on 2016/11/17.
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private Context context;
    private String[] Tabs = new String[]{"按年","按月"};
    public MyFragmentPagerAdapter(FragmentManager fm, Context context){
        super(fm);
        this.context = context;
    }
    @Override
    public Fragment getItem(int position) {
        return MyFragment.newInstance(position+1);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Tabs[position];
    }
}
