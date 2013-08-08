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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SwiftyMain extends Activity implements OnItemSelectedListener {

	private ListView mainListView;
	private ViewGroup editableChallenge;
	private Spinner editablePart;
	private TextView nonEditablePart;

	private List<Pun> puns = new ArrayList<Pun>();
	private List<Pun> challenges = new ArrayList<Pun>();

    public static final int CHALLENGE_PUN = R.id.challenge;
	private boolean addingNew = false;
	private SwiftyAdapter adapter;
	private static final String SWTAG = "swiftys"; // outer json object key (could be timestamp in future)
	private static final String SpinnerSentinal = "Select One (or nothing to Cancel)";
	private static final String challengesURL = "http://tom-swifty.appspot.com/challenges.json";
    private String dropdownSelection = null;
	private SharedPreferences myDefaultSP = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		myDefaultSP = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.main_list_view);
		mainListView = (ListView)findViewById(R.id.listview);
		editableChallenge = (ViewGroup)findViewById(R.id.editableChallenge);
		editablePart = (Spinner)findViewById(R.id.challengesSpinner);
		nonEditablePart = (TextView)findViewById(R.id.editTextAdverb);
		// If newly installed, and Preference Activity was not run by the user, then the XML defaults won't be
		// available.  Thus copy defaults from the XML definition to the PreferenceManager. 
		PreferenceManager.setDefaultValues(this, R.xml.userpreferences, false);
		   
		//editablePart.setOnKeyListener(finishedChallenge);
		editablePart.setOnItemSelectedListener(this);

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

	/** Get the latest challenges data. */
	@Override
	protected void onStart() {
		super.onStart();
		new DownloadFilesTask(this).execute(new String[] {challengesURL});
	}
	
	/** Restore user data, with a fallback to the sample data.*/
	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedPref = getPreferences(0);
		// GET user data
	    //String gettUponResume = sharedPref.getString(getString(R.string.substitueSubjectKey), "Tommyxx");
		//Log.i(getClass().getName(),"onRESUME : gettUponResume: "+ gettUponResume);
		// GET user data from prefs (fallback to file)
        String punCache = sharedPref.getString(SWTAG, "");
        if (punCache.isEmpty()) {
    		InputStream fis = getResources().openRawResource(R.raw.sample);
    		punCache = convertToString(fis);
        }
		puns = getData(punCache);
		adapter = new SwiftyAdapter(this,  puns);
		mainListView.setAdapter(adapter);
		Log.i(getClass().getName()," onResume() getting saved Puns");

	}
	
	/** Save edits. Needs to be quick. */
	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences uiState = getPreferences(0);
		SharedPreferences.Editor editor = uiState.edit();
		//editor.putString(getString(R.string.substitueSubjectKey), substituteSubject);// PreferencesActivity already handles saving its own.
		editor.putString(SWTAG, jsonStringify(puns)); 
		editor.commit();
		Log.i(getClass().getName(),"onPause() persisting Puns");
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
	 * Stringify a Pun list.
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

	/** Convert serialized Json to data. */
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
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(getClass().getName(), e.getMessage());
		}
		finally {
			try {if (out != null) out.close();} catch (IOException e) {}
			urlConnection.disconnect();
		}
	}
	

	/**
	 * Converts an input stream to a String.
	 * http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
	 * @return the string
	 */
	public static String convertToString(InputStream in) {

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
	
    /** Start a challenge edit. */
	private void startingChallenge() {
		addingNew = true;
		editableChallenge.setVisibility(View.VISIBLE);
		editableChallenge.requestFocus(); 
		// fetch a new challenge and put it in textedit area. 
		Pun newPun = challenges.get(new Random().nextInt(challenges.size() - 1));
		editableChallenge.setTag(CHALLENGE_PUN, newPun);// attach the fully defined pun for use after finished editing
		nonEditablePart.setText(newPun.getAdverb());
		//editablePart.setText(newPun.getStmt());
	}

	/** Callback from remote fetch.*/
	public void setChallenges(String puns) {
		challenges = getData(puns);
		String [] allChallengeStmts = new String[challenges.size()+1];
		allChallengeStmts[0] = SpinnerSentinal;//http://stackoverflow.com/questions/867518/how-to-make-an-android-spinner-with-initial-text-select-one
		for (int i=0;i<challenges.size(); i++) {
			allChallengeStmts[i+1] = challenges.get(i).getStmt();
		}
		ArrayAdapter<String> challengesAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, allChallengeStmts);
		editablePart.setAdapter(challengesAdapter);
		challengesAdapter.notifyDataSetChanged();
	}

	/** Finished editing Challenge.*/
	//http://stackoverflow.com/questions/8321251/why-onnothingselected-is-not-called
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.challengesSpinner: {
			dropdownSelection = (String) parent.getItemAtPosition(position);
			if (dropdownSelection.startsWith(SpinnerSentinal)) {
				Log.i ("cancel Challenge", "");
				cancelAdd();
			} else {
				Log.i(this.getClass().getCanonicalName(),"selected challenged editable part");
				cancelAdd();
				Pun finishedPun = (Pun)editableChallenge.getTag(CHALLENGE_PUN);
				editableChallenge.setTag(null);//clear cache
				finishedPun.setStmt(dropdownSelection);
				String defaultIfJustInstalled = getString(R.string.defaultSubject);
			    String subject2 = myDefaultSP.getString(getString(R.string.substitueSubjectKey), defaultIfJustInstalled);

				finishedPun.setAuthor(subject2);
				finishedPun.setCreated(Pun.NOW);
			    puns.add(0, finishedPun);
				adapter.notifyDataSetChanged();
			}
			break;
		}
		default: {
			assert false: "Invalid View id.";
		break;
		}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		assert false :"todo";
	}
}
