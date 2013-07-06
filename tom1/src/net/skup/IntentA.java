package net.skup;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class IntentA extends Activity {

	public void gobackButtonOnClick(View src) {
		//      Intent i = new Intent(this, IntentActionDemo.class);
		//      startActivity(i);
		//       The above adds to the stack. Using finish instead.
		Log.i("in sync activity","");
		finish();
	}
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sync_view);
    }
}
