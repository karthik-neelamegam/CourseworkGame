package user_interface;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import logic.AIPlayer;
import logic.HumanPlayer;
import logic.ReducedGraph;
import map.DeadEndProbability;
import map.Maze;
import map.MazeType;
import map.SurfacePicker;

public class GameScreen implements Screen {

	private ScreenDisplayer screenDisplayer;
	private GameSettings gameSettings;
	private boolean paused;
	private Maze maze;
	private HumanPlayer player;
	private AIPlayer ai;
	public GameScreen(ScreenDisplayer screenDisplayer, GameSettings gameSettings) {
		this.screenDisplayer = screenDisplayer;
		this.gameSettings = gameSettings;
		paused = false;
		maze = new Maze(10, 10, 0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight(), DeadEndProbability.MEDIUM, 0.1, 10, 10,
				SurfacePicker.getDefaultSurfacePicker(), Color.YELLOW, 0, 0,
				9, 9, Color.BLACK, MazeType.KRUSKAL);
		player = new HumanPlayer(maze.getStartCell(), maze.getCellSide() / 10,
				Color.GREEN, 10);
		ai = new AIPlayer(maze.getEndCell(), maze.getStartCell(), maze.getCellSide() / 10, Color.RED, 10, new ReducedGraph(maze.getStartCell()), 10, 2);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		player.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		player.keyReleased(e);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void enter() {

	}

	@Override
	public void leave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(double delta) {
		if (!paused) {
			player.update(delta);
			ai.update(delta);
		}
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());
		g.setColor(Color.WHITE);
		maze.render(g);
		player.render(g);
		ai.render(g);
		if (paused) {
			renderPauseOverlay(g);
		}
	}

	public void renderPauseOverlay(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.2f));
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());
		g2d.setColor(Color.WHITE);
		g2d.drawString(
				"Press ESCAPE to resume or Q to quit and exit to main menu",
				50, 50);
	}
}
