package net.skup.swifty.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.skup.swifty.DownloadFilesTask;
import net.skup.swifty.DownloadFilesTask.Downloader;

public class ChallengesProvider implements Downloader {

	private List<Pun> challenges = new ArrayList<Pun>();
    private List<Long> blacklist = new ArrayList<Long>();//used up
	private int limit = 100;
	public static final String challengesURL = "http://tom-swifty.appspot.com/challenges.json";

    
	private static ChallengesProvider instance = null;
	private ChallengesProvider() {
	}
	
	public static ChallengesProvider getInstance() {
		if (instance == null) {
			instance = new ChallengesProvider();
		}
		return instance;
	}
	
	public int size() {
		return challenges.size();
	}
	
	int blacklistSize() {
		return blacklist.size();
	}
	
	/** Disqualify or consume a pun. */
    public void disqualify(long id) {
    	blacklist.add(id);
    }
    
	public ChallengeBlock getChallenge(int size) {
		
		List<String> adverbs = new ArrayList<String>(size);
		Random r = new Random(challenges.size());
		int idx = -1;
		Pun pun = null;
		do {
			idx = r.nextInt();
			pun = challenges.get(idx);
		} while (blacklist.contains(pun.getCreatedTimeSeconds()));
		
		adverbs.add(pun.getAdverb());
		do {
			int nextCandidate = r.nextInt();
			if (nextCandidate == idx) continue; 
			adverbs.add(challenges.get(nextCandidate).getAdverb());

		} while (adverbs.size() == size);
		return new ChallengeBlock(pun, adverbs);
	}
	
	public static class ChallengeBlock {
		public final List<String> candidates; 
		public final Pun pun;
		public ChallengeBlock(Pun pun, List<String> candidates) {
			super();
			this.pun = pun;
			this.candidates = candidates;
		}
	}

	public void fetch( int max) {
		limit = max;
		new DownloadFilesTask(this).execute(new String[] {challengesURL});
	}

	@Override
	public void setData(String data) {
		List<Pun> newPuns = Pun.deserializeJson(data);
		for (Pun p : newPuns) {
			if ( ! blacklist.contains(p.getCreatedTimeSeconds())) {
				challenges.add(p);
			}
			if (challenges.size() >= limit) break;
		}
	}
}
