package bft_log.Tests.OptimizedRandomGen;

import java.io.Serializable;
import java.util.Hashtable;

public class TableCommit extends Hashtable<Integer, TestRandomShareCommitMsg> implements Serializable{
	public TableCommit(){
		super();
	}
}
