package com.byacht.superbdeliver.Receiver;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.byacht.superbdeliver.Utils.AmapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dn on 2017/9/9.
 */

public class PhoneCallReceiver extends BroadcastReceiver {
    private Context context;
    private List<String> mPhoneNumberList;
    private int mCurrentIndex = 1;
    private int mCurrentCallState = TelephonyManager.CALL_STATE_IDLE;
    private int mLastCallState = TelephonyManager.CALL_STATE_IDLE;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        System.out.println("action" + intent.getAction());
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        mCurrentCallState = tm.getCallState();
        if (mCurrentCallState == TelephonyManager.CALL_STATE_IDLE && mLastCallState == TelephonyManager.CALL_STATE_OFFHOOK && mPhoneNumberList.size() > mCurrentIndex) {
            Intent dialIntent = new Intent(Intent.ACTION_CALL);
            dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialIntent.setData(Uri.parse("tel:" + mPhoneNumberList.get(mCurrentIndex)));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            context.startActivity(dialIntent);
            Log.d("htout", "currentCallState=" + mCurrentCallState + " lastCallState:" + mLastCallState);
        }
        Log.d("htout", "currentCallState:" + mCurrentCallState);
        if (mLastCallState == TelephonyManager.CALL_STATE_OFFHOOK && mCurrentCallState == TelephonyManager.CALL_STATE_IDLE) {
            if (mPhoneNumberList.size() == mCurrentIndex) {
                isCalling.setCalling(false);
            }
            mCurrentIndex++;
        }
        mLastCallState = mCurrentCallState;

        //去电
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d("htout", "call OUT:" + phoneNumber);
        }
    }

    public interface OnCalling {
        void setCalling(boolean isCalling);
    }

    public void setPhoneNumberList(List<String> numberList) {
        mPhoneNumberList = numberList;
        mCurrentIndex = 1;
    }

    public void setCurrentIndex(int index) {
        this.mCurrentIndex = index;
    }

    private OnCalling isCalling;

    public void setOnCallingListener(OnCalling isCalling) {
        this.isCalling = isCalling;
    }

}
