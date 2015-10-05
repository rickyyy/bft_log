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
import java.security.SignatureException;

import bft_log.ComputationConfig;
import bft_log.Log;
import bft_log.update.UploadServer;
import bftsmart.tom.MessageContext;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import bftsmart.tom.ServiceReplica;


public class QueryServer extends DefaultRecoverable {
	private int myId;
	public static int counter = 0;	//It is used as entry index for the log. Constantly increasing.
	private Log bftLog;
	private ServiceReplica replica;
	private UploadServer upServer;
	private boolean iAmExecutionNode;
	private ComputationConfig config;
	
	public QueryServer (int id) throws IOException, ClassNotFoundException {
		this.myId = id;
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
	
	public byte[] executeOrdered(byte[] command, MessageContext msgCtx) throws ClassNotFoundException, IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(command);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			byte[] resultBytes = null;
			QueryMessage q = (QueryMessage) in.readObject();
			// Proper validation of the signature and digest of the query
			try {
				if (q.ut.verifySignedDigest(q.pk, q.digest, q.signedDigest) && q.verifyHash(q.requestedItems, q.operation, q.ts, q.rand)){
					counter += 1;
					System.out.println(String.valueOf(q.rand));
					int executionNode = mapToNode(q.rand, q.MAX_SIZE);
					q.setExecutionNode(executionNode);
					bftLog.addEntry(counter, q);
					System.out.println(bftLog.toString());
					if (executionNode != this.myId){
						System.out.println("I AM NODE " + String.valueOf(this.myId) + " AND I AM NOT THE EXECUTION NODE");
						upServer.sendShare(q);
					}
					if (q != null){
						resultBytes = q.toString().getBytes();
					}
					return resultBytes;
				}
				else {
					String s = "Warning: Signature AND/OR Digest CORRUPTED!";
					System.out.println("Query is discarded: signature or digest are tampered");
					System.out.println(bftLog.toString());
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
	
	public int mapToNode (int random, int MAX_SIZE){
		int totalNumberNodes = this.config.n;
		double executionNode = (random/(double)MAX_SIZE)*totalNumberNodes;
		System.out.print("(" + random + "/" + MAX_SIZE + ")* " + totalNumberNodes + "= " + executionNode);
		return (int)executionNode;
	}
	
}