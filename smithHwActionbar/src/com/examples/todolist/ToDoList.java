package com.examples.todolist;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ToDoList extends Activity {
	
  
  private boolean addingNew = false;
  private ArrayList<String> todoItems;
  private ListView myListView;
  private EditText myEditText;
  static ArrayAdapter<String> aa;

  public int getListSize() {
	  return aa.getCount();
  }
  
  /** Called when the activity is first created. */
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.v("ToDoList - activity menu takehome","inside onCreate()");
    
    // Inflate your view
    setContentView(R.layout.main);
    
    // Get references to UI widgets
    myListView = (ListView)findViewById(R.id.myListView);
    myEditText = (EditText)findViewById(R.id.myEditText);

    todoItems = new ArrayList<String>();
    
    int resID = R.layout.todolist_item;
    aa = new ArrayAdapter<String>(this, resID, todoItems);
    myListView.setAdapter(aa);
        
    myEditText.setOnKeyListener(new OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
          if (keyCode == KeyEvent.KEYCODE_ENTER) {
            todoItems.add(0, myEditText.getText().toString());
            myEditText.setText("");
            aa.notifyDataSetChanged();
            cancelAdd();
            return true; 
          }
        return false;
      }
    });
    

    ActionBar bar = getActionBar();
    bar.setDisplayShowTitleEnabled(true);
    bar.setTitle(R.string.actionBarTitle);
    registerForContextMenu(myListView);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
	  super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.hw_nunber1, menu);
      return true;
  }

  /**
   * Three states for the options menu: Empty, Adding Item, and Not Empty. 
   * If Empty the options menu has [Add]. If Adding Item, the options menu
   * has [Add, Cancel]. If Not Empty the options menu has [add,delete].
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    int idx = myListView.getSelectedItemPosition();

    String removeTitle = getString(addingNew ? 
                                   R.string.cancel : R.string.remove);

    MenuItem removeItem = menu.findItem(R.id.removeMenuItem);
    assert removeItem != null : "not fining rm-menu item";
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
    menu.add(0, R.id.removeMenuItem, Menu.NONE, R.string.remove);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
	  super.onOptionsItemSelected(item);
	  int index = myListView.getSelectedItemPosition();
	  // Handle item selection
	  switch (item.getItemId()) {
	  case R.id.addMenuItem:
		  addNewItem();
		  return true;
	  case R.id.removeMenuItem:
		  if (addingNew) {
			  cancelAdd();
		  } 
		  else {
			  removeItem(index);
		  }             
		  return true;
	  default:
		  return super.onOptionsItemSelected(item);
	  }
  }
  
  @Override
  public boolean onContextItemSelected(MenuItem item) {  
    super.onContextItemSelected(item);
    switch (item.getItemId()) {
      case (R.id.removeMenuItem): {
        AdapterView.AdapterContextMenuInfo menuInfo;
        menuInfo =(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int index = menuInfo.position;

        removeItem(index);
        return true;
      }
    }
    return false;
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
    todoItems.remove(_index);
    aa.notifyDataSetChanged();  
  }
}