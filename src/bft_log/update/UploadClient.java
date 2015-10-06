package bft_log.update;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import bft_log.ComputationConfig;
import bft_log.Host;
import bft_log.Utils;
import bft_log.aontrs.Aont;
import bft_log.aontrs.ReedSolomonShardGenerator;


public class UploadClient {
	
	//private File file;
	//private Set<byte[]> shares;
	private ComputationConfig config;
	private PublicKey pk;
	private PrivateKey sk;
	private Utils ut;
	private String aontPath;
	
	public UploadClient(PublicKey pk, PrivateKey sk){
		this.config = new ComputationConfig();
		this.pk = pk;
		this.sk = sk;
		this.ut = new Utils();
	}
	
	public void uploadClientFile(File f){
		try {
			
			//Generate an AONT package
			Aont fileAontPackage = new Aont();
			fileAontPackage.aontPackage = fileAontPackage.AontEncoding(f);
			aontPath = f.getAbsolutePath() + ".aont";
			fileAontPackage.getAontEncodedPackageAsFile(aontPath);
			
			//Generates shards of the AONT package
			File packageToShard = new File (aontPath);
			ReedSolomonShardGenerator shardGen = new ReedSolomonShardGenerator(packageToShard);
			shardGen.ShardGenerator();
			int idFile = f.getName().hashCode();
				
			//Call the upload Protocol.
			uploadClientProtocol(idFile, packageToShard);
			
		} catch (IOException | ClassNotFoundException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
	}
	
	// The client connects to each Server independently.
	// To each server, one specific shard is sent.
	// A Server receives one shard per ID.
	public void uploadClientProtocol(int id ,File share) throws ClassNotFoundException, IOException{
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		int counterAck = 0;
		
		for (int i=0; i<config.listServer.size();i++){
			Host s = config.listServer.get(i);
			String ipSocket = s.getIp().toString();
			try{
				//Prepare the Shard i.
				File shardFile = new File (share.getParentFile(), share.getName() + ".share" + i);
				byte[] shareValue = ut.getBytesFromFile(shardFile);
				
				//Connects to Node i.
				Socket sock = new Socket(s.getIp().getHostString(), s.getPort());
				out = new ObjectOutputStream(sock.getOutputStream());
				in = new ObjectInputStream(sock.getInputStream());
				
				//Generates an UploadMessage for the specific data item
				UploadMessage msg = new UploadMessage(id, s.getId(), shareValue, "root");
				
				//Sign message and includes public key.
				msg.setSignedDigest(sk);
				msg.setPk(pk);
				
				//Forwards UploadMessage to Node i.
				out.writeObject(msg);
				
				//Receives an Acknowledge Message from Node i.
				AcknowledgeUploadMessage msgFromServer = null;
				msgFromServer = (AcknowledgeUploadMessage) in.readObject();
				
				
				//TODO Implement a verification that tries to match if the Acknowledgment is good for the UploadMessage sent earlier.

				//Updates the counter of ACK messages received.
				counterAck += 1;
				
				//TODO here I am just using a trick, that stops to sends after he successfully sent to n nodes. 
				//It would be better if the configuration would load the list of the n nodes. (now we have more than n, and some are not active).
				
				//If it receives N ACK messages, the Protocol has been completed successfully.
				if (counterAck == config.n){
					System.out.println("Upload Protocol terminated successfully");
					sock.close();
					return;
				}
				sock.close();
			} catch (ConnectException e) {
			    System.out.println("Connection Failed : " + ipSocket.toString() + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				out.close();
				in.close();
			}
		}
		System.out.println("Upload Protocol failed");
	}
}
