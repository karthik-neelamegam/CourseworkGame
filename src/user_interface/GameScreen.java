package user_interface;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import logic.HumanPlayer;
import map.Maze;
import map.SurfacePicker;

public class GameScreen implements Screen {

	private ScreenDisplayer screenDisplayer;
	private GameSettings gameSettings;
	private boolean paused;
	private Maze maze;
	private HumanPlayer player;
	
	public GameScreen(ScreenDisplayer screenDisplayer, GameSettings gameSettings) {
		this.screenDisplayer = screenDisplayer;
		this.gameSettings = gameSettings;
		paused = false;
		maze = new Maze(20,20,0,0,screenDisplayer.getWidth(), screenDisplayer.getHeight(), 0d, 0.1, 10, 10, SurfacePicker.getDefaultSurfacePicker(), Color.YELLOW);
		player = new HumanPlayer(maze.getStartCell(),maze.getCellSide()/5,Color.GREEN, 10);
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
		if(!paused) {
			player.update(delta);
		}
	}
	
	@Override
	public void render(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenDisplayer.getWidth(), screenDisplayer.getHeight());
		g.setColor(Color.WHITE);
		maze.render(g);
		player.render(g);
		if(paused) {
			renderPauseOverlay(g);
		}
	}
	
	public void renderPauseOverlay(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0,0,screenDisplayer.getWidth(), screenDisplayer.getHeight());
		g2d.setColor(Color.WHITE);
		g2d.drawString("Press ESCAPE to resume or Q to quit and exit to main menu", 50, 50);
	}
}
