package bft_log.Tests;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtRCommitPhaseOutput;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtReceiver;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenReceiver;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.miracl.MiraclDlogECF2m;

public class TestSSLCommitSelector {
	private Map<PartyData, Map<String, Channel>> connections;
	private ArrayList<CmtReceiver> receiverList = null;
    private DlogGroup dlog = null;
	
    public TestSSLCommitSelector (Map<PartyData, Map<String, Channel>> conn){
		this.connections = conn;
		receiverList = new ArrayList<>();
	}
	
	public ArrayList<CmtReceiver> getReceiverList() {
		return receiverList;
	}

	public ArrayList<CmtRCommitPhaseOutput> waitCommitments(ArrayList<CmtRCommitPhaseOutput> listCommit) throws IllegalArgumentException, IOException{
		
	    while(true){
	    	 for (Map<String, Channel> i : connections.values()){
    	    	Iterator it = i.values().iterator();
    	    	dlog = new MiraclDlogECF2m("K-233");
    	    	while(it.hasNext()){
    				try {
    					System.out.println("TRY TRY TRY");
    					CmtReceiver receiver = new CmtPedersenReceiver((Channel) it.next(), dlog, new SecureRandom());
    					receiverList.add(receiver);
    					
    		    	    //Receive the commitment on the commit value
    		    	    CmtRCommitPhaseOutput output = receiver.receiveCommitment();
		    	    	listCommit.add(output);
		    	    	
		    	    	System.out.println("(primary) Size of list Commit: " + listCommit.size());
		    	    	break;
    				}catch (SecurityLevelException | InvalidDlogGroupException | ClassNotFoundException | IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    	    	}
	    	 }
	    	 System.out.println("HERE LOOP");
	    	 return listCommit;
		}
	}
}
