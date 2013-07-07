package net.skup;

import java.util.List;

import net.skup.model.Pun;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SwiftyAdapter extends ArrayAdapter<Pun> {


	private final Activity context;
	private List<Pun> list = null;
	
	public SwiftyAdapter(Activity context, List<Pun> objects) {
		super(context, R.layout.swifty_view, objects);
		this.context = context;
	    this.list = objects;
	}

	static class ViewHolder {
		protected TextView text;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.swifty_view, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) view.findViewById(R.id.adverb);
			view.setTag(viewHolder);
		} else {
			view = convertView;
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.text.setText(list.get(position).getAdverb());
		return view;
	}
}
