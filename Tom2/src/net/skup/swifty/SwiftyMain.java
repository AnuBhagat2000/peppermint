package net.skup.swifty;

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

import net.skup.R;
import net.skup.swifty.model.ChallengesProvider;
import net.skup.swifty.model.Pun;
import net.skup.swifty.model.ChallengesProvider.ChallengeBlock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
	private static final String SpinnerSentinal = "Select One (or nothing to Cancel)";
    private String dropdownSelection = null;
	private SharedPreferences myDefaultSP = null;
	private Object mActionMode;
	private int selectedItem = -1;
	
	/* Contextual Action Bar (CAB) is the visual for Contextual Action Mode. It overlays the action bar. 
	 * http://www.vogella.com/articles/AndroidListView/article.html#listview_actionbar
	 */
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			// Assumes that you have "contexual.xml" menu resources
			inflater.inflate(R.menu.rowselection, menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menuitem1_show:
				deleteSwifty(selectedItem);
				mode.finish();// Action picked, so close the CAB
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			selectedItem = -1;
		}
	};

	private void deleteSwifty(final int index) {
		//		final Pun pun = (Pun) mainListView.getItemAtPosition(selectedItem);
		//		puns.remove(pun);
		mainListView.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
			@Override
			public void run() {
				puns.remove(index);
				adapter.notifyDataSetChanged();
				mainListView.setAlpha(1);
			}
		});
	}
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

		//http://www.vogella.com/articles/AndroidListView/article.html#listview_actionbar
		mainListView.setOnItemLongClickListener(new OnItemLongClickListener() {

		      @Override
		      public boolean onItemLongClick(AdapterView<?> parent, View view,  int position, long id) {

		        if (mActionMode != null) {
		          return false;
		        }
		        selectedItem = position;

		        // Start the CAB using the ActionMode.Callback defined above
		        mActionMode = SwiftyMain.this.startActionMode(mActionModeCallback);
		        view.setSelected(true);
		        return true;
		      }
		    });
	}

	/** Get the latest challenges data. */
	@Override
	protected void onStart() {
		super.onStart();
		ChallengesProvider.getInstance().fetch(100);
	}
	
	/** Restore user data, with a fallback to the sample data.*/
	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedPref = getPreferences(0);
		// GET user data from prefs (fallback to file)
        String punCache = sharedPref.getString(Pun.SWTAG, "");
        if (punCache.isEmpty()) {
    		InputStream fis = getResources().openRawResource(R.raw.sample);
    		punCache = convertToString(fis);
        }
		puns = Pun.deserializeJson(punCache);
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
		editor.putString(Pun.SWTAG, Pun.jsonStringify(puns)); 
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
		case R.id.helpMenuItem:{
			startActivity(new Intent(this,HelpActivity.class));
			return true;
		}
		default:{
			return super.onOptionsItemSelected(item);
		}
		}
		return true;
	}

	private void cancelAdd() {
		addingNew = false;
		editableChallenge.setVisibility(View.GONE);
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
		private void startingChallenge2() {
			addingNew = true;
			editableChallenge.setVisibility(View.VISIBLE);
			editableChallenge.requestFocus(); 
			ChallengeBlock b = ChallengesProvider.getInstance().getChallenge(7);
			Pun newPun = b.pun;
			editableChallenge.setTag(CHALLENGE_PUN, newPun);// attach the fully defined pun for use after finished editing
			nonEditablePart.setText(newPun.getStmt());
			// prepare list of adverbs
			ArrayAdapter<String> challengesAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, b.candidates);
			editablePart.setAdapter(challengesAdapter);
			challengesAdapter.notifyDataSetChanged();
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
	}

	/** Callback from remote fetch.*/
	public void setChallenges(String puns) {
		challenges = Pun.deserializeJson(puns);
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
			    ChallengesProvider.getInstance().disqualify(finishedPun.getCreatedTimeSeconds()); 
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
	


// Various ways to offer  a delete. What teacher did, he adds menuitem to the Options Menu when there is at least
// one menu item. .... but this didn't work for me...

//	/** 
//	 * If list size > 0 include a delete option menu item. 
//	 */
//	 @Override
//	  public boolean onPrepareOptionsMenu(Menu menu) {
//	    super.onPrepareOptionsMenu(menu);
//
//	    int idx = mainListView.getSelectedItemPosition();
//	    MenuItem removeSwiftyMenuItem = menu.findItem(R.id.removeMenuItem);
//	    removeSwiftyMenuItem.setTitle("Delete Swifty");
//	    removeSwiftyMenuItem.setVisible(idx > -1);
//
//	    return true;
//	  }
//	  
//	  @Override
//	  public void onCreateContextMenu(ContextMenu menu,  View v,  ContextMenu.ContextMenuInfo menuInfo) {
//	    super.onCreateContextMenu(menu, v, menuInfo);
//
//	    menu.setHeaderTitle("header title...");
//	    menu.add(0, R.id.removeMenuItem, Menu.NONE, R.string.removeSwifty);
//	  }
}
