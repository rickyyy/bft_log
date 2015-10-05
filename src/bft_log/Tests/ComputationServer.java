package bft_log.tests;

import java.io.IOException;

import bft_log.ComputationConfig;
import bft_log.query.QueryServer;
import bft_log.update.UploadServer;

public class ComputationServer {
	private static ComputationConfig config;
	private static QueryServer qs;
	private static UploadServer us;
	
	public static void main(String[] args) throws NumberFormatException, IOException, ClassNotFoundException {
        if (args.length < 1) {
            System.out.println("Usage: ComputationServer <server id>");
            System.exit(0);
        }
        
        config = new ComputationConfig();	//TODO integrate config with the two servers. In order to have n, t and f set dynamically.

        qs = new QueryServer(Integer.parseInt(args[0]));
        qs.setConfig(config);
        us = new UploadServer(Integer.parseInt(args[0]));
        qs.setUpServer(us);
		System.out.println("Server Created");
        us.startUploadServer();
    }
}
