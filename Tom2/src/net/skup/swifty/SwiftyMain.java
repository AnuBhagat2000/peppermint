package net.skup.swifty;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.skup.swifty.model.ChallengesProvider;
import net.skup.swifty.model.ChallengesProvider.ChallengeBlock;
import net.skup.swifty.model.Pun;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
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

public class SwiftyMain extends Activity implements OnItemSelectedListener, TextToSpeech.OnInitListener {

	private ListView mainListView;
	private ViewGroup editableChallenge;
	private Spinner editablePart;
	private TextView nonEditablePart;
	private List<Pun> puns = new ArrayList<Pun>();
	private boolean saySwiftyPref = true;
	private static final String SENTINAL = "(Select Your Adverb)";
    public static final int CHALLENGE_PUN = R.id.challenge;
	private SwiftyAdapter adapter;
    private String dropdownSelection = null;
	private Object mActionMode;
	private int longClickItem = -1;
	private int spinnerPrevSelection = -1;
	private int MY_DATA_CHECK_CODE = -99;
	private TextToSpeech mTts; 
	private SharedPreferences prefManager;

	
	/* Contextual Action Bar (CAB) is the visual for Contextual Action Mode. It overlays the action bar. 
	 * http://www.vogella.com/articles/AndroidListView/article.html#listview_actionbar
	 */
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);
			return true;
		}
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; 
		}
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.deleteMenuitem: {

				deleteSwifty(longClickItem);
				mode.finish();// Action picked, so close the CAB
				return true;
			}
			case R.id.sayitMenuitem:{

				saySwifty(longClickItem);
				mode.finish();
				return true;
			}
			case R.id.emailMenuitem:{

				emailSwifty(longClickItem); 
				mode.finish();
				return true;
			}
			default:
				return false;
			}
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			longClickItem = -1;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//myDefaultSP = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.main_list_view);
		mainListView = (ListView)findViewById(R.id.listview);
		editableChallenge = (ViewGroup)findViewById(R.id.editableChallenge);
		editablePart = (Spinner)findViewById(R.id.challengesSpinner);
		nonEditablePart = (TextView)findViewById(R.id.nonEditableChallenge);
		initPreferencesManagement();
		   
		//editablePart.setOnKeyListener(finishedChallenge);
		editablePart.setOnItemSelectedListener(this);

		//http://www.vogella.com/articles/AndroidListView/article.html#listview_actionbar
		mainListView.setOnItemLongClickListener(new OnItemLongClickListener() {

		      @Override
		      public boolean onItemLongClick(AdapterView<?> parent, View view,  int position, long id) {

		        if (mActionMode != null) {
		          return false;
		        }
		        longClickItem = position;

		        // Start the CAB using the ActionMode.Callback defined above
		        mActionMode = SwiftyMain.this.startActionMode(mActionModeCallback);
		        view.setSelected(true);
		        return true;
		      }
		    });

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	    saySwiftyPref = sharedPrefs.getBoolean("saySwiftyPref", true);
		Log.i(getClass().getSimpleName(),"sound pref:"+saySwiftyPref);

		mTts = new TextToSpeech(this, this);
		Intent checkIntent = new Intent(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA); 
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		Log.i(getClass().getSimpleName(),"sent intent for Text to speech engine");		

	}

	/**
	 * If newly installed, and Preference Activity was not run by the user, then the XML defaults won't be
	 * available.  Copy defaults from the XML definition to the PreferenceManager. 
	 * http://www.helloandroid.com/tutorials/preferenceactivity-basics
	 */
	private void initPreferencesManagement() {
		PreferenceManager.setDefaultValues(this, R.xml.userpreferences, false);
		prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	/** Get the latest challenges data. */
	@Override
	protected void onStart() {
		super.onStart();
		ChallengesProvider.getInstance(getApplicationContext()).fetch(100);
	}
	
	/** Restore user data, with a fallback to the sample data.*/
	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedPref = getPreferences(0);
        String punCache = sharedPref.getString(Pun.SWTAG, "");
		Set<String> bl = sharedPref.getStringSet("blacklist", null);
		ChallengesProvider.getInstance(getApplicationContext()).putBlacklist(bl);
		Log.i(getClass().getSimpleName()+" onResume","... retrieve... blacklist.size "+ ((bl==null) ? "null": bl.size()));
        if (punCache.isEmpty()) {
    		InputStream fis = getResources().openRawResource(R.raw.sample);
    		if (fis == null) {
        		String s = getClass().getSimpleName()+" onResume could not read sample file";
        		Log.e(getClass().getSimpleName()+" onResume",s);
        		throw new RuntimeException(s);
    		}
    		punCache = Pun.convertToString(fis);
    		Log.i(getClass().getSimpleName()+" onResume","got Puns from sample file");
        } else {
    		Log.i(getClass().getSimpleName()+" onResume","restored Puns from persistence.");
        }
		puns = Pun.deserializeJson(punCache);
		adapter = new SwiftyAdapter(this,  puns);
		mainListView.setAdapter(adapter);
	}
	
	/** Save edits. Needs to be quick. */
	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences uiState = getPreferences(0);
		SharedPreferences.Editor editor = uiState.edit();
		//editor.putString(getString(R.string.substitueSubjectKey), substituteSubject);// PreferencesActivity already handles saving its own.
		editor.putString(Pun.SWTAG, Pun.jsonStringify(puns)); 
		Set<String> bl = ChallengesProvider.getInstance(getApplicationContext()).getBlacklist();
		Log.i(getClass().getSimpleName()+" onPause","... persist... blacklist.size:"+ bl.size());
		editor.putStringSet("blacklist", bl);
		editor.commit();
	}
	
	@Override 
	protected void onStop() {
		super.onStop();
		Log.i(getClass().getSimpleName()+" onStop","onStop");
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
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

	private void saySwifty(final int index) {
		boolean  saySwiftyPref = prefManager.getBoolean(getString(R.string.ttsKey),true);
		Log.i(getClass().getSimpleName()+" saySwifty","sound enabled:"+saySwiftyPref);

		if (saySwiftyPref) {
			StringBuilder sb = new StringBuilder(puns.get(index).getStmt());
			sb.append(puns.get(index).getAdverb());
			mTts.speak(sb.toString(), TextToSpeech.QUEUE_ADD, null);//or QUUE_FLUSH
		} 
	}
	
	private void emailSwifty(final int index) {
		
		String  victim = prefManager.getString(getString(R.string.emailRecipientKey),"tom.swifty@goodmail.com");
		String  owner = prefManager.getString(getString(R.string.substitueSubjectKey),"Tommy Swift");
		StringBuilder sb = new StringBuilder(puns.get(index).getStmt());
		sb.append(" ");
		sb.append(puns.get(index).getAdverb());
		Intent email = new Intent(Intent.ACTION_SEND);
		email.setType("message/rfc822");
		email.putExtra(Intent.EXTRA_EMAIL, new String[] {victim}); 
		email.putExtra(Intent.EXTRA_SUBJECT, "a Tom Swifty from "+owner); 
		email.putExtra(Intent.EXTRA_TEXT, sb.toString()+"\n\n\n"+"from Tom Swifty android app at https://github.com/irsmith/peppermint"); 
		Log.i(getClass().getSimpleName()+" emailSwifty","victim:"+victim+" owner: "+owner);

		startActivity(Intent.createChooser(email, "Choose an Email client :"));
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
	
	/** Start a challenge edit. */
	private void startingChallenge() {

		ChallengeBlock b = ChallengesProvider.getInstance(getApplicationContext()).getChallenge(3, SENTINAL);
		if (b == null || b.candidates.size() == 0) {
			Toast.makeText(getApplicationContext(), "No challenges available.", Toast.LENGTH_LONG).show();
			return;
		}
		Pun newPun = b.pun;
		editableChallenge.setTag(CHALLENGE_PUN, newPun);// attach the fully defined pun for use after finished editing
		nonEditablePart.setText(newPun.getStmt());
		ArrayAdapter<String> challengesAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, b.candidates);
		editablePart.setAdapter(challengesAdapter);
		challengesAdapter.notifyDataSetChanged();
		
		mainListView.animate().setDuration(1000).alpha(0).withEndAction(new Runnable() {
			@Override
			public void run() {
				editableChallenge.setVisibility(View.VISIBLE);
				editableChallenge.requestFocus();
				mainListView.setAlpha(1);
			}
		});
	}

	/** Finished editing Challenge.
	 * 	http://stackoverflow.com/questions/8321251/why-onnothingselected-is-not-called
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		if (spinnerPrevSelection < 0 || spinnerPrevSelection == position) {
			spinnerPrevSelection = position;
			return;
		} 		
		spinnerPrevSelection = position;
		//Log.i (getClass().getSimpleName()+" onItemSelected", "pos/prevSelection:"+position+"/"+spinnerPrevSelection);
		
		switch (parent.getId()) {
		case R.id.challengesSpinner: {
			dropdownSelection = (String) parent.getItemAtPosition(position);
			if (dropdownSelection.startsWith(SENTINAL)) {
				//Log.i (getClass().getSimpleName()+" onItemSelected-SENTINAL","sel=0"); 
				editableChallenge.setVisibility(View.GONE);
			} else {

				final Pun finishedPun = (Pun)editableChallenge.getTag(CHALLENGE_PUN);
				if (finishedPun == null) throw new RuntimeException("null finsihed pun");
				Log.i (getClass().getSimpleName()+" onItemSelected","selected challenged editable part");
				editableChallenge.setTag(null);//clear cache
				finishedPun.setAdverb(dropdownSelection);
				finishedPun.setAuthor(finishedPun.getAuthor());
				finishedPun.setFormattedCreationTime(Pun.NOW);
				ChallengesProvider.getInstance(getApplicationContext()).disqualify(finishedPun.getKey()); 
				puns.add(0, finishedPun);
				adapter.notifyDataSetChanged();
				editableChallenge.setVisibility(View.GONE);
			}
			break;
		}
		default: {
			throw new RuntimeException("Invalid View id.");
		}
		}
		spinnerPrevSelection = -1;
	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		throw new RuntimeException("todo");
	}
	
	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data) { 
		
		if (requestCode == MY_DATA_CHECK_CODE) {
				if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
					Log.i(getClass().getSimpleName(),"onActivityResult Text to speech engine exists SUCCESS");		
					mTts = new TextToSpeech(this, this);
				} else {
					Log.i(getClass().getSimpleName(),"onActivityResult Text to speech engine needs installation");		
					Intent installIntent = new Intent(); installIntent.setAction(
							TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA); startActivity(installIntent);
				} }
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			Log.i(getClass().getSimpleName(),"Text to speech engine finished SUCCESS");		
			mTts.setLanguage(Locale.US);
		}
		else
			Log.i(getClass().getSimpleName(),"Text to speech engine failed");		
	}
}
