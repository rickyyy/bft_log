package bft_log.query.execution;



import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import bft_log.query.QueryMessage;


//TODO So far it is implemented as it ask only one file at the time.
//This is the message that is sent from Non-Execution Nodes to the Execution Node. It contains some basic information
//the request made by the client, and the shares stored by the server.
public class ExecutionMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8280882380151584571L;
	private int queryID;
	private byte[] clientRequestSignature;
	private String itemRequested;
	private int executionNode;
	private int sendingNode;
	private byte[] shardSent;
	private boolean readyToReceiveShare;
	
	public ExecutionMessage(QueryMessage q){
		this.queryID = q.id;
		this.clientRequestSignature = q.signedDigest;
		this.executionNode = q.executionNode;
		this.readyToReceiveShare = false;
	}

	
	public boolean isReadyToReceiveShare() {
		return readyToReceiveShare;
	}

	public void setReadyToReceiveShare(boolean readyToReceiveShare) {
		this.readyToReceiveShare = readyToReceiveShare;
	}

	public int getQueryID() {
		return queryID;
	}

	public int getSendingNode() {
		return sendingNode;
	}

	public String getItemRequested() {
		return itemRequested;
	}

	public void setItemRequested(String itemRequested) {
		this.itemRequested = itemRequested;
	}

	public void setSendingNode(int sendingNode) {
		this.sendingNode = sendingNode;
	}

	public byte[] getShardSent() {
		return shardSent;
	}

	public void setShardToSend(byte[] shardToSend) {
		this.shardSent = shardToSend;
	}

	public byte[] getClientRequestSignature() {
		return clientRequestSignature;
	}

	public int getExecutionNode() {
		return executionNode;
	}
	
}
