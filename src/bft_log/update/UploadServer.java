package bft_log.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Hashtable;
import java.util.Iterator;

import bft_log.ComputationConfig;
import bft_log.Host;
import bft_log.ShareStorage;
import bft_log.Utils;
import bft_log.query.ExecutionMessage;
import bft_log.query.QueryMessage;

public class UploadServer {
	private int id;
	private int PORT;
	private ServerSocket uploadChannel;
	private ComputationConfig conf;
	private Utils ut;
	private Hashtable<Integer, ShareStorage> store;	//Storage in the future can also be implemented through a SQL DB. For simplicity we use a Hashmap in memory.
	private String serverDiskPath;
	
	public UploadServer(int id) throws IOException, ClassNotFoundException{
		this.id = id;
		this.conf = new ComputationConfig();
		this.ut = new Utils();
		this.store = new Hashtable<Integer, ShareStorage>();
		System.out.println("Starting Upload Server ID: " + String.valueOf(this.id));
	}
	
	private void initializeUploadServer() throws IOException{
		Host h = conf.listServer.get(id); 
		this.PORT = h.getPort();
		this.uploadChannel = new ServerSocket(PORT);
		serverEnvironment();
	}
	
	private void serverEnvironment(){
		this.serverDiskPath = conf.appPath + "test/server" + String.valueOf(this.id);
		File folder = new File(serverDiskPath);
		if (folder.mkdirs()){
			System.out.println("Server Folder Created Successfully!!!");
		}
		else {
			System.out.println("Failed to Create the Server Folder - Or it already exists");
		}
	}
	
	public void startUploadServer() throws IOException, ClassNotFoundException{
		initializeUploadServer();
		while(true){
			Socket clientSocket = uploadChannel.accept();

			ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
			
			//Receive the incoming message from the connected client.
			Object obj = inFromClient.readObject();
			
			if (obj instanceof UploadMessage){	
				UploadMessage recMsg = (UploadMessage) obj;
				if (verifyUploadMessage(recMsg) && storeShareInMemory(recMsg.getId(), recMsg.getShare(), recMsg.getPolicyGroup())){
					recMsg.setAcknowledge();
					storeShareOnDisk(recMsg);
					AcknowledgeUploadMessage ack = new AcknowledgeUploadMessage(recMsg.getId(), recMsg.getNodeId(), recMsg.getSignedDigest());
					outToClient.writeObject(ack);
				} else {
					System.out.println("Upload Message Signature OR Digest Not Valid! Message Discarded\n");
				}
			} else if (obj instanceof ExecutionMessage){
				ExecutionMessage exec = (ExecutionMessage) obj;
				ut.getFileFromBytes(exec.getShardSent(), this.serverDiskPath + "/" + exec.getItemRequested().hashCode() + ".share" + String.valueOf(this.id));
				System.out.println("Execution Message from Node " + exec.getSendingNode());
				System.out.println("ExecMessage (exNode, ItemReq): " + exec.getExecutionNode() + " " + exec.getItemRequested());
				outToClient.writeObject(exec);
			} else {
				System.out.println("Object Not Recognized.");
			}
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
	
	private boolean storeShareInMemory(Integer id, byte[] share, String policy){
		ShareStorage ss = new ShareStorage(share, policy);
		if (store.get(id)==null){
			store.put(id, ss);
			System.out.println("Share added locally!");
			return true;
		}
		else{
			System.out.println("COLLISION!!! A Share with same ID has been already stored.");
			return false;
		}
	}
	
	private void storeShareOnDisk(UploadMessage u){
		String onDiskPath = serverDiskPath + "/" + u.getId() + ".share" + String.valueOf(this.id);
		ut.getFileFromBytes(u.getShare(), onDiskPath);
	}
	
	public void sendShare(QueryMessage q){
		ExecutionMessage exec = new ExecutionMessage(q);
		Host exNode = this.conf.listServer.get(exec.getExecutionNode());
		for (String s : q.requestedItems){
			Path path = Paths.get(this.serverDiskPath + "/" + s.hashCode() + ".share" + String.valueOf(this.id));
			try {
				byte[] shard = Files.readAllBytes(path);
				exec.setItemRequested(s);
				exec.setShardToSend(shard);
				exec.setSendingNode(this.id);
				Socket sock = new Socket(exNode.getIp().getHostString(), exNode.getPort());
				ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				out.writeObject(exec);
				ExecutionMessage miao = (ExecutionMessage) in.readObject();
				System.out.println(String.valueOf(miao.getItemRequested()));
				sock.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}

