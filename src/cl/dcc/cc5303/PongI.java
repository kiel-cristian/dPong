package cl.dcc.cc5303;

public interface PongI {
	public GameState doGameIteration(GameState state);
	void handleHumanBounce(int i);
	void handleBounce(int i);
	void handleBall();
}
