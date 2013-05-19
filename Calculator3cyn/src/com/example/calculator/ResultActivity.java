package com.example.calculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends Activity implements OnClickListener{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_layout );
        
        Intent a = getIntent();
        String s = a.getExtras().getString("Result");
        TextView resultText = (TextView)findViewById(R.id.result);
        resultText.setText(s);
        Button goBackButton= (Button)findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(this);
    }
    
	@Override
	public void onClick(View arg0) {
		//Intent i = new Intent(this, ToDoList.class);
		//startActivity(i);
		finish();
	}

}
