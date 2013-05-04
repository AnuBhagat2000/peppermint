package com.example.calculator;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SmithCalcMainActivity extends Activity {

	private static final double seminal = -99999.999;
	private static final String converter = "%.3f";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		TextView operatorView = (TextView) findViewById(R.id.operatorView);
		operatorView.setTag(0); 
		return true;
	}

	/** Copies from the operator button to the operator readonly view.*/
	public void onClickOperator(View view) {
		TextView operatorView = (TextView) findViewById(R.id.operatorView);
		final Button button = (Button) findViewById(view.getId());
		String buttonText = String.valueOf(button.getText());
		operatorView.setText(buttonText);
		operatorView.setTag(button.getId()); // save the operator
	}

	public void onClickAnswer(View view) {
		final TextView left = (TextView) findViewById(R.id.leftOperand);
		final TextView right = (TextView) findViewById(R.id.rightOperand);
		final TextView displayAnsw = (TextView) findViewById(R.id.displayAnswer);

		String leftText = String.valueOf(left.getText());
		String rightText = String.valueOf(right.getText());
		
		double leftDouble = seminal;
		double rDouble = seminal;
		
		try {
			leftDouble = Double.parseDouble(leftText);
		} catch (Exception e) {
			CharSequence c = String.format(Locale.US,"Left operand could not be converted.");
			Toast.makeText(getApplicationContext(), c, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		try {
			rDouble = Double.parseDouble(rightText);
		} catch (Exception e) {
			CharSequence c = String.format("Right operand could not be converted - ensure it is a number.");
			Toast.makeText(getApplicationContext(), c, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		
		double res = seminal;
		TextView operatorView = (TextView) findViewById(R.id.operatorView);
		
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
	

		displayAnsw.setText(String.valueOf(res));

	}

	
}
