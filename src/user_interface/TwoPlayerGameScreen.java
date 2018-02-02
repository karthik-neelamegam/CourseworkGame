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

public class TwoPlayerGameScreen extends GameScreen {

	public TwoPlayerGameScreen(ScreenDisplayer screenDisplayer, Level level) {
		super(screenDisplayer);
		setUpLevel(level);
	}

	@Override
	protected Set<Player> createPlayersOnLevelSetUp(GameMap map) {
		Set<Player> players = new HashSet<Player>();
		double playerBaseVel = map.getCellSide()
				* GameConstants.PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS;
		double playerProportionOfCellDimensions = GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS;
		int numCheckpoints = map.getNumCheckpoints();
		players.add(new HumanPlayer(map.getStartCell(), map.getEndCell(), playerBaseVel,
				GameConstants.PLAYER1_COLOR, GameConstants.PLAYER1_DEFAULT_NAME, playerProportionOfCellDimensions,numCheckpoints,
				GameConstants.PLAYER1_UP, GameConstants.PLAYER1_DOWN,
				GameConstants.PLAYER1_LEFT, GameConstants.PLAYER1_RIGHT));
		players.add(new HumanPlayer(map.getEndCell(), map.getStartCell(), playerBaseVel,
				GameConstants.PLAYER2_COLOR, GameConstants.PLAYER2_DEFAULT_NAME, playerProportionOfCellDimensions,numCheckpoints,
				GameConstants.PLAYER2_UP, GameConstants.PLAYER2_DOWN,
				GameConstants.PLAYER2_LEFT, GameConstants.PLAYER2_RIGHT));
		return players;
	}

	@Override
	protected void renderRoundOverOverlay(Graphics g) {
		StringBuilder informationMessageBuilder = new StringBuilder();
		String instructionsMessage = "Press: [ESC] to exit to main menu";
		informationMessageBuilder.append(winner.getName());
		informationMessageBuilder.append(" won!");
		renderOverlay(g, informationMessageBuilder.toString(), instructionsMessage);
	}

	@Override
	protected void doWhenRoundOverAndKeyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			screenDisplayer.setScreen(new MainMenuScreen(screenDisplayer));
		}
	}

}
