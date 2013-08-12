package net.skup.swifty.model;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.skup.swifty.model.ChallengesProvider.ChallengeBlock;

public class ChallengesProviderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPutObjectObject() {
		int limit = 10;
		ChallengesProvider cp =  ChallengesProvider.getInstance();
		cp.fetch(limit); // load up to 10 data entries
		// 10 may not be available since data size is small , or black list prevented it.

		Assert.assertEquals(limit,cp.size());
		ChallengeBlock b =	cp.getChallenge(3);

		Assert.assertEquals(3, b.candidates.size()); 
		cp.disqualify(b.pun.getCreatedTimeSeconds());

		Assert.assertEquals(1, cp.blacklistSize());
		Assert.assertEquals(limit -1, cp.size()); 

	}

}
