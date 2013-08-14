package net.skup.swifty.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.skup.R;
import net.skup.swifty.DownloadFilesTask;
import net.skup.swifty.DownloadFilesTask.Downloader;
import android.app.Activity;
import android.util.Log;

public class ChallengesProvider extends Activity implements Downloader {

	private Set<Pun> challenges = new HashSet<Pun>();
    private Set<Long> blacklist = new HashSet<Long>();//used up
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
	
	public int available() {
		return challenges.size();
	}
	
	int blacklistSize() {
		return blacklist.size();
	}
	
	/** Disqualify or consume a pun. */
    public void disqualify(long id) {
    	Iterator<Pun> it = challenges.iterator();
    	boolean found = false;
    	Pun p = null;
        while (it.hasNext()) { 
        	p = (Pun) it.next();
        	if (p.getCreatedTimeSeconds() == id) {
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
     * @param sentinal - if null this is added as the first position in the list 
     * @param max - the maximum size of the challenge list
     * @return a list of challenges or null upon error
     */
	public ChallengeBlock getChallenge(int max, String sentinal) {
		Log.i(getClass().getName(),"getChallenge: challenges:"+challenges.size() +" blacklist:"+blacklistSize());

		if (challenges.size() <= 0) {
			fetchSynchronously(max);
			if (challenges.size() <= 0) {
				Log.e(getClass().getName(),"getChallenge: could not get challenges, even fallback data.");
                return null;
			}
		}
        
		List<String> adverbs = new ArrayList<String>(max);
		Pun challengePun = null;
		
		// find a qualified challenge and its correct answer
		Iterator<Pun> it = challenges.iterator();
		if (it.hasNext()) {
			do {
				challengePun = it.next();
			} while (!blacklist.isEmpty() &&  blacklist.contains(challengePun.getCreatedTimeSeconds()));
		}
		adverbs.add(challengePun.getAdverb());


		// add N unique candidate adverbs
		it = challenges.iterator();
		while (it.hasNext() && (adverbs.size() < max)) {
			challengePun = it.next();
			if (adverbs.contains(challengePun.getAdverb())) continue;
			if (blacklist.contains(challengePun)) continue;
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

	/** Fallback data.*/
	List<Pun> fetchSynchronously(int max) {
    	List<Pun> challengesSych = new ArrayList<Pun>();
		InputStream fis = getResources().openRawResource(R.raw.challenges);
		String stringified = Pun.convertToString(fis);
		challengesSych = Pun.deserializeJson(stringified);
		Log.i(getClass().getName(), "fetchSynchronously:: challenges.size"+ challengesSych.size());
		return challengesSych;
	}
		
	public void fetch(int max) {
		limit = max;
		new DownloadFilesTask(this).execute(new String[] {challengesURL+"ff"});
	}

	@Override
	public void setData(String data) {
		List<Pun> newPuns = Pun.deserializeJson(data);
		Log.i(getClass().getName(), "setData Puns.size/blacklist size"+ newPuns.size() +"/"+blacklist.size());

		for (Pun p : newPuns) {
			//TODO do not add redundant
			if ( ! blacklist.contains(p.getCreatedTimeSeconds() /*&& ! (challenges).find(p.getCreatedTimeSeconds())*/)) {
				challenges.add(p);
			}
			if (challenges.size() >= limit) break;
		}
		Log.i(getClass().getName(), "setData challenges.size"+ challenges.size());

	}
}
