package com.example.calculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends Activity {

	TextView detailsText = null;
	String detailsFromCaller = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_activity);

		Intent intent = getIntent();
		if (intent != null && intent.getExtras() != null) {
			detailsFromCaller = intent.getExtras().getString(SmithCalcMainActivity.DETAILS_KEY);
		} else {
			throw new RuntimeException("data not receieved from calling activity");
		}
		detailsText = (TextView)findViewById(R.id.resultDetailsTextView);
		detailsText.setText(detailsFromCaller);
	}

	public void gobackButtonOnClick(View view) {
		Log.i("in result activity","");
		finish();
	}
	
	public void onClickShowPrevious(View view) {
		String s =  SmithCalcMainActivity.getPrevFromLocalOrPref(getPreferences(MODE_PRIVATE), detailsFromCaller);
		Toast toast = Toast.makeText(getApplicationContext(), "Previous caluclation: "+s, Toast.LENGTH_LONG); 
		toast.show();
	}
}
