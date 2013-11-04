package cl.dcc.cc5303;

public abstract class PongThread extends Thread implements PongThreadI{
	protected boolean working = true;
	protected boolean running = true;
	protected static final int UPDATE_RATE = 1000;
	
	@Override
	public void run(){
		while(running){
			try{
				preWork();
				if(working){
					work();
				}
				postWork();
				pauseWork();
			}
			catch(InterruptedException ex){
				System.out.println("Servicio Pong interrumpido");
				this.end();
			}
		}
	}
	
	@Override
	public synchronized void pause(){
		working = false;
	}
	
	@Override
	public synchronized void unPause(){
		working = true;
	}

	@Override
	public synchronized void end(){
		running = false;
	}
	
	@Override
	public abstract void preWork() throws InterruptedException;
	
	@Override
	public abstract void work() throws InterruptedException;
	
	@Override
	public abstract void postWork() throws InterruptedException;
	
	@Override
	public abstract void pauseWork() throws InterruptedException;

}
