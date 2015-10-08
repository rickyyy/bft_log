package bft_log.query.execution;

import java.io.Serializable;
import java.util.Arrays;

import bft_log.query.QueryMessage;

public class ApprovedExecution implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3844007459633755917L;
	private byte[] signedDigest;
	private int executionNode;
	private int idApprovedQuery;
	
	public ApprovedExecution(QueryMessage qm){
		this.idApprovedQuery = qm.id;
		this.signedDigest = qm.signedDigest;
		this.executionNode = qm.executionNode;
	}

	@Override
	public String toString() {
		return "ApprovedExecution [signedDigest=" + Arrays.toString(signedDigest) + ", executionNode=" + executionNode
				+ ", idApprovedQuery=" + idApprovedQuery + "]";
	}

	public int getIdApprovedQuery() {
		return idApprovedQuery;
	}

	public byte[] getSignedDigest() {
		return signedDigest;
	}

	public int getExecutionNode() {
		return executionNode;
	}
	
}
