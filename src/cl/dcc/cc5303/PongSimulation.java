package cl.dcc.cc5303;

public class PongSimulation extends PongThread {
	private static final long INACTIVITY_TIMEOUT = 3000;
	private Match match;
	public long[] lastActivity;
	private boolean started;
	private GameState state;
	private Pong pong;
	
	public PongSimulation(Match match){
		this.match        = match;
		this.state        = new GameState();
		this.lastActivity = new long[4];
		this.started      = false;
		this.pong         = new Pong();
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
	
	public void serverUpdate(int playerNum, int position) {
		synchronized(state){
			state.serverUpdate(pong.scores.getScores(), pong.historical.getScores(), playerNum, position);
		}
		touchPlayer(playerNum);
	}
	
	public void updateServerScores() {
		synchronized(state){
			state.updateServerScores(pong.scores.getScores(), pong.historical.getScores());
			((HistoricalScoreBoardSimple)pong.historical).setScores(state.historicalScores);
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
		synchronized(state){
			state = pong.doGameIteration(state);
		}
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
			System.out.println("nuevo match: " + match.getID());
			resetGame();
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
		for (int i=0; i < PongClient.MAX_PLAYERS; i++) {
			lastActivity[i] = currentMilis;
		}
		synchronized(state){
			this.state.unPause();
		}
	}
	
	private void checkPlayersActivity() {
		long checkTime = System.currentTimeMillis();
		for (int i=0; i < PongClient.MAX_PLAYERS; i++) {
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
		// Remuevo del score historico el jugador
		pong.historical.removePlayer(playerNum);
		
		// Se pone en 0 el puntaje del jugador que se fue
		int[] scores = pong.scores.getScores();
		scores[playerNum]  = 0;
		pong.scores.setScores(scores, getPlaying());
	}
	
	private synchronized void resetActivity(){
		long currentMilis = System.currentTimeMillis();
		for (int i=0; i < PongClient.MAX_PLAYERS; i++) {
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
			pong.scores.reset(getPlaying());
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

	public void setScores(int[] scores, boolean[] playing) {
		pong.scores.setScores(scores, playing);
	}

}