package core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class GameScreen implements Screen {
	/*
	 * This class is for the screen in which the actual game is played.
	 */

	/*
	 * This class implements the Screen interface. The update and render
	 * abstract methods need to be implemented (polymorphism). These will need
	 * to contain code to be executed every game cycle. The keyPressed abstract
	 * method also needs to be implemented. This will need to contain code to be
	 * executed when a key is pressed.
	 */

	/*
	 * The ScreenDisplayer object that is updating and displaying this screen.
	 * It needs to be stored so that its SetScreen method can be used to change
	 * the screen being displayed on it. This is aggregation as the GameScreen
	 * class has a HAS-A relationship with the ScreenDisplayer class but the
	 * screenDisplayer object will not be destroyed if the GameScreen object is
	 * destroyed.
	 */
	private final ScreenDisplayer screenDisplayer;

	/*
	 * The game mode being played. This is aggregation as the GameScreen class
	 * has a HAS-A relationship with the GameMode enum type but the gameMode
	 * enum will not be destroyed if the GameScreen object is destroyed.
	 */
	private final GameMode gameMode;

	/*
	 * The level being played. This is aggregation as the GameScreen class has a
	 * HAS-A relationship with the Level enum type but the currentLevel enum
	 * will not be destroyed if the GameScreen object is destroyed.
	 */
	private Level currentLevel;

	/*
	 * The maze generated for the current level. This is composition as the
	 * GameScreen class has a HAS-A relationship with the Maze class and the
	 * maze object will be destroyed if the GameScreen object is destroyed.
	 */
	private Maze maze;

	/*
	 * The Player objects that will be moving through the maze for the current
	 * level. The List interface is used rather than a concrete class such as
	 * ArrayList because it separates the actual implementation of the List
	 * interface from this class's use of the interface's methods, allowing the
	 * implementation to change (say, from ArrayList to LinkedList) in the
	 * future. This is composition as the GameScreen class has a HAS-A
	 * relationship with the Player class and the Player objects in the players
	 * list will be destroyed if the GameScreen object is destroyed.
	 */
	private List<Player> players;

	/*
	 * The winning player when a player has won the current level. This is
	 * composition as the GameScreen class has a HAS-A relationship with the
	 * Player class and the winner object will be destroyed if the GameScreen
	 * object is destroyed.
	 */
	private Player winner;

	/*
	 * Whether the level has been finished by a player.
	 */
	private boolean roundOver;

	/*
	 * Whether the game is paused.
	 */
	private boolean paused;

	/*
	 * Constructor.
	 */
	public GameScreen(ScreenDisplayer screenDisplayer, GameMode gameMode,
			Level level) {
		this.screenDisplayer = screenDisplayer;
		this.gameMode = gameMode;
		paused = false;
		setUpLevel(level);
	}

	/*
	 * Initialises or, if already initialised, resets the class variables
	 * currentLevel, maze, players, winner, roundOver given the attributes of
	 * the level parameter and gameMode.
	 */
	protected void setUpLevel(Level level) {
		currentLevel = level;
		winner = null;

		/*
		 * players must be made null before roundOver is set to false so that
		 * the update method doesn't iterate over the old players list and think
		 * that a player has won, even though this winning player was from the
		 * previous round.
		 */
		players = null;
		roundOver = false;
		/*
		 * These are the maze constructor arguments, most of which are
		 * determined by the attributes of the level enum.
		 */
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
		MazeType mazeType = level.getMazeType();

		/*
		 * Creates a maze with the above values as arguments.
		 */
		maze = new Maze(numCellsWide, numCellsHigh, x, y, cellSideLength,
				deadEndProbability, wallProportionOfCellDimensions,
				checkpointProportionOfCellDimensions,
				numCheckpointsExcludingEndpoints, surfaceRatios,
				checkpointColor, wallColor, groundColor, mazeType);

		/*
		 * An ArrayList implementation is used because it is efficient with
		 * respect to memory and iteration time complexity.
		 */
		players = new ArrayList<Player>();

		int numCheckpoints = maze.getNumCheckpoints();
		double humanPlayerBaseVel = maze.getCellSideLength()
				* GameConstants.PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS;
		double aiPlayerBaseVel = maze.getCellSideLength()
				* GameConstants.AI_PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS;
		switch (gameMode) {
		/*
		 * If AGAINST_AI is the game mode, then a human player and an AI player
		 * need to be created and added to the players list. The end cell for
		 * the human player needs to be the start cell for the AI player, and
		 * vice versa.
		 */
		case AGAINST_AI:
			players.add(new HumanPlayer(maze.getStartCell(), maze.getEndCell(),
					humanPlayerBaseVel,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER1_COLOR,
					GameConstants.PLAYER1_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, GameConstants.PLAYER1_UP,
					GameConstants.PLAYER1_DOWN, GameConstants.PLAYER1_LEFT,
					GameConstants.PLAYER1_RIGHT));
			players.add(new AIPlayer(maze.getEndCell(), maze.getStartCell(),
					aiPlayerBaseVel, GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER2_COLOR,
					GameConstants.PLAYER2_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, new ReducedGraph(maze.getStartCell())));
			break;

		/*
		 * If TWO_PLAYER is the game mode, then two human players need to be
		 * created and added to the players list. The end cell for one human
		 * player needs to be the start cell for the other, and vice versa.
		 */
		case TWO_PLAYER:
			players.add(new HumanPlayer(maze.getStartCell(), maze.getEndCell(),
					humanPlayerBaseVel,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER1_COLOR,
					GameConstants.PLAYER1_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, GameConstants.PLAYER1_UP,
					GameConstants.PLAYER1_DOWN, GameConstants.PLAYER1_LEFT,
					GameConstants.PLAYER1_RIGHT));
			players.add(new HumanPlayer(maze.getEndCell(), maze.getStartCell(),
					humanPlayerBaseVel,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER2_COLOR,
					GameConstants.PLAYER2_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS,
					numCheckpoints, GameConstants.PLAYER2_UP,
					GameConstants.PLAYER2_DOWN, GameConstants.PLAYER2_LEFT,
					GameConstants.PLAYER2_RIGHT));
			break;

		/*
		 * If TRAINING is the game mode, then only one human player needs to be
		 * created and added to the players list.
		 */
		case TRAINING:
			players.add(new HumanPlayer(maze.getStartCell(), maze.getEndCell(),
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

	/*
	 * Methods from the Screen interface that need to be implemented
	 * (polymorphism).
	 */

	/*
	 * Calls the keyPressed method of the HumanPlayer objects in Players. If ESC
	 * is pressed, flips the paused variable. If the game is paused or if the
	 * round is over, executes methods to carry out the actions represented by
	 * the given options.
	 */
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		int keyCode = keyEvent.getKeyCode();

		if (!paused && !roundOver) {
			/*
			 * The game is being played. Pressing ESC should pause the game.
			 */
			if (keyCode == KeyEvent.VK_ESCAPE) {
				paused = true;
			} else {
				for (Player player : players) {
					if (player instanceof HumanPlayer) {
						/*
						 * If player is a HumanPlayer object, then the key
						 * presses may be the keys used for controlling the
						 * player. This line of code first casts player to
						 * HumanPlayer so that it can call the keyPressed method
						 * of the HumanPlayer class and execute code that is
						 * required to be executed when a key, corresponding to
						 * the keyEvent argument, is pressed.
						 */
						((HumanPlayer) player).keyPressed(keyEvent);
					}
				}
			}
		} else {
			if (paused) {
				/*
				 * The pause overlay will be displayed. Pressing ESC should lead
				 * to the game being unpaused. Pressing Q should return to the
				 * main menu.
				 */
				if (keyCode == KeyEvent.VK_ESCAPE) {
					paused = false;
				} else if (keyCode == KeyEvent.VK_Q) {
					screenDisplayer.setScreen(new MainMenuScreen(
							screenDisplayer));
				}
			}
			if (roundOver) {
				/*
				 * The round overlay will be displayed.
				 */
				switch (keyEvent.getKeyCode()) {
				/*
				 * If the game mode is AGAINST_AI, then pressing enter should
				 * let the user to progress to the next level, so the setUpLevel
				 * method is called with the next level enum.
				 */
				case KeyEvent.VK_ENTER:
					if (gameMode == GameMode.AGAINST_AI) {
						Level nextLevel = currentLevel.getNextLevel();
						/*
						 * If nextLevel is null, which would happen if the user
						 * has reached the last level, then we should not call
						 * setUpLevel as this will cause the program to crash.
						 */
						if (nextLevel != null) {
							setUpLevel(currentLevel.getNextLevel());
						}
					}
					break;
				/*
				 * Pressing ESC should return to the main menu.
				 */
				case KeyEvent.VK_ESCAPE:
					screenDisplayer.setScreen(new MainMenuScreen(
							screenDisplayer));
					break;
				}
			}
		}
	}

	/*
	 * If the game is not paused and if the round is not over, updates each
	 * Player objects in players. If a player has finished the round, sets
	 * roundOver to true and winner to the Player object which finished.
	 */
	@Override
	public void update() {
		/*
		 * If the game is paused or if the round is over, then the game
		 * variables should not be updated.
		 */
		if (!paused && !roundOver) {
			/*
			 * If players is pointing to a null reference, then iterating over
			 * it would lead to a NullPointerException.
			 */
			if (players != null) {
				for (Player player : players) {
					/*
					 * This is dynamic polymorphism as player can be an object
					 * of any class extending the Player abstract class (i.e.
					 * HumanPlayer or AIPlayer) and the update method can
					 * contain different code depending on which class is
					 * extending the Player class.
					 */
					player.update();

					/*
					 * If a player has finished, then the round is over and the
					 * winner should be the player who has finished.
					 */
					if (player.finished()) {
						roundOver = true;
						winner = player;
						break;
					}
				}
			}
		}
	}

	/*
	 * Renders the maze and each Player object in players. If the game is
	 * paused, displays an overlay with instructions on how to resume the game
	 * or go back to the main menu. If the round has finished, displays an
	 * overlay showing the outcome of the round (i.e. who won) and instructions
	 * on what to do next, depending on the game mode.
	 */
	@Override
	public void render(Graphics graphics) {
		/*
		 * The colour and font used in the graphics object before this method is
		 * called needs to be stored so that it can be restored at the end of
		 * the method (see below). This prevents side effects when the graphics
		 * object is used again.
		 */
		Color lastColor = graphics.getColor();
		Font lastFont = graphics.getFont();

		/*
		 * Draws the background. The background should be the same colour as
		 * walls (so that it is clear that players cannot move outside the
		 * maze). The background should be rendered before everything else so
		 * that everything else appears above the background.
		 */
		graphics.setColor(GameConstants.WALL_COLOR);
		graphics.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());

		/*
		 * Renders the maze. The maze should be rendered before the players so
		 * that the players appear above the maze.
		 */
		maze.render(graphics);

		/*
		 * Renders each player in the players list.
		 */
		for (Player player : players) {
			player.render(graphics);
		}

		/*
		 * If the game is paused, then the pause overlay should be rendered on
		 * top of the game.
		 */
		if (paused) {
			String informationMessage = "PAUSED";
			String instructionsMessage = "Press: [ESC] to resume; [Q] to exit to main menu";
			renderOverlay(graphics, informationMessage, instructionsMessage);
		}

		/*
		 * If the round is over, then the round-over overlay should be rendered
		 * on top of the game.
		 */
		if (roundOver) {
			StringBuilder informationMessageBuilder = new StringBuilder();
			StringBuilder instructionsMessageBuilder = new StringBuilder();
			switch (gameMode) {

			/*
			 * If the game mode is AGAINST_AI, then the overlay should say
			 * whether the user won or lost and at which level.
			 */
			case AGAINST_AI:
				/*
				 * If the winner object is an AIPlayer object, then the user
				 * (playing a HumanPlayer object) has lost, so the overlay
				 * should say which level the user lost at and there should be
				 * an option to quit to the main menu.
				 */
				if (winner instanceof AIPlayer) {
					informationMessageBuilder.append("You lost at level ");
					informationMessageBuilder.append(currentLevel.toString());
					instructionsMessageBuilder
							.append("Press: [ESC] to exit to main menu");
				} else {
					Level nextLevel = currentLevel.getNextLevel();
					if (nextLevel == null) {
						/*
						 * This means that the user has won the last level, so
						 * the overlay should say that the user won the game and
						 * there should not be an option to go to the next level
						 * (as there isn't one) but there should be an option to
						 * quit to the main menu.
						 */
						informationMessageBuilder.append("You won the game.");
						instructionsMessageBuilder
								.append("Press: [ESC] to exit to main menu");
					} else {
						/*
						 * This means that the user has won a level that isn't
						 * the last level, so the overlay should say which level
						 * the user has won and there should be an option to
						 * play the next level or quit to the main menu.
						 */
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

			/*
			 * If the game mode is TWO_PLAYER, then the overlay should say which
			 * player won and there should be an option to return to the main
			 * menu.
			 */
			case TWO_PLAYER:
				instructionsMessageBuilder
						.append("Press: [ESC] to exit to main menu");
				informationMessageBuilder.append(winner.getName());
				informationMessageBuilder.append(" won!");
				break;

			/*
			 * If the game mode is TRAINING, then the overlay should say that
			 * the player has finished and there should be an option to return
			 * to the main menu.
			 */
			case TRAINING:
				informationMessageBuilder.append("You finished!");
				instructionsMessageBuilder
						.append("Press: [ESC] to exit to main menu");
				break;
			}
			renderOverlay(graphics, informationMessageBuilder.toString(),
					instructionsMessageBuilder.toString());
		}

		graphics.setFont(lastFont);
		graphics.setColor(lastColor);
	}

	/*
	 * Renders an overlay (e.g. a pause overlay or a round-over overlay) with a
	 * given information message and an instructions message, telling the user
	 * what to do next. This prevents the need for pop-ups.
	 */
	private void renderOverlay(Graphics g, String informationMessage,
			String instructionsMessage) {
		/*
		 * The colour and font used in the graphics object before this method is
		 * called needs to be stored so that it can be restored at the end of
		 * the method (see below). This prevents side effects when the graphics
		 * object is used again.
		 */
		Color lastColor = g.getColor();
		Font lastFont = g.getFont();

		Font menuFont = GameConstants.getMenuFont(screenDisplayer.getHeight());

		/*
		 * Draws overlay background (in a translucent colour, so that the game
		 * can still be seen in the background).
		 */
		g.setColor(GameConstants.GAME_OVERLAY_COLOR);
		g.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());

		FontMetrics fontMetrics = g.getFontMetrics(menuFont);
		int informationWidth = fontMetrics.stringWidth(informationMessage);
		int instructionsWidth = fontMetrics.stringWidth(instructionsMessage);

		/*
		 * Draws the information and instructions strings in their desired
		 * positions with the font menuFont and the desired menu text colour.
		 */
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
