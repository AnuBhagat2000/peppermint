package com.examples.todolist;

import java.util.Random;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * This is the app widget's Broadcast Receiver. Note it implements receive()
 * This is your UI for your widget.  If your widget's UI includes a UI compnent with a listener
 * this is where you might attach the listener (if you have a configurator for your widget you
 * would attach the listener in the configurator.
 * 
 * A BroadcastReceiver must finish his onReceive() method within 5 secs.


 * All long running operations in these methods should be performed in a service, as the execution time for a broadcast receiver is limited. Using asynchronous processing in the onReceive() method does not help as the system can kill the broadcast process after his onReceive() method.
 *
 */
public class MyExampleAppWidgetProvider extends AppWidgetProvider 
{
	private static final String tag = "skupWidgetProvider";
	
	
	/*
	 * * This is called to update the App Widget at intervals defined by the updatePeriodMillis attribute in the
 *  AppWidgetProviderInfo . This method *may be* called when the user *adds* the App Widget, so it should
 *   perform the essential setup, such as define event handlers for (if your widget has a view) Views
 *    and start a temporary Service, if necessary. 
 *    
 *    However, if you have declared a configuration Activity,
 *     this method is *not* called when the user adds the App Widget, but is called for the subsequent updates. 
 *     It is the responsibility of the configuration Activity to perform the first update when configuration
 *      is done. 
	 * 
	 * The most important AppWidgetProvider callback is onUpdate() because it is called when each 
	 * App Widget is added to a host (unless you use a configuration Activity). If your App Widget 
	 * accepts any user interaction events, then you need to register the event handlers 
	 * in this callback. If your App Widget doesn't create temporary files or databases,
	 *  or perform other work that requires clean-up, then onUpdate() may be the only callback method you need to define
	 *  
	 *  
	 *  A Widget gets its data on a periodic timetable. There are two methods to update a widget, one is based on an XML configuration file and the other is based on the Android AlarmManager service.

In the widget configuration file you can specify a fixed update interval. The system will wake up after this time interval and call your broadcast receiver to update the widget. The smallest update interval is 1800000 milliseconds (30 minutes).

The AlarmManager allows you to be more resource efficient and to have a higher frequency of updates. To use this approach you define a service and schedule this service via the AlarmManager regularly. This service updates the widget.


	 */
	@Override
    public void onUpdate(Context context, 
                        AppWidgetManager appWidgetManager, 
                        int[] appWidgetIds) 
   {
        Log.d(tag, "onUpdate called");
        final int N = appWidgetIds.length;
        Log.d(tag, "Number of widgets:" + N);
        for (int i=0; i<N; i++)     {
            int appWidgetId = appWidgetIds[i];
            //biz logic
            Log.d(tag, "updateAppWidget in the configurator:" );
//        	//get my view hierarchy
//        	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);
//        	Random random = new Random();
//        	views.setTextViewText(R.id.cntDisplay, Integer.toString(random.nextInt()));

            MyExampleAppWidgetConfigure.updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    public void onDeleted(Context context, int[] appWidgetIds)  {
        Log.d(tag, "onDelete called");
     
    }
    
    /*
     * Your BroadcastReceiver typically extends the AppWidgetProvider class.

The AppWidgetProvider class implements the onReceive() method, extracts the required information and calls the following widget lifecycle methods.
This is called for every broadcast and before each of the above callback methods. You normally don't need to implement this method because the default 
AppWidgetProvider implementation filters all App Widget broadcasts and calls the above methods as appropriate.

     */
    @Override 
    public void onReceive(Context context, Intent intent) 
    { 
        final String action = intent.getAction(); 
    	Log.i(this.getClass().getName()," onReceive... broadcast provider,action:"+action);

    	// for some reason this author wants to do stuff upon deleted.  Else the other msgs are delegated to super.
    	// The other msgs are APPWIDGET_ENABLED and _UPDATE
        if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action))  {
        	Log.i(this.getClass().getName(),"in on receive on the Widget Provider...");
        	Bundle extras = intent.getExtras();
        	
            final int appWidgetId = extras.getInt 
                              (AppWidgetManager.EXTRA_APPWIDGET_ID, 
                               AppWidgetManager.INVALID_APPWIDGET_ID);
            
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) 
            { 
                this.onDeleted(context, new int[] { appWidgetId }); 
            } 
        } 
        else { 
            super.onReceive(context, intent); 
        } 
    }
    public void onEnabled(Context context)   {
        Log.d(tag, "onEnabled called");
    	//first widget comes
    }

    public void onDisabled(Context context)   {
        //when last widget leaves
    }

//    private void updateAppWidget(Context context, 
//                          AppWidgetManager appWidgetManager,
//                           int appWidgetId)   {
//    	//bis logic
//   }
}

