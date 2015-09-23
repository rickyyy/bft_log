package bft_log;

import java.io.File;
import java.util.Set;

import com.tiemens.secretshare.main.cli.MainSplit.SplitInput;

public class Upload {
	
	private File file;
	private Set<byte[]> shares;
	static public int f = 1; //max number of tolerated compromised nodes
	static public int t = f+1;	//threshold
	static public int n = 3*f+1;	//total number of nodes
	
	
	public Upload(File file){
		this.file = file;
	}

}
