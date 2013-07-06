package net.skup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class Tom extends Activity {

	private enum Tags {
		created, author, subject, stmt, adverb
	}
	static final private int ADD_NEW_TODO = Menu.FIRST;
	static final private int REMOVE_TODO = Menu.FIRST + 1;

	private static final String TEXT_ENTRY_KEY = "TEXT_ENTRY_KEY";
	private static final String ADDING_ITEM_KEY = "ADDING_ITEM_KEY";
	private static final String SELECTED_INDEX_KEY = "SELECTED_INDEX_KEY";

	private boolean addingNew = false;
	private ArrayList<ToDoItem> todoItems;
	private ListView myListView;
	private EditText myEditText;
	private ToDoItemAdapter aa;

	ToDoDBAdapter toDoDBAdapter;
	Cursor toDoListCursor;

	public void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
		setContentView(R.layout.main);

		myListView = (ListView)findViewById(R.id.myListView);
		myEditText = (EditText)findViewById(R.id.myEditText);

		// TODO async
		StrictMode.ThreadPolicy policy = new StrictMode.
				ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 

		
		InputStream is = getDataWithURL("http://10.0.2.2:8080/sample.json");
		if (true/*is == null*/) {
			// Could not open sample.json get FileNotFound... cannot be opened is prob a compressed file
			//http://thedevelopersinfo.com/2009/11/27/using-files-as-raw-resources-in-android/
			//http://stackoverflow.com/questions/6186866/java-io-filenotfoundexception-this-file-can-not-be-opened-as-a-file-descriptor
			//InputStream itt = getDataWithFile("sample.json");
//			if (itt == null) {
//				Log.e(this.getClass().getName(),"could not read from file...");
//			}

		}
        String json = convertToString(is);
		populateMap(json);

		todoItems = new ArrayList<ToDoItem>();
		int resID = R.layout.todolist_item;
		aa = new ToDoItemAdapter(this, resID, todoItems);
		myListView.setAdapter(aa);

		myEditText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN)
					if (keyCode == KeyEvent.KEYCODE_ENTER) {
						ToDoItem newItem = new ToDoItem(myEditText.getText().toString(), 0);
						toDoDBAdapter.insertTask(newItem);
						updateArray();
						myEditText.setText("");
						aa.notifyDataSetChanged();
						cancelAdd();
						return true; 
					}
				return false;
			}
		});

		registerForContextMenu(myListView);
		restoreUIState();

		toDoDBAdapter = new ToDoDBAdapter(this);

		// Open or create the database
		toDoDBAdapter.open();

		populateTodoList();
	}

	private void populateMap(String data) {
		
		List<HashMap<String, String>> swiftys = new ArrayList<HashMap<String, String>>();
		try {
			JSONObject json = new JSONObject(data);

			// Getting Array of swiftys
		    JSONArray s = json.getJSONArray("swiftys");
		    
		    for (int i = 0; i < s.length(); i++){
		        JSONObject c = s.getJSONObject(i);
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(Tags.created.name(),  c.getString(Tags.created.name()));
                map.put(Tags.author.name(),  c.getString(Tags.author.name()));
                map.put(Tags.stmt.name(),  c.getString(Tags.stmt.name()));
                map.put(Tags.adverb.name(),  c.getString(Tags.adverb.name()));
                map.put(Tags.subject.name(),  c.getString(Tags.subject.name()));
                swiftys.add(map);
		    }
			Log.i(this.getClass().getName(),  "Number of entries " + swiftys.size());
		} catch (Exception e) {
			Log.e(this.getClass().getName(),"Could not parse json."+ e.getMessage());
		}		
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

	public void writeJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put("name", "Jack Hack");
			object.put("score", new Integer(200));
			object.put("current", new Double(152.32));
			object.put("nickname", "Hacker");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println(object);
	} 
	private void populateTodoList() {
		// Get all the todo list items from the database.
		toDoListCursor = toDoDBAdapter. getAllToDoItemsCursor();
		startManagingCursor(toDoListCursor);

		// Update the array.
		updateArray();
	}

	private void updateArray() {
		toDoListCursor.requery();

		todoItems.clear();

		if (toDoListCursor.moveToFirst())
			do { 
				String task = toDoListCursor.getString(toDoListCursor.getColumnIndex(ToDoDBAdapter.KEY_TASK));
				long created = toDoListCursor.getLong(toDoListCursor.getColumnIndex(ToDoDBAdapter.KEY_CREATION_DATE));
				int taskid = toDoListCursor.getInt(toDoListCursor.getColumnIndex(ToDoDBAdapter.KEY_ID));
				ToDoItem newItem = new ToDoItem(task, new Date(created), taskid);
				todoItems.add(0, newItem);
			} while(toDoListCursor.moveToNext());

		aa.notifyDataSetChanged();
	}

	private void restoreUIState() {
		// Get the activity preferences object.
		SharedPreferences settings = getPreferences(0);

		// Read the UI state values, specifying default values.
		String text = settings.getString(TEXT_ENTRY_KEY, "");
		Boolean adding = settings.getBoolean(ADDING_ITEM_KEY, false);

		// Restore the UI to the previous state.
		if (adding) {
			addNewItem();
			myEditText.setText(text);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(SELECTED_INDEX_KEY, myListView.getSelectedItemPosition());

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		int pos = -1;

		if (savedInstanceState != null)
			if (savedInstanceState.containsKey(SELECTED_INDEX_KEY))
				pos = savedInstanceState.getInt(SELECTED_INDEX_KEY, -1);

		myListView.setSelection(pos);
	}

	public void gobackButtonOnClick(View v) {
		Log.i("irene","goback")  ;
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Get the activity preferences object.
		SharedPreferences uiState = getPreferences(0);
		// Get the preferences editor.
		SharedPreferences.Editor editor = uiState.edit();

		// Add the UI state preference values.
		editor.putString(TEXT_ENTRY_KEY, myEditText.getText().toString());
		editor.putBoolean(ADDING_ITEM_KEY, addingNew);
		// Commit the preferences.
		editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Create and add new menu items.
		MenuItem itemAdd = menu.add(0, ADD_NEW_TODO, Menu.NONE,
				R.string.add_new);
		MenuItem itemRem = menu.add(0, REMOVE_TODO, Menu.NONE,
				R.string.remove);

		// Assign icons
		itemAdd.setIcon(R.drawable.add_new_item);
		itemRem.setIcon(R.drawable.remove_item);

		// Allocate shortcuts to each of them.
		itemAdd.setShortcut('0', 'a');
		itemRem.setShortcut('1', 'r');

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		int idx = myListView.getSelectedItemPosition();

		String removeTitle = getString(addingNew ? 
				R.string.cancel : R.string.remove);

		MenuItem removeItem = menu.findItem(REMOVE_TODO);
		removeItem.setTitle(removeTitle);
		removeItem.setVisible(addingNew || idx > -1);

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, 
			View v, 
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle("Selected To Do Item");
		menu.add(0, REMOVE_TODO, Menu.NONE, R.string.remove);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		int index = myListView.getSelectedItemPosition();

		switch (item.getItemId()) {
		case (REMOVE_TODO): {
			if (addingNew) {
				cancelAdd();
			} 
			else {
				removeItem(index);
			}
			return true;
		}
		case (ADD_NEW_TODO): { 
			addNewItem();
			return true; 
		}
		}

		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {  
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case (REMOVE_TODO): {
			AdapterView.AdapterContextMenuInfo menuInfo;
			menuInfo =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			int index = menuInfo.position;

			removeItem(index);
			return true;
		}
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Close the database
		toDoDBAdapter.close();
	}

	private void cancelAdd() {
		addingNew = false;
		myEditText.setVisibility(View.GONE);
	}

	private void addNewItem() {
		addingNew = true;
		myEditText.setVisibility(View.VISIBLE);
		myEditText.requestFocus(); 
	}

	private void removeItem(int _index) {

		ToDoItem item = todoItems.get(_index);
		raiseAlertDialog("Are you sure?", item);
	}

	private void raiseAlertDialog(String msg, ToDoItem item){

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog dialog = null;
		final long selectedId = item.getTaskId();

		builder.setMessage(msg)
		.setPositiveButton(R.string.cancel,new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.e("not removing"," ");
				dialog.dismiss();
			}

		}).setNegativeButton("Go ahead and delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				Log.e(" removing"," ");
				toDoDBAdapter.removeTask(selectedId);
				updateArray();
			}
		});

		dialog = builder.create();
		dialog.show();
	}

	public void syncOnClick(View v) {

		Log.i("sync on click from main","");
		Intent i = new Intent(this, IntentA.class);
		startActivity(i);
	}

}