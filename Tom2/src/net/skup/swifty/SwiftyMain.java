package net.skup.swifty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.skup.R;
import net.skup.swifty.model.ChallengesProvider;
import net.skup.swifty.model.ChallengesProvider.ChallengeBlock;
import net.skup.swifty.model.Pun;
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
import android.widget.Toast;

public class SwiftyMain extends Activity implements OnItemSelectedListener {

	private ListView mainListView;
	private ViewGroup editableChallenge;
	private Spinner editablePart;
	private TextView nonEditablePart;
	private List<Pun> puns = new ArrayList<Pun>();
	private static final String SENTINAL = "Select One (or nothing to Cancel)";
    public static final int CHALLENGE_PUN = R.id.challenge;
	private boolean addingNew = false;
	private SwiftyAdapter adapter;
    private String dropdownSelection = null;
	private SharedPreferences myDefaultSP = null;
	private Object mActionMode;
	private int selectedItem = -1;
	
	/* Contextual Action Bar (CAB) is the visual for Contextual Action Mode. It overlays the action bar. 
	 * http://www.vogella.com/articles/AndroidListView/article.html#listview_actionbar
	 */
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.rowselection, menu);
			return true;
		}
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; 
		}
		@Override
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
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			selectedItem = -1;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		myDefaultSP = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.main_list_view);
		mainListView = (ListView)findViewById(R.id.listview);
		editableChallenge = (ViewGroup)findViewById(R.id.editableChallenge);
		editablePart = (Spinner)findViewById(R.id.challengesSpinner);
		nonEditablePart = (TextView)findViewById(R.id.nonEditableChallenge);
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
    		punCache = Pun.convertToString(fis);
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

	//// business logic 

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

	
	private void deleteSwifty(final int index) {
		mainListView.animate().setDuration(2000).alpha(0).withEndAction(new Runnable() {
			@Override
			public void run() {
				puns.remove(index);
				adapter.notifyDataSetChanged();
				mainListView.setAlpha(1);
			}
		});
	}
	
	private void cancelAdd() {
		addingNew = false;
		//editableChallenge.setVisibility(View.GONE);
	}

	/** Start a challenge edit. */
	private void startingChallenge() {

		ChallengeBlock b = ChallengesProvider.getInstance().getChallenge(3, SENTINAL);
		if (b.candidates.size() == 0) {
			Toast.makeText(getApplicationContext(), "No challenges available.", Toast.LENGTH_LONG).show();
			return;
		}
		addingNew = true;
		editableChallenge.setVisibility(View.VISIBLE);
		editableChallenge.requestFocus();

		Pun newPun = b.pun;
		editableChallenge.setTag(CHALLENGE_PUN, newPun);// attach the fully defined pun for use after finished editing
		nonEditablePart.setText(newPun.getStmt());
		ArrayAdapter<String> challengesAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, b.candidates);
		editablePart.setAdapter(challengesAdapter);
		challengesAdapter.notifyDataSetChanged();
	}


	/** Finished editing Challenge.*/
	//http://stackoverflow.com/questions/8321251/why-onnothingselected-is-not-called
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Log.i(this.getClass().getCanonicalName(),"onItemSelected () ...sel:"+position);

		switch (parent.getId()) {
		case R.id.challengesSpinner: {
			dropdownSelection = (String) parent.getItemAtPosition(position);
			if (dropdownSelection.startsWith(SENTINAL)) {
				Log.i (getClass().getName(),"sel=0 cancel Challenge....");
				cancelAdd();
			} else {

				Pun finishedPun = (Pun)editableChallenge.getTag(CHALLENGE_PUN);
				if (finishedPun == null) {
					// weird spinner event upon initialization
					//http://stackoverflow.com/questions/5624825/spinner-onitemselected-executes-when-it-is-not-suppose-to/5918177#5918177
					Log.i(this.getClass().getCanonicalName(),"onItemSelected () ignored....");
                    return;
				}
				Log.i(this.getClass().getCanonicalName(),"onItemSelected () selected challenged editable part");
				cancelAdd();
				editableChallenge.setTag(null);//clear cache
				finishedPun.setAdverb(dropdownSelection);
				//chString defaultIfJustInstalled = getString(R.string.defaultSubject);
			    //String author = myDefaultSP.getString(getString(R.string.substitueSubjectKey), defaultIfJustInstalled); if EditText

				finishedPun.setAuthor(finishedPun.getAuthor());
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
	
}
