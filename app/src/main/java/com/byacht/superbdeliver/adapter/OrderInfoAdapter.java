package com.byacht.superbdeliver.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.byacht.superbdeliver.R;
import com.byacht.superbdeliver.model.OrderInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dn on 2018/4/21.
 */

public class OrderInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<OrderInfo> mInfoList;
    private List<Integer> mAddressIndexList;
    private List<String> mAddressList;

    public OrderInfoAdapter(List<OrderInfo> infoList, List<String> addressList) {
        mInfoList = infoList;
        mAddressList = addressList;
        mAddressIndexList = new ArrayList<>();
        int index = 0;
        for (OrderInfo orderInfo : infoList) {
            mAddressIndexList.add(index);
            List<String> phoneNumber = orderInfo.getPhoneNumberList();
            index += phoneNumber.size() + 1;
        }
    }

    public static int ORDER_INFO = 0;
    public static int ORDER_ADDRESS = 1;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == ORDER_INFO) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_info_item, parent, false);
            return new OrderInfoViewHolder(view);
        } else if (viewType == ORDER_ADDRESS) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_info_address_item, parent, false);
            return new OrderInfoAddressViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int i = 0;
        for (; i < mAddressIndexList.size(); i++) {
            if (position < mAddressIndexList.get(i)) {
                break;
            }
        }
        if (holder instanceof OrderInfoViewHolder) {
            ((OrderInfoViewHolder) holder).phoneNumberTv.setText(mInfoList.get(i - 1).getPhoneNumberList().get(position - mAddressIndexList.get(i - 1) - 1));

            Log.d("htout", "kaoyafan:" + position);
        } else {
            ((OrderInfoAddressViewHolder)holder).addressTv.setText(mInfoList.get(i - 1).getXPoint() + "," + mInfoList.get(i - 1).getYPoint());
            Log.d("htout", "point:" + position + " " + mInfoList.get(i - 1).getXPoint());
        }
    }

    @Override
    public int getItemCount() {
        if (mInfoList.size() == 0) {
            return 0;
        }
        return mAddressIndexList.get(mAddressIndexList.size() - 1) + mInfoList.get(mInfoList.size() - 1).getPhoneNumberList().size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (mAddressIndexList.contains(position)) {
            return ORDER_ADDRESS;
        } else {
            return ORDER_INFO;
        }
    }

    class OrderInfoAddressViewHolder extends RecyclerView.ViewHolder {

        TextView addressTv;

        public OrderInfoAddressViewHolder(View itemView) {
            super(itemView);
            addressTv = (TextView) itemView.findViewById(R.id.order_info_address_tv);
        }
    }

    class OrderInfoViewHolder extends RecyclerView.ViewHolder {
        TextView phoneNumberTv;

        public OrderInfoViewHolder(View itemView) {
            super(itemView);
            phoneNumberTv = (TextView) itemView.findViewById(R.id.order_info_phone_tv);
        }
    }
}
