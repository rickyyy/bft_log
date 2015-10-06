package bft_log.update;

import java.io.File;
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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Hashtable;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import bft_log.ComputationConfig;
import bft_log.Host;
import bft_log.ShareStorage;
import bft_log.Utils;
import bft_log.aontrs.Aont;
import bft_log.aontrs.ReedSolomonShardReconstructor;
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
	private PublicKey pk;
	private PrivateKey sk;
	
	//Constructor without crypto keys
	public UploadServer(int id) throws IOException, ClassNotFoundException{
		this.id = id;
		this.conf = new ComputationConfig();
		this.ut = new Utils();
		this.store = new Hashtable<Integer, ShareStorage>();
		//System.out.println("Starting Upload Server ID: " + String.valueOf(this.id));
	}
	
	//Constructor that uses crypto.
	public UploadServer(int id, PrivateKey skInput, PublicKey pkInput) throws IOException, ClassNotFoundException{
		this.id = id;
		this.conf = new ComputationConfig();
		this.ut = new Utils();
		this.store = new Hashtable<Integer, ShareStorage>();
		this.pk = pkInput;
		this.sk = skInput;
		//System.out.println("Starting Upload Server ID: " + String.valueOf(this.id));
	}
	
	//Initialization of Upload Server parameters.
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
	
	// Server Running waiting for messages from Clients or Servers.
	public void startUploadServer() throws IOException, ClassNotFoundException{
		initializeUploadServer();
		while(true){
			Socket clientSocket = uploadChannel.accept();
			ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
			ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
			
			//Receive the incoming message from the connected client.
			Object obj = inFromClient.readObject();
			
			//Procedure to handle Upload messages
			if (obj instanceof UploadMessage){	
				UploadMessage recMsg = (UploadMessage) obj;
				
				//UploadMessage validation: Digital Signature + Hash + Access Control mechanism (not implemented)
				if (recMsg.verifyUploadMessage() && storeShareInMemory(recMsg.getId(), recMsg.getShare(), recMsg.getPolicyGroup())){
					
					//Write the share locally on disk.
					storeShareOnDisk(recMsg);
					
					//Prepare ACK message for the client and send it.
					AcknowledgeUploadMessage ack = new AcknowledgeUploadMessage(recMsg.getId(), recMsg.getNodeId(), recMsg.getSignedDigest());
					outToClient.writeObject(ack);
				} else {
					System.out.println("Upload Message Signature OR Digest Not Valid! Message Discarded\n");
				}
				
				//Procedure to handle Execution messages. Only one node per request (= Execution Node)
				//runs these operations.
			} else if (obj instanceof ExecutionMessage){
				ExecutionMessage exec = (ExecutionMessage) obj;
				
				//TODO check if servers messages are validated or not.
				
				//Set the path where share is stored and write the share locally.
				String pathShare = this.serverDiskPath + "/" + exec.getItemRequested().hashCode() + ".share" + String.valueOf(exec.getSendingNode());
				ut.writeFileFromBytes(exec.getShardSent(), pathShare);
				
				//TODO This runs everytime a message arrives. It should be fixed. It should run once per request.
				//TODO The result should be stored in a file/table. (the client will then ask for result of a request to ex node and
				//the result will be sent to him.
				
				
				//Take the shares for a specific ID and Reconstruct the original file.
				reconstructAontPackage(exec.getItemRequested().hashCode());
				
				//TODO Sends back the result to the client.
				outToClient.writeObject(exec);
			} else {
				System.out.println("Object Not Recognized.");
			}
			outToClient.close();
			clientSocket.close();
		}
	}
	
	//Store in a "HashMap" that functions as a Database. Tuple (Identified, content of the Share, Access Policy)
	private boolean storeShareInMemory(Integer id, byte[] share, String policy){
		ShareStorage ss = new ShareStorage(share, policy);
		if (store.get(id)==null){
			store.put(id, ss);
			System.out.println("Share added locally in the HashTable!");
			return true;
		}
		else{
			System.out.println("COLLISION!!! A Share with same ID has been already stored.");
			return false;
		}
	}
	
	//Write the Upload message on the disk.
	private void storeShareOnDisk(UploadMessage u){
		String onDiskPath = this.serverDiskPath + "/" + u.getId() + ".share" + String.valueOf(this.id);
		ut.writeFileFromBytes(u.getShare(), onDiskPath);
	}
	
	
	//Reconstruct the shares stored locally for a specific ID. It generates an intermediate file 
	//that represents the encoded AONT package. It then decode the AONT package and writes the 
	//original message.
	private void reconstructAontPackage(int idFileRequested){
		String pathReconstructed = this.serverDiskPath + "/" + String.valueOf(idFileRequested);
		File reconstruct = new File(pathReconstructed);
		try {
			ReedSolomonShardReconstructor rsr = new ReedSolomonShardReconstructor(reconstruct);
			reconstruct.delete();
			File fileToDecode = new File(pathReconstructed + ".decoded");
			Aont encodedAont = new Aont(fileToDecode);
			byte[] decodedAont = encodedAont.AontDecoding(encodedAont.aontPackage, pathReconstructed);
			fileToDecode.delete();
			rsr.ShardDeletion(this.id);
		} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
	}
	
	//This method is called by QueryServer. A Node, after receiving and approving a certain QueryRequest,
	//it sends to the Execution Node the shares it owns of the file requested in the query. 
	public void sendShare(QueryMessage q){
		ExecutionMessage exec = null;
		
		
		//For each data item requested in the query.
		for (String s : q.requestedItems){
			exec = new ExecutionMessage(q);
			Host exNode = this.conf.listServer.get(exec.getExecutionNode());	//Get the host details of the Execution Node
			Path path = Paths.get(this.serverDiskPath + "/" + s.hashCode() + ".share" + String.valueOf(this.id));
			try {
				//Load the content of the shard stored locally and generate an Execution message.
				byte[] shard = Files.readAllBytes(path);
				exec.setItemRequested(s);
				exec.setShardToSend(shard);
				exec.setSendingNode(this.id);
				
				//Connect to the Execution Node and send to it the Execution message.
				Socket sock = new Socket(exNode.getIp().getHostString(), exNode.getPort());
				ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				out.writeObject(exec);
				
				//Receive the response from the Execution Node.
				//TODO Need to be implemented properly. So far it just receives back what he sent.
				ExecutionMessage miao = (ExecutionMessage) in.readObject();
				
				//Close the connection.
				sock.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}

