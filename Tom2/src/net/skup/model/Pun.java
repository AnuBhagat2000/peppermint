package net.skup.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.skup.R;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Pun {
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
	
	/**
	 * Create a pun given a statement (with substitutable subject) and an adverb.  Uses current time and current subject.
	 * @param statement
	 * @param adverb
	 */
	public Pun(String statement, String adverb, String substituteSubject) {
		
		createdTimeSeconds = System.currentTimeMillis() / 1000;
		formattedCreationTime = sdf.format(createdTimeSeconds);
		this.author = substituteSubject;
		this.subject = author; 
		this.stmt = statement;
		this.adverb = adverb;
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
		this.formattedCreationTime = created;
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
	
}
