package bft_log;

import java.io.Serializable;
import java.util.Date;

public class UploadMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 55555L;
	private int id;
	private int nodeId;
	private byte[] share;
	private String policyGroup;
	private Date ts;
	private byte[] digest;
	private boolean acknowledge;

	public UploadMessage(int id, int node, byte[] shareI, String policy){
		this.id = id;
		this.nodeId = node;
		this.share = shareI;
		this.policyGroup = policy;
		this.ts = new Date();
	}

	@Override
	public String toString() {
		return "UploadMessage [id=" + id + ", nodeId=" + nodeId + ", policyGroup=" + policyGroup + ", ts=" + ts
				+ ", acknowledge=" + acknowledge + "]";
	}

	public int getId() {
		return id;
	}

	public byte[] getShare() {
		return share;
	}

	public String getPolicyGroup() {
		return policyGroup;
	}
	
	public void receivedUploadMessage(){
		this.acknowledge = true;
	}
	
}
