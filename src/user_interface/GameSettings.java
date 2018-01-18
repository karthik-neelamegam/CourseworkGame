package user_interface;

public class GameSettings {
	
	private Difficulty difficulty;
	private GameMode gameMode;

	public GameSettings(Difficulty difficulty, GameMode gameMode) {
		this.difficulty = difficulty;
		this.gameMode = gameMode;
	}
	
	public static GameSettings getDefaultSettings() {
		return new GameSettings(Difficulty.MEDIUM, GameMode.SINGLE_PLAYER);
	}
	
	public GameMode getGameMode() {
		return gameMode;
	}
	
}
