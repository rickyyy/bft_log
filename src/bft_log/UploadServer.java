package bft_log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class UploadServer {
	private int id;
	private int PORT;
	private ServerSocket uploadChannel;
	private ComputationConfig conf;
	
	public UploadServer(int id) throws IOException, ClassNotFoundException{
		this.id = id;
		conf = new ComputationConfig();
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
			//System.out.println("Message received: " + String.valueOf(recMsg.getId()));
			recMsg.receivedUploadMessage();
			outToClient.writeObject(recMsg);
			outToClient.close();
			clientSocket.close();
		}
	}
}
