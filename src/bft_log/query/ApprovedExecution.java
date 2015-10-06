package bft_log.query;

import java.io.Serializable;
import java.util.Arrays;

public class ApprovedExecution implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3844007459633755917L;
	private byte[] signedDigest;
	private int executionNode;
	
	public ApprovedExecution(QueryMessage qm){
		this.signedDigest = qm.signedDigest;
		this.executionNode = qm.executionNode;
	}

	@Override
	public String toString() {
		return "ApprovedExecution [signedDigest=" + Arrays.toString(signedDigest) + ", executionNode=" + executionNode
				+ "]";
	}

	public byte[] getSignedDigest() {
		return signedDigest;
	}

	public int getExecutionNode() {
		return executionNode;
	}
	
}
