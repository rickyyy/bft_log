package bft_log.query.result;

import java.io.Serializable;
import java.util.Arrays;

//This class represents the result sent from the server to the client for a previously approved query.
public class Result implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7103003026571898099L;
	private int idQuery;
	private byte[] signedDigest;
	private String result;	//the type of the result highly depends on the application. For simplicity here we send back a string.
	
	public Result(int id, byte[] digest){
		this.idQuery = id;
		this.signedDigest = digest;
	}

	public String getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "Result [idQuery=" + idQuery + ", signedDigest=" + Arrays.toString(signedDigest) + ", result=" + result
				+ "]";
	}

	public void setResult(String result) {
		this.result = result;
	}

	public int getIdQuery() {
		return idQuery;
	}

	public byte[] getSignedDigest() {
		return signedDigest;
	}
}
