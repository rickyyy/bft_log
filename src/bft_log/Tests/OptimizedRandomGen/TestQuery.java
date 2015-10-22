package bft_log.Tests.OptimizedRandomGen;



import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.HashSet;
import java.util.Set;

import bft_log.query.QueryMessage;



public class TestQuery {
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException{
		// read a file as requested item of the query
		String f = "/bft_log/src/bft_log/Test1";
		Set<String> requestedItems = new HashSet<String>();
		requestedItems.add(f);
		
		// Generate public key for the query
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(2048, random);
		KeyPair pair = keyGen.generateKeyPair();
		PublicKey pk = pair.getPublic();
		PrivateKey sk = pair.getPrivate();
		
		// Operation to compute
		String o = "count";
		
		QueryMessage q = new QueryMessage(requestedItems, o, pk);
		q.initializeQuery();
		try {
			q.signDigest(sk);
		} catch (InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		q.printQuery();
		try {
			boolean verif_signature = q.ut.verifySignedDigest(q.pk, q.digest, q.signedDigest);
			boolean verif_digest = q.verifyHash(q.requestedItems, q.operation, q.ts, q.rand);
			System.out.println("Verified Signature : " + verif_signature + "\n" +
							   "Verified Digest: " + verif_digest);
		} catch (InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
