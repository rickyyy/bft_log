package bft_log.update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import bft_log.ComputationConfig;
import bft_log.Host;


public class UploadClient {
	
	//private File file;
	//private Set<byte[]> shares;
	private ComputationConfig config;
	private PublicKey pk;
	private PrivateKey sk;
	
	public UploadClient(PublicKey pk, PrivateKey sk){
		this.config = new ComputationConfig();
		this.pk = pk;
		this.sk = sk;
	}
	
	//The client connects to each single server and it does independent uploads. Now it works in sequence. Having multiple threads opening different sockets could be an alternative.
	public void uploadClient() throws ClassNotFoundException, IOException{
		
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		String exampleShare = "ExampleShare";
		int counterAck = 0;
		byte[] shareValue = exampleShare.getBytes();
		
		for (int i=0; i<config.listServer.size();i++){
			Host s = config.listServer.get(i);
			String ipSocket = s.getIp().toString();
			try{
			    //System.out.println("Connecting to ... " + ipSocket.toString() + "\n");
				Socket sock = new Socket(s.getIp().getHostString(), s.getPort());
				out = new ObjectOutputStream(sock.getOutputStream());
				in = new ObjectInputStream(sock.getInputStream());
				UploadMessage msg = new UploadMessage(2, s.getId(), shareValue, "root");
				msg.setSignedDigest(sk);
				msg.setPk(pk);
				System.out.println(msg.toString());
				out.writeObject(msg);
				AcknowledgeUploadMessage msgFromServer = null;
				msgFromServer = (AcknowledgeUploadMessage) in.readObject();
				//TODO Implement a verification that tries to match if the Acknowledgment is good for the UploadMessage sent earlier.
				//System.out.print(msgFromServer.toString() + "\n");
				counterAck += 1;
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
		//If clients receive n valid acknowledge messages, then the protocol finishes successfully.
		if (counterAck == config.n){
			System.out.println("Upload Protocol terminated successfully");
		}
		else {
			System.out.println("Upload Protocol failed");
		}
	}
}
