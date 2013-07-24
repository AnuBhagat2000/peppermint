package com.example.calculator;

import java.util.Locale;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

	public static final String DETAILS_KEY = "details";
	public static final String NO_CALC_EVER_PERFORMED = "No details because no calcualation yet.";
	public static String DETAILS_NOT_YET_PERSISTED = "No details because not yet persisted";

	private String details = NO_CALC_EVER_PERFORMED;

	private static final int ANSWER_NOTIFICATION_ID = 99;
	//private static NotificationCompat.Builder answerNotificationBuilder = null;
	private NotificationManager mNotificationManager = null;
	private int numMessages = 0;
	//private TaskStackBuilder stackBuilder = null;

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
		operatorView.setTag(R.id.buttonAdd); 
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
		displayAnsw.setText("");
	}

	public void onClickDetails(View view) {
		Intent resultIntent = new Intent(this, ResultActivity.class);
		resultIntent.putExtra(DETAILS_KEY, details);
		details = NO_CALC_EVER_PERFORMED;
		startActivity(resultIntent);//this is wrong?what if activity is already started
	}

	
	public void onClickShowPrevious(View view) {
		
		String s = getPrevFromLocalOrPref(getPreferences(MODE_PRIVATE), details);
		Toast toast = Toast.makeText(getApplicationContext(), "Previous calculation: "+s,  Toast.LENGTH_LONG); 
		toast.show();
	}
	
    public void onClickFactorial(View view) {
    	Intent i = new Intent(this,FactorialActivity.class);
		startActivityForResult(i, 1);
	}

    public void  onClickBindingFactorial(View view) {
    	Intent i = new Intent(this,FactorialBindingActivity.class);
		startActivityForResult(i, 111);
	}
    
    @Override
    public void onActivityResult(int request, int resultCode,
    		Intent data) {
    	if ((request == 1) && (resultCode == RESULT_OK)) {
                displayAnsw.setText(Double.toString(
                		data.getExtras().getDouble("result")));

        }
    	
    	if ((request == 111) && (resultCode == RESULT_OK)) {
            displayAnsw.setText(Double.toString(
            		data.getExtras().getDouble("result")));

    }
    	
    }
	/**
	 *  Return local cache if a calculation has been done.
     *  Return Preferences if a prefs have been saved from a previous user session. 
	 *  else
	 *  Return "Never Persisted"
	 * @param sharedPreferences 
	 */
	public static String getPrevFromLocalOrPref(SharedPreferences sharedPreferences, String localCache) {
		
		if ( ! localCache.equals(NO_CALC_EVER_PERFORMED)) {
			return localCache;
		} 
		assert localCache.equals(NO_CALC_EVER_PERFORMED);
		return sharedPreferences.getString(DETAILS_KEY, DETAILS_NOT_YET_PERSISTED);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (details.equals(NO_CALC_EVER_PERFORMED)) return;

		SharedPreferences uiState = getPreferences(0);
		// Get the preferences editor.
		SharedPreferences.Editor editor = uiState.edit();

		// Add the UI state preference values.
		editor.putString(DETAILS_KEY, details);
		editor.commit();
		Log.i("onPause","persisting "+details);
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

		StringBuilder sb = null;
		double res = seminal;
		switch ((Integer)operatorView.getTag()){
		case R.id.buttonAdd:{
			res = leftDouble + rDouble;
			sb = new StringBuilder("adding "+leftText+" plus "+rightText+" to get ");
			break;
		}
		case R.id.buttonSubtract:{
			res = leftDouble - rDouble;
			sb = new StringBuilder("subtracting "+leftText+" minus "+rightText+" to get ");
			break;
		}
		case R.id.buttonMultiply:{
			res = leftDouble * rDouble;
			sb = new StringBuilder("multiplying "+leftText+" time "+rightText+" to get ");
			break;
		}
		case R.id.buttonDivide:{
			res = leftDouble / rDouble;
			sb = new StringBuilder("dividing "+leftText+" by "+rightText+" to get ");
			break;
		}
		default: {
			throw new RuntimeException("invalid ID");
		}
		}
		String numericResult = String.valueOf(res);
		details = sb.toString() + numericResult;
		displayAnsw.setText(numericResult);
		//displayStatusBarNotification(details);
	}

	/**
	 * may 14th assignment, smith
	 * Construct a notification using a non-changing base part and a secondary update part.
	 * The numMessages counts the number of updates.
	 * 
	 * If a (backward compatible) builder is not yet instantiated, make one.
	 * @see http://developer.android.com/guide/topics/ui/notifiers/notifications.html
	 * @param result - the calculation result, this is the business data
	 * 
	 */
	//private void displayStatusBarNotification(String result) {

