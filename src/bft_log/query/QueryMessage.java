package bft_log.query;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import bft_log.Utils;


public class QueryMessage implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7836836376114345990L;
	private String separator = " ";
	private String message = "";
	public int MAX_SIZE = (int) Math.pow(2.0, 16.0);
	public int id; //TODO SET ID TO THE QUERY MESSAGE AUTOMATICALLY!
	public Set<String> requestedItems = new HashSet<String>();
	public String operation;
	public int rand;
	public Date ts;
	public byte[] digest;
	public int executionNode;
	public Utils ut;
	public PublicKey pk;
	public byte[] signedDigest;
	
	
	// initialization of the query. 
	public QueryMessage(Set<String> data, String o, PublicKey pk){
		this.requestedItems = data;
		this.operation = o;
		this.pk = pk;
		this.ut = new Utils();
	}
	
	// Query constructor for ROOT query. This is used to initialize the status of the log of the servers.
	public QueryMessage(Set<String> data, String o) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		this.requestedItems = data;
		this.operation = o;
		this.ut = new Utils();
		initializeRootQuery();
	}
	
	//Encode the attributes of a Query into a single string.
	private String messageEncoding(Set<String> items, String op, Date timestamp, int r){
		String s = "" + String.valueOf(items.hashCode()) + separator + op + separator + timestamp.toString() + separator + String.valueOf(r);
		return s;
	}
	
	@Override
	public String toString() {
		return "Query [id= " + id + ", requestedItems=" + requestedItems + ", operation=" + operation + ", ts=" + ts + ", Random=" + rand + ", Execution Node= " + executionNode + "]";
	}
	
	public int getExecutionNode() {
		return executionNode;
	}

	public void setExecutionNode(int executionNode) {
		this.executionNode = executionNode;
	}
	
	public int getRand() {
		return rand;
	}

	public void setRand(int rand) {
		this.rand = rand;
	}

	//Mainly used for debug.
	public void printQuery(){
		Iterator<String> iter = this.requestedItems.iterator();
		String filename = "";
		while (iter.hasNext()){
			filename += iter.next().toString();
		}
		String s = ("The data requested is = " + filename + "\n" + 
					"The requested operation is = " + this.operation + "\n" +
					"The selected random number is = " + String.valueOf(this.rand) + "\n" +
					"The time the query was generated is = " + this.ts.toString() + "\n" +
					"The message input of digest is = " + this.message + "\n" +
					"The digest of the query is = " + Arrays.toString(this.digest) + "\n" + 
					"The public key PK of the client is = " + this.pk);
		System.out.println(s);
		return;
	}
	
	//Generates a shared initial knowledge. It is used to guarantee that the servers "bootstraps" their log in the same way.
	public void initializeRootQuery() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		this.ts = new Date(1442925151);	//common timestamp among servers to have the same starting point of view (Root query)
		this.rand = 11111111;			//common random number to have same starting point
		this.message = messageEncoding(this.requestedItems, this.operation, this.ts, this.rand);
		this.digest = ut.createDigest(message);
	}
	
	//Initialization method of a Query request.
	public void initializeQuery() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		this.ts = new Date();
		try {
				this.rand = SecureRandom.getInstance("SHA1PRNG", "SUN").nextInt(MAX_SIZE);	//Generates a random value between 0 and 2^16
				this.id = SecureRandom.getInstance("SHA1PRNG", "SUN").nextInt(MAX_SIZE);
			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
				e.printStackTrace();
			}
		this.message = messageEncoding(this.requestedItems, this.operation, this.ts, this.rand);
		this.digest = ut.createDigest(message);	
	}
	
	public void signDigest(PrivateKey sk) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException{
		this.signedDigest = ut.signDigest(sk, this.digest);
	}
	
	public boolean verifyHash(Set<String> items, String op, Date timestamp, int r) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		String s = messageEncoding(items, op, timestamp, r);
		return ut.verifyHash(s, this.digest);
	}
}
