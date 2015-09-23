package bft_log;

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

import bftsmart.tom.MessageContext;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;
import bftsmart.tom.ServiceReplica;


public class ComputationServer extends DefaultRecoverable {
	public static int counter = 0;
	private Log bftLog;
	public ComputationServer (int id) {
		bftLog = new Log();
		new ServiceReplica(id, this, this);
	}
	
	public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: ComputationServer <server id>");
            System.exit(0);
        }

        new ComputationServer(Integer.parseInt(args[0]));
		System.out.println("Server Created");
    }

	@Override
	public byte[] executeUnordered(byte[] command, MessageContext msgCtx) {
		ByteArrayInputStream bis = new ByteArrayInputStream(command);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			byte[] resultBytes = null;
			Query q = (Query) in.readObject();
			q.printQuery();
			if (q != null){
				resultBytes = q.toString().getBytes();
			}
			return resultBytes;
			
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
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
		System.out.println("I am in appExecuteBatch");
		byte [][] replies = new byte[commands.length][];
        for (int i = 0; i < commands.length; i++) {
            if(msgCtxs != null && msgCtxs[i] != null) {
            try {
				replies[i] = executeOrdered(commands[i],msgCtxs[i]);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            } else
				try {
					executeOrdered(commands[i],null);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
			Query q = (Query) in.readObject();
			// Proper validation of the signature and digest of the query
			try {
				if (q.verifySignedDigest() && q.verifyHash(q.requestedItems, q.operation, q.ts, q.rand)){
					counter += 1;
					int executionNode = mapToNode(q.rand, q.MAX_SIZE);
					q.setExecutionNode(executionNode);
					bftLog.addEntry(counter, q);
					System.out.println(bftLog.toString());
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchProviderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SignatureException e) {
				// TODO Auto-generated catch block
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
	
	//TODO the total number of nodes should be taken dynamically from the configuration of the system
	private int mapToNode (int random, int maxRange){
		int totalNumberNodes = 4;
		double executionNode = (random/(double)maxRange)*totalNumberNodes;
		System.out.print("(" + random + "/" + maxRange + ")* " + totalNumberNodes + "= " + executionNode);
		return (int)executionNode;
	}
}