//		// Lazily Instantiate the notification 
//		if (answerNotificationBuilder == null) {
//
//			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//			stackBuilder = TaskStackBuilder.create(this);
//
//			answerNotificationBuilder = new NotificationCompat.Builder(this)
//			.setSmallIcon(R.drawable.pumpcloseg)
//			.setContentTitle("Did Calc")
//			.setTicker("open drawer for details... ")
//			;
//			numMessages = 0;
//
//			applyBigViewStyle(answerNotificationBuilder, result);
//
//			// Creates an explicit intent for an Activity in your app
//			Intent resultIntent = new Intent(this, ResultActivity.class);
//
//			// The stack builder object will contain an artificial back stack for the
//			// started Activity.
//			// This ensures that navigating backward from the Activity leads out of
//			// your application to the Home screen.
//			stackBuilder = TaskStackBuilder.create(this);
//			// Adds the back stack for the Intent (but not the Intent itself)
//			stackBuilder.addParentStack(ResultActivity.class);
//			// Adds the Intent that starts the Activity to the top of the stack
//			stackBuilder.addNextIntent(resultIntent);
//
//			// Make a Wrapped Intent
//			// A PendingIntent simply wraps a regular Intent inside an object such that another 
//			// application can send the original Intent as if your application had done so.
//			PendingIntent resultPendingIntent =
//					stackBuilder.getPendingIntent(
//							0,
//							PendingIntent.FLAG_UPDATE_CURRENT
//							);
//			answerNotificationBuilder.setContentIntent(resultPendingIntent);
//		}
//
//		//		Notifications remain visible until one of the following happens:
//		//			The user dismisses the notification either individually or by using "Clear All" (if the notification can be cleared).
//		//			The user clicks the notification, and you called setAutoCancel() when you created the notification.
//		//			You call cancel() for a specific notification ID. This method also deletes ongoing notifications.
//		//			You call cancelAll(), which removes all of the notifications you previously issued.
//		mNotificationManager.cancel(ANSWER_NOTIFICATION_ID);
//
//		applyBigViewStyle(answerNotificationBuilder, result);
//
//
//		// update the non-base secondary part
//		numMessages += 1;
//
//		String s = String.format(Locale.US,"statusBar notification stack count: %d numMessages: %d calculatorAnswer: %s\n ",
//				stackBuilder.getIntentCount(), numMessages, result);
//
//		answerNotificationBuilder
//		.setContentText(s)
//		.setNumber(numMessages);
//		Log.i("display statusBar notification", s);
//
//
//		// Notify the user
//		// Because the ID remains unchanged, the existing notification is updated.
//		mNotificationManager.notify(ANSWER_NOTIFICATION_ID, answerNotificationBuilder.build());
	//}

	/**
	 * Applying a big view style to a notification

       When you need to issue a notification multiple times for the same type of event, you should avoid making a completely new notification. Instead, you should consider updating a previous notification, either by changing some of its values or by adding to it, or both.

       For example, Gmail notifies the user that new emails have arrived by increasing its count of unread messages and by adding a summary of each email to the notification. This is called "stacking" the notification; it's described in more detail in the Notifications Design guide.

       Note: This Gmail feature requires the "inbox" big view style, which is part of the expanded notification feature available starting in Android 4.1.

       To have a notification appear in a big view when it's expanded, first create a NotificationCompat.Builder object with the normal view options you want. Next, call Builder.setStyle() with a big view style object as its argument.

       Remember that expanded notifications are not available on platforms prior to Android 4.1. To learn how to handle notifications for Android 4.1 and for earlier platforms, read the section Handling compatibility.

	 * @param answerNotificationBuilder2
	 */
//	private void applyBigViewStyle(Builder notBuilder, String businessData) {
//		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//
//		String[] events = { businessData};
//		// Sets a title for the Inbox style big view
//		inboxStyle.setBigContentTitle("Details:");
//
//		// Moves business data into the big view
//		for (int i=0; i < events.length; i++) {
//
//			inboxStyle.addLine(events[i]);
//		}
//		// Moves the big view style object into the notification object.
//		notBuilder.setStyle(inboxStyle);
//	}

}
