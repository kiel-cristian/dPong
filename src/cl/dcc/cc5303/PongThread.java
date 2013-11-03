package cl.dcc.cc5303;

public abstract class PongThread extends Thread implements PongThreadI{
	protected boolean working = true;
	protected boolean running = true;
	
	@Override
	public void run(){
		while(running){
			try{
				preWork();
				if(working){
					work();
				}
				freeWork();
				Thread.sleep(workRate()); // milliseconds
			}
			catch(InterruptedException ex){
				System.out.println("Servicio Pong interrumpido");
				this.end();
			}
		}
	}
	
	@Override
	public int workRate(){
		return 1000/60;
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
	public abstract void freeWork() throws InterruptedException;
}
