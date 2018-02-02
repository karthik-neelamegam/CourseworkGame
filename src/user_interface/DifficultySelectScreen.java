package user_interface;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import map.Level;

public class DifficultySelectScreen implements Screen {
	private ScreenDisplayer screenDisplayer;

	public DifficultySelectScreen(ScreenDisplayer screenDisplayer) {
		this.screenDisplayer = screenDisplayer;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Difficulty difficulty;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_1:
			difficulty = Difficulty.EASY;
			break;
		case KeyEvent.VK_2:
			difficulty = Difficulty.MEDIUM;
			break;
		case KeyEvent.VK_3:
			difficulty = Difficulty.HARD;
			break;
		case KeyEvent.VK_ESCAPE:
			screenDisplayer.setScreen(new MainMenuScreen(screenDisplayer));
		default:
			difficulty = null;
		}
		if (difficulty != null) {
			screenDisplayer.setScreen(new AgainstAIGameScreen(screenDisplayer, difficulty, Level.TWO));
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter() {
		// TODO Auto-generated method stub

	}

	@Override
	public void leave() {
		screenDisplayer = null;
	}

	@Override
	public void update(double delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
		String instructionsMessage = "Select a difficulty (Press the key indicated in brackets): ";
		List<String> difficultyMessages = new ArrayList<String>();
		difficultyMessages.add("[1] Easy");
		difficultyMessages.add("[2] Medium");
		difficultyMessages.add("[3] Hard");
		String exitMessage = "Press: [ESC] to exit to main menu";
		FontMetrics fontMetrics = g.getFontMetrics();
		int maxWidth = 0;
		for (String levelMessage : difficultyMessages) {
			int width = fontMetrics.stringWidth(levelMessage);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());
		g.setColor(Color.WHITE);
		g.drawString(instructionsMessage,
				(screenDisplayer.getWidth() - fontMetrics
						.stringWidth(instructionsMessage)) / 2,
				screenDisplayer.getHeight() / (difficultyMessages.size() + 3));
		for (int i = 0; i < difficultyMessages.size(); i++) {
			g.drawString(
					difficultyMessages.get(i),
					(screenDisplayer.getWidth() - maxWidth) / 2,
					(i + 2) * screenDisplayer.getHeight()
							/ (difficultyMessages.size() + 3));
		}
		g.drawString(exitMessage,
				(screenDisplayer.getWidth() - fontMetrics
						.stringWidth(exitMessage)) / 2,
						(difficultyMessages.size() + 2)*screenDisplayer.getHeight() / (difficultyMessages.size() + 3));
		g.setColor(lastColor);
	}

}
