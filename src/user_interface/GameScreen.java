package user_interface;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import logic.AIPlayer;
import logic.HumanPlayer;
import logic.Player;
import logic.ReducedGraph;
import map.GameConstants;
import map.GameMap;
import map.GameMapType;
import map.Level;
import map.Surface;

public class GameScreen implements Screen {

	private final ScreenDisplayer screenDisplayer;
	private final GameMode gameMode;
	private Level currentLevel;
	private GameMap map;
	private List<Player> players;
	private Player winner;
	private boolean roundOver;
	private boolean paused;

	public GameScreen(ScreenDisplayer screenDisplayer, GameMode gameMode,
			Level level) {
		this.screenDisplayer = screenDisplayer;
		this.gameMode = gameMode;
		paused = false;
		setUpLevel(level);
	}

	protected void setUpLevel(Level level) {
		currentLevel = level;
		winner = null;
		players = null; // must be removed before roundOver set to false so that
						// update method doesn't think round is over again.
		roundOver = false;
		int numCellsWide = level.getNumCellsWide();
		int numCellsHigh = level.getNumCellsHigh();
		double cellSideLength = Math.min(screenDisplayer.getHeight()
				/ numCellsHigh, screenDisplayer.getWidth() / numCellsWide);
		double width = numCellsWide * cellSideLength;
		double height = numCellsHigh * cellSideLength;
		double x = (screenDisplayer.getWidth() - width) / 2;
		double y = (screenDisplayer.getHeight() - height) / 2;
		double deadEndProbability = level.getDeadEndProbability();
		double wallProportionOfCellDimensions = GameConstants.WALL_PROPORTION_OF_CELL_DIMENSIONS;
		double checkpointProportionOfCellDimensions = GameConstants.CHECKPOINT_PROPORTION_OF_CELL_DIMENSIONS;
		int numCheckpointsExcludingEndpoints = level
				.getNumCheckpointsExcludingEndpoints();
		EnumMap<Surface, Double> surfaceRatios = level.getSurfaceRatios();

		Color checkpointColor = GameConstants.CHECKPOINT_COLOR;
		Color wallColor = GameConstants.WALL_COLOR;
		Color groundColor = GameConstants.GROUND_COLOR;
		GameMapType mapType = level.getMapType();
		map = new GameMap(numCellsWide, numCellsHigh, x, y, cellSideLength,
				deadEndProbability, wallProportionOfCellDimensions,
				checkpointProportionOfCellDimensions,
				numCheckpointsExcludingEndpoints, surfaceRatios,
				checkpointColor, wallColor, groundColor, mapType);
		players = new ArrayList<Player>();
		int numCheckpoints = map.getNumCheckpoints();
		double humanPlayerBaseVel = map.getCellSideLength()
				* GameConstants.PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS;
		double aiPlayerBaseVel = map.getCellSideLength()
				* GameConstants.AI_PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS;
		switch (gameMode) {
		case AGAINST_AI:
			players.add(new HumanPlayer(map.getStartCell(), map.getEndCell(),
					humanPlayerBaseVel,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER1_COLOR,
					GameConstants.PLAYER1_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, GameConstants.PLAYER1_UP,
					GameConstants.PLAYER1_DOWN, GameConstants.PLAYER1_LEFT,
					GameConstants.PLAYER1_RIGHT));
			players.add(new AIPlayer(map.getEndCell(), map.getStartCell(),
					aiPlayerBaseVel, GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER2_COLOR,
					GameConstants.PLAYER2_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, new ReducedGraph(map.getStartCell())));
			break;
		case TWO_PLAYER:
			players.add(new HumanPlayer(map.getStartCell(), map.getEndCell(),
					humanPlayerBaseVel,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER1_COLOR,
					GameConstants.PLAYER1_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, GameConstants.PLAYER1_UP,
					GameConstants.PLAYER1_DOWN, GameConstants.PLAYER1_LEFT,
					GameConstants.PLAYER1_RIGHT));
			players.add(new HumanPlayer(map.getEndCell(), map.getStartCell(),
					humanPlayerBaseVel,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER2_COLOR,
					GameConstants.PLAYER2_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, GameConstants.PLAYER2_UP,
					GameConstants.PLAYER2_DOWN, GameConstants.PLAYER2_LEFT,
					GameConstants.PLAYER2_RIGHT));
			break;
		case TRAINING:
			players.add(new HumanPlayer(map.getStartCell(), map.getEndCell(),
					humanPlayerBaseVel,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER1_COLOR,
					GameConstants.PLAYER1_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, GameConstants.PLAYER1_UP,
					GameConstants.PLAYER1_DOWN, GameConstants.PLAYER1_LEFT,
					GameConstants.PLAYER1_RIGHT));
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (!paused && !roundOver) {
			if (keyCode == KeyEvent.VK_ESCAPE) {
				paused = true;
			} else {
				for (Player player : players) {
					if (player instanceof HumanPlayer) {
						((HumanPlayer) player).keyPressed(e);
					}
				}
			}
		} else {
			if (paused) {
				if (keyCode == KeyEvent.VK_ESCAPE) {
					paused = false;
				} else if (keyCode == KeyEvent.VK_Q) {
					screenDisplayer.setScreen(new MainMenuScreen(
							screenDisplayer));
				}
			}
			if (roundOver) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					if (gameMode == GameMode.AGAINST_AI) {
						Level nextLevel = currentLevel.getNextLevel();
						if (nextLevel != null) {
							setUpLevel(currentLevel.getNextLevel());
						}
					}
					break;
				case KeyEvent.VK_ESCAPE:
					screenDisplayer.setScreen(new MainMenuScreen(
							screenDisplayer));
					break;
				}
			}
		}
	}

	@Override
	public void update() {
		if (!paused && !roundOver) {
			map.update();
			if (players != null) {
				for (Player player : players) {
					player.update();
					if (player.finished()) {
						roundOver = true;
						winner = player;
						break;
					}
				}
			}
		}
	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());
		map.render(g);
		for (Player player : players) {
			player.render(g);
		}
		if (paused) {
			String informationMessage = "PAUSED";
			String instructionsMessage = "Press: [ESC] to resume; [Q] to exit to main menu";
			renderOverlay(g, informationMessage, instructionsMessage);
		}
		if (roundOver) {
			StringBuilder informationMessageBuilder = new StringBuilder();
			StringBuilder instructionsMessageBuilder = new StringBuilder();
			switch (gameMode) {
			case AGAINST_AI:
				if (winner instanceof AIPlayer) {
					informationMessageBuilder.append("You lost at level ");
					informationMessageBuilder.append(currentLevel.toString());
					instructionsMessageBuilder
							.append("Press: [ESC] to exit to main menu");
				} else {
					Level nextLevel = currentLevel.getNextLevel();
					if (nextLevel == null) {
						informationMessageBuilder.append("You won the game.");
						instructionsMessageBuilder
								.append("Press: [ESC] to exit to main menu");
					} else {
						informationMessageBuilder.append("You won level: ");
						informationMessageBuilder.append(currentLevel
								.toString());
						instructionsMessageBuilder
								.append("Press: [ENTER] for level ");
						instructionsMessageBuilder.append(nextLevel.toString());
						instructionsMessageBuilder
								.append("; [ESC] to exit to main menu");
					}
				}
				break;
			case TWO_PLAYER:
				instructionsMessageBuilder
						.append("Press: [ESC] to exit to main menu");
				informationMessageBuilder.append(winner.getName());
				informationMessageBuilder.append(" won!");
				break;
			case TRAINING:
				informationMessageBuilder.append("You finished!");
				instructionsMessageBuilder
						.append("Press: [ESC] to exit to main menu");
				break;
			}
			renderOverlay(g, informationMessageBuilder.toString(),
					instructionsMessageBuilder.toString());
		}
		g.setColor(lastColor);
	}

	// one line each, keep messages short
	protected void renderOverlay(Graphics g, String informationMessage,
			String instructionsMessage) {
		Color lastColor = g.getColor();
		Font lastFont = g.getFont();
		Font menuFont = GameConstants.getMenuFont(screenDisplayer.getHeight());
		g.setColor(GameConstants.GAME_OVERLAY_COLOR);
		g.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());
		FontMetrics fontMetrics = g.getFontMetrics(menuFont);
		int informationWidth = fontMetrics.stringWidth(informationMessage);
		int instructionsWidth = fontMetrics.stringWidth(instructionsMessage);
		g.setColor(GameConstants.MENU_TEXT_COLOR);
		g.setFont(menuFont);
		g.drawString(informationMessage,
				(screenDisplayer.getWidth() - informationWidth) / 2,
				screenDisplayer.getHeight() / 3);
		g.drawString(instructionsMessage,
				(screenDisplayer.getWidth() - instructionsWidth) / 2,
				2 * screenDisplayer.getHeight() / 3);
		g.setFont(lastFont);
		g.setColor(lastColor);
	}

}
