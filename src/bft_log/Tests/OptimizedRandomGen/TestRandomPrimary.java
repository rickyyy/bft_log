package bft_log.Tests.OptimizedRandomGen;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitValue;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtRCommitPhaseOutput;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtReceiver;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.miracl.MiraclDlogECF2m;

public class TestRandomPrimary {
	private Map<PartyData, Map<String, Channel>> connections;
    private DlogGroup dlog = null;
	private TableCommit valueTable;
	private Hashtable<Integer, Integer> otherCommitments;
	private int myCommittedValue;

    public TestRandomPrimary (Map<PartyData, Map<String, Channel>> conn){
		this.connections = conn;
		valueTable = new TableCommit();
		this.otherCommitments = new Hashtable<>();
	}

	public void waitCommitments() throws IllegalArgumentException, IOException{
		while(true){
	    	 for (PartyData m : connections.keySet()){
	    		Map<String, Channel> i = connections.get(m);
    	    	Iterator it = i.values().iterator();
    	    	dlog = new MiraclDlogECF2m("K-233");
    	    	while(it.hasNext()){
    				try {
    					CmtReceiver receiver = new MyCmtReceiver((Channel) it.next(), dlog, new SecureRandom());
    					System.out.println(((MyCmtReceiver)receiver).getH());
    					
    		    	    //Receive the commitment on the commit value
    		    	    CmtRCommitPhaseOutput output = receiver.receiveCommitment();
    		    	    
    		    	    //Get information to add at the hashtable of the primary
    					TestRandomShareCommitMsg msg = ((MyCmtReceiver)receiver).getCommMap();
    					valueTable.put(Math.abs(m.hashCode()), msg);
    					
		    	    	break;
    				}catch (SecurityLevelException | InvalidDlogGroupException | ClassNotFoundException | IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    	    	}
	    	 }
	    	 System.out.println(valueTable.toString());
	    	 break;
		}
		sendChosenCmt();
	}
	
	//TODO Now it sends all the commitments back to user.
	private void sendChosenCmt(){
		System.out.println("Sending Table to replicas ... ");
		for (Map<String, Channel> i : connections.values()){
			Iterator it = i.values().iterator();
			while(it.hasNext()){
				try {
					((Channel)it.next()).send(this.valueTable);
					System.out.println("Table Sent Successfully! : " + this.valueTable);
					break;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		myCommittedValue = receiveDecommitments();
		System.out.println(myCommittedValue);
	}
	
	public Integer receiveDecommitments(){
		for( Integer i : valueTable.keySet()){
			for (PartyData m : connections.keySet()){
				if (Math.abs(m.hashCode()) == i){
					Map<String, Channel> map = connections.get(m);
					Iterator it = map.values().iterator();
					while(it.hasNext()){
						try {
							CmtReceiver receiver = new MyCmtReceiver((Channel) it.next(), dlog, new SecureRandom());
							((MyCmtReceiver)receiver).setH(valueTable.get(i).getH());
							((MyCmtReceiver)receiver).setPreviouslyCommittedValues(valueTable);
							CmtCommitValue val = ((MyCmtReceiver)receiver).receiveDecommitment(2, Math.abs(m.hashCode()));
							
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
		for (Integer i : otherCommitments.values()){
			myCommittedValue += i;
		}
		System.out.println("Distributed Random Number Generated: " + Math.abs(myCommittedValue));
		return Math.abs(myCommittedValue);
		
	}
}
