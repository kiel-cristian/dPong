package cl.dcc.cc5303.server;

import cl.dcc.cc5303.GameState;
import cl.dcc.cc5303.GameStateInfo;
import cl.dcc.cc5303.HistoricalScoreBoardSimple;
import cl.dcc.cc5303.Pong;
import cl.dcc.cc5303.PongThread;
import cl.dcc.cc5303.Utils;
import cl.dcc.cc5303.client.ClientPong;

public class ServerGameThread extends PongThread {
	private static final long INACTIVITY_TIMEOUT = 5000;
	private ServerMatch serverMatch;
	public long[] lastActivity;
	private boolean started;
	private GameState state;
	private Pong pong;
	
	public ServerGameThread(ServerMatch serverMatch){
		this.serverMatch        = serverMatch;
		this.state        = new GameState();
		this.lastActivity = new long[4];
		this.started      = false;
		this.pong         = new Pong();
		resetActivity();
	}
	
	public void togglePause() {
		if (working) {
			userPause();
		}
		else if (state.userPaused){
			userUnPause();
		}
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
	
	public void setState(GameStateInfo state){
		synchronized(this.state){
			this.state.updateFromInfo(state);
		}
	}
	
	public void fullUpdate(GameStateInfo migratedGameState) {
		synchronized(state){
			state.updateFromInfo(migratedGameState);
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
		if (serverMatch.migration.emigrating) {
			running = false;
			return;
		}
	}

	@Override
	public void work() throws InterruptedException {
		synchronized(state){
			state = pong.doGameIteration(state);
		}
		
		if(!started){
			started = true;
		}
		checkPlayersActivity();
	}

	@Override
	public void postWork() throws InterruptedException {
		if(!started){
			touchPlayers();
		}
	}
	
	@Override
	public void pauseWork() throws InterruptedException{
		if(state.winner){
			Thread.sleep(600000 /50);
			System.out.println("nuevo match: " + serverMatch.getID());
			resetGame();
		}
		else{
			Thread.sleep(PongThread.UPDATE_RATE/ 60);
		}
	}

	private void userPause() {
		super.pause();
		synchronized(state) {
			state.userPause();
		}
	}
	
	private void userUnPause() {
		super.unPause();
		restartLastActivity();
		synchronized(state){
			this.state.userUnPause();
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
		restartLastActivity();
		synchronized(state){
			this.state.unPause();
		}
	}
	
	private void restartLastActivity() {
		long currentMilis = System.currentTimeMillis();
		for (int i=0; i < ClientPong.MAX_PLAYERS; i++) {
			lastActivity[i] = currentMilis;
		}
	}
	
	private void checkPlayersActivity() {
		long checkTime = System.currentTimeMillis();
		for (int i=0; i < ClientPong.MAX_PLAYERS; i++) {
			synchronized(state){
				if (checkTime - lastActivity[i] > INACTIVITY_TIMEOUT) {
					serverMatch.removePlayerByTimeout(i);
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
		for (int i=0; i < ClientPong.MAX_PLAYERS; i++) {
			lastActivity[i] = currentMilis;
		}
	}

	private void touchPlayer(int num) {
		lastActivity[num] = System.currentTimeMillis();
	}
	
	private void touchPlayers(){
		long milis = System.currentTimeMillis();

		for (int i = 0; i < Pong.MAX_PLAYERS; i++){
			if (state.isPlaying(i)){
				lastActivity[i] = milis;
			}
		}
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

	public GameStateInfo state() {
		synchronized(state){
			return state.packInfo();
		}
	}

	public void setScores(int[] scores, boolean[] playing) {
		pong.scores.setScores(scores, playing);
	}

}