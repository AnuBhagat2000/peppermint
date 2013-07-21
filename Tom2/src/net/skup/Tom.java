package net.skup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.skup.model.Pun;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Tom extends Activity {

	private ListView mainListView;
	private ViewGroup editableChallenge;
	private EditText editablePart;
	private TextView nonEditablePart;

	private List<Pun> puns ;
    public static final int CHALLENGE_PUN = R.id.challenge;
	boolean sortByLaughSetting = true; //vs sort by groan
	public static final String sortByLaugh = "sortByLaugh";
	private boolean addingNew = false;
	private SwiftyAdapter adapter;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_list_view);
		mainListView = (ListView)findViewById(R.id.listview);
		editableChallenge = (ViewGroup)findViewById(R.id.editableChallenge);
		editablePart = (EditText)findViewById(R.id.editTextSubject);
		nonEditablePart = (TextView)findViewById(R.id.editTextAdverb);


		/** Finished editing a Challenge. */
		editablePart.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						cancelAdd();

						Pun finishedPun = (Pun)editableChallenge.getTag(CHALLENGE_PUN);
						if (finishedPun == null) {
							finishedPun = new Pun("a","b","c");
						}
						finishedPun.setStmt(editablePart.getText().toString());
					    puns.add(0, finishedPun);
					    editablePart.setText("");
						adapter.notifyDataSetChanged();
						return true; 
					}
				return false;
			}
		});

		// TODO async
		StrictMode.ThreadPolicy policy = new StrictMode.
				ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 


		InputStream is = getDataWithURL("http://tom-swifty.appspot.com/sample.json");//http://10.0.2.2:8080/sample.json");
		if (true/*is == null*/) {
			// Could not open sample.json get FileNotFound... cannot be opened is prob a compressed file
			//http://thedevelopersinfo.com/2009/11/27/using-files-as-raw-resources-in-android/
			//http://stackoverflow.com/questions/6186866/java-io-filenotfoundexception-this-file-can-not-be-opened-as-a-file-descriptor
						InputStream itt = getDataWithFile("raw/sample.json");
						if (itt == null) {
							Log.e(this.getClass().getName(),"could not read from file...");
						}

		}
		puns = getData(convertToString(is));
		final ListView listview = (ListView) findViewById(R.id.listview);
		adapter = new SwiftyAdapter(this,  puns);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final String item = (String) parent.getItemAtPosition(position);
				view.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
					@Override
					public void run() {
						puns.remove(item);
						adapter.notifyDataSetChanged();
						view.setAlpha(1);
					}
				});
			}

		});

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getApplicationContext(),
						"Click ListItem Number " + position, Toast.LENGTH_LONG)
						.show();
			}
		}); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		int index = mainListView.getSelectedItemPosition();

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:{
			sortByLaughSetting = !sortByLaughSetting;
			startActivity(new Intent(this, Prefs.class));
			break;
		}
		case R.id.challenge:{
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			String substituteSubject = sharedPref.getString(getString(R.string.substitueSubjectKey), "Tommy");
			Log.i("substituteSubject", substituteSubject);

			addChallenge();
			return true;
		}
		default:{
			return super.onOptionsItemSelected(item);
		}
		}
		return true;
	}

	@Override
	protected void onPause() {

		super.onPause();
		SharedPreferences uiState = getPreferences(0);
		SharedPreferences.Editor editor = uiState.edit();
		editor.putBoolean(sortByLaugh, sortByLaughSetting);
		editor.commit();
		Log.i("onPause","persisting "+sortByLaugh+ " "+sortByLaughSetting);
	}

	private List<Pun> getData(String data) {

		List<Pun> swiftys = new ArrayList<Pun>();
		try {
			JSONObject json = new JSONObject(data);

			// Getting Array of swiftys
			JSONArray s = json.getJSONArray("swiftys");

			for (int i = 0; i < s.length(); i++){
				JSONObject c = s.getJSONObject(i);
				Pun p = new Pun( c.getString(Pun.T.created.name()), c.getString(Pun.T.author.name()),
						c.getString(Pun.T.stmt.name()),c.getString(Pun.T.adverb.name()),c.getString(Pun.T.subject.name()));
				swiftys.add(p);
			}
			Log.i(this.getClass().getName(),  "Number of entries " + swiftys.size());
		} catch (Exception e) {
			Log.e(this.getClass().getName(),"Could not parse json."+ e.getMessage());
		}	
		return swiftys;
	}

	private void cancelAdd() {
		addingNew = false;
		editableChallenge.setVisibility(View.GONE);
	}
	
	

	private void removeItem(int _index) {
		puns.remove(_index);
		adapter.notifyDataSetChanged();  
	}
	
	
	private InputStream getDataWithFile(String fname) {
		InputStream iss = null;
		AssetFileDescriptor descriptor;

		try {
			descriptor = getAssets().openFd(fname);
			iss =	descriptor.createInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return iss;
	}

	/**
	 * Open a URL.
	 * http://www.vogella.com/articles/AndroidNetworking/article.html
	 */
	private InputStream getDataWithURL(String u) {
		InputStream is = null;

		try {
			URL url = new URL(u);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			is = con.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	/**
	 * Converts an input stream to a String.
	 * http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
	 * @return the string
	 */
	private String convertToString(InputStream in) {

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	} 
	
	/** 
	 * Action to create a challenge.
	 */
	private void addChallenge() {
		addingNew = true;
		editableChallenge.setVisibility(View.VISIBLE);
		editableChallenge.requestFocus(); 
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String substituteSubject = sharedPref.getString(getString(R.string.substitueSubjectKey), "Tommy2");
		String subj = getString(R.string.substitueSubjectKey)+ " gushed";
		subj = subj.replace(getString(R.string.substitueSubjectKey), substituteSubject);
		//fetch a new pun from somewhere
		Pun newPun = new Pun("		", subj, substituteSubject);
		editableChallenge.setTag(CHALLENGE_PUN, newPun);// attach the fully defined pun for use after finished editing
		nonEditablePart.setText(newPun.getAdverb());
		editablePart.setText(newPun.getStmt());
	}
}
