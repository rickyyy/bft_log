package bft_log;

import bftsmart.tom.ServiceProxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class ComputationClient {
	
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, IOException{
		
		Query request = null;
		if(args.length < 1) {
			System.out.println("Usage: java ComputationClient <process id>");
			System.exit(-1);
		}
		//RSAKeyLoader keys = new RSAKeyLoader(0, "D:/Profiles/BortolameottiR/Desktop/My Distributed System/Implementation/library-BFT-SMaRt-v1.0-beta/library-BFT-SMaRt-v1.0-beta/config/");
		ServiceProxy queryProxy = new ServiceProxy(Integer.parseInt(args[0]));
		Set<String> items = new HashSet<String>();
		items.add("AndreasPeter.docx");
		String operation = "count";
		
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(2048, random);
		KeyPair pair = keyGen.generateKeyPair();
		PublicKey pk = pair.getPublic();
		PrivateKey sk = pair.getPrivate();
		
		try {
			request = new Query(items, operation, pk);
			request.initializeQuery();
			request.signDigest(sk);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		generateRequest(request, queryProxy);
	}
	
	static public String generateRequest(Query q, ServiceProxy proxy) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			//q.requestedItems.add("Malicious.txt"); //Uncomment to simulate an attack that tries to modify the real query
			out = new ObjectOutputStream(bos);
			out.writeObject(q);
			byte[] reply = proxy.invokeOrdered(bos.toByteArray());
			if (reply != null){
				String previousValue = new String(reply);
				System.out.println(previousValue);
				return previousValue;
			}
			return null;
			
		} finally {
			try {
				if (out != null){
					out.close();
				}
			} catch (IOException ex) {
			    // ignore close exception
			}
			try {
				bos.close();
			} catch (IOException ex) {
			    // ignore close exception
			}
		}
	}
}
