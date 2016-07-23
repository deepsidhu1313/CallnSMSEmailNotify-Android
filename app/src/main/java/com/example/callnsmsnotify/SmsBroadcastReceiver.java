package com.example.callnsmsnotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                final String smsBody = smsMessage.getMessageBody().toString();
                final String address = smsMessage.getOriginatingAddress();

                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody + "\n";
                java.util.Date dt = new java.util.Date();

                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                final String timestamp = sdf.format(dt);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SendMail.sendEmail("" + MainActivity.MAIL_TO, "SMS From: " + address, "\n\n\t\t" + smsBody + "\n\n\t\t Timestamp: " + timestamp + "\n\n\t\tBattery Level: " + MainActivity.batteryPct + "\n\n\t\t");

                    }
                }).start();
            }
            Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            //this will update the UI with message
            //  SmsActivity inst = SmsActivity.instance();
            //  inst.updateList(smsMessageStr);
        }
    }
}