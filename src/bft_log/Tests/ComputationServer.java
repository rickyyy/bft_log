package bft_log.Tests;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import bft_log.ComputationConfig;
import bft_log.query.QueryServer;
import bft_log.update.UploadServer;

public class ComputationServer {
	private static ComputationConfig config;
	private static QueryServer qs;
	private static UploadServer us;
	private static PrivateKey sk;
	private static PublicKey pk;
	
	public static void main(String[] args) throws NumberFormatException, IOException, ClassNotFoundException {
        if (args.length < 1) {
            System.out.println("Usage: ComputationServer <server id>");
            System.exit(0);
        }
        
        config = new ComputationConfig();	//TODO integrate config with the two servers. In order to have n, t and f set dynamically.
        
        //Crypto key pairs can be either generated on the fly or loaded from a file.
        //generateKeyPair();
        qs = new QueryServer(Integer.parseInt(args[0]));
        qs.setConfig(config);
        us = new UploadServer(Integer.parseInt(args[0]));
        qs.setUpServer(us);
		System.out.println("Server Created");
        us.startUploadServer();
    }
	
	//Generate a key pair for the server, on the fly
	static private void generateKeyPair(){
  		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
	  		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
	  		keyGen.initialize(2048, random);
	  		KeyPair pair = keyGen.generateKeyPair();
	  		pk = pair.getPublic();
	  		sk = pair.getPrivate();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		
	}
}
