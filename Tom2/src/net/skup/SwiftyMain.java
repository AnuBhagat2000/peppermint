package net.skup;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.skup.model.Pun;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
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

public class SwiftyMain extends Activity {

	private ListView mainListView;
	private ViewGroup editableChallenge;
	private EditText editablePart;
	private TextView nonEditablePart;

	private List<Pun> puns = new ArrayList<Pun>();
	private List<Pun> challenges = new ArrayList<Pun>();
	private String substituteSubject = null;

    public static final int CHALLENGE_PUN = R.id.challenge;
	private boolean addingNew = false;
	private SwiftyAdapter adapter;
	private static final String SWTAG = "swiftys"; // outer json object key (could be timestamp in future)

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_list_view);
		mainListView = (ListView)findViewById(R.id.listview);
		editableChallenge = (ViewGroup)findViewById(R.id.editableChallenge);
		editablePart = (EditText)findViewById(R.id.editTextSubject);
		nonEditablePart = (TextView)findViewById(R.id.editTextAdverb);
		//final ListView listview = (ListView) findViewById(R.id.listview);

		/** Finished editing a Challenge. */
		editablePart.setOnKeyListener(finishedChallenge);

		// TODO async -- Strict Mode produces Network On Main exception w/out this permit all
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 


		mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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

		mainListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getApplicationContext(),
						"Click ListItem Number " + position, Toast.LENGTH_LONG)
						.show();
			}
		}); 
	}

	/*
	 * Get the latest challenges data.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		
		// Get challenges from web
		InputStream web_is = getDataWithURL("http://tom-swifty.appspot.com/challenges.json");
				//"http://tom-swifty.appspot.com/sample.json");//http://10.0.2.2:8080/sample.json");
		challenges = getData(convertToString(web_is));
	}
	
	/*
	 * Restore Settings values.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(getClass().getName()," onPause- getting saved data");

		SharedPreferences sharedPref = getPreferences(0);;//PreferenceManager.getDefaultSharedPreferences(this); bug , onpause did not read these bk in
		// GET Settings and GET user data
	    substituteSubject = sharedPref.getString(getString(R.string.substitueSubjectKey), "Tommy");
		Log.i(getClass().getName(),"substituteSubject: "+ substituteSubject);
		// GET user data from prefs (fallback to file)
        String punCache = sharedPref.getString(SWTAG, "");
        if (punCache.isEmpty()) {
    		InputStream fis = getResources().openRawResource(R.raw.sample);
    		punCache = convertToString(fis);
        }
		puns = getData(punCache);
		adapter = new SwiftyAdapter(this,  puns);
		mainListView.setAdapter(adapter);

	}
	
	/*
	 * Save edits made by user edit of my own puns, or new Settings.
	 */
	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences uiState = getPreferences(0);
		SharedPreferences.Editor editor = uiState.edit();
		
		// PUT settings and PUT user data
		editor.putString(getString(R.string.substitueSubjectKey), substituteSubject);
		editor.putString(SWTAG, jsonStringify(puns)); //save user data to SWTAG bucket in shared prefs
		
		editor.commit();
		Log.i(getClass().getName()," onPause() persisting userData(puns) and Settings (need to be quick)");
	}
	
	@Override 
	protected void onStop() {
		super.onStop();
		Log.i(getClass().getName()," onStop- saving data");

		
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
			startActivity(new Intent(this, Prefs.class));
			break;
		}
		case R.id.challenge:{
		

			startingChallenge();
			return true;
		}
		default:{
			return super.onOptionsItemSelected(item);
		}
		}
		return true;
	}



	/*
	 * 
	 * http://stackoverflow.com/questions/4841952/convert-arraylist-to-jsonarray
	 */
	private String jsonStringify(List<Pun> puns)  {

		JSONArray ja = new JSONArray();
		for (int i=0;i<puns.size();i++) {
			ja.put(puns.get(i).getJSONObject());
		}

		JSONObject row = new JSONObject();
		try {
			row.put(SWTAG, ja);
		} catch (JSONException e) {
			Log.e(getClass().getName(), e.getMessage());
		}
		return row.toString();
	}

	private List<Pun> getData(String data) {

		List<Pun> swiftys = new ArrayList<Pun>();
		try {
			JSONObject json = new JSONObject(data);
			// Getting Array of swiftys
			JSONArray s = json.getJSONArray(SWTAG);

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
	
	

	private void postData(String u, String data) {
		URL url;
		HttpURLConnection urlConnection = null;
		OutputStream out =null;

		try {
			url = new URL(u);

			urlConnection = (HttpURLConnection) url.openConnection();

			urlConnection.setDoOutput(true);
			urlConnection.setChunkedStreamingMode(0);
			urlConnection.setRequestMethod("POST");//todo 

		    out = new BufferedOutputStream(urlConnection.getOutputStream());
			out.write(data.getBytes());

			//			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			//			readStream(in);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(getClass().getName(), e.getMessage());
		}
		finally {
			try {if (out != null) out.close();} catch (IOException e) {}
			urlConnection.disconnect();
		}
	}
	
	
	// see http://androidsnippets.com/executing-a-http-post-request-with-httpclient
	/**
	 * Open a URL.
	 * http://www.vogella.com/articles/AndroidNetworking/article.html
	 */
	private InputStream getDataWithURL(String u) {
		InputStream is = null;
		HttpURLConnection con = null;

		try {
			URL url = new URL(u);
			con = (HttpURLConnection) url.openConnection();
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
				try {reader.close();
				} catch (IOException e) {}
			}
		}
		return sb.toString();
	} 
	
	/** 
	 * Action to create a challenge.
	 */
	private void startingChallenge() {
		addingNew = true;
		editableChallenge.setVisibility(View.VISIBLE);
		editableChallenge.requestFocus(); 


		// fetch a new challenge
		Pun newPun = challenges.get(new Random().nextInt(challenges.size() - 1));

		Log.i (getClass().getName(), "substitute Subject:"+substituteSubject);
        // TODO replace the placeholder in the subject line with the substituue subject. For this to work, need raw data to have SUBJ buried in it.
		String subjectPart = newPun.getAdverb();
		subjectPart = subjectPart.replace(getString(R.string.substitueSubjectKey), substituteSubject);
		
		editableChallenge.setTag(CHALLENGE_PUN, newPun);// attach the fully defined pun for use after finished editing
		nonEditablePart.setText(newPun.getAdverb());
		editablePart.setText(newPun.getStmt());
	}
	
	private OnKeyListener finishedChallenge = new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN)
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					cancelAdd();

					Pun finishedPun = (Pun)editableChallenge.getTag(CHALLENGE_PUN);
					finishedPun.setStmt(editablePart.getText().toString());
				    puns.add(0, finishedPun);
				    editablePart.setText("");
					adapter.notifyDataSetChanged();
					return true; 
				}
			return false;
		}
	};
	
}
