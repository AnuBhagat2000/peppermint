package com.example.calculator;

// Calculator Exercise (updated exercise May 14, 2013)
// Cynthia Lee-Klawender 
// May 15, 2013
//
// The App Name is MyCalculator with a green calculator icon
//
/* Exercise for which the updated version is based :
•	create a new Activity "Result Activity"
	o	Add "Go Back" button
	o	when "Go Back" button is clicked, takes back to Calculator Activity
	o	add an intent for the Answer button onClick to fire the ResultActivity 
		WITH Extra so ResultActivity could display the result
•	Use SharedPreferences to store/retrieve last calculation
	o	add a TextView to display it on any activity (NOTE: used result TextView for this)
	o	could be just the result or the whole calculation
•	Add a StatusBar notification every time a new calculation is done
	o	Preview, include calculation (he meant to display the calculation in the 
		StatusBar whenever the user clicked on the answer button)

*/
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

	private int operatorID=-1; // stores the current operator, default -1
	private TextView leftTextView;
	private TextView rightTextView;
	private TextView opTextView;
	private TextView resultView; // changed to display last calculation
	private String resultStr="";
	
	public static final String PREFS_NAME = "MyPrefsFile";
	public static final String RESULT_KEY="RESULTS";
	private static final int NOTIFY_ID = 1; // unique id to modify/cancel notification later on
	private NotificationCompat.Builder mBuilder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// assign instances of member TextViews to instance variables
		leftTextView=(TextView)findViewById(R.id.left_operand);
		rightTextView=(TextView)findViewById(R.id.right_operand);
		opTextView=(TextView)findViewById(R.id.operator);
		resultView=(TextView)findViewById(R.id.result);
		
		restoreUIState();						// FOR PART 2 OF EXERCISE
	}
	
	private void restoreUIState() {				// FOR PART 2 OF EXERCISE
	    // Get the activity preferences object.
	    SharedPreferences settings = getPreferences(0);

	    // Read the UI state values, specifying default values.
	    String text = settings.getString(RESULT_KEY, "");

	    // Display last calculation in the previous state.
	    resultView.setText("Last calculation: "+text);
	    
	    resultStr=text; // for always displaying last calculation (my choice)
	}
	  

    protected void onPause() {					// FOR PART 2 OF EXERCISE
	    super.onPause();
	    
	    // Get the activity preferences object.
	    SharedPreferences uiState = getPreferences(0);
	    // Get the preferences editor.
	    SharedPreferences.Editor editor = uiState.edit();

	    // Add the UI state preference values.
	    editor.putString(RESULT_KEY, resultStr);
	    // Commit the preferences.
	    editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// onClickOperator
	// called when the add, subtract, multiply or divide buttons are clicked
	public void onClickOperator(View view){
		String buttonText= (((Button)view).getText()).toString();
		operatorID = view.getId();  // get ID of operator button clicked

		opTextView.setText("  "+buttonText); 

		//resultView.setText("");  // "clear" the text in the result TextView
	} // end onClickOperator
	
	// onClickAnswer
	//     will be called when the "=" button (to get the answer) button is clicked
	public void onClickAnswer(View view){
		String leftStr=String.valueOf(leftTextView.getText());
		String rightStr=String.valueOf(rightTextView.getText());
		String prevResultStr= resultStr;				// added to keep last calculation
		double leftValue=0.;
		double rightValue=0.;
		
		try {
			leftValue = Double.parseDouble(leftStr);
			rightValue = Double.parseDouble(rightStr);
		}// end try
		catch(NumberFormatException nfe){
			raiseAlertDialog("Invalid Number(s)",
					"One or both of the operands have invalid numbers!").show(); // create AlertDialog
			return ;
		}
		
		resultStr=leftStr;

		switch(operatorID){
		case R.id.add: 
			resultStr+=(" + "+ rightStr+" = "+(leftValue+rightValue)); break;
		case R.id.subtract: 
			resultStr+=(" - "+ rightStr+" = "+(leftValue-rightValue)); break;
		case R.id.multiply: 
			resultStr+=(" * "+ rightStr+" = "+(leftValue*rightValue)); break;
		case R.id.divide: 
			resultStr+=(" / "+ rightStr+" = "+(leftValue/rightValue)); break;
		default: 
			// No operator chosen, so raise an AlertDialog
				raiseAlertDialog("Operator Not Chosen",
						"Press on an operator\nTHEN press on \"=\"").show(); // create AlertDialog
				resultStr= prevResultStr;  // put old result back
				return ;
		} // end switch

		resultView.setText("Last calculation: "+ prevResultStr);//related to Part 2
		
	    if( mBuilder != null )								// for PART 3 of 05-14-2013 Exercise
	    	  updateNotification();
	    else
	    	  setNotification();

	    Intent i = new Intent(this, ResultActivity.class);	// for PART 1 of 05-14-2013 Exercise
	    i.putExtra("Result", resultStr);
	    startActivity(i);
	} // end onClickOperator

	// Calls this the first time a valid answer it generated
	private void setNotification(){							// for PART 3 of 05-14-2013 Exercise
		// Instantiate the notification
		int icon = R.drawable.info;
		mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(icon)
		        .setContentTitle("My notification")
		        .setTicker(resultStr)
		        ;
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, ResultActivity.class);
	    resultIntent.putExtra("Result", resultStr);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ResultActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
	} // end setNotification
	
	// Calls updateNotification after the first time a valid answer is generated
	private void updateNotification(){							// for PART 3 of 05-14-2013 Exercise
		NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mBuilder.setTicker(resultStr); // set new result

	// Notify the user
		mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
	}
	
	// raiseAlertDialog
	//		the title and message as parameters
	//		method returns the return value of create() on the AlertBuilder
	public Dialog raiseAlertDialog(String title,
								String message){
		AlertDialog.Builder builder;
		
		builder=new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle( title );
		builder.setMessage( message );
		builder.setNeutralButton(String.valueOf("OK"),new OnClickListener() {
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.cancel();
			} // end onClick
		});
		
		return builder.create();

	} // end raiseAlertDialog
	
	
	// onClickClear
	//     will be called when the "c" (clear) button is clicked
	public void onClickClear(View view){
		leftTextView.setText("");
		rightTextView.setText("");
		opTextView.setText("");
		resultView.setText("Last calculation: "+resultStr);  //keep last calculation showing
		operatorID=-1;
		leftTextView.requestFocus();
	} // end onClickClear
}
