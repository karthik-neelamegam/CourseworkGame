package user_interface;

import map.Level;

public class GameState {
	
	private GameMode gameMode;
	private Difficulty difficulty;
	private Level level;

	public GameState(Difficulty difficulty, GameMode gameMode, Level level) {
		this.difficulty = difficulty;
		this.gameMode = gameMode;
		this.level = level;
	}
		
	public GameMode getGameMode() {
		return gameMode;
	}
	
}
