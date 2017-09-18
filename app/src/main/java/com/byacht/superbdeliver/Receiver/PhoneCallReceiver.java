package com.byacht.superbdeliver.Receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
    private int flag = 0;
    private int flag2 = 0;
    private Context context;
    public List<String> phoneNumberList;
    private int currentIndex = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
//        phoneNumberList = intent.getStringArrayListExtra("phoneNumberList");
        System.out.println("action"+intent.getAction());
        //去电
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d("htout", "call OUT:" + phoneNumber);
            Log.d("htout", "flag:" + flag + " flag2:" + flag2);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }else{
            //查了下android文档，貌似没有专门用于接收来电的action,所以，非去电即来电.
            //如果我们想要监听电话的拨打状况，需要这么几步 :
            /*
            *第一：获取电话服务管理器TelephonyManager manager = this.getSystemService(TELEPHONY_SERVICE);
            * 第二：通过TelephonyManager注册我们要监听的电话状态改变事件。manager.listen(new MyPhoneStateListener(),
                    * PhoneStateListener.LISTEN_CALL_STATE);这里的PhoneStateListener.LISTEN_CALL_STATE就是我们想要
                    * 监听的状态改变事件，初次之外，还有很多其他事件哦。
            * 第三步：通过extends PhoneStateListener来定制自己的规则。将其对象传递给第二步作为参数。
            * 第四步：这一步很重要，那就是给应用添加权限。android.permission.READ_PHONE_STATE
            */
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    // 设置一个监听器
    PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // 注意，方法必须写在super方法后面，否则incomingNumber无法获取到值。
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("htout", "空闲状态" + " flag:" + flag + " flag2:" + flag2);
                    if (flag > 0 && flag2 > 0) {
                        flag = 0;
                        flag2 = 0;
                        Log.d("htout", "拨打下一个电话");
                        if (currentIndex < phoneNumberList.size()) {
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("tel:" + phoneNumberList.get(currentIndex)));
                            context.startActivity(intent);
                            currentIndex++;
                        } else {
                            callOver.setCallOver(true);
                        }
                    }
                    if (flag > 0 && flag2 == 0) {
                        flag2++;
                        Log.d("htout", "拨打电话 flag2:" + flag2);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d("htout", "拨打或接听");
                    flag ++;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d("htout", "来电号码:" + incomingNumber);
                    break;
            }
        }
    };

    public interface CallOver {
        void setCallOver(boolean isCallOver);
    }

    private CallOver callOver;

    public void setCallOverListener(CallOver callOver) {
        this.callOver = callOver;
    }

}
