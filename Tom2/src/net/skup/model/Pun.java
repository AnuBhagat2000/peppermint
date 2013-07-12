package net.skup.model;

public class Pun {
	public enum T {
		created, author, subject, stmt, adverb
	}
	private String created;
	private String author;
	private String subject;
	private String stmt;
	private String adverb;
	
	public Pun(String c, String auth, String statement, String adv, String subj) {
		created = c;
		author = auth;
		stmt = statement;
		adverb = adv;
		subject = subj;

	}
	public String getCreated() {
		return created;
	}
	public void setCreated(String created) {
		this.created = created;
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
	public CharSequence getAdverb() {
		return adverb;
	}
	
}
