package user_interface;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

public interface Screen {
	/*
	 * This is an interface to be used by different screens (e.g. the main menu
	 * screen and the game screen) that are displayed using a ScreenDisplayer
	 * object.
	 */

	/*
	 * Called every loop cycle in the game loop. Updates the variables that need
	 * to be updated every cycle.
	 */
	public void update();

	/*
	 * Called every loop cycle in the game loop. Draws the display using the
	 * Graphics object.
	 */
	public void render(Graphics g);

	/*
	 * Called when a key is pressed.
	 */
	public void keyPressed(KeyEvent e);
}
