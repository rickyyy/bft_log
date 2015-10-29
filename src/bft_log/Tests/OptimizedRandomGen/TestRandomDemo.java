package bft_log.Tests.OptimizedRandomGen;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import com.sun.glass.ui.Size;

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
		
		//remove nodes that are not needed. In this case we load from the conf file 5 nodes, but only 4 are working. Therefore we remove 1 from the list.
		if (list.size()>conf.n){
			int remove = conf.n-list.size();
			for(; remove>0;remove--){
				list.remove(conf.n);
			}
		}
		
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
		System.out.println("Time starting the protocol: " + LocalDateTime.now());
		if (primaryIndex==myId){	//I am the primary node
			
			//Update the list with the received commitments
    	    TestRandomPrimary testCmtSel = new TestRandomPrimary(connections, conf);
    	    testCmtSel.waitCommitments();
    	    
		} else {	//I am one of the other nodes. I have to commit. If I am selected, I then decommit.
			
			//Take the channel to the primary node
			Map<String, Channel> primaryCh = connections.get(listOfParties.get(myId));
			
			TestRandomReplica cmtToPrimary = new TestRandomReplica(primaryCh, myself);
			cmtToPrimary.sendToPrimary(myId);
			cmtToPrimary.send(connections);
		}
	}
}
