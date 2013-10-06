package cl.dcc.cc5303;

import java.util.Random;

public class PongBall extends Rectangle {
	public double vx, vy;

	public PongBall(double x, double y, double w, double h) {
		super(x, y, w, h);
		vx = 0.4;
		vy = 0.3;
	}

  public PongBall(){
    super(Pong.WIDTH * 0.5, Pong.HEIGHT * 0.5, 10, 10);

    setRandomVelocity();
  }

  public boolean checkIfGoesLeft(int minLeft, int step){
    if(x + vx*step < minLeft){
      return true;
    }
    else{
      return false;
    }
  }

  public boolean checkIfGoesRight(int maxRight, int step){
    if(x + vx*step > maxRight){
      return true;
    }
    else{
      return false;
    } 
  }

  public boolean checkIfGoesDown(int minBottom, int step){
    if(y + vy*step < minBottom){
      return true;
    }
    else{
      return false;
    } 
  }

  public boolean checkIfGoesUp(int maxTop, int step){
    if(y + vy*step > maxTop){
      return true;
    }
    else{
      return false;
    }
  }

  public void reset(){
    x = Pong.WIDTH * 0.5;
    y = Pong.HEIGHT * 0.5;
    w = 10;
    h = 10;

    setRandomVelocity();
  }

  private void setRandomVelocity(){
    Random rand = new Random();
    int dirx = rand.nextInt(2);
    int diry = rand.nextInt(2);
    
    if(dirx > 0){
      dirx = -1;
    }
    if(diry > 0){
      diry = -1;
    }
    
    vx = rand.nextFloat()/2*dirx + 0.1*dirx;
    vy = rand.nextFloat()/2*diry + 0.1*diry;

    if(vx == 0){
      vx = 0.2;
    }
    if(vy == 0){
      vy = -0.2;
    }

  }

}
