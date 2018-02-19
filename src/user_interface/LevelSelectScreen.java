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
	/*
	 * This class is the level select screen and executes methods in response to
	 * key presses corresponding to the menu options.
	 */

	/*
	 * This class implements the Screen interface. The update and render
	 * abstract methods need to be implemented (polymorphism). These will need
	 * to contain code to be executed every game cycle. The keyPressed abstract
	 * method also needs to be implemented. This will need to contain code to be
	 * executed when a key is pressed.
	 */

	/*
	 * The ScreenDisplayer object that is updating and displaying this screen.
	 * It needs to be stored so that its SetScreen method can be used to change
	 * the screen being displayed on it. This is aggregation as the
	 * LevelSelectScreen class has a HAS-A relationship with the ScreenDisplayer
	 * class but the screenDisplayer object will not be destroyed if the
	 * LevelSelect object is destroyed.
	 */
	private ScreenDisplayer screenDisplayer;

	/*
	 * The game mode that should be used to create the GameScreen object after
	 * selecting a level.
	 */
	private GameMode gameMode;

	/*
	 * Constructor.
	 */
	public LevelSelectScreen(ScreenDisplayer screenDisplayer, GameMode gameMode) {
		this.screenDisplayer = screenDisplayer;
		this.gameMode = gameMode;
	}

	/*
	 * Methods from the Screen interface that need to be implemented
	 * (polymorphism).
	 */

	/*
	 * Executes methods to carry out the actions represented by each menu
	 * option.
	 */
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		Level level;
		switch (keyEvent.getKeyCode()) {
		/*
		 * If a number key from 1 to 6 is pressed, the corresponding level
		 * should be selected.
		 */
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

		/*
		 * If ESC is pressed, return to the main menu.
		 */
		case KeyEvent.VK_ESCAPE:
			screenDisplayer.setScreen(new MainMenuScreen(screenDisplayer));
		default:
			level = null;
		}

		/*
		 * If no level is selected, then we should not start a game with a null
		 * level, as this will cause the program to crash.
		 */
		if (level != null) {
			screenDisplayer.setScreen(new GameScreen(screenDisplayer, gameMode,
					level));
		}
	}

	/*
	 * Does nothing as nothing in this class needs to be updated every cycle.
	 */
	@Override
	public void update() {
	}

	/*
	 * Renders the menu, displaying the background image, title, instructions on
	 * what to do, and all the menu options.
	 */
	@Override
	public void render(Graphics graphics) {
		/*
		 * The colour and font used in the graphics object before this method is
		 * called needs to be stored so that it can be restored at the end of
		 * the method (see below). This prevents side effects when the graphics
		 * object is used again.
		 */
		Color lastColor = graphics.getColor();
		Font lastFont = graphics.getFont();

		Font menuFont = GameConstants.getMenuFont(screenDisplayer.getHeight());
		String instructionsMessage = "Select a level (Press the key indicated in brackets): ";

		/*
		 * An ArrayList is used to store the game mode options because it makes
		 * it easier to iterate over them, which is required below.
		 */
		List<String> levelMessages = new ArrayList<String>();
		levelMessages.add("[1] Level One");
		levelMessages.add("[2] Level Two");
		levelMessages.add("[3] Level Three");
		levelMessages.add("[4] Level Four");
		levelMessages.add("[5] Level Five");
		levelMessages.add("[6] Level Six");

		String exitMessage = "Press: [ESC] to exit to main menu";

		/*
		 * This block of code iterates through levelMessages and selects the
		 * string that has the largest width when displayed on screen with the
		 * font menuFont. This width is then used to determine the position of
		 * all the level strings on the screen so that they can all be
		 * left-aligned and centered with respect to the largest width string
		 * (so that the menu looks balanced).
		 */
		FontMetrics menuFontMetrics = graphics.getFontMetrics(menuFont);
		int maxWidth = 0;
		for (String levelMessage : levelMessages) {
			int width = menuFontMetrics.stringWidth(levelMessage);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}

		/*
		 * Draws the background image. The background should be rendered before
		 * everything else so that everything else appears above the background.
		 */
		graphics.drawImage(GameConstants.MENU_BACKGROUND_IMAGE, 0, 0,
				screenDisplayer.getWidth(), screenDisplayer.getHeight(), null);
		
		/*
		 * This block of code draws the instruction string, exit option, and
		 * iterates over levelMessages and draws the level option strings in
		 * their desired positions with the font menuFont and the desired menu
		 * text colour.
		 */
		
		graphics.setColor(GameConstants.MENU_TEXT_COLOR);
		graphics.setFont(menuFont);
		graphics.drawString(instructionsMessage,
				(screenDisplayer.getWidth() - menuFontMetrics
						.stringWidth(instructionsMessage)) / 2,
				screenDisplayer.getHeight() / (levelMessages.size() + 3));
		for (int i = 0; i < levelMessages.size(); i++) {
			graphics.drawString(
					levelMessages.get(i),
					(screenDisplayer.getWidth() - maxWidth) / 2,
					(i + 2) * screenDisplayer.getHeight()
							/ (levelMessages.size() + 3));
		}
		graphics.drawString(exitMessage,
				(screenDisplayer.getWidth() - menuFontMetrics
						.stringWidth(exitMessage)) / 2,
				(levelMessages.size() + 2) * screenDisplayer.getHeight()
						/ (levelMessages.size() + 3));

		graphics.setFont(lastFont);
		graphics.setColor(lastColor);
	}

}
