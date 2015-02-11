package org.mosquitto.android.powerviewer;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import org.mosquitto.android.mqttdemo.R;
import org.mosquitto.android.powerviewer.MQTTService;

public class PowerViewer extends Activity
{
    private TextView mText = null;
	private StatusUpdateReceiver statusUpdateIntentReceiver;
	private MQTTMessageReceiver messageIntentReceiver;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mText = (TextView)findViewById(R.id.text);

		SharedPreferences settings = getSharedPreferences(MQTTService.APP_ID, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("broker", "oojah.dyndns.org");
		editor.putString("topic", "sensors/cc128/ch1");
		editor.commit();

		statusUpdateIntentReceiver = new StatusUpdateReceiver();
		IntentFilter intentSFilter = new IntentFilter(MQTTService.MQTT_STATUS_INTENT);
		registerReceiver(statusUpdateIntentReceiver, intentSFilter);

		messageIntentReceiver = new MQTTMessageReceiver();
		IntentFilter intentCFilter = new IntentFilter(MQTTService.MQTT_MSG_RECEIVED_INTENT);
		registerReceiver(messageIntentReceiver, intentCFilter);

		Intent svc = new Intent(this, MQTTService.class);
		startService(svc);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(mText != null){
			WindowManager w = getWindowManager();
            Display d = w.getDefaultDisplay();
            
            //mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, (float)(mText.getMeasuredWidth()/6));
            mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, d.getWidth()/5);
            //mText.setText(Integer.toString(d.getWidth()));
        }
    }

	public class StatusUpdateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle notificationData = intent.getExtras();
			String newStatus = notificationData.getString(MQTTService.MQTT_STATUS_MSG);
			// ...
		}
	}

	public class MQTTMessageReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle notificationData = intent.getExtras();
			String newTopic = notificationData.getString(MQTTService.MQTT_MSG_RECEIVED_TOPIC);
			String newData = notificationData.getString(MQTTService.MQTT_MSG_RECEIVED_MSG);
			// ...
			mText.setText(newData);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Intent svc = new Intent(this, MQTTService.class);
		stopService(svc);

		unregisterReceiver(statusUpdateIntentReceiver);
		unregisterReceiver(messageIntentReceiver);
	}
/*
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus){
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			mNotificationManager.cancel(MQTTService.MQTT_NOTIFICATION_UPDATE);
		}
	}
*/
}
