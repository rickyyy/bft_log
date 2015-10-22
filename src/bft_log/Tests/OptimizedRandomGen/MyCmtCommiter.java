package bft_log.Tests.OptimizedRandomGen;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.exceptions.CheatAttemptException;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitter;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenCommitmentPhaseValues;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenCommitter;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.GroupElementSendableData;

public class MyCmtCommiter extends CmtPedersenCommitter implements CmtCommitter {

	public MyCmtCommiter(Channel channel, DlogGroup dlog, SecureRandom random) throws SecurityLevelException,
			InvalidDlogGroupException, ClassNotFoundException, IOException, CheatAttemptException {
		super(channel, dlog, random);
		System.out.println("Committer instantiated");
		// TODO Auto-generated constructor stub
	}
	
	//We use this class because "GroupElement" is not serializable
	public GroupElementSendableData getH(){
		return this.h.generateSendableData();
	}
	
	public void setH(GroupElementSendableData h){
		this.h = dlog.reconstructElement(true, h);
	}
	
	public Map<Long, CmtPedersenCommitmentPhaseValues> getTableOfCommitments(){
		return this.commitmentMap;
	}
	
	public void setTableOfCommitments(Map<Long, CmtPedersenCommitmentPhaseValues> tbl){
		this.commitmentMap = tbl;
	}
}
