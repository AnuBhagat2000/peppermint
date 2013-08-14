package net.skup.swifty.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Pun {
	
	public static final String SWTAG = "swiftys"; // outer json object key (could be timestamp in future)
	public static final String NOW = "NOW";
	static public final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
	public enum T {
		created, author, subject, stmt, adverb
	}
	private String formattedCreationTime;
	private long createdTimeSeconds = -1;
	private String author;
	private String subject;
	private String stmt;
	private String adverb;

	public JSONObject getJSONObject() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(T.created.name(), String.valueOf(createdTimeSeconds));
			obj.put(T.author.name(), author);
			obj.put(T.subject.name(), subject);
			obj.put(T.stmt.name(), stmt);
			obj.put(T.adverb.name(), adverb);
		} catch (JSONException e) {
			Log.e(getClass().getName(), "Pun getJSONObject JSONException: "+e.getMessage());
		}
		return obj;
	}

	/**
	 * 
	 * @param creationTimeSeconds - eg "123456789" in seconds
	 * @param auth - author
	 * @param statement - first part
	 * @param adv - second part
	 * @param subj - the subject eg "Tom"
	 */
	public Pun(String creationTimeSeconds, String auth, String statement, String adv, String subj) {

		createdTimeSeconds =  Long.parseLong(creationTimeSeconds);
		formattedCreationTime = sdf.format(new Date(createdTimeSeconds * 1000));
		author = auth;
		stmt = statement;
		adverb = adv;
		subject = subj;

	}

	public String getCreated() {
		if (formattedCreationTime == null || formattedCreationTime.isEmpty()) {
			formattedCreationTime = Long.toString(createdTimeSeconds);
		}
		return formattedCreationTime;
	}

	public long getCreatedTimeSeconds() {
		if (createdTimeSeconds == -1) {
			//
			createdTimeSeconds = 1000;// TODO convert from created string formatted time to long
		}
		return createdTimeSeconds;
	}

	public void setCreated(String created) {
		if (created.equals(NOW)) {
			createdTimeSeconds = System.currentTimeMillis() / 1000;
			formattedCreationTime = sdf.format(createdTimeSeconds * 1000);
		} else {
			formattedCreationTime = created;
		}
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getStmt() {
		return stmt;
	}
	public void setStmt(String stmt) {
		this.stmt = stmt;
	}
	public void setAdverb(String adverb) {
		this.adverb = adverb;
	}
	public String getAdverb() {
		return adverb;
	}

	/** Convert serialized Json to data. */
	public static List<Pun> deserializeJson(String data) {

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
			Log.i(Pun.class.getName(),  "Number of entries " + swiftys.size());
		} catch (Exception e) {
			Log.e(Pun.class.getName(),"Could not parse json."+ e.getMessage()+" successful entries " + swiftys.size());
		}	
		return swiftys;
	}
	
	/*
	 * Stringify a Pun list.
	 * http://stackoverflow.com/questions/4841952/convert-arraylist-to-jsonarray
	 */
	public static String jsonStringify(List<Pun> puns)  {

		JSONArray ja = new JSONArray();
		for (int i=0;i<puns.size();i++) {
			ja.put(puns.get(i).getJSONObject());
		}

		JSONObject row = new JSONObject();
		try {
			row.put(SWTAG, ja);
		} catch (JSONException e) {
			Log.e(Pun.class.getName(), e.getMessage());
		}
		return row.toString();
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
	
}
