package user_interface;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import map.Level;

public class MainMenuScreen implements Screen {
	private ScreenDisplayer screenDisplayer;
	public MainMenuScreen(ScreenDisplayer screenDisplayer) {
		this.screenDisplayer = screenDisplayer;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_1:
			screenDisplayer.setScreen(new AgainstAIGameScreen(screenDisplayer, Level.ONE));
			break;
		case KeyEvent.VK_2:
			screenDisplayer.setScreen(new LevelSelectScreen(screenDisplayer, GameMode.TWO_PLAYER));
			break;
		case KeyEvent.VK_3:
			screenDisplayer.setScreen(new LevelSelectScreen(screenDisplayer, GameMode.TRAINING));
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
	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
		Font baseFont = g.getFont();
		Font titleFont = new Font("serif", Font.ITALIC, 60);
		String titleMessage = "MazeRace";
		String instructionsMessage = "Select a game mode (Press the key indicated in brackets): ";
		List<String> gameModeMessages = new ArrayList<String>();
		gameModeMessages.add("[1] Against AI");
		gameModeMessages.add("[2] Two Player");
		gameModeMessages.add("[3] Training");
		FontMetrics fontMetrics = g.getFontMetrics();
		int maxWidth = 0;
		for (String levelMessage : gameModeMessages) {
			int width = fontMetrics.stringWidth(levelMessage);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight());
		g.setColor(Color.YELLOW);
		g.setFont(titleFont);
		g.drawString(titleMessage, (screenDisplayer.getWidth()-g.getFontMetrics(titleFont).stringWidth(titleMessage))/2, screenDisplayer.getHeight()/(gameModeMessages.size() + 3));
		g.setFont(baseFont);
		g.setColor(Color.WHITE);
		g.drawString(instructionsMessage,
				(screenDisplayer.getWidth() - fontMetrics
						.stringWidth(instructionsMessage)) / 2,
				2*screenDisplayer.getHeight() / (gameModeMessages.size() + 3));
		for (int i = 0; i < gameModeMessages.size(); i++) {
			g.drawString(
					gameModeMessages.get(i),
					(screenDisplayer.getWidth() - maxWidth) / 2,
					(i + 3) * screenDisplayer.getHeight()
							/ (gameModeMessages.size() + 3));
		}
		g.setColor(lastColor);
	}

}
