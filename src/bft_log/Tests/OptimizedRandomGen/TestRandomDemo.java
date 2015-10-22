package bft_log.Tests.OptimizedRandomGen;

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
import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.multiPartyComm.MultipartyCommunicationSetup;
import edu.biu.scapi.comm.multiPartyComm.SSLSocketMultipartyCommunicationSetup;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SocketPartyData;
import edu.biu.scapi.exceptions.DuplicatePartyException;

public class TestRandomDemo {
	public static void main(String[] args) throws SSLException, DuplicatePartyException, IOException, TimeoutException{
		if (args.length < 1) {
            System.out.println("Usage: TestSSLPrimary <server1 id>");
            System.exit(0);
        }
		ComputationConfig conf = new ComputationConfig();
		int myId = Integer.valueOf(args[0]);
		int primaryIndex = 0;	//TODO This should be loaded dynamically
		List<Host> list = conf.listServer;
			
		//Just because in the config file there is one server more than needed. We want to remove it from here because we do not use it.
		list.remove(4);
		
		List<PartyData> listOfParties = new ArrayList<>();
		for (Host i : list){
			InetAddress ip = i.getIp().getAddress();
			int port = i.getPort();
			PartyData p = new SocketPartyData(ip, port);
			listOfParties.add(p);
		}
		
		PartyData myself = listOfParties.get(myId);
		
		//Based on the myId value, it is defined what is the current running node (this is done in case of having a single properties file for all the servers)
		//In case the server has its own file, as long as it is properly ordered (i.e., it has its own IP as IP0) it is also fine.
		PartyData tmp = listOfParties.get(0);
		PartyData orig = listOfParties.get(myId);
		listOfParties.remove(0);
		listOfParties.add(0, orig);
		listOfParties.remove(myId);
		listOfParties.add(myId, tmp);
		
		MultipartyCommunicationSetup setup = new SSLSocketMultipartyCommunicationSetup(listOfParties, "changeit");
		HashMap<PartyData, Object> connectionsPerParty = new HashMap<PartyData, Object>();
		
		connectionsPerParty.put(listOfParties.get(1), 2);
		connectionsPerParty.put(listOfParties.get(2), 2);
		connectionsPerParty.put(listOfParties.get(3), 2);
		
		System.out.println(connectionsPerParty.size());
		
		Map<PartyData, Map<String, Channel>> connections = setup.prepareForCommunication(connectionsPerParty, 2000000);
		
		System.out.println(connections);
		System.out.println("All connections are established");
		
		if (primaryIndex==myId){	//I am the primary node
			
			//Update the list with the received commitments
    	    TestRandomPrimary testCmtSel = new TestRandomPrimary(connections);
    	    testCmtSel.waitCommitments();
    	    
    	    
//    	    //Send the selection to other replicas
//    	    for (Map<String, Channel> i : connections.values()){
//	    		 Iterator it = i.values().iterator();
//    	    	 while(it.hasNext()){
//    	    		 Channel ch = (Channel) it.next();
//    	    		 ch.send(listCommit);
//    	    		 System.out.println("Message sent");
//    	    	 }
//	    	 }
		} else {	//I am one of the other nodes. I have to commit. If I am selected, I then decommit.
			
			//Take the channel to the primary node
			Map<String, Channel> primaryCh = connections.get(listOfParties.get(myId));
			
			TestRandomReplica cmtToPrimary = new TestRandomReplica(primaryCh, myself);
			cmtToPrimary.sendToPrimary(myId);
			cmtToPrimary.send(connections);
			//TODO Until here it works. Now we have to verify the commit message.
			
			//Receive the selected commit from the primary node
//			for (Channel i : primaryCh.values()){
//				try {
//					System.out.println("Received Commit Set");
//					listCommit = (ArrayList<CmtRCommitPhaseOutput>) i.receive();
//					System.out.println("(replica) Size of list Commit: " + listCommit.size());
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}
	}
}
