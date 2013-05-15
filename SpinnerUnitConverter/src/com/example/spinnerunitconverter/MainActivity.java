package com.example.spinnerunitconverter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity  implements OnItemSelectedListener{
	Spinner fromUnitSpinner, toUnitSpinner, unitTypeSpinner;
	ArrayAdapter<CharSequence> temperatureAdapter, weightAdapter;
	TextView answerTextField;
	Context context = null;
	EditText input = null;

	static private final double  lbsToKG = 0.453592; 
	static private final double  kgToPounds = 2.20462;
	static private final double  fToCoffset = 32;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		unitTypeSpinner = (Spinner)findViewById(R.id.spinnerUnitType);
		fromUnitSpinner = (Spinner) findViewById(R.id.fromUnit);
		toUnitSpinner = (Spinner) findViewById(R.id.toUnit);
		answerTextField = (TextView)findViewById(R.id.answer);
		input = (EditText)findViewById(R.id.inputValue);

		unitTypeSpinner.setOnItemSelectedListener(this);
		fromUnitSpinner.setOnItemSelectedListener(this);
		toUnitSpinner.setOnItemSelectedListener(this);

		ArrayAdapter<CharSequence> unitTypeAdapter = ArrayAdapter.createFromResource(this,
				R.array.unitTypes, android.R.layout.simple_spinner_item);
		unitTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		unitTypeSpinner.setAdapter(unitTypeAdapter);

		temperatureAdapter = ArrayAdapter.createFromResource(this, R.array.temperatures, android.R.layout.simple_spinner_item);
		temperatureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		weightAdapter = ArrayAdapter.createFromResource(this, R.array.weights, android.R.layout.simple_spinner_item);
		weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		context = new Context(Context.UnitContext.temperatures, Context.Temperatures.degC.ordinal(), Context.Temperatures.degF.ordinal());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** GO 
	 * //	Celsius to Fahrenheit (°C × (9/5)) + 32 = °F
	 * //	Fahrenheit to Celsius 	(°F - 32) * (5/9) = °C
	 * 
	 * */
	public void onClickGoButton(View v){

		StringBuilder sb = new StringBuilder(input.getText().toString());
		double value = 0;
		try {
			value = Double.parseDouble(input.getText().toString());
		} catch (Exception ec) {
			input.setText(R.string.zero);
		}
		//Log.i("value text field:",input.getText().toString());

		switch (context.unit) {
		case temperatures:{

			if (context.from == Context.Temperatures.degC.ordinal()) {
				sb.append(" degree C = ");
				sb.append(Double.toString((value * 1.8) + fToCoffset));
				sb.append(" degree F");

			} else {
				sb.append(" degree F = ");
				sb.append(Double.toString((value - fToCoffset) * 0.5555));
				sb.append(" degree C");
			}
			break;
		}
		case weights:{
			if (context.from == Context.Weights.kg.ordinal()){
				sb.append(" kg = ");
				sb.append(Double.toString(value * kgToPounds));
				sb.append(" pounds");
			} else {
				sb.append(" pounds = ");
				sb.append(Double.toString(value * lbsToKG));
				sb.append(" kg");
			}
			break;
		}
		}
		answerTextField.setText(sb.toString());
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {


		switch (parent.getId()) {
		case R.id.spinnerUnitType:{

			String selected = (String) parent.getItemAtPosition(position);
			if (selected.startsWith("weight")) {
				context = new Context(Context.UnitContext.weights, 0, 1);
				fromUnitSpinner.setAdapter(weightAdapter);
				toUnitSpinner.setAdapter(weightAdapter);
			} else {
				context = new Context(Context.UnitContext.temperatures, 0, 1);
				fromUnitSpinner.setAdapter(temperatureAdapter);
				toUnitSpinner.setAdapter(temperatureAdapter);
			}
			fromUnitSpinner.setSelection(0, true);
			toUnitSpinner.setSelection(1, true);
			onClickGoButton(null);
			break;
		}
		case R.id.fromUnit: {

			if (context == null) return;
			toUnitSpinner.setSelection( fromUnitSpinner.getSelectedItemPosition()==0?1:0, true); //toggle
			context = new Context(context.unit, fromUnitSpinner.getSelectedItemPosition(), toUnitSpinner.getSelectedItemPosition());
			onClickGoButton(null);
			break;
		}
		case R.id.toUnit: {

			if (context == null) return;
			fromUnitSpinner.setSelection(toUnitSpinner.getSelectedItemPosition()==0?1:0, true); //toggle
			context = new Context(context.unit, fromUnitSpinner.getSelectedItemPosition(), toUnitSpinner.getSelectedItemPosition());
			onClickGoButton(null);
			break;
		}
		default: {
			raiseAlertDialog("Invalid View id.");
			break;
		}
		}
	}		

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

	private void raiseAlertDialog(String msg){

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog dialog = null;

		builder.setMessage(msg)
		.setPositiveButton("OK",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog = builder.create();
		dialog.show();
	}

	private static final class Context {

		UnitContext unit = UnitContext.unset;
		int from;
		int to;
		public  enum UnitContext {temperatures, weights, unset};
		public  enum Weights {kg,pounds};
		public  enum Temperatures {degC, degF};

		public Context (UnitContext c, int from, int to){
			unit = c;
			this.from = from;
			this.to = to;
		}
		
		@Override
		public  String toString() {
			return unit.name()+" From:"+Integer.toString(from)+" To:"+Integer.toString(to);
		}
	}
}
