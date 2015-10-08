package bft_log.query.execution;

import java.util.ArrayList;
import java.util.Hashtable;

import bft_log.ComputationConfig;
import bft_log.query.result.ReceivedShare;

public class ExecutionTable extends Hashtable<Integer, ArrayList<ReceivedShare>>{
	private Integer queryId;
	private ArrayList<ReceivedShare> shareList;
	private Hashtable<Integer, Integer> expectedNumItemsQuery;
	private ComputationConfig conf;
	
	public ExecutionTable(ComputationConfig c){
		expectedNumItemsQuery = new Hashtable<Integer, Integer>();
		this.conf = c;
	}
	
	public void insertExpectedNumItemsQuery(int idQuery, int sizeQuery){
		if (!expectedNumItemsQuery.containsKey(idQuery)){
			this.expectedNumItemsQuery.put(idQuery, sizeQuery);
		}
	}
	
	public Integer getExpectedNumItemsQuery(int idQuery){
		return this.expectedNumItemsQuery.get(idQuery);
	}
	
	public boolean readyToReconstruction(int idQuery){
		boolean ready = false;
		int size = expectedNumItemsQuery.get(idQuery);
		ArrayList<ReceivedShare> list = this.get(idQuery);
		if (list.size()!=size){
			return ready;
		} else {
			for (ReceivedShare s : list){
				if(s.getCounter() < conf.t){
					return ready;
				}
			}
			ready = true;
			return ready;
		}
	}
	
	public void removeExecutedQuery(int id){
		this.remove(id);
	}
	
	public void updateTable(ExecutionMessage msg){
		int id = msg.getQueryID();
		int index = -1;
		boolean equal = false;
		if (this.containsKey(id)){
			shareList = this.get(id);
			for (ReceivedShare rs : shareList){
				if(rs.getIdShare().equals(msg.getItemRequested())){
					equal = true;
					index = shareList.indexOf(rs);
				}
			}
			if(equal == true){
				this.get(id).get(index).increaseCounter();
				System.out.println(this.get(id).get(index).toString());
			} else {
				ReceivedShare newShare = new ReceivedShare(msg.getItemRequested());
				this.get(id).add(newShare);
			}
			
		} else {
			ReceivedShare newShare = new ReceivedShare(msg.getItemRequested());
			ArrayList<ReceivedShare> newList = new ArrayList<ReceivedShare>();
			newList.add(newShare);
			this.put(id, newList);
		}
	}
}
