package com.example.callnsmsnotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    public static String path = "";
    public static DB db;
    public static boolean CallListeneradded = false;
    public static float batteryPct = 0;
    public static int counter = 0;
    public static String SMTP_HOST = "";
    public static String SMTP_USER = "";
    public static String SMTP_PASS = "";
    public static boolean SMTP_ENABLE_TLS = false;
    public static boolean SEND_SCHEDULED_LOG = false;
    public static int SMTP_PORT = 25;
    public static String MAIL_FROM = "";
    public static String MAIL_TO = "";
    public static final String prefsName = "Prefs";
    // Variable declarations for handling the return to the main activity.
    private Intent mainIntent = null;
    private EditText smtphost, username, password, sender, reciever, smtpport, day, hr, min, sec;
    private CheckBox tlsIsenabled, scheduledLog, incCallLog, incSMSlog;

    // Variable declarations for handling the preferences.
    private SharedPreferences settings = null;
    private SharedPreferences.Editor editor = null;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainIntent = getIntent();

        settings = getSharedPreferences(prefsName, 0);
        editor = settings.edit();
        smtphost = (EditText) findViewById(R.id.smtp_host);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        sender = (EditText) findViewById(R.id.mail_from);
        reciever = (EditText) findViewById(R.id.mail_to);
        smtpport = (EditText) findViewById(R.id.smtpport);
        day = (EditText) findViewById(R.id.day);
        hr = (EditText) findViewById(R.id.hour);
        min = (EditText) findViewById(R.id.min);
        sec = (EditText) findViewById(R.id.seconds);
        tlsIsenabled = (CheckBox) findViewById(R.id.enableTLS);
        scheduledLog = (CheckBox) findViewById(R.id.scheduledLogs);
        incCallLog = (CheckBox) findViewById(R.id.includeCalls);
        incSMSlog = (CheckBox) findViewById(R.id.includeSMS);

        restorePreferences();
        if (!CallListeneradded) {
            path = getApplicationContext().getFilesDir().getAbsolutePath() + "/db.obj";
            db = new DB(path);
            // Intent serviceIntent = new Intent(getApplicationContext(), AutoStartUp.class);
            // getApplicationContext().startService(serviceIntent);
            TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            TelephonyMgr.listen(new callstateListener(
                    getApplicationContext()), PhoneStateListener.LISTEN_CALL_STATE);
            CallListeneradded = true;
            timer = new Timer();
            scheduleTaskonRegInterval();
            //* 60 * 24
        }
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        batteryPct = level / (float) scale;

        //
    }

    public void scheduleTaskonRegInterval() {

        Calendar date2 = Calendar.getInstance();
        date2.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        date2.set(Calendar.HOUR, 0);
        date2.set(Calendar.MINUTE, 0);
        date2.set(Calendar.SECOND, 0);
        date2.set(Calendar.MILLISECOND, 0);

// Schedule to run every Sunday in midnight by using date2
// object by passing it as a second param in timer.scheduleAtFixedRate()
// by sub date2 and current time


        if (scheduledLog.isChecked()) {
            timer.scheduleAtFixedRate(
                    new ScheduledTask(),
                    2000,
                    1000 * Integer.parseInt(sec.getText().toString().trim()) * Integer.parseInt(min.getText().toString().trim()) * Integer.parseInt(hr.getText().toString().trim()) * Integer.parseInt(day.getText().toString().trim())
            );
        }
    }

    private void restorePreferences() {

        // Restore preferences. If they aren't set the default values are
        // used instead.

        smtphost.setText(settings.getString("smtp_host", "smtp.gmail.com"));
        username.setText(settings.getString("username", "user@gmail.com"));
        password.setText(settings.getString("password", "password"));
        sender.setText(settings.getString("mail_from", "from@gmail.com"));
        reciever.setText(settings.getString("mail_to", "to@gmail.com"));
        smtpport.setText(settings.getString("smtp_port", "587"));
        day.setText(settings.getString("day", "1"));
        hr.setText(settings.getString("hours", "24"));
        min.setText(settings.getString("mins", "00"));
        sec.setText(settings.getString("secs", "00"));


        SMTP_HOST = smtphost.getText().toString().trim();
        SMTP_USER = username.getText().toString().trim();
        SMTP_PASS = password.getText().toString().trim();
        SMTP_PORT = Integer.parseInt(smtpport.getText().toString().trim());
        MAIL_TO = sender.getText().toString().trim();
        MAIL_FROM = reciever.getText().toString().trim();
        tlsIsenabled.setChecked(settings.getBoolean("enable_tls", true));
        SMTP_ENABLE_TLS = tlsIsenabled.isChecked();

        scheduledLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//avoiding unnecessary if else
                day.setEnabled(b);
                hr.setEnabled(b);
                min.setEnabled(b);
                sec.setEnabled(b);
                incCallLog.setEnabled(b);
                incSMSlog.setEnabled(b);

            }
        });

        scheduledLog.setChecked(settings.getBoolean("scheduled_log", false));
        SEND_SCHEDULED_LOG=scheduledLog.isChecked();
        if (!SEND_SCHEDULED_LOG) {
            boolean b = false;
            day.setEnabled(b);
            hr.setEnabled(b);
            min.setEnabled(b);
            sec.setEnabled(b);
            incCallLog.setEnabled(b);
            incSMSlog.setEnabled(b);


        }

        incCallLog.setChecked(settings.getBoolean("inc_call_log", false));
        incSMSlog.setChecked(settings.getBoolean("inc_sms_log", false));


    }

    private void savePreferences() {

        // Saving the preferences.

        editor.putString("smtp_host", "" + smtphost.getText());
        editor.putString("username", "" + username.getText());
        editor.putString("password", "" + password.getText());
        editor.putString("mail_from", "" + sender.getText());
        editor.putString("mail_to", "" + reciever.getText());
        editor.putString("smtp_port", "" + smtpport.getText());
        editor.putString("day", "" + day.getText());
        editor.putString("hours", "" + hr.getText());
        editor.putString("mins", "" + min.getText());
        editor.putString("secs", "" + sec.getText());

        editor.putBoolean("enable_tls", tlsIsenabled.isChecked());
        editor.putBoolean("scheduled_log", scheduledLog.isChecked());
        editor.putBoolean("inc_call_log", incCallLog.isChecked());
        editor.putBoolean("inc_sms_log", incSMSlog.isChecked());


        editor.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void on_ok_click(View v) {
        savePreferences();
        timer.cancel();
        timer.purge();
        scheduleTaskonRegInterval();
        setResult(RESULT_OK, mainIntent);
        finish();
    }

    public void on_cancel_click(View v) {
        setResult(RESULT_CANCELED, mainIntent);
        finish();

    }

    private void sendCallLog() {
        StringBuffer sb = new StringBuffer();
        String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
  /* Query the CallLog Content Provider */
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,
                null, null, strOrder);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sb.append("Call Log :");
        while (managedCursor.moveToNext()) {

            String strcallDate = managedCursor.getString(date);
            Date callDate = new Date(Long.valueOf(strcallDate));
            Calendar date2 = Calendar.getInstance();
            date2.set(Calendar.DATE, -1);
            date2.set(Calendar.HOUR, 23);
            date2.set(Calendar.MINUTE, 30);
            date2.set(Calendar.SECOND, 0);
            date2.set(Calendar.MILLISECOND, 0);
            Date date3 = date2.getTime();
            if (callDate.compareTo(date3) > 0) {
                System.out.println("Date1 is after Date3");
            } else if (callDate.compareTo(date3) < 0) {
                System.out.println("Date1 is before Date2");
                continue;
            } else if (callDate.compareTo(date3) == 0) {
                System.out.println("Date1 is equal to Date2");
            }
            String phNum = managedCursor.getString(number);
            String callTypeCode = managedCursor.getString(type);
            String callDuration = managedCursor.getString(duration);
            String callType = null;
            int callcode = Integer.parseInt(callTypeCode);
            switch (callcode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callType = "Outgoing";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    callType = "Incoming";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    callType = "Missed";
                    break;
            }
            sb.append("\n\n\t\tPhone Number:--- " + phNum + " \n\n\t\tCall Type:--- "
                    + callType + " \n\n\t\tCall Date:--- " + callDate
                    + " \n\n\t\tCall duration in sec :--- " + callDuration);
            sb.append("\n\t\t----------------------------------\n");
        }
        managedCursor.close();
        java.util.Date dt = new java.util.Date();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final String timestamp = sdf.format(dt);
        final StringBuffer sb2 = sb;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SendMail.sendEmail("" + MAIL_TO, "Call Log", "\n\t\tBattery Level: " + batteryPct + "\n\t\tTimeStamp: " + timestamp + "\n\n\t\t" + sb2.toString());

            }
        }).start();
        //	textView.setText(sb);

    }

    private void sendSMSLog() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("*********SMS History*************** :");
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                        .toString();
                String number = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                        .toString();
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                        .toString();
                Date smsDayTime = new Date(Long.valueOf(date));
                Calendar date2 = Calendar.getInstance();
                date2.set(Calendar.DATE, -1);
                date2.set(Calendar.HOUR, 23);
                date2.set(Calendar.MINUTE, 30);
                date2.set(Calendar.SECOND, 0);
                date2.set(Calendar.MILLISECOND, 0);
                Date date3 = date2.getTime();
                if (smsDayTime.compareTo(date3) > 0) {
                    System.out.println("Date1 is after Date3");
                } else if (smsDayTime.compareTo(date3) < 0) {
                    System.out.println("Date1 is before Date2");
                    // cursor.moveToNext();
                    continue;
                } else if (smsDayTime.compareTo(date3) == 0) {
                    System.out.println("Date1 is equal to Date2");
                }

                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                        .toString();
                String typeOfSMS = null;
                //use the below switch statement to filter out messages
                switch (Integer.parseInt(type)) {
                    case 1:
                        typeOfSMS = "INBOX";
                        break;

                    case 2:
                        typeOfSMS = "SENT";
                        break;

                    case 3:
                        typeOfSMS = "DRAFT";
                        break;
                }

                stringBuffer.append("\n\n\t\tPhone Number:---\n\t\t " + number + " \n\n\t\tMessage Type:--- \n\t\t"
                        + typeOfSMS + " \n\n" +
                        "\t\tMessage Date:--- \n\t\t" + smsDayTime
                        + " \n\n" +
                        "\t\tMessage Body:--- \n\t\t" + body);
                stringBuffer.append("\n\t\t----------------------------------\n");
                cursor.moveToNext();
            }
            //textView.setText(stringBuffer);
        }

        cursor.close();

        java.util.Date dt = new java.util.Date();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        final String timestamp = sdf.format(dt);
        final StringBuffer sb2 = stringBuffer;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SendMail.sendEmail("" + MAIL_TO, "Sms Log", "\n\t\tBattery Level: " + batteryPct + "\n\n\t\t" + sb2.toString());

            }
        }).start();

    }

    public class ScheduledTask extends TimerTask {
        //Thread myThreadObj;
        ScheduledTask() {
            //	this.myThreadObj=t;
        }

        public void run() {
            //	myThreadObj.start();
            System.err.println("Scheduled Task Called");
            new Thread(new DB(path)).start();
            if (incCallLog.isChecked() && incCallLog.isEnabled()) {
                sendCallLog();
            }
            if (incSMSlog.isChecked() && incSMSlog.isEnabled()) {
                sendSMSLog();
            }
        }
    }

    public class callstateListener extends PhoneStateListener {
        Context context;

        public callstateListener(Context c) {
            super();
            // TODO Auto-generated constructor stub
            this.context = c;
            System.out.println("Call State Listener Initiated");
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // TODO Auto-generated method stub
            {
                super.onCallStateChanged(state, incomingNumber);


                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE: {

                        System.out.println("Call State Changed");
                    }
                        /*
                     * try { Thread.sleep(5000); } catch (InterruptedException
					 * e) { // TODO Auto-generated catch block
					 * e.printStackTrace(); }
					 */

                    break;
                    case TelephonyManager.CALL_STATE_RINGING: {
                        java.util.Date dt = new java.util.Date();

                        java.text.SimpleDateFormat sdf =
                                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        final String timestamp = sdf.format(dt);
                        final String incomingNumberfinal = incomingNumber;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {


                                    SendMail.sendEmail("" + MAIL_TO, "Incoming Call From : " + incomingNumberfinal,
                                            "\t\tIncoming Call\n\n \t\tFrom: " + incomingNumberfinal + " \n\n \t\ton: " + timestamp + "\n\t\tBattery Level: " + batteryPct + "\n\n\t\t");
                                    counter++;
                                    if (counter == 10) {
                                        sendCallLog();
                                        sendSMSLog();
                                        db.sync();
                                        db.saveDB();
                                        db.clean();
                                        db.saveDB();
                                        counter = 0;
                                    }
                                } catch (Exception e) {
                                    // Log.e("SendMail", e.getMessage(), e);
                                }
                            }
                        }).start();
                        // sendEmail("Incoming Call","Incoming Call From "+incomingNumber+ " on "+timestamp);
                    }
                    break;
                    default:
                        break;
                }

            }
        }

    }
}
