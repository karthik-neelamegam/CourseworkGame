package user_interface;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import logic.AIPlayer;
import logic.HumanPlayer;
import logic.Player;
import logic.ReducedGraph;
import map.GameConstants;
import map.GameMap;
import map.Level;

public class AgainstAIGameScreen extends GameScreen {

	public AgainstAIGameScreen(ScreenDisplayer screenDisplayer, Level startLevel) {
		super(screenDisplayer);
		setUpLevel(startLevel);
	}

	@Override
	protected Set<Player> createPlayersOnLevelSetUp(GameMap map) {
		Set<Player> players = new HashSet<Player>();
		double playerBaseVel = map.getCellSideLength()/20;
				//* GameConstants.PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS;
		int numCheckpoints = map.getNumCheckpoints();
		players.add(new HumanPlayer(map.getStartCell(), map.getEndCell(), playerBaseVel, GameConstants.PLAYER_TOLERANCE_CONSTANT,
				GameConstants.PLAYER1_COLOR, GameConstants.PLAYER1_DEFAULT_NAME, GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS, numCheckpoints,
				GameConstants.PLAYER1_UP, GameConstants.PLAYER1_DOWN,
				GameConstants.PLAYER1_LEFT, GameConstants.PLAYER1_RIGHT));
		players.add(new AIPlayer(map.getEndCell(), map.getStartCell(),
				playerBaseVel, GameConstants.PLAYER_TOLERANCE_CONSTANT, GameConstants.PLAYER2_COLOR, GameConstants.PLAYER2_DEFAULT_NAME, 
				GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS, numCheckpoints, new ReducedGraph(map.getStartCell())));
		return players;
	}
	
	@Override
	protected void renderRoundOverOverlay(Graphics g) {
		StringBuilder informationMessageBuilder = new StringBuilder();
		StringBuilder instructionsMessageBuilder = new StringBuilder();
		if(winner instanceof AIPlayer) {
			informationMessageBuilder.append("You lost at level ");
			informationMessageBuilder.append(currentLevel.toString());
			instructionsMessageBuilder.append("Press: [ESC] to exit to main menu");
		} else {
			Level nextLevel = currentLevel.getNextLevel();
			if(nextLevel == null) {
				informationMessageBuilder.append("You won the game.");
				instructionsMessageBuilder.append("Press: [ESC] to exit to main menu");
			} else {
				informationMessageBuilder.append("You won level: ");
				informationMessageBuilder.append(currentLevel.toString());
				instructionsMessageBuilder.append("Press: [ENTER] for level ");
				instructionsMessageBuilder.append(nextLevel.toString());
				instructionsMessageBuilder.append("; [ESC] to exit to main menu");
			}
		}
		renderOverlay(g, informationMessageBuilder.toString(), instructionsMessageBuilder.toString());
	}

	@Override
	protected void doWhenRoundOverAndKeyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_ENTER:
			setUpLevel(currentLevel.getNextLevel());
			break;
		case KeyEvent.VK_ESCAPE:
			screenDisplayer.setScreen(new MainMenuScreen(screenDisplayer));
			break;
		}
	}

}
