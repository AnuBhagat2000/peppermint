package net.skup.swifty;

import java.util.List;

import net.skup.swifty.R;
import net.skup.swifty.model.Pun;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class SwiftyAdapter extends ArrayAdapter<Pun> {


	private final Activity context;
	private List<Pun> puns = null;
	
	public SwiftyAdapter(Activity context, List<Pun> objects) {
		super(context, R.layout.row_view, objects);
		this.context = context;
	    this.puns = objects;
	}

	/** View Holder class contains all my updatable views.*/
	static class ViewHolder {
		protected TextView statement;
		protected TextView adverb;
		protected TextView author;
		protected TextView time;
	}

	
	/**
	 * Uses View Holder pattern for smooth scrolling.
	 * http://developer.android.com/training/improving-layouts/smooth-scrolling.html
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.row_view, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.adverb = (TextView) view.findViewById(R.id.adverb);
			viewHolder.statement = (TextView) view.findViewById(R.id.statement);
			viewHolder.time = (TextView)view.findViewById(R.id.timestamp);
			viewHolder.author = (TextView)view.findViewById(R.id.author);
			view.setTag(viewHolder);
			
		} else {
			view = convertView;
		}
		
		ViewHolder holder = (ViewHolder) view.getTag(); // get all subviews
		holder.time.setText(puns.get(position).getCreated());
		holder.author.setText(puns.get(position).getAuthor());
		holder.statement.setText(puns.get(position).getStmt()); //"I want a martini."
		holder.adverb.setText(puns.get(position).getAdverb()); //TODO substitute subject "[Tom] said dryly"
		holder.statement.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				final int position = v.getId();
				final EditText stmt = (EditText) v;

				String strValue = stmt.getText().toString();
				Log.i(getClass().getSimpleName()+" getView","User set EditText value to " + strValue +" pos: "+position);
			}
		});
		return view;
	}
}
