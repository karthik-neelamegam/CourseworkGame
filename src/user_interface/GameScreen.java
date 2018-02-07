package user_interface;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Set;

import logic.HumanPlayer;
import logic.Player;
import map.GameConstants;
import map.GameMap;
import map.GameMapType;
import map.Level;
import map.Surface;

public abstract class GameScreen implements Screen {

	protected ScreenDisplayer screenDisplayer;
	protected Level currentLevel;
	private GameMap map;
	protected Set<Player> players;
	protected Player winner;
	private boolean roundOver;
	private boolean paused;
	
	
	public GameScreen(ScreenDisplayer screenDisplayer) {
		this.screenDisplayer = screenDisplayer;
		paused = false;
		roundOver = false;
		winner = null;
	}

	protected void setUpLevel(Level level) {
		currentLevel = level;
		winner = null;
		players = null;//new HashSet<Player>(); // must be removed before roundOver set to false so that update method doesn't think round is over again.
		roundOver = false;
		int numCellsWide = level.getNumCellsWide();
		int numCellsHigh = level.getNumCellsHigh();
		double cellSideLength = Math.min(screenDisplayer.getHeight()/numCellsHigh, screenDisplayer.getWidth()/ numCellsWide);
		double width = numCellsWide * cellSideLength;
		double height = numCellsHigh * cellSideLength;
		double x = (screenDisplayer.getWidth()-width)/2;
		double y = (screenDisplayer.getHeight()-height)/2;
		double deadEndProbability = level.getDeadEndProbability();
		double wallProportionOfCellDimensions = GameConstants.WALL_PROPORTION_OF_CELL_DIMENSIONS;
		double checkpointProportionOfCellDimensions = GameConstants.CHECKPOINT_PROPORTION_OF_CELL_DIMENSIONS;
		int numCheckpointsExcludingEndpoints = level.getNumCheckpointsExcludingEndpoints();
		EnumMap<Surface, Double> surfaceRatios = level.getSurfaceRatios();
		
		Color checkpointColor = GameConstants.CHECKPOINT_COLOR;
		Color wallColor = GameConstants.WALL_COLOR;
		Color groundColor = GameConstants.GROUND_COLOR;
		GameMapType mapType = level.getMapType();
		map = new GameMap(numCellsWide, numCellsHigh, x, y, cellSideLength, deadEndProbability, wallProportionOfCellDimensions, checkpointProportionOfCellDimensions,
				numCheckpointsExcludingEndpoints, surfaceRatios, checkpointColor, wallColor,
				groundColor, mapType);
		players = createPlayersOnLevelSetUp(map);
	}
	
	protected abstract Set<Player> createPlayersOnLevelSetUp(GameMap map);
	
	protected abstract void renderRoundOverOverlay(Graphics g);
	
	protected abstract void doWhenRoundOverAndKeyPressed(KeyEvent e);
	
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(!paused && !roundOver) {
			if(keyCode == KeyEvent.VK_ESCAPE) {
				paused = true;
			}
			for(Player player : players) {
				if(player instanceof HumanPlayer) {
					((HumanPlayer)player).keyPressed(e);
				}
			}
		} else {
			if(paused){
				if(keyCode == KeyEvent.VK_ESCAPE) {
					paused = false;
				} else if(keyCode == KeyEvent.VK_Q) {
					screenDisplayer.setScreen(new MainMenuScreen(screenDisplayer));
				}
			}
			if(roundOver) {
				doWhenRoundOverAndKeyPressed(e);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void update() {
		if (!paused && !roundOver) {
			map.update();
			if(players != null) {
				for (Player player : players) {
					player.update();
					if(player.finished()) {
						System.out.println("FINISHED");
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
		for(Player player : players) {
			player.render(g);
		}
		if (paused) {
			renderPauseOverlay(g);
		}
		if (roundOver) {
			renderRoundOverOverlay(g);
		}
		g.setColor(lastColor);
	}

	private void renderPauseOverlay(Graphics g) {
		String informationMessage = "PAUSED";
		String instructionsMessage = "Press: [ESC] to resume; [Q] to exit to main menu";
		renderOverlay(g, informationMessage, instructionsMessage);
	}
	
	//one line each, keep messages short
	protected void renderOverlay(Graphics g, String informationMessage, String instructionsMessage) {
		Color lastColor = g.getColor();
		Color overlayColor = new Color(0, 0, 0, 0.7f);
		g.setColor(overlayColor);
		g.fillRect(0, 0, screenDisplayer.getWidth(), screenDisplayer.getHeight());
		FontMetrics fontMetrics = g.getFontMetrics();
		int informationWidth = fontMetrics.stringWidth(informationMessage);
		int instructionsWidth = fontMetrics.stringWidth(instructionsMessage);
		g.setColor(Color.WHITE);
		g.drawString(informationMessage, (screenDisplayer.getWidth()-informationWidth)/2, screenDisplayer.getHeight()/3);
		g.drawString(instructionsMessage, (screenDisplayer.getWidth()-instructionsWidth)/2, 2*screenDisplayer.getHeight()/3);
		g.setColor(lastColor);
	}
	
}
