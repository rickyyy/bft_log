package bft_log;

import java.net.InetSocketAddress;

//Represent a Host (used for servers in our application).
public class Host {
	private InetSocketAddress ip;
	private int port;
	private int id;
	public Host(InetSocketAddress ip, int port, int id){
		this.ip = ip;
		this.port = port;
		this.id = id;
	}
	public InetSocketAddress getIp() {
		return ip;
	}
	public int getId() {
		return id;
	}
	public int getPort() {
		return port;
	}
	@Override
	public String toString() {
		return "Host [ip=" + ip.toString() + ", id= " + id + "]";
	}
}
