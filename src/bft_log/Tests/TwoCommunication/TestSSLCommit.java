package bft_log.Tests.TwoCommunication;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import edu.biu.scapi.comm.Channel;
import edu.biu.scapi.comm.twoPartyComm.LoadSocketParties;
import edu.biu.scapi.comm.twoPartyComm.PartyData;
import edu.biu.scapi.comm.twoPartyComm.SSLSocketCommunicationSetup;
import edu.biu.scapi.comm.twoPartyComm.TwoPartyCommunicationSetup;
import edu.biu.scapi.exceptions.CheatAttemptException;
import edu.biu.scapi.exceptions.CommitValueException;
import edu.biu.scapi.exceptions.DuplicatePartyException;
import edu.biu.scapi.exceptions.InvalidDlogGroupException;
import edu.biu.scapi.exceptions.SecurityLevelException;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitValue;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCommitter;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtRCommitPhaseOutput;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtReceiver;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenCommitter;
import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.pedersen.CmtPedersenReceiver;
import edu.biu.scapi.primitives.dlog.DlogGroup;
import edu.biu.scapi.primitives.dlog.miracl.MiraclDlogECF2m;


/*
 * This test is used to get a bit of practice with the Scapi library. To verify that the connections is established, run two different instances of this
 * class. 
 * 
 * TestSSL 0 1 (creates a server0 trying to connect to a server 1)
 * TestSSL 1 0 (creates a server1 trying to connect to a server 0)
 * 
 * The two arguments can be any of the ids used in the properties file: SSLFile.
 * 
 * At this point the connection is successfully established.
 */
public class TestSSLCommit {
	public static void main(String[] args) throws SSLException, DuplicatePartyException, IOException, TimeoutException{
		if (args.length < 2) {
            System.out.println("Usage: TestSSL <server1 id> <server2 id>");
            System.exit(0);
        }
		int server1 = Integer.parseInt(args[0]);
		int server2 = Integer.parseInt(args[1]);
		LoadSocketParties loadParties = new LoadSocketParties("config/SSLFile");
		List<PartyData> listOfParties = loadParties.getPartiesList();
		TwoPartyCommunicationSetup commSetup = new SSLSocketCommunicationSetup(listOfParties.get(server1), listOfParties.get(server2), "changeit");
		
		//Call the prepareForCommunication function to establish one connection within 2000000 milliseconds.
	    Map<String, Channel> connections = commSetup.prepareForCommunication(1, 2000000);
	    
	    System.out.println("Connection Successfully established!\n");

	    //Return the channel with the other party. There was only one channel created.
	    System.out.println(connections.values().toString());
	    
	    for (Channel i : connections.values()){
		    try {
		    	//System.out.println("SIZE OF CONNECTIONS: " + connections.values().size() + "\n");
		    	//create the committer
		    	DlogGroup dlog = new MiraclDlogECF2m("K-233");
			    System.out.println("dlog generated\n");
	
				CmtCommitter committer = new CmtPedersenCommitter(i, dlog, new SecureRandom());
			    
				//generate CommitValue from string
			    CmtCommitValue val = committer.generateCommitValue(new String("123456789").getBytes());
			    
			    //Commit on the commit value with id 2
			    committer.commit(val, 2);
			    
			    //decommit id 2
			    committer.decommit(2);
	    	    
			} catch (ClassNotFoundException | CheatAttemptException | SecurityLevelException
					| InvalidDlogGroupException | CommitValueException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
	    }
	}
}
