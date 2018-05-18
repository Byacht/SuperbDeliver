package com.byacht.superbdeliver.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byacht.superbdeliver.R;

import java.util.List;

/**
 * Created by dn on 2018/5/14.
 */

public class AddOrderInfoAdapter extends RecyclerView.Adapter<AddOrderInfoAdapter.AddOrderInfoViewHolder> {

    private List<Integer> mSizeList;

    public AddOrderInfoAdapter(List<Integer> size) {
        mSizeList = size;
    }

    @Override
    public AddOrderInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_order_info_item, parent, false);
        return new AddOrderInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AddOrderInfoViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mSizeList.size();
    }

    class AddOrderInfoViewHolder extends RecyclerView.ViewHolder {
        public AddOrderInfoViewHolder(View itemView) {
            super(itemView);
        }
    }

}
