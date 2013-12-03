package cl.dcc.cc5303;

public interface PongThreadI {
	public void run();
	public void pause();
	public void unPause();
	public  void end();
	public void preWork() throws InterruptedException;
	public void work() throws InterruptedException;
	public void postWork() throws InterruptedException;
	public void pauseWork() throws InterruptedException;
}
