package bft_log.query;

public class ReceivedShare {
	private String idShare;
	private int counter;
	
	public ReceivedShare(String idShare){
		this.idShare = idShare;
		this.counter = 1;	//We assume the execution node stores its own share.
	}

	@Override
	public String toString() {
		return "ReceivedShare [idShare=" + idShare + ", counter=" + counter + "]";
	}

	public String getIdShare() {
		return idShare;
	}

	public void setIdShare(String idShare) {
		this.idShare = idShare;
	}

	public int getCounter() {
		return counter;
	}
	
	public void increaseCounter(){
		this.counter += 1;
	}
	
}
