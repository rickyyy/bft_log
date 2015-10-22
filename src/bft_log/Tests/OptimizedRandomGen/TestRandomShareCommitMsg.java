package bft_log.Tests.OptimizedRandomGen;

import java.io.Serializable;

import edu.biu.scapi.interactiveMidProtocols.commitmentScheme.CmtCCommitmentMsg;
import edu.biu.scapi.primitives.dlog.GroupElementSendableData;

public class TestRandomShareCommitMsg implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3499473595604018380L;
	private long queryId;	//Identified used later on for the decommit phase
	private CmtCCommitmentMsg commitMsg;	//C - The real committed value
	private GroupElementSendableData h;
	
	public TestRandomShareCommitMsg(long id, CmtCCommitmentMsg c, GroupElementSendableData h){
		this.queryId = id;
		this.commitMsg = c;
		this.h = h;
	}

	public long getQueryId() {
		return queryId;
	}

	public CmtCCommitmentMsg getCommitMsg() {
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
