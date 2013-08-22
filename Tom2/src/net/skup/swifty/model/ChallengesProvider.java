package net.skup.swifty.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.skup.swifty.DownloadFilesTask;
import net.skup.swifty.DownloadFilesTask.Downloader;
import net.skup.swifty.R;
import android.content.Context;
import android.util.Log;

public class ChallengesProvider implements Downloader {

	private List<Pun> challenges = new ArrayList<Pun>();
    private Set<String> blacklist = new HashSet<String>();
	private int limit = 100;
	public static final String challengesURL = "http://tom-swifty.appspot.com/challenges.json";
    private static Context applicationContext;
    
	private static ChallengesProvider instance = null;
	private ChallengesProvider() {
	}
	
	public static ChallengesProvider getInstance(Context a) {
		if (instance == null) {
			applicationContext = a;
			instance = new ChallengesProvider();
		}
		return instance;
	}
	
	public int available() {
		return challenges.size();
	}
	
	int blacklistSize() {
		return blacklist.size();
	}
	
	/** Disqualify or consume a pun. */
    public void disqualify(String id) {
    	Iterator<Pun> it = challenges.iterator();
    	boolean found = false;
    	Pun p = null;
        while (it.hasNext()) { 
        	p = (Pun) it.next();
        	if (p.getKey() == id) {
        		found = true;
        		break;
        	}
        }
        if (found) challenges.remove(p);
    	blacklist.add(id);
    }
    
    public ChallengeBlock getChallenge(int max) {
    	return getChallenge(max, null);
    }
    
    /**
     * Prepares a set of challenges containing one match of a Pun statement to its adverb and (max - 1) mismatches,
     * If data is not available from the fetch falls back to sample data.  
     * @param sentinal - if not null this is added as the first position in the list 
     * @param max - the maximum size of the challenge list
     * @return a list of challenges or null upon error
     */
	public ChallengeBlock getChallenge(int max, String sentinal) {
		
		Log.i(getClass().getSimpleName()+" getChallenge ", "challenges.size:"+challenges.size() +" blacklist-size:"+blacklistSize());
		if (challenges.size() <= 0) {
			challenges = fetchSynchronously(max);
			if (challenges.size() <= 0) {
				Log.e(getClass().getName(),"getChallenge: could not get challenges, even fallback data.");
                return null;
			}
		}
        
		List<String> adverbs = new ArrayList<String>(max);
		Pun challengePun = null;
		Collections.shuffle(challenges);
		
		// find a qualified challenge and its correct answer 
		Iterator<Pun> it = challenges.iterator();
		if (it.hasNext()) {
			do {
				challengePun = it.next();
			} while (!blacklist.isEmpty() &&  blacklist.contains(challengePun.getKey()));
		}
		adverbs.add(challengePun.getAdverb());


		// add N unique candidate adverbs
		it = challenges.iterator();
		while (it.hasNext() && (adverbs.size() < max)) {
			challengePun = it.next();
			if (adverbs.contains(challengePun.getAdverb())) continue;
			if (blacklist.contains(challengePun.getKey())) continue;
			adverbs.add(challengePun.getAdverb());
		}
		
		Collections.shuffle(adverbs);
		if (sentinal != null && !sentinal.isEmpty()) adverbs.add(0, sentinal);
		return new ChallengeBlock(challengePun, adverbs);
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

	/** Fetch fallback data, synchronously from a file.*/
	List<Pun> fetchSynchronously(int max) {
    	List<Pun> challengesSych = new ArrayList<Pun>();
    	if (applicationContext == null) throw new RuntimeException("no context");
		InputStream fis = applicationContext.getResources().openRawResource(R.raw.challenges);
		String stringified = Pun.convertToString(fis);
		challengesSych = Pun.deserializeJson(stringified);
		Log.i(getClass().getSimpleName()+" fetchSynchronously","got fallbk challenges.size"+ challengesSych.size());
		return challengesSych;
	}
		
	public void fetch(int max) {
		limit = max;
		new DownloadFilesTask(this).execute(new String[] {challengesURL});
	}


	/**
	 * Callback from data fetcher. 
	 * Uses the blacklist to prevent addition of redundant or used data.
	 */
	@Override
	public void setData(String data) {
		List<Pun> newPuns = Pun.deserializeJson(data);

		for (Pun p : newPuns) {
			// do not add redundant
			if ( ! challenges.contains(p.getKey() )) {
				Log.i(getClass().getSimpleName()+" setData : ","loading :"+p.getKey());
				challenges.add(p);
			} else {
				Log.i(getClass().getSimpleName()+" setData : ","skipping load for :"+p.getKey());
			}
			if (challenges.size() >= limit) break;
		}
		Log.i(getClass().getSimpleName()+"setData","challenges.size:"+ challenges.size() +" blacklist.size:"+blacklist.size());
	}

	public Set<String> getBlacklist() {
		return blacklist;
	}

	public void putBlacklist(Set<String> bl) {
		if (bl == null || bl.size() == 0) {
			return;
		}
		blacklist.clear();
		for (String s : bl) {
			blacklist.add(s);
		}
	}
}
