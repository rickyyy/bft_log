package bft_log.query;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import bft_log.ComputationConfig;
import bft_log.Host;
import bft_log.Tests.OptimizedRandomGen.TestRandomPrimary;
import bft_log.Tests.OptimizedRandomGen.TestRandomReplica;
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.multiPartyComm.MultipartyCommunicationSetup;
import edu.biu.scapi.comm.multiPartyComm.SSLSocketMultipartyCommunicationSetup;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;

public class DistributedRandomNumberGenerator {
	private ComputationConfig conf;
	private int myId;
	private int primaryIndex;
	private List<PartyData> listOfParties;
	private PartyData myself;
	private Map<PartyData, Map<String, Channel>> connections;
	
	public DistributedRandomNumberGenerator (ComputationConfig c, int myId){
		this.conf = c;
		this.myId = myId;
		this.primaryIndex = c.getLm().getCurrentLeader();
		setupConnections();
	}
	
	private void setupConnections(){
		listOfParties = new ArrayList<>();
		List<Host> list = conf.listServer;
		
		for (Host i : list){
			InetAddress ip = i.getIp().getAddress();
			int port = i.getPort()+1;
			PartyData p = new SocketPartyData(ip, port);
			listOfParties.add(p);
		}
		
		this.myself = listOfParties.get(myId);
		
		//Based on the myId value, it is defined what is the current running node (this is done in case of having a single properties file for all the servers)
		//In case the server has its own file, as long as it is properly ordered (i.e., it has its own IP as IP0) it is also fine.
		PartyData tmp = listOfParties.get(0);
		PartyData orig = listOfParties.get(myId);
		listOfParties.remove(0);
		listOfParties.add(0, orig);
		listOfParties.remove(myId);
		listOfParties.add(myId, tmp);
		
		MultipartyCommunicationSetup setup;
		try {
			setup = new SSLSocketMultipartyCommunicationSetup(listOfParties, "changeit");
			HashMap<PartyData, Object> connectionsPerParty = new HashMap<PartyData, Object>();
			
			connectionsPerParty.put(listOfParties.get(1), 2);
			connectionsPerParty.put(listOfParties.get(2), 2);
			connectionsPerParty.put(listOfParties.get(3), 2);
			
			System.out.println(connectionsPerParty.size());
			
			this.connections = setup.prepareForCommunication(connectionsPerParty, 2000000);
		} catch (SSLException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected Integer runProtocol() throws IllegalArgumentException, IOException{
		if (primaryIndex==myId){	//I am the primary node
					
			//Update the list with the received commitments
    	    TestRandomPrimary testCmtSel = new TestRandomPrimary(connections);
    	    testCmtSel.waitCommitments();
    	    return testCmtSel.receiveDecommitments();
    	    
		} else {	//I am one of the other nodes. I have to commit. If I am selected, I then decommit.
			
			//Take the channel to the primary node
			Map<String, Channel> primaryCh = connections.get(listOfParties.get(myId));
			
			TestRandomReplica cmtToPrimary = new TestRandomReplica(primaryCh, myself);
			cmtToPrimary.sendToPrimary(myId);
			return cmtToPrimary.send(connections);
		}
	}
}
