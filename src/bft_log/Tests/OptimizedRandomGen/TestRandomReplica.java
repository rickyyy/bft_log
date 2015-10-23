package bft_log.Tests.OptimizedRandomGen;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.exceptions.CheatAttemptException;
import edu.biu.scapi.exceptions.CommitValueException;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitValue;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitter;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtReceiver;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenCommitmentPhaseValues;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.miracl.MiraclDlogECF2m;

public class TestRandomReplica {
	private PartyData myself;
	private Map<String, Channel> primaryCh;
	private CmtCommitter committer = null;
	private Map<Long, CmtPedersenCommitmentPhaseValues> committedToPrimary;
	private DlogGroup dlog = null;
	private CmtCommitValue val = null;
	private long idCmt;
	private TableCommit valueTable = null;
	private Hashtable<Integer, Integer> otherCommitments;
	private int myCommittedValue;

	public TestRandomReplica(Map<String, Channel> primaryChannel, PartyData myId){
		this.primaryCh = primaryChannel;
		this.myself = myId;
		this.idCmt = 2;
		this.otherCommitments = new Hashtable<>();
		SecureRandom r = new SecureRandom();
		try {
			this.myCommittedValue = SecureRandom.getInstance("SHA1PRNG", "SUN").nextInt(10000);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public Integer send(Map<PartyData, Map<String, Channel>> connections){
		for( Integer i : valueTable.keySet()){
			if (i == Math.abs(myself.hashCode())){ // I have to send because I was selected by the primary
				for (PartyData m : connections.keySet()){
					if (m!=myself){
						Map <String, Channel> map = connections.get(m);
						Iterator it = map.values().iterator();
						while (it.hasNext()){
							try {
								committer = new MyCmtCommiter((Channel) it.next(), dlog, new SecureRandom());
								((MyCmtCommiter)committer).setTableOfCommitments(committedToPrimary);
								committer.decommit(idCmt);
							} catch (ClassNotFoundException | CheatAttemptException | SecurityLevelException
									| InvalidDlogGroupException | IOException | CommitValueException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			} else {	//I have to receive the message from the node that is responsible for the decommitment.
				for (PartyData m : connections.keySet()){
					if (Math.abs(m.hashCode()) == i){
						Map<String, Channel> map = connections.get(m);
						Iterator it = map.values().iterator();
						while(it.hasNext()){
							try {
								CmtReceiver receiver = new MyCmtReceiver((Channel) it.next(), dlog, new SecureRandom());
								((MyCmtReceiver)receiver).setH(this.valueTable.get(i).getH());
								((MyCmtReceiver)receiver).setPreviouslyCommittedValues(valueTable);
								CmtCommitValue val = ((MyCmtReceiver)receiver).receiveDecommitment(idCmt, Math.abs(m.hashCode()));
								
								String committedString = new String(receiver.generateBytesFromCommitValue(val));
								if (!otherCommitments.containsKey(Math.abs(m.hashCode()))){
									otherCommitments.put(Math.abs(m.hashCode()), Integer.valueOf(committedString));
								}
							    System.out.println(committedString);
							} catch (SecurityLevelException | InvalidDlogGroupException | IOException | ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		for (Integer i : otherCommitments.values()){
			myCommittedValue += i;
		}
		System.out.println("Distributed Random Number Generated: " + Math.abs(myCommittedValue));
		return Math.abs(myCommittedValue);
	}
	
	public void sendToPrimary(int myId){
		
		for (Channel i : primaryCh.values()){
			System.out.println("Committing to the primary node ... ");
			try {
				dlog = new MiraclDlogECF2m("K-233");
				committer = new MyCmtCommiter((Channel) i, dlog, new SecureRandom());
				
				//generate CommitValue from string
				val = committer.generateCommitValue(new String(String.valueOf(myCommittedValue)).getBytes());
			    
			    //Commit on the commit value with id 2
			    committer.commit(val, idCmt);
			    
			    this.committedToPrimary = ((MyCmtCommiter)committer).getTableOfCommitments();
			    
			    System.out.println("Committed!");
			    break;
			} catch (ClassNotFoundException | CheatAttemptException | SecurityLevelException
					| InvalidDlogGroupException | CommitValueException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		receiveCmtTable();
	}
	
	@SuppressWarnings("unchecked")
	private void receiveCmtTable(){
		System.out.println("Waiting for the Committed Table chosen by the primary ...");
		while (true){
			for (Channel i : primaryCh.values()){
				try {
					this.valueTable = (TableCommit)i.receive();
					if (this.valueTable!=null){
						System.out.println("Committed Table Received!");
						System.out.println(valueTable.toString());
						break;
					}
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		}
	}
}
