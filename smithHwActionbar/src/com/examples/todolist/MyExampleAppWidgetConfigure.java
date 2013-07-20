package com.examples.todolist;

import java.util.Random;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class MyExampleAppWidgetConfigure extends Activity  {
	private static String tag = "ConfigureBDayWidgetActivity";
	private static Random random = new Random(100);
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	/** Called when the activity is first created.
	 * This pattern taken from ExampleAppWidgetConfigure in API Demos
	 *  */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if they press the back button.
		setResult(RESULT_CANCELED);


		// Set the view layout resource to use.
		setContentView(R.layout.cfg_widget);

		// Find the widget id from the intent. We will return this to the App Widget Host.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt( AppWidgetManager.EXTRA_APPWIDGET_ID,  AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}
	}

	/** Copies from the operator button to the operator readonly view.*/
	public void onClickGoButton(View view) {

		final  Context context = MyExampleAppWidgetConfigure.this;

		// IN this section, which is common to both configure and the app widget proper -- we do the update AND notify widget manager..
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		MyExampleAppWidgetConfigure.updateAppWidget(context, appWidgetManager, mAppWidgetId);
		
		// Send back intent result.
		// Make sure we pass back the original appWidgetId
		Intent resultValue = new Intent();
		resultValue.putExtra(
				AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();

	}

	/** Common to both Cfg and Widget -- this is the main business view update.  When your widget has a configurator, your widget provider's update method
	 *  and this method will typically have the same business logic (thus we offer common code.
	 *  
	 * @param mAppWidgetId2 - passing this in from a non static method. It is needed to notify the App Widget Manager.
	 * */
	public static void updateAppWidget(Context context,   AppWidgetManager appWidgetManager, int mAppWidgetId2)   {

		//biz logic
		Log.d(tag, "updateAppWidget in the configurator:" );
		
	    // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);
        int todoCount = ToDoList.aa.getCount(); //?? how to get reference to my app? from the bundle?
    	views.setTextViewText(R.id.cntDisplay, Integer.toString(random.nextInt()));

		// Tell the widget provider we have an update 
		appWidgetManager.updateAppWidget(mAppWidgetId2, views);
	}
}