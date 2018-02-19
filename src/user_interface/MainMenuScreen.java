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
	/*
	 * This class is the main menu screen and executes methods in response to
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
	 * MainMenuScreen class has a HAS-A relationship with the ScreenDisplayer
	 * class but the screenDisplayer object will not be destroyed if the
	 * MainMenuScreen object is destroyed.
	 */
	private final ScreenDisplayer screenDisplayer;

	/*
	 * Constructor.
	 */
	public MainMenuScreen(ScreenDisplayer screenDisplayer) {
		this.screenDisplayer = screenDisplayer;
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
		switch (keyEvent.getKeyCode()) {
		/*
		 * If 1 is pressed, then a game screen against the AI should be started
		 * and the game should start from the first level.
		 */
		case KeyEvent.VK_1:
			screenDisplayer.setScreen(new GameScreen(screenDisplayer,
					GameMode.AGAINST_AI, Level.ONE));
			break;

		/*
		 * If 2 or 3 is pressed, then the user must first select a level to play
		 * so the level select screen is needed. The game mode is passed as an
		 * argument so that once the level is selected, a game can be started
		 * with the correct game mode.
		 */
		case KeyEvent.VK_2:
			screenDisplayer.setScreen(new LevelSelectScreen(screenDisplayer,
					GameMode.TWO_PLAYER));
			break;
		case KeyEvent.VK_3:
			screenDisplayer.setScreen(new LevelSelectScreen(screenDisplayer,
					GameMode.TRAINING));
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
		Font titleFont = GameConstants
				.getTitleFont(screenDisplayer.getHeight());

		String titleMessage = "MazeRace";
		String instructionsMessage = "Select a game mode (Press the key indicated in brackets): ";

		/*
		 * An ArrayList is used to store the game mode options because it makes
		 * it easier to iterate over them, which is required below.
		 */
		List<String> gameModeMessages = new ArrayList<String>();
		gameModeMessages.add("[1] Against AI");
		gameModeMessages.add("[2] Two Player");
		gameModeMessages.add("[3] Training");

		/*
		 * This block of code iterates through gameModeMessages and selects the
		 * string that has the largest width when displayed on screen with the
		 * font menuFont. This width is then used to determine the position of
		 * all the game mode strings on the screen so that they can all be
		 * left-aligned and centered with respect to the largest width string
		 * (so that the menu looks balanced).
		 */
		FontMetrics menuFontMetrics = graphics.getFontMetrics(menuFont);
		int maxWidth = 0;
		for (String gameModeMessage : gameModeMessages) {
			int width = menuFontMetrics.stringWidth(gameModeMessage);
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
		 * Draws the title string in the desired position with the font
		 * titleFont and the desired title text colour.
		 */
		graphics.setColor(GameConstants.TITLE_TEXT_COLOR);
		graphics.setFont(titleFont);
		graphics.drawString(
				titleMessage,
				(screenDisplayer.getWidth() - graphics
						.getFontMetrics(titleFont).stringWidth(titleMessage)) / 2,
				screenDisplayer.getHeight() / (gameModeMessages.size() + 3));

		/*
		 * This block of code draws the instruction string and iterates over
		 * gameModeMessages and draws the game mode option strings in their
		 * desired positions with the font menuFont and the desired menu text
		 * colour.
		 */
		graphics.setColor(GameConstants.MENU_TEXT_COLOR);
		graphics.setFont(menuFont);
		graphics.drawString(instructionsMessage,
				(screenDisplayer.getWidth() - graphics.getFontMetrics(menuFont)
						.stringWidth(instructionsMessage)) / 2,
				2 * screenDisplayer.getHeight() / (gameModeMessages.size() + 3));
		for (int i = 0; i < gameModeMessages.size(); i++) {
			graphics.drawString(
					gameModeMessages.get(i),
					(screenDisplayer.getWidth() - maxWidth) / 2,
					(i + 3) * screenDisplayer.getHeight()
							/ (gameModeMessages.size() + 3));
		}

		graphics.setFont(lastFont);
		graphics.setColor(lastColor);
	}

}
