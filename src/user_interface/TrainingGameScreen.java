package user_interface;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import logic.HumanPlayer;
import logic.Player;
import map.GameConstants;
import map.GameMap;
import map.Level;

public class TrainingGameScreen extends GameScreen {

	public TrainingGameScreen(ScreenDisplayer screenDisplayer, Level level) {
		super(screenDisplayer);
		setUpLevel(level);
	}

	@Override
	protected Set<Player> createPlayersOnLevelSetUp(GameMap map) {
		Set<Player> players = new HashSet<Player>();
		double playerBaseVel = map.getCellSideLength()
				* GameConstants.PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS;
		int numCheckpoints = map.getNumCheckpoints();
		players.add(new HumanPlayer(map.getStartCell(), map.getEndCell(), playerBaseVel, GameConstants.PLAYER_TOLERANCE_CONSTANT,
				GameConstants.PLAYER1_COLOR, GameConstants.PLAYER1_DEFAULT_NAME, GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS, numCheckpoints,
				GameConstants.PLAYER1_UP, GameConstants.PLAYER1_DOWN,
				GameConstants.PLAYER1_LEFT, GameConstants.PLAYER1_RIGHT));		
		return players;
	}

	@Override
	protected void renderRoundOverOverlay(Graphics g) {
		String informationMessage = "You finished!";
		String instructionsMessage = "Press: [ESC] to exit to main menu";
		renderOverlay(g, informationMessage, instructionsMessage);
	}

	@Override
	protected void doWhenRoundOverAndKeyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			screenDisplayer.setScreen(new MainMenuScreen(screenDisplayer));
		}
	}

}
