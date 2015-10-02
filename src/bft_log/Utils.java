package bft_log;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

public class Utils implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 88888L;
	private String digestAlg = "SHA-256";
	private String digestEnc = "UTF-16";
	private String signAlg = "SHA1withDSA";
	private String signProv = "SUN";
	
	public Utils(){
	}
	
	/* Create a digest for the root */
	public byte[] createDigest(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		MessageDigest md = MessageDigest.getInstance(digestAlg);
		md.update(message.getBytes(digestEnc));
		return md.digest();
	}
	
	public byte[] hashBytes(byte[] input) throws NoSuchAlgorithmException{
		MessageDigest md = MessageDigest.getInstance(digestAlg);
		md.update(input);
		return md.digest();
	}
	
	/*The digest is signed with the Private Key of the Client*/
	public byte[] signDigest(PrivateKey sk, byte[] digest) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException{
		Signature sig = Signature.getInstance(signAlg, signProv);
		sig.initSign(sk);
		sig.update(digest);
		return sig.sign();
	}
	
	@Override
	public String toString() {
		return "Utils [is Existing]";
	}

	/* The signed Digest is verified using the public key of the user*/
	public boolean verifySignedDigest(PublicKey pk, byte[] digest, byte[] signedDigest) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException{
		Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
		sig.initVerify(pk);
		sig.update(digest);
		return sig.verify(signedDigest);
	}
	
	public boolean verifyHash(String s, byte[] digest) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		byte[] verify = createDigest(s);
		//System.out.println("Verified Hash : " + DigestToHex(verify) + "\n");
		return Arrays.equals(verify, digest);
	}
	
}
