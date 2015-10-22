package bft_log.Tests.OptimizedRandomGen;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Hashtable;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCCommitmentMsg;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitValue;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtReceiver;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenDecommitmentMessage;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenReceiver;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.GroupElementSendableData;

public class MyCmtReceiver extends CmtPedersenReceiver implements CmtReceiver {
	private TestRandomShareCommitMsg msg;
	private Hashtable<Integer, TestRandomShareCommitMsg> valueTable = null;
	
	public MyCmtReceiver(Channel channel, DlogGroup dlog, SecureRandom random)
			throws SecurityLevelException, InvalidDlogGroupException, IOException {
		super(channel, dlog, random);
		// TODO Auto-generated constructor stub	\
	}
	
	public void setPreviouslyCommittedValues(Hashtable<Integer, TestRandomShareCommitMsg> tbl){
		this.valueTable = tbl;
	}
	
	public TestRandomShareCommitMsg getCommMap(){
		if (commitmentMap.size() > 1){
			System.err.println("On this channel have been sent more than one commit with same id.");
		} else {
			for ( Long i : this.commitmentMap.keySet()){
				this.
				msg = new TestRandomShareCommitMsg(i, this.commitmentMap.get(i), this.getH());
				return msg;
			}
		}
		return null;
	}
	
	//We use this class because "GroupElement" is not serializable
	public GroupElementSendableData getH(){
		return this.h.generateSendableData();
	}
	
	public void setH(GroupElementSendableData h){
		this.h = dlog.reconstructElement(true, h);
	}
	
	public CmtCommitValue receiveDecommitment(long id, int nodeId) throws ClassNotFoundException, IOException {
		CmtPedersenDecommitmentMessage message = null;
		System.out.println("I am waiting to receive the decommitment from: " + nodeId);
		try { 
			message = (CmtPedersenDecommitmentMessage) channel.receive(); 
		} catch (ClassNotFoundException e) { 
			throw new ClassNotFoundException("Failed to receive decommitment. The error is: " + e.getMessage()); 
		} catch (IOException e) { 
			throw new IOException("Failed to receive decommitment. The error is: " + e.getMessage()); 
 		} 
 		if (!(message instanceof CmtPedersenDecommitmentMessage)){ 
 			throw new IllegalArgumentException("The received message should be an instance of CmtPedersenDecommitmentMessage"); 
 		} 
		
		CmtPedersenDecommitmentMessage msg = (CmtPedersenDecommitmentMessage) message;
		CmtCCommitmentMsg  receivedCommitment = valueTable.get(nodeId).getCommitMsg(); 
		
		return super.verifyDecommitment(receivedCommitment, msg); 
	}
}
