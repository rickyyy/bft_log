package bft_log.query;



import bftsmart.tom.ServiceProxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

import bft_log.update.UploadClient;

//This is an example of a Client. Here the client just sequentially makes an Upload and Query request, on the same item.
//TODO Make it interactive. 
public class QueryClient {
	
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, IOException, ClassNotFoundException{
		QueryMessage request = null;
		if(args.length < 1) {
			System.out.println("Usage: java ComputationClient <process id>");
			System.exit(-1);
		}
		//RSAKeyLoader keys = new RSAKeyLoader(0, "D:/Profiles/BortolameottiR/Desktop/My Distributed System/Implementation/library-BFT-SMaRt-v1.0-beta/library-BFT-SMaRt-v1.0-beta/config/");
		ServiceProxy queryProxy = new ServiceProxy(Integer.parseInt(args[0]));
		
		//Set an example of data items to analyze (its ID for example) and the operation to compute on it.
		Set<String> items = new HashSet<String>();
		items.add("Test1");
		String operation = "count";
		
		//Generate a key pair for the client
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(2048, random);
		KeyPair pair = keyGen.generateKeyPair();
		PublicKey pk = pair.getPublic();
		PrivateKey sk = pair.getPrivate();
		
		//File to upload
		File f = new File("/home/riccardo/git/bft_log/src/bft_log/Test1");
		
		//Instance of the Update protocol
		UploadClient up = new UploadClient(pk, sk);
		up.uploadClientFile(f);
		
		//Instantiate the example query. Send a query request to all servers
		try {
			request = new QueryMessage(items, operation, pk);
			request.initializeQuery();
			request.signDigest(sk);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		generateRequest(request, queryProxy);
	}
	
	// Method to sends a request that uses the Byzantine consensus protocol with invokeOrdered()
	static public String generateRequest(QueryMessage q, ServiceProxy proxy) throws IOException{
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
