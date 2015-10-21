package bft_log.Tests;

import java.io.IOException;
import java.security.SecureRandom;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCCommitmentMsg;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtReceiver;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenReceiver;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.GroupElement;
import edu.biu.scapi.primitives.dlog.GroupElementSendableData;

public class MyCmtReceiver extends CmtPedersenReceiver implements CmtReceiver {
	private TestRandomShareCommitMsg msg;
	
	public MyCmtReceiver(Channel channel, DlogGroup dlog, SecureRandom random)
			throws SecurityLevelException, InvalidDlogGroupException, IOException {
		super(channel, dlog, random);
		// TODO Auto-generated constructor stub	\
	}
	
	public TestRandomShareCommitMsg getCommMap(){
		if (commitmentMap.size() > 1){
			System.err.println("On this channel have been sent more than one commit with same id.");
		} else {
			for ( Long i : this.commitmentMap.keySet()){
				msg = new TestRandomShareCommitMsg(i, (GroupElementSendableData) ((CmtCCommitmentMsg) this.commitmentMap.get(i)).getCommitment(), this.getH());
				return msg;
			}
		}
		return null;
	}
	
	//We use this class because "GroupElement" is not serializable
	public GroupElementSendableData getH(){
		return this.h.generateSendableData();
	}
}
