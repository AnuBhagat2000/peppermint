package net.skup.model;

public class Pun {
	public enum T {
		created, author, subject, stmt, adverb
	}
	String created;
	String author;
	String subject;
	String stmt;
	String adverb;
	public Pun(String c, String auth, String subj, String statement, String adv) {
		created = c;
		author = auth;
		subject = subj;
		stmt = statement;
		adverb = adv;
	}
	public CharSequence getAdverb() {
		return adverb;
	}
	
}
