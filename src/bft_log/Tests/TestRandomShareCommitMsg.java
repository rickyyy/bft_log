package bft_log.Tests;

import java.io.Serializable;

import edu.biu.scapi.primitives.dlog.GroupElement;
import edu.biu.scapi.primitives.dlog.GroupElementSendableData;

public class TestRandomShareCommitMsg implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3499473595604018380L;
	private long queryId;	//Identified used later on for the decommit phase
	private GroupElementSendableData commitMsg;	//C - The real committed value
	private GroupElementSendableData h;
	
	public TestRandomShareCommitMsg(long id, GroupElementSendableData c, GroupElementSendableData h){
		this.queryId = id;
		this.commitMsg = c;
		this.h = h;
	}

	public long getQueryId() {
		return queryId;
	}

	public GroupElementSendableData getCommitMsg() {
		return commitMsg;
	}

	public GroupElementSendableData getH() {
		return h;
	}

	@Override
	public String toString() {
		return "TestRandomShareCommitMsg [queryId=" + queryId + ", commitMsg=" + commitMsg + ", h=" + h + "]";
	}

}
