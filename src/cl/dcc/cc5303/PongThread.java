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
	public void pause(){
		synchronized(this){
			working = false;
		}
	}
	
	@Override
	public void unPause(){
		synchronized(this){
			working = true;
		}
	}

	@Override
	public void end(){
		synchronized(this){
			running = false;
		}
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
