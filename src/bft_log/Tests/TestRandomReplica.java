package bft_log.Tests;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.exceptions.CheatAttemptException;
import edu.biu.scapi.exceptions.CommitValueException;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitValue;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitter;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenCommitter;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.miracl.MiraclDlogECF2m;

public class TestRandomReplica {
	private Map<String, Channel> primaryCh;
	private CmtCommitter committer = null;
	private DlogGroup dlog = null;
	private CmtCommitValue val = null;
	private long idCmt;
	
	public TestRandomReplica(Map<String, Channel> primaryChannel){
		this.primaryCh = primaryChannel;
		this.idCmt = 2;
	}
	
	public void sendToPrimary(int myId){
		for (Channel i : primaryCh.values()){
			System.out.println("Committing to the primary node ... ");
			try {
				dlog = new MiraclDlogECF2m("K-233");
				committer = new CmtPedersenCommitter((Channel) i, dlog, new SecureRandom());
				
				//generate CommitValue from string
				val = committer.generateCommitValue(new String("123" + myId).getBytes());
			    
			    //Commit on the commit value with id 2
			    committer.commit(val, idCmt);
			    
			    //Decommit
			    //committer.decommit(idCmt);
			    
			    System.out.println("Committed!");
			    break;
			} catch (ClassNotFoundException | CheatAttemptException | SecurityLevelException
					| InvalidDlogGroupException | CommitValueException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	}
}
