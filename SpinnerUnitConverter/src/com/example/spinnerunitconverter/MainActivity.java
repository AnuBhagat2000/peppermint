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
import android.widget.Spinner;
import android.widget.TextView;

import com.example.spinnerunitconverter.MainActivity.Context.UnitContext;

public class MainActivity extends Activity  implements OnItemSelectedListener{
	Spinner fromUnitSpinner, toUnitSpinner, unitTypeSpinner;
	ArrayAdapter<CharSequence> temperatureAdapter, weightAdapter;
	TextView answerTextField;
	Context context = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		unitTypeSpinner = (Spinner)findViewById(R.id.spinnerUnitType);
		fromUnitSpinner = (Spinner) findViewById(R.id.fromUnit);
		toUnitSpinner = (Spinner) findViewById(R.id.toUnit);
		answerTextField = (TextView)findViewById(R.id.answer);


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

		context = new Context();
		context.unit = Context.UnitContext.temp;
		context.from = Context.Unit.degC;
		context.to = Context.Unit.degF;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** GO */
	public void onClickGoButton(View v){
		Log.i("onClickGoButton","onClickGoButton");	
		answerTextField.setText("answ"+Math.random());
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
		
		Log.i("onItemSel", "top onItemSelected");

		switch (parent.getId()) {
		case R.id.spinnerUnitType:{
			Log.i("onItemSel", "");
			String selected = (String) parent.getItemAtPosition(position);
			if (selected.startsWith("weight")) {
				Log.i("onItemSel", "selected weight. ");
				fromUnitSpinner.setAdapter(weightAdapter);
				toUnitSpinner.setAdapter(weightAdapter);

			} else {
				Log.i("onItemSel", "selected temp. ");

				fromUnitSpinner.setAdapter(temperatureAdapter);
				toUnitSpinner.setAdapter(temperatureAdapter);

			}
			fromUnitSpinner.setSelection(0, true);
			toUnitSpinner.setSelection(0, true);
			break;
		}
		case R.id.fromUnit: {
			if (context == null) return;
				if (context.unit == UnitContext.temp) {
					Log.i("from unit selected:",(String)fromUnitSpinner.getSelectedItem()); // c or f degrees
					toUnitSpinner.setSelection( ~ fromUnitSpinner.getSelectedItemPosition() , true);
				} else {
					Log.i("from unit selected:",(String)fromUnitSpinner.getSelectedItem()); //  kg or pounds
					toUnitSpinner.setSelection( ~ fromUnitSpinner.getSelectedItemPosition() , true);
				}
			break;
		}
		default: {
			raiseAlertDialog("Invalid View id.");
            break;
		}
		}

		if (parent.getId() == R.id.spinnerUnitType) {
			Log.i("onItemSel", "");
			String selected = (String) parent.getItemAtPosition(position);
			if (selected.startsWith("weight")) {
				Log.i("onItemSel", "selected weight. ");
				fromUnitSpinner.setAdapter(weightAdapter);
				toUnitSpinner.setAdapter(weightAdapter);

			} else {
				Log.i("onItemSel", "selected temp. ");

				fromUnitSpinner.setAdapter(temperatureAdapter);
				toUnitSpinner.setAdapter(temperatureAdapter);

			}
		} else {
			if (parent.getId() == R.id.fromUnit) {

				if (context != null) {
					if (context.unit == UnitContext.temp) {
						
						Log.i("from unit selected:",(String)fromUnitSpinner.getSelectedItem()); // c or f degrees
						toUnitSpinner.setSelection( ~ fromUnitSpinner.getSelectedItemPosition() , true);
					} else {
						Log.i("from unit selected:",(String)fromUnitSpinner.getSelectedItem()); //  kg or pounds
						toUnitSpinner.setSelection( ~ fromUnitSpinner.getSelectedItemPosition() , true);
					}
				} else {
					raiseAlertDialog("Choose a unit first.");
				}
			} else if (parent.getId() == R.id.toUnit) {
			}
			else {
				throw new RuntimeException ("invalid ID");

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

	static final class Context {
		public  enum UnitContext {wt, temp, unset};
		public  enum Unit {pounds, kg, degF, degC, unset};

		UnitContext unit = UnitContext.unset;
		Unit from = Unit.unset;
		Unit to = Unit.unset;



	}

}
