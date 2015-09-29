package bft_log;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;


public class UploadClient {
	
	//private File file;
	//private Set<byte[]> shares;
	private ComputationConfig config;
	
	public UploadClient(){
		this.config = new ComputationConfig();
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
				UploadMessage msg = new UploadMessage(1, s.getId(), shareValue, "root");
				out.writeObject(msg);
				UploadMessage msgFromServer = null;
				msgFromServer = (UploadMessage) in.readObject();
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