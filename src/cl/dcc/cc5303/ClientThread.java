package cl.dcc.cc5303;

public class ClientThread extends Thread{
	public int id;
	public boolean runStatus;
	public ClientThread(int id, Runnable run){
		super(run);
		this.id = id;
		this.runStatus = true;
	}
	
	public void toggleStatus(){
		this.runStatus = !(this.runStatus);
	}
}
