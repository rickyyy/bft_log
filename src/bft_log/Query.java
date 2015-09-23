package bft_log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class Query implements java.io.Serializable {
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 12345L;
	public Set<String> requestedItems = new HashSet<String>();
	public String operation;
	public int rand;
	public Date ts;
	public byte[] digest;
	private byte[] signedDigest;
	private PublicKey pk;
	private String separator = " ";
	private String message = "";
	
	
	// initialization of the query. 
	public Query(Set<String> data, String o, PublicKey pk){
		this.requestedItems = data;
		this.operation = o;
		this.pk = pk;
	}
	
	// Query constructor for ROOT query (for the log)
	public Query(Set<String> data, String o) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		this.requestedItems = data;
		this.operation = o;
		initializeRootQuery();
	}
	
	@Override
	public String toString() {
		return "Query [requestedItems=" + requestedItems + ", operation=" + operation + ", ts=" + ts + "]";
	}
	
	/*Print the Arguments of the Query*/
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
	
	public void initializeRootQuery() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		this.ts = new Date(1442925151);	//common timestamp among servers to have the same starting point of view (Root query)
		this.rand = 11111111;			//common random number to have same starting point
		this.message = messageEncoding(this.requestedItems, this.operation, this.ts, this.rand);
		this.digest = createDigest(message);
	}
	
	public void initializeQuery() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		this.ts = new Date();
		try {
				this.rand = SecureRandom.getInstance("SHA1PRNG", "SUN").nextInt();
			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
				e.printStackTrace();
			}
		this.message = messageEncoding(this.requestedItems, this.operation, this.ts, this.rand);
		this.digest = createDigest(message);	
	}

	/*The digest is signed with the Private Key of the Client*/
	public void signDigest(PrivateKey sk) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException{
		Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
		sig.initSign(sk);
		sig.update(this.digest);
		this.signedDigest = sig.sign();
	}
	
	/* The signed Digest is verified using the public key of the user*/
	public boolean verifySignedDigest() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException{
		Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
		sig.initVerify(this.pk);
		sig.update(this.digest);
		return sig.verify(this.signedDigest);
	}
	
	public boolean verifyHash(Set<String> items, String op, Date timestamp, int r) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		String s = messageEncoding(items, op, timestamp, r);
		byte[] verify = createDigest(s);
		//System.out.println("Verified Hash : " + DigestToHex(verify) + "\n");
		return Arrays.equals(verify, this.digest);
	}
	
	/* Create a digest of the message that contains the parameters of the query*/
	private byte[] createDigest(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(message.getBytes("UTF-16"));
		return md.digest();
	}
	
	/*Encode the attributes of a Query into a single string.*/
	private String messageEncoding(Set<String> items, String op, Date timestamp, int r){
		String s = "" + String.valueOf(items.hashCode()) + separator + op + separator + timestamp.toString() + separator + String.valueOf(r);
		return s;
	}

}
