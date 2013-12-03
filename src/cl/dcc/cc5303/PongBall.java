package cl.dcc.cc5303;

import java.io.Serializable;
import java.util.Random;

import cl.dcc.cc5303.client.ClientPong;

public class PongBall extends Rectangle implements Serializable{
	private static final long serialVersionUID = -8169819468494935096L;
	public double vx, vy;

	public PongBall(){
		super(ClientPong.WIDTH * 0.5, ClientPong.HEIGHT * 0.5, 10, 10);

		setRandomVelocity();
	}

	public PongBall(int ballX, int ballY, double vx2, double vy2) {
		super(ballX, ballY, 10, 10);
		vx = vx2;
		vy = vy2;
	}

	public void changeXDir(double pond){
		vx = -vx * pond;
	}

	public void changeYDir(double pond){
		vy = -vy * pond;
	}

	private boolean isInBarVerticalRange(Rectangle bar){
		return bottom() < bar.top() && top() > bar.bottom();
	}

	private boolean isInBarHorizontalRange(Rectangle bar){
		return left() < bar.right() && right() > bar.left();
	}

	public boolean willCrashWithBarByRight(Rectangle bar, double step){
		return isInBarVerticalRange(bar) && vx < 0 && right() >= bar.right() && left() <= bar.right();
	}
	public boolean willCrashWithBarByLeft(Rectangle bar, double step){
		return isInBarVerticalRange(bar) && vx > 0 && left() <= bar.left() && right() >= bar.left();
	}
	public boolean willCrashWithBarByTop(Rectangle bar, double step){
		return isInBarHorizontalRange(bar) && vy > 0 && top() >= bar.bottom() && bottom() <= bar.bottom();
	}
	public boolean willCrashWithBarByBottom(Rectangle bar, double step){
		return isInBarHorizontalRange(bar) && vy < 0 && bottom() <= bar.top() && top() >= bar.top();
	}

	public boolean checkIfGoesLeft(int minLeft, double step){
		return x + vx*step < minLeft;
	}

	public boolean checkIfGoesRight(int maxRight, double step){
		return x + vx*step > maxRight;
	}

	public boolean checkIfGoesDown(int minBottom, double step){
		return y + vy*step > minBottom;
	}

	public boolean checkIfGoesUp(int maxTop, double step){
		return y + vy*step < maxTop;
	}

	public void reset(){
		x = ClientPong.WIDTH * 0.5;
		y = ClientPong.HEIGHT * 0.5;
		w = 10;
		h = 10;

		setRandomVelocity();
	}

	private void setRandomVelocity(){
		Random rand = new Random();
		float dirx = rand.nextFloat();
		float diry = rand.nextFloat();
		
		if(dirx > 0.5){
		  dirx = -1;
		}
		else{
			dirx = 1;
		}
		
		if(diry > 0.5){
			diry = -1;
		}
		else{
			diry = 1;
		}
		
		vx = rand.nextFloat()/3*dirx + 0.25*dirx;
		vy = rand.nextFloat()/3*diry + 0.25*diry;
		
		if(vx == 0){
		  vx = 0.2;
		}
		if(vy == 0){
		  vy = -0.2;
		}

  }

	public void copy(PongBall ball) {
		this.x = ball.x;
		this.y = ball.y;
		this.vx = ball.vx;
		this.vy = ball.vy;
	}

	public void move(double dx, double dy) {
		this.x += dx;
		this.y += dy;
		
	}

}
