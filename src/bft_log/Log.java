package bft_log;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class Log implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 11111L;
	private Hashtable<Integer, LogEntry> log;
	private LogEntry root;
	private Utils ut;
	
	public Log (){
		this.log = new Hashtable<Integer, LogEntry>();
		this.ut = new Utils();
		initialize();
	}
	
	private void initialize(){
		String s = "ROOT";
		HashSet<String> rootRequest = new HashSet<String>();
		rootRequest.add(s);
		try {
			Query rootQuery = new Query(rootRequest, "Check");
			byte[] rootHash = ut.createDigest("ROOT");
			root = new LogEntry(rootHash, rootQuery);
			this.log.put(0, root);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public Hashtable<Integer, LogEntry> getLog() {
		return log;
	}
	
	@Override
	public String toString() {
		Iterator<Map.Entry<Integer, LogEntry>> it = log.entrySet().iterator();
		String print = "Current Status of the Log:\n";
		while (it.hasNext()){
			Map.Entry<Integer, LogEntry> entry = it.next();
			print += "Position: " + entry.getKey().toString() + " Values: " + entry.getValue().toString() + "\n";
		}
		return print;
	}

	public void addEntry(Integer id, Query current){
		LogEntry previous = this.log.get(id-1);
		Query prevQuery = previous.getQuery();
		LogEntry newEntry = new LogEntry(prevQuery.digest, current);
		this.log.put(id, newEntry);
	}
	
	public boolean verifyLog(Integer index){
		boolean result = true;
		for (int i = index.intValue(); i >= 0; i--){
			if (i == 0 && result == true){
				LogEntry last = this.log.get(i);
				if (last.equals(root)){
					return true;
				}
			}
			LogEntry lastFromIndex = this.log.get(i);
			LogEntry previous = this.log.get(i-1);
			result = lastFromIndex.verifyLogEntries(previous, lastFromIndex);
		}
		return result;
	}
}
