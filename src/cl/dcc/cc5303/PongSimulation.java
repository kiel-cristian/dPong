package cl.dcc.cc5303;

public class PongSimulation extends PongThread {
	private static final long INACTIVITY_TIMEOUT = 3000;
	private Match match;
	public long[] lastActivity;
	private boolean started;
	private GameState state;
	
	
	public PongSimulation(Match match){
		this.match = match;
		this.state = new GameState();
		this.lastActivity = new long[4];
		this.started = false;
		resetActivity();
	}
	
	public boolean alreadyStarted(){
		return started;
	}
	
	public void setPlayers(int numPlayers){
		synchronized(state){
			this.state.numPlayers = numPlayers;
		}
	}
	
	public void disableAllPlayers() {
		synchronized(state){
			Utils.setFalse(state.playing); // todo false para esperar jugadores
		}
	}
	
	public void setState(GameState state){
		synchronized(this.state){
			this.state = new GameState();
			this.state.fullUpdate(state);
		}
	}
	
	public void fullUpdate(GameState migratedGameState) {
		synchronized(state){
			state.fullUpdate(migratedGameState);
		}
	}
	
	public void serverUpdate(int[] scores, int playerNum, int position) {
		synchronized(state){
			state.serverUpdate(scores, playerNum, position);
		}
		touchPlayer(playerNum);
	}
	
	public void updateServerScores(int[] scores) {
		synchronized(state){
			state.updateServerScores(scores);
		}
	}

	@Override
	public void preWork() throws InterruptedException {
		if (match.migration.emigrating) {
			running = false;
			return;
		}
	}

	@Override
	public void work() throws InterruptedException {
		Pong.doGameIteration(state);
		checkForWinnerServer();
		checkPlayersActivity();
	}

	@Override
	public void postWork() throws InterruptedException {
		if(!started){
			started = true;
		}
	}
	
	@Override
	public void pauseWork() throws InterruptedException{
		if(state.winner){
			Thread.sleep(600000 /50);
			match.resetGame();
		}
		else{
			Thread.sleep(PongThread.UPDATE_RATE/ 60);
		}
	}
	
	@Override
	public void pause(){
		super.pause();
		synchronized(state){
			this.state.pause();
		}
	}
	
	@Override
	public void unPause(){
		super.unPause();
		long currentMilis = System.currentTimeMillis();
		for (int i=0; i < Pong.MAX_PLAYERS; i++) {
			lastActivity[i] = currentMilis;
		}
		synchronized(state){
			this.state.unPause();
		}
	}
	
	private void checkForWinnerServer(){
		if(match.score.isAWinner()){
			match.historical.addWinner(state.winnerPlayer);
			state.setMatchWinner(match.score.getWinner(), match.historical.getScores());
		}
	}
	
	private void checkPlayersActivity() {
		long checkTime = System.currentTimeMillis();
		for (int i=0; i < Pong.MAX_PLAYERS; i++) {
			synchronized(state){
				if (state.isPlaying(i) && (checkTime - lastActivity[i] > INACTIVITY_TIMEOUT)) {
					System.out.println("Removiendo jugador por inactividad");
					match.removePlayer(i);
				}
			}
		}
	}

	public void addPlayer(int num) {
		synchronized(state){
			state.enablePlayer(num);
		}
		touchPlayer(num);
	}
	
	public void removePlayer(int playerNum){
		synchronized(state){
			state.disablePlayer(playerNum);
		}
	}
	
	private synchronized void resetActivity(){
		long currentMilis = System.currentTimeMillis();
		for (int i=0; i < Pong.MAX_PLAYERS; i++) {
			lastActivity[i] = currentMilis;
		}
	}

	private void touchPlayer(int num) {
		lastActivity[num] = System.currentTimeMillis();
	}
	
	
	public boolean playersReady() {
		synchronized(state){
			return state.playersReady();
		}
	}
	
	public int playersCount() {
		synchronized(state){
			return state.playersCount();
		}
	}

	public void resetGame() {
		synchronized(state){
			state.resetGame();
		}
	}
	
	public boolean[] getPlaying(){
		synchronized(state){
			return state.playing;
		}
	}

	public void resetPlayerBars() {
		synchronized(state){
			state.resetPlayerBars();
		}
	}
	
	public boolean isPlaying(int playerNum){
		synchronized(state){
			return state.isPlaying(playerNum);
		}
	}

	public GameState state() {
		synchronized(state){
			return state;
		}
	}

}