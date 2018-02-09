package user_interface;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import map.GameConstants;
import map.Level;

public class MainMenuScreen implements Screen {
	private final ScreenDisplayer screenDisplayer;
	public MainMenuScreen(ScreenDisplayer screenDisplayer) {
		this.screenDisplayer = screenDisplayer;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_1:
			screenDisplayer.setScreen(new GameScreen(screenDisplayer, GameMode.AGAINST_AI, Level.ONE));
			break;
		case KeyEvent.VK_2:
			screenDisplayer.setScreen(new LevelSelectScreen(screenDisplayer, GameMode.TWO_PLAYER));
			break;
		case KeyEvent.VK_3:
			screenDisplayer.setScreen(new LevelSelectScreen(screenDisplayer, GameMode.TRAINING));
		}
	}
	
	@Override
	public void update() {
	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
		Font lastFont = g.getFont();
		Font menuFont = GameConstants.getMenuFont(screenDisplayer.getHeight());
		Font titleFont = GameConstants.getTitleFont(screenDisplayer.getHeight());
		String titleMessage = "MazeRace";
		String instructionsMessage = "Select a game mode (Press the key indicated in brackets): ";
		List<String> gameModeMessages = new ArrayList<String>();
		gameModeMessages.add("[1] Against AI");
		gameModeMessages.add("[2] Two Player");
		gameModeMessages.add("[3] Training");
		FontMetrics fontMetrics = g.getFontMetrics(menuFont);
		int maxWidth = 0;
		for (String levelMessage : gameModeMessages) {
			int width = fontMetrics.stringWidth(levelMessage);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		
		g.drawImage(GameConstants.MENU_BACKGROUND_IMAGE, 0,0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight(), null);
		
		g.setColor(GameConstants.TITLE_TEXT_COLOR);
		g.setFont(titleFont);
		g.drawString(titleMessage, (screenDisplayer.getWidth()-g.getFontMetrics(titleFont).stringWidth(titleMessage))/2, screenDisplayer.getHeight()/(gameModeMessages.size() + 3));
		
		g.setColor(GameConstants.MENU_TEXT_COLOR);
		g.setFont(menuFont);
		
		g.drawString(instructionsMessage,
				(screenDisplayer.getWidth() - g.getFontMetrics(menuFont)
						.stringWidth(instructionsMessage)) / 2,
				2*screenDisplayer.getHeight() / (gameModeMessages.size() + 3));
		for (int i = 0; i < gameModeMessages.size(); i++) {
			g.drawString(
					gameModeMessages.get(i),
					(screenDisplayer.getWidth() - maxWidth) / 2,
					(i + 3) * screenDisplayer.getHeight()
							/ (gameModeMessages.size() + 3));
		}
		
		g.setFont(lastFont);
		g.setColor(lastColor);
	}

}
