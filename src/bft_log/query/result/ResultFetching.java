package bft_log.query.result;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import bft_log.Host;
import bft_log.query.execution.ApprovedExecution;

public class ResultFetching {
	private int idExecNode;
	private Host executionNodeToConnect;
	private Result res;
	
	public ResultFetching(ApprovedExecution aex){
		this.idExecNode = aex.getExecutionNode();
		this.res = new Result(aex.getIdApprovedQuery(), aex.getSignedDigest());
	}
	
	public void getExecNode(ArrayList<Host> list){
		this.executionNodeToConnect = list.get(this.idExecNode);
	}
	
	public String sendResultRequest(){
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		String ipSocket = this.executionNodeToConnect.getIp().toString();
		System.out.println("Connecting to ... " + ipSocket);
		try {
			
			//Connects to the Execution Node. It will reply with the result for the query we asked for.
			Socket sock = new Socket(this.executionNodeToConnect.getIp().getHostString(), this.executionNodeToConnect.getPort());
			System.out.println("Connected!");
			out = new ObjectOutputStream(sock.getOutputStream());
			in = new ObjectInputStream(sock.getInputStream());
			out.writeObject(this.res);
			
			//Get response form server
			Result responseServer = null;
			responseServer = (Result) in.readObject();
			return responseServer.getResult();
			
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error sendResultRequest: " + e.getMessage());
		}
		
		return null;
	}
}
