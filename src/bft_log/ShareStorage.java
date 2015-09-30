package bft_log;

public class ShareStorage {
	private byte[] share;
	private String policy;
	
	public ShareStorage(byte[] s, String p){
		this.share = s;
		this.policy = p;
	}

	public byte[] getShare() {
		return share;
	}

	public String getPolicy() {
		return policy;
	}
}
