package user_interface;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import map.Maze;
import map.SurfacePicker;

public class GameScreen implements Screen {

	private ScreenDisplayer screenDisplayer;
	private GameSettings gameSettings;
	private boolean paused;
	private Maze maze;
	
	public GameScreen(ScreenDisplayer screenDisplayer, GameSettings gameSettings) {
		this.screenDisplayer = screenDisplayer;
		this.gameSettings = gameSettings;
		paused = false;
		maze = new Maze(20,20,0,0,screenDisplayer.getWidth(), screenDisplayer.getHeight(), 1, 10, SurfacePicker.getUniformSurfacePicker());
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
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
			//update stuff
		}
	}
	
	@Override
	public void render(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenDisplayer.getWidth(), screenDisplayer.getHeight());
		g.setColor(Color.WHITE);
		maze.render(g);
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
