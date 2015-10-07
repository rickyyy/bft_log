package bft_log.query;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;

import com.sun.org.apache.xml.internal.security.c14n.implementations.UtfHelpper;

import bft_log.ComputationConfig;
import bft_log.Log;
import bft_log.Utils;
import bft_log.update.UploadServer;
import bftsmart.tom.MessageContext;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import bftsmart.tom.ServiceReplica;


public class QueryServer extends DefaultRecoverable {
	private int myId;
	public static int counter = 0;	//It is used as entry index for the log. Constantly increasing.
	private Log bftLog;
	private Utils ut;
	private ServiceReplica replica;	//Byzantine fault tolerant consensus server.
	private UploadServer upServer;	//Server that handles the logic of the applicaiton.
	private boolean iAmExecutionNode;
	private ComputationConfig config;
	private PrivateKey sk;
	private PublicKey pk;
	
	//Constructor without crypto keys
	public QueryServer (int id) throws IOException, ClassNotFoundException {
		this.ut = new Utils();
		this.myId = id;
		this.bftLog = new Log();
		this.replica = new ServiceReplica(id, this, this);
	}
	
	//Constructor with crypto keys
	public QueryServer (int id, PrivateKey sk1, PublicKey pk1) throws IOException, ClassNotFoundException {
		this.myId = id;
		this.sk = sk1;
		this.pk = pk1;
		this.bftLog = new Log();
		this.replica = new ServiceReplica(id, this, this);
	}
	
	public void setUpServer(UploadServer upServer) {
		this.upServer = upServer;
	}

	public void setConfig(ComputationConfig config) {
		this.config = config;
	}
	
	// This method is never used, because we always want to execute all the operations in order.
	@Override
	public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
		ByteArrayInputStream bis = new ByteArrayInputStream(command);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			byte[] resultBytes = null;
			QueryMessage q = (QueryMessage) in.readObject();
			q.printQuery();
			if (q != null){
				resultBytes = q.toString().getBytes();
			}
			return resultBytes;
			
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bis.close();
			} catch (IOException ex){
				//ignore close exception
			}
			try {
				if (in!=null){
					in.close();
				}
			} catch (IOException ex){
				//ignore close exception
			}
		}
		return null;
	}

	@Override
	public void installSnapshot(byte[] state) {
		ByteArrayInputStream bis = new ByteArrayInputStream(state);
	    try {
	        ObjectInput in = new ObjectInputStream(bis);
	        bftLog = (Log)in.readObject();
	        in.close();
	        bis.close();
	    } catch (ClassNotFoundException e) {
	        System.out.print("Coudn't find Log: " + e.getMessage());
	        e.printStackTrace();
	    } catch (IOException e) {
	        System.out.print("Exception installing the application state: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	@Override
	public byte[] getSnapshot() {
		try {
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ObjectOutputStream out = new ObjectOutputStream(bos);
	        out.writeObject(bftLog);
	        out.flush();
	        out.close();
	        bos.close();
	        return bos.toByteArray();
	    } catch (IOException e) {
	        System.out.println("Exception when trying to take a + " +
	                "snapshot of the application state" + e.getMessage());
	        e.printStackTrace();
	        return new byte[0];
	    }
	}

	@Override
	public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs) {
		byte [][] replies = new byte[commands.length][];
        for (int i = 0; i < commands.length; i++) {
            if(msgCtxs != null && msgCtxs[i] != null) {
            try {
				replies[i] = executeOrdered(commands[i],msgCtxs[i]);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            } else
				try {
					executeOrdered(commands[i],null);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
        return replies;
	}
	
	//This method describes how QueryMessage are processed by the server. The communication part is oblivious to us. Everything
	//is handled by the BFT-Smart library. 
	public byte[] executeOrdered(byte[] command, MessageContext msgCtx) throws ClassNotFoundException, IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(command);
		ObjectInput in = null;
		try {
			//Receive a QueryMessage
			in = new ObjectInputStream(bis);
			byte[] resultBytes = null;
			QueryMessage q = (QueryMessage) in.readObject();
			
			try {
				//Verify the correctness of the signature and the digest.
				if (q.ut.verifySignedDigest(q.pk, q.digest, q.signedDigest) && q.verifyHash(q.requestedItems, q.operation, q.ts, q.rand)){
					//Counter determines the index in the log. It constantly increases.
					counter += 1;
					
					//Compute (deterministically) who is the Execution Node, and add this value to the request.
					int executionNode = mapToNode(q.rand, q.MAX_SIZE);
					q.setExecutionNode(executionNode);
					
					//Add the Query request to the log.
					bftLog.addEntry(counter, q);
					
					//As debug: Print the current status of the log.
					System.out.println(bftLog.toString());
					
					//If this node is NOT the Execution Node, sends the shares to the Execution Node.
					if (executionNode != this.myId){
						upServer.sendShare(q);
					} else {
						System.out.println("I am node " + String.valueOf(this.myId) + " and I am the execution node");
						upServer.executionQuerySetup(q);
					}
					
					ApprovedExecution aex = new ApprovedExecution(q);
					
					if (aex != null){
						//Sends back this message to the client.
						resultBytes = ut.ObjectToByte(aex);
					}
					return resultBytes;
				}
				else {
					//If the query is not validated. Print on standard output error messages.
					//TODO implement it through real Exceptions.
					String s = "Warning: Signature AND/OR Digest CORRUPTED!";
					System.out.println("Query is discarded: signature or digest are tampered");
					System.out.println(bftLog.toString());
					
					//Sends back the warning message to the user.
					resultBytes = s.getBytes();
					return resultBytes;
				}
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				e.printStackTrace();
			}
			
			return null;

		} finally {
			try {
				bis.close();
			} catch (IOException ex){
				//ignore close exception
			}
			try {
				if (in!=null){
					in.close();
				}
			} catch (IOException ex){
				//ignore close exception
			}
		}
	}
	
	//Based on the existing number of nodes, Maps the random value generated by the user
	//to one of the N nodes. The chosen node is defined as Execution Node.
	public int mapToNode (int random, int MAX_SIZE){
		int totalNumberNodes = this.config.n;
		double executionNode = (random/(double)MAX_SIZE)*totalNumberNodes;
		System.out.print("(" + random + "/" + MAX_SIZE + ")* " + totalNumberNodes + "= " + executionNode);
		return (int)executionNode;
	}
	
}
