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

public class LevelSelectScreen implements Screen {

	private ScreenDisplayer screenDisplayer;
	private GameMode gameMode;

	public LevelSelectScreen(ScreenDisplayer screenDisplayer, GameMode gameMode) {
		this.screenDisplayer = screenDisplayer;
		this.gameMode = gameMode;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		Level level;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_1:
			level = Level.ONE;
			break;
		case KeyEvent.VK_2:
			level = Level.TWO;
			break;
		case KeyEvent.VK_3:
			level = Level.THREE;
			break;
		case KeyEvent.VK_4:
			level = Level.FOUR;
			break;
		case KeyEvent.VK_5:
			level = Level.FIVE;
			break;
		case KeyEvent.VK_6:
			level = Level.SIX;
			break;
		case KeyEvent.VK_ESCAPE:
			screenDisplayer.setScreen(new MainMenuScreen(screenDisplayer));
		default:
			level = null;
		}
		if (level != null) {
			screenDisplayer.setScreen(new GameScreen(
					screenDisplayer, gameMode, level));
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
		String instructionsMessage = "Select a level (Press the key indicated in brackets): ";
		List<String> levelMessages = new ArrayList<String>();
		levelMessages.add("[1] Level One");
		levelMessages.add("[2] Level Two");
		levelMessages.add("[3] Level Three");
		levelMessages.add("[4] Level Four");
		levelMessages.add("[5] Level Five");
		levelMessages.add("[6] Level Six");
		String exitMessage = "Press: [ESC] to exit to main menu";
		FontMetrics fontMetrics = g.getFontMetrics(menuFont);
		int maxWidth = 0;
		for (String levelMessage : levelMessages) {
			int width = fontMetrics.stringWidth(levelMessage);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		g.drawImage(GameConstants.MENU_BACKGROUND_IMAGE, 0, 0, screenDisplayer.getWidth(),
				screenDisplayer.getHeight(), null);
		g.setColor(GameConstants.MENU_TEXT_COLOR);
		g.drawString(instructionsMessage,
				(screenDisplayer.getWidth() - g.getFontMetrics(menuFont)
						.stringWidth(instructionsMessage)) / 2,
				screenDisplayer.getHeight() / (levelMessages.size() + 3));
		for (int i = 0; i < levelMessages.size(); i++) {
			g.drawString(
					levelMessages.get(i),
					(screenDisplayer.getWidth() - maxWidth) / 2,
					(i + 2) * screenDisplayer.getHeight()
							/ (levelMessages.size() + 3));
		}
		g.drawString(exitMessage,
				(screenDisplayer.getWidth() - fontMetrics
						.stringWidth(exitMessage)) / 2,
						(levelMessages.size() + 2)*screenDisplayer.getHeight() / (levelMessages.size() + 3));
		g.setFont(lastFont);
		g.setColor(lastColor);
	}

}
