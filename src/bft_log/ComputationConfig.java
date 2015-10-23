package bft_log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import bftsmart.consensus.executionmanager.LeaderModule;
import bftsmart.reconfiguration.util.HostsConfig;

public class ComputationConfig {
	public static String appPath = "/Users/BortolameottiR/workspace/bft_log/";
	public ArrayList<Host> listServer;	//the list of servers in the system
	static public int f = 1; //max number of tolerated compromised nodes (SECURITY PARAMETER)
	static public int t = f+1;	//threshold
	static public int n = 3*f+1;	//total number of nodes
	private LeaderModule lm;
	
	public ComputationConfig(){
		this.listServer = listServer();
		this.lm = new LeaderModule();
	}
	
	public LeaderModule getLm() {
		return lm;
	}
	
	//TODO not in use yet. To fix and to check whether we really need it or not.
//	public ArrayList<Host> aliveServerList(){
//		HostsConfig hc = new HostsConfig(appPath+"config", "hosts_upload_port.txt");
//		listServer = new ArrayList<Host>();
//		int[] hosts = hc.getHostsIds();
//		Socket testSock;
//		for(int i=hosts.length-1; i>=0; i--){
//			//7001 is the "special" value in the default configuration of bftsmart and does not represent the "real" replicas
//			if(hosts[i]!=7001){	
//				int id = hosts[i];
//				Host h = new Host(hc.getRemoteAddress(id), hc.getPort(id), id);
//				String ip = h.getIp().getHostString();
//				try {
//					testSock = new Socket(ip, h.getPort());
//					listServer.add(id, h);
//					testSock.close();
//				} catch(IOException e){
//					//System.err.println("Server " + h.getId() + " is not listening on port " + h.getPort());
//				}
//			}
//		}
//		return listServer;
//	}


	// Allows the client to retrieve the information of the existing servers and sends them data independently.
	// TODO add check regarding the list of server "shorter" than the number of nodes required OR loads dynamically the content of the file and set f t and n.
	private ArrayList<Host> listServer(){
		HostsConfig hc = new HostsConfig(appPath+"config", "hosts_upload_port.txt");
		listServer = new ArrayList<Host>();
		int[] hosts = hc.getHostsIds();
		for(int i=hosts.length-1; i>=0; i--){
			//7001 is the "special" value in the default configuration of bftsmart and does not represent the "real" replicas
			if(hosts[i]!=7001){	
				int id = hosts[i];
				Host h = new Host(hc.getRemoteAddress(id), hc.getPort(id), id);
				listServer.add(id, h);
			}
		}
		return listServer;
	}
}
