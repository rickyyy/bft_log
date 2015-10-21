package bft_log.Tests;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.corba.se.impl.ior.GenericTaggedComponent;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtRCommitPhaseOutput;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtReceiver;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenReceiver;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.miracl.MiraclDlogECF2m;

public class TestRandomPrimary {
	private Map<PartyData, Map<String, Channel>> connections;
	private ArrayList<CmtReceiver> receiverList = null;
    private DlogGroup dlog = null;
	private Hashtable<Integer, TestRandomShareCommitMsg> valueTable;

    public TestRandomPrimary (Map<PartyData, Map<String, Channel>> conn){
		this.connections = conn;
		receiverList = new ArrayList<>();
		valueTable = new Hashtable<Integer, TestRandomShareCommitMsg>();
	}
	
	public ArrayList<CmtReceiver> getReceiverList() {
		return receiverList;
	}

	public ArrayList<CmtRCommitPhaseOutput> waitCommitments(ArrayList<CmtRCommitPhaseOutput> listCommit) throws IllegalArgumentException, IOException{
		
		while(true){
	    	 for (PartyData m : connections.keySet()){
	    		Map<String, Channel> i = connections.get(m);
    	    	Iterator it = i.values().iterator();
    	    	dlog = new MiraclDlogECF2m("K-233");
    	    	while(it.hasNext()){
    				try {
    					CmtReceiver receiver = new MyCmtReceiver((Channel) it.next(), dlog, new SecureRandom());
    					receiverList.add(receiver);
    					System.out.println(((MyCmtReceiver)receiver).getH());
    					
    		    	    //Receive the commitment on the commit value
    		    	    CmtRCommitPhaseOutput output = receiver.receiveCommitment();
    		    	    
    		    	    //Get information to add at the hashtable of the primary
    					TestRandomShareCommitMsg msg = ((MyCmtReceiver)receiver).getCommMap();
    					valueTable.put(m.hashCode(), msg);
    					
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
	    	 System.out.println(valueTable.toString());
	    	 return listCommit;
		}
	}
}
