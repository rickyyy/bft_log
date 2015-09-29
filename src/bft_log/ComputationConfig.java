package bft_log;

import java.util.ArrayList;

import bftsmart.reconfiguration.util.HostsConfig;

public class ComputationConfig {
	public ArrayList<Host> listServer;	//the list of servers in the system
	static public int f = 1; //max number of tolerated compromised nodes
	static public int t = f+1;	//threshold
	static public int n = 3*f+1;	//total number of nodes
	
	public ComputationConfig(){
		this.listServer = listServer();
	}
	
	// TODO Allows the client to retrieve the information of the existing servers and sends them data independently.
	private ArrayList<Host> listServer(){
		HostsConfig hc = new HostsConfig("/Users/BortolameottiR/workspace/bft_log/config", "hosts_upload_port.txt");
		listServer = new ArrayList<Host>();
		int[] hosts = hc.getHostsIds();
		System.out.println(String.valueOf(hosts.length));
		for(int i=hosts.length-1; i>=0; i--){
			//7001 is the "special" value in the default configuration of bftsmart and does not represent the "real" replicas
			if(hosts[i]!=7001){	
				int id = hosts[i];
				Host h = new Host(hc.getRemoteAddress(id), hc.getPort(id), id);
				listServer.add(id, h);
			}
		}
		System.out.println(listServer.toString());
		return listServer;
	}
}
