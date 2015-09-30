package bft_log.update;

import java.io.Serializable;

public class AcknowledgeUploadMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1780497009731030698L;
	private int id;
	private int node;
	private byte[] uploadMessageSignature;
	
	public AcknowledgeUploadMessage(int id, int node, byte[] sig){
		this.id = id;
		this.node = node;
		this.uploadMessageSignature = sig;
	}

	public int getId() {
		return id;
	}

	public int getNode() {
		return node;
	}

	public byte[] getUploadMessageSignature() {
		return uploadMessageSignature;
	}
		
}
