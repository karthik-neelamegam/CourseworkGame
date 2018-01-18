package user_interface;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyEvent;

public class MainMenuScreen implements Screen {
	private ScreenDisplayer screenDisplayer;
	private GameSettings gameSettings;
	public MainMenuScreen(ScreenDisplayer screenDisplayer, GameSettings gameSettings) {
		this.screenDisplayer = screenDisplayer;
		this.gameSettings = gameSettings;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			screenDisplayer.setScreen(new LoadingScreen(screenDisplayer, gameSettings));
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void enter() {
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
		g.drawString("Press SPACE to start!",
				screenDisplayer.getWidth() / 2 - 50,
				screenDisplayer.getHeight() / 2);
	}

}
