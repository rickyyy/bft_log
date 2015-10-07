package bft_log;

import java.util.ArrayList;

import bftsmart.reconfiguration.util.HostsConfig;

public class ComputationConfig {
	public static String appPath = "/Users/BortolameottiR/workspace/bft_log/";
	public ArrayList<Host> listServer;	//the list of servers in the system
	static public int f = 1; //max number of tolerated compromised nodes (SECURITY PARAMETER)
	static public int t = f+1;	//threshold
	static public int n = 3*f+1;	//total number of nodes
	
	public ComputationConfig(){
		this.listServer = listServer();
	}
	
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
