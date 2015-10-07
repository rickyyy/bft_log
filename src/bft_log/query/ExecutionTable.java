package bft_log.query;

import java.util.ArrayList;
import java.util.Hashtable;

import bft_log.ComputationConfig;

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
	
	public boolean readyToReconstruction(int idQuery){
		boolean ready = false;
		int size = expectedNumItemsQuery.get(idQuery);
		ArrayList<ReceivedShare> list = this.get(idQuery);
		if (list.size()!=size){
			System.out.println("Arraylist size: " + String.valueOf(list.size()));
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
				System.out.println("IDSHARE: " + rs.getIdShare() + " MSG ITEM REQ: " + msg.getItemRequested());
				if(rs.getIdShare().equals(msg.getItemRequested())){
					System.out.println("SONO UGUALI");
					equal = true;
					index = shareList.indexOf(rs);
				}
			}
			if(equal == true){
				this.get(id).get(index).increaseCounter();
				System.out.println("Counter increased for a share.");
				System.out.println(this.get(id).get(index).toString());
			} else {
				ReceivedShare newShare = new ReceivedShare(msg.getItemRequested());
				System.out.println("New Received Share created: " + newShare.toString());
				this.get(id).add(newShare);
			}
			
		} else {
			System.out.println("Created new instance in the Table");
			ReceivedShare newShare = new ReceivedShare(msg.getItemRequested());
			ArrayList<ReceivedShare> newList = new ArrayList<ReceivedShare>();
			newList.add(newShare);
			this.put(id, newList);
		}
	}
}
