package bft_log.update;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import bft_log.Utils;

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
	private byte[] signedDigest;
	private PublicKey pk;
	
	private boolean acknowledge;
	private Utils ut;
	private String separator = " ";

	public UploadMessage(int id, int node, byte[] shareI, String policy){
		this.id = id;
		this.nodeId = node;
		this.share = shareI;
		this.policyGroup = policy;
		this.ts = new Date();
		initializeDigest();
	}
	
	
	@Override
	public String toString() {
		return "UploadMessage [id=" + String.valueOf(id) + ", nodeId=" + String.valueOf(nodeId) + ", share=" + Arrays.toString(share) + ", policyGroup="
				+ policyGroup + ", ts=" + ts.toString() + ", signedDigest=" + Arrays.toString(signedDigest) + ", pk=" + pk.toString() + "]";
	}

	private void initializeDigest(){
		ut = new Utils();
		String msg = uploadMessageEncoding();
		try {
			this.signedDigest = ut.createDigest(msg);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public boolean isAcknowledge() {
		return acknowledge;
	}

	public void setAcknowledge() {
		this.acknowledge = true;
	}

	public byte[] getSignedDigest() {
		return signedDigest;
	}
	
	public void setSignedDigest(PrivateKey sk) {
		try {
			this.signedDigest = ut.signDigest(sk, this.signedDigest);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException e) {
			e.printStackTrace();
		}
	}

	public void setPk(PublicKey pk) {
		this.pk = pk;
	}

	public PublicKey getPk() {
		return pk;
	}


	/*Encode the attributes of a Query into a single string.*/
	public String uploadMessageEncoding(){
		String s = "" + String.valueOf(this.id) + separator + Arrays.toString(this.share) + separator + this.policyGroup + separator + this.ts.toString() ;
		return s;
	}

	public int getId() {
		return id;
	}

	public int getNodeId() {
		return nodeId;
	}


	public byte[] getShare() {
		return share;
	}

	public String getPolicyGroup() {
		return policyGroup;
	}
	
}
