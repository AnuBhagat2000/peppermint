package net.skup.swifty.model;

import junit.framework.Assert;
import net.skup.swifty.model.ChallengesProvider.ChallengeBlock;
import android.test.ActivityTestCase;

public class ChallengesProviderTest extends ActivityTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPutObjectObject() throws InterruptedException {
		int limit = 10;
		ChallengesProvider cp =  ChallengesProvider.getInstance(getActivity().getApplicationContext());

		cp.fetch(limit); // load up to 10 data entries

		Thread.sleep(6000);
		Assert.assertEquals(limit,cp.available());// 10 may not be available since data size is small , or black list prevented it.


		ChallengeBlock b =	cp.getChallenge(3 , "sentinal"); 
		Assert.assertEquals(4, b.candidates.size()); 
		b =	cp.getChallenge(3); // get up to 3
		Assert.assertEquals(3, b.candidates.size()); 
		
		cp.disqualify(b.pun.getKey()); //consume the candidate associated with pun
		Assert.assertEquals(1, cp.blacklistSize());
		Assert.assertEquals(9, cp.available());  // smaller number of candidates are now available
		
		b =	cp.getChallenge(200); 
		Assert.assertEquals(9, b.candidates.size()); 
		cp.disqualify(b.pun.getKey()); 
		Assert.assertEquals(8, cp.available()); 
		Assert.assertEquals(2, cp.blacklistSize());
		b =	cp.getChallenge(0); 

		Assert.assertEquals(0, b.candidates.size()); 



	}

}
