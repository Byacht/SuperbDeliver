package com.byacht.superbdeliver.adapter;

import android.content.Context;
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
 * Created by dn on 2018/5/17.
 */

public class MyOrderInfoAdapter extends RecyclerView.Adapter<MyOrderInfoAdapter.MyOrderInfoViewHolder>{

    private Context mContext;
    private List<String> mAddressList;
    private List<List<String>> mPhoneList;

    private int[] color = {R.color.pink, R.color.purple, R.color.orange, R.color.yellow, R.color.green};

    public MyOrderInfoAdapter(Context context, List<OrderInfo> orderInfos) {
        mContext = context;
        mAddressList = new ArrayList<>();
        mPhoneList = new ArrayList<>();
        for (OrderInfo orderInfo : orderInfos) {
            mAddressList.add(orderInfo.getPlace());
            mPhoneList.add(orderInfo.getPhoneNumberList());
        }
    }

    @Override
    public MyOrderInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_order_info_item, parent, false);
        return new MyOrderInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyOrderInfoViewHolder holder, int position) {
        holder.addressTv.setText("地点：" + mAddressList.get(position));
        Log.d("htout", "color:" + position % 5);
        holder.addressTv.setTextColor(mContext.getResources().getColor(color[position % 5]));
//        notifyItemChanged(position);
        StringBuilder sb = new StringBuilder("号码：\n");
        for (int i = 0; i < mPhoneList.get(position).size() - 1; i++) {
            sb.append(mPhoneList.get(position).get(i));
            sb.append("\n");
        }
        sb.append(mPhoneList.get(position).get(mPhoneList.get(position).size() - 1));
        holder.phoneTv.setText(sb.toString());
        holder.phoneTv.setTextColor(mContext.getResources().getColor(color[(position + 1) % 5]));
    }

    @Override
    public int getItemCount() {
        return mAddressList.size();
    }

    class MyOrderInfoViewHolder extends RecyclerView.ViewHolder {
        TextView addressTv;
        TextView phoneTv;

        public MyOrderInfoViewHolder(View itemView) {
            super(itemView);
            addressTv = (TextView) itemView.findViewById(R.id.order_address_tv);
            phoneTv = (TextView) itemView.findViewById(R.id.order_phone_tv);
        }
    }

}
