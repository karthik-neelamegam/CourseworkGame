package user_interface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import logic.HumanPlayer;

public class LoadingScreen implements Screen {

	private ScreenDisplayer screenDisplayer;
	private GameSettings gameSettings;

	public LoadingScreen(ScreenDisplayer screenDisplayer,
			GameSettings gameSettings) {
		this.screenDisplayer = screenDisplayer;
		this.gameSettings = gameSettings;
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

	public void initGame() {
		System.out.println("Initialising game");
		GameScreen gameScreen = new GameScreen(screenDisplayer, gameSettings);
		System.out.println("Initialised game");
		screenDisplayer.setScreen(gameScreen);
	}

	@Override
	public void enter() {
/*		Thread initThread = new Thread() {
			public void run() {
				initGame();
			}
		};
		initThread.start();*/
		initGame();
	}

	@Override
	public void leave() {
		screenDisplayer = null;
		gameSettings = null;
	}

	@Override
	public void update(double delta) {
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());
		g.setColor(Color.WHITE);
		g.drawString("Loading...", 50, 50);
	}

}
