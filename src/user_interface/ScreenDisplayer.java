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
	private int targetFps;

	public ScreenDisplayer(int targetFps) {
		super();
		setFocusable(true);
		addKeyListener(this);
		running = false;
		this.targetFps = targetFps;
	}

	public void setScreen(Screen screen) {
		if(currentScreen != null) {
			currentScreen.leave();
		}
		currentScreen = screen;
		currentScreen.enter();
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
		long previousLoopTime = System.nanoTime();
		long optimalLoopTime = 1000000000 / targetFps;
		while (running) {
			long currentTime = System.nanoTime();
			long updateLength = currentTime - previousLoopTime;
			previousLoopTime = currentTime;
			double delta = updateLength / ((double) optimalLoopTime);
			if(currentScreen != null) {
				currentScreen.update(delta);
			}
			repaint();
			long timeDifference = System.nanoTime() - previousLoopTime;
			long sleepTimeInMillis = (optimalLoopTime - timeDifference) / 1000000;
			if(sleepTimeInMillis < 0) {
				System.out.println(""+(optimalLoopTime - timeDifference));
			}
			try {
				Thread.sleep(sleepTimeInMillis);
				
			} catch (InterruptedException e) {
				System.out.println(e.getStackTrace());
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
