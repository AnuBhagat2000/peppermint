package com.example.calculator;

import java.util.Locale;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SmithCalcMainActivity extends Activity {

	private static final double seminal = -999.99;
	private TextView operatorView = null;
	private TextView left = null;
	private TextView right = null;
	private TextView displayAnsw = null;

	private static final int ANSWER_NOTIFICATION_ID = 99;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		operatorView = (TextView) findViewById(R.id.operatorView);
		left = (TextView) findViewById(R.id.leftOperand);
		right = (TextView) findViewById(R.id.rightOperand);
		displayAnsw = (TextView) findViewById(R.id.displayAnswer);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		operatorView.setTag(0); 
		return true;
	}

	/** Clear.*/
	public void onClear(View view) {
		left.setText("");
		right.setText("");
		displayAnsw.setText("");
	}

	/** Copies from the operator button to the operator readonly view.*/
	public void onClickOperator(View view) {
		final Button button = (Button) findViewById(view.getId());
		String buttonText = String.valueOf(button.getText());
		operatorView.setText(buttonText);
		operatorView.setTag(button.getId()); // tag the operator
	}

	/** Go */
	public void onClickAnswer(View view) {

		String leftText = String.valueOf(left.getText());
		String rightText = String.valueOf(right.getText());
		double leftDouble = seminal;
		double rDouble = seminal;

		try {
			leftDouble = Double.parseDouble(leftText);
		} catch (Exception e) {
			onClear(view);
			CharSequence c = String.format(Locale.US,"Left operand could not be converted.");
			Toast.makeText(getApplicationContext(), c, Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			rDouble = Double.parseDouble(rightText);
		} catch (Exception e) {
			onClear(view);
			CharSequence c = String.format("Right operand could not be converted - ensure it is a number.");
			Toast.makeText(getApplicationContext(), c, Toast.LENGTH_SHORT).show();
			return;
		}

		double res = seminal;
		switch ((Integer)operatorView.getTag()){
		case R.id.buttonAdd:{
			res = leftDouble + rDouble;
			break;
		}
		case R.id.buttonSubtract:{
			res = leftDouble - rDouble;
			break;
		}
		case R.id.buttonMultiply:{
			res = leftDouble * rDouble;
			break;
		}
		case R.id.buttonDivide:{
			res = leftDouble / rDouble;
			break;
		}
		default: {
			break;
		}
		}
		String result = String.valueOf(res);
		displayAnsw.setText(result);
		displayStatusBarNotification(result);
	}

	private void displayStatusBarNotification(String result) {
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Instantiate the notification "Did CalcNotification"
		//http://mobile.tutsplus.com/tutorials/android/android-fundamentals-status-bar-notifications/
		Notification didCalcNotification = new Notification();
		didCalcNotification.icon = R.drawable.pumpcloseg;
		didCalcNotification.tickerText = "Caluclating....the... answer.... holdon";
		didCalcNotification.when = System.currentTimeMillis();



		// Instantiate the Wrapped Intent
		// A PendingIntent simply wraps a regular Intent inside an object such that another 
		//application can send the original Intent as if your application had done so.

		Context context = getApplicationContext();// TODO where is this method defined
		
		
		Intent didCalcIntent = new Intent(context, ResultActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, didCalcIntent, 0);//why is this called content intent?
			
		//Step 4: Preparing the Notification Event
		didCalcNotification.setLatestEventInfo(context, "contentTitle",  "contentText", contentIntent);
		
		// Notify the user
		mNotificationManager.notify(ANSWER_NOTIFICATION_ID, didCalcNotification);

	}
}
