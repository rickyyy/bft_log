package bft_log;

import java.util.Arrays;

import bft_log.query.QueryMessage;


public class LogEntry implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 22222L;
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private byte[] previousQueryDigest;
	private QueryMessage query;
	
	public LogEntry(byte[] digest, QueryMessage query){
		this.previousQueryDigest = digest;
		this.query = query;
	}
	
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	@Override
	public String toString() {
		return "LogEntry [previousQueryDigest=" + bytesToHex(previousQueryDigest) + ", currentQuery="
				+ query + "]";
	}

	public byte[] getPreviousQueryDigest() {
		return previousQueryDigest;
	}

	public void setPreviousQueryDigest(byte[] previousQueryDigest) {
		this.previousQueryDigest = previousQueryDigest;
	}

	public QueryMessage getQuery() {
		return query;
	}

	public void setCurrentQuery(QueryMessage currentQuery) {
		this.query = currentQuery;
	}
	
	public boolean verifyLogEntries(LogEntry prev, LogEntry current){
		if (Arrays.equals(prev.query.digest, current.previousQueryDigest)){
			return true;
		}
		else {
			return false;
		}
	}
	
}
