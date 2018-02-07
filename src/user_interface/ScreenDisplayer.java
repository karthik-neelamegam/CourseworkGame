package user_interface;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

public class ScreenDisplayer extends JPanel implements KeyListener {
	private Screen currentScreen;
	private boolean running;
	private final double desiredTimePerCycle;

	public ScreenDisplayer(int desiredGameHz) {
		super();
		setFocusable(true);
		addKeyListener(this);
		running = false;
		desiredTimePerCycle = 1e9 / desiredGameHz;
	}

	public void setScreen(Screen screen) {
		currentScreen = screen;
	}

	public void run() {
		Thread loop = new Thread() {
			public void run() {
				gameLoop();
			}
		};
		running = true;
		loop.start();
	}

	private void gameLoop() {
		double previousCycleTime = System.nanoTime();
		while (running) {
			double currentTime = System.nanoTime();
			while(currentTime - previousCycleTime > desiredTimePerCycle) {
				currentScreen.update();
				previousCycleTime += desiredTimePerCycle;
			}
			repaint();
			while(currentTime - previousCycleTime < desiredTimePerCycle) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				currentTime = System.nanoTime();
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(new Font("sansserif", Font.PLAIN, 20));
		super.paintComponent(g);
		if (currentScreen != null) {
			currentScreen.render(g);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (currentScreen != null) {
			currentScreen.keyPressed(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (currentScreen != null) {
			currentScreen.keyReleased(e);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (currentScreen != null) {
			currentScreen.keyReleased(e);
		}
	}

}
