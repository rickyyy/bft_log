package bft_log.update;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;

import bft_log.ComputationConfig;
import bft_log.Host;
import bft_log.Utils;

public class UploadServer {
	private int id;
	private int PORT;
	private ServerSocket uploadChannel;
	private ComputationConfig conf;
	private Utils ut;
	
	public UploadServer(int id) throws IOException, ClassNotFoundException{
		this.id = id;
		conf = new ComputationConfig();
		this.ut = new Utils();
		System.out.println("Starting Upload Server ID: " + String.valueOf(this.id));
		initializeUploadServer();
		startUploadServer();
	}
	
	private void initializeUploadServer() throws IOException{
		Host h = conf.listServer.get(id); 
		this.PORT = h.getPort();
		this.uploadChannel = new ServerSocket(PORT);
	}
	
	private void startUploadServer() throws IOException, ClassNotFoundException{
		while(true){
			Socket clientSocket = uploadChannel.accept();
			//System.out.println("Upload Socket Extablished with Client ...");
			ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
			
			UploadMessage recMsg = (UploadMessage) inFromClient.readObject();
			
			//Incoming message is verified
			if (verifyUploadMessage(recMsg)){
				recMsg.receivedUploadMessage();
				outToClient.writeObject(recMsg);
			} else {
				System.out.println("Upload Message Signature OR Digest Not Valid! Message Discarded\n");
			}
			
			//System.out.println("Message received: " + String.valueOf(recMsg.getId()));
			outToClient.close();
			clientSocket.close();
		}
	}
	
	//Here is implemented the validity of the message. From digest and signatures to policies
	//TODO still policies have to be implemented.
	private boolean verifyUploadMessage(UploadMessage msg){
		System.out.println(this.ut.toString());
		boolean verify = false;
		byte[] signature = msg.getSignedDigest();
		PublicKey pk = msg.getPk();
		String s = msg.uploadMessageEncoding();
		System.out.println(s);
		try {
			byte[] digest = ut.createDigest(s);
			verify = ut.verifySignedDigest(pk, digest, signature);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
			e.printStackTrace();
		}
		return verify;
	}
}
