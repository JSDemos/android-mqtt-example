/*
 * This is an example Android Activity using Dale Lane's MQTTService and the
 * IBM Java library.
 *
 * It is a very simple activity that uses a single text label to display the
 * data coming in on an MQTT topic.
 */
package org.mosquitto.android.mqtt;

import org.mosquitto.android.mqttdemo.R;

import android.app.Activity;
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

public class MQTTDemo extends Activity {
	private TextView mText = null;
	private StatusUpdateReceiver statusUpdateIntentReceiver;
	private MQTTMessageReceiver messageIntentReceiver;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mText = (TextView) findViewById(R.id.text);

		SharedPreferences settings = getSharedPreferences(MQTTService.APP_ID, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("broker", "localhost"); // CHANGE ME to your broker
													// address
		editor.putString("topic", "hello/world"); // CHANGE ME to your topic
		editor.commit();

		statusUpdateIntentReceiver = new StatusUpdateReceiver();
		IntentFilter intentSFilter = new IntentFilter(
				MQTTService.MQTT_STATUS_INTENT);
		registerReceiver(statusUpdateIntentReceiver, intentSFilter);

		messageIntentReceiver = new MQTTMessageReceiver();
		IntentFilter intentCFilter = new IntentFilter(
				MQTTService.MQTT_MSG_RECEIVED_INTENT);
		registerReceiver(messageIntentReceiver, intentCFilter);

		Intent svc = new Intent(this, MQTTService.class);
		startService(svc);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mText != null) {
			WindowManager w = getWindowManager();
			Display d = w.getDefaultDisplay();

			/*
			 * Set text size dynamically. This assumes we're putting a text
			 * value of at most 5 characters on screen.
			 */
			mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, d.getWidth() / 5);
		}
	}

	public class StatusUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			String newStatus = notificationData
					.getString(MQTTService.MQTT_STATUS_MSG);
			// ...
		}
	}

	public class MQTTMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			/* The topic of this message. */
			String newTopic = notificationData
					.getString(MQTTService.MQTT_MSG_RECEIVED_TOPIC);
			/* The message payload. */
			String newData = notificationData
					.getString(MQTTService.MQTT_MSG_RECEIVED_MSG);

			/* Display the payload on the text label. */
			mText.setText(newData);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent svc = new Intent(this, MQTTService.class);
		stopService(svc);

		unregisterReceiver(statusUpdateIntentReceiver);
		unregisterReceiver(messageIntentReceiver);
	}
}
