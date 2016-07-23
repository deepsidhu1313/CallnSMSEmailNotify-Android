package com.example.callnsmsnotify;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class AutoStartUp extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

		Intent intent = new Intent(AutoStartUp.this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		// do something when the service is created
	}



}