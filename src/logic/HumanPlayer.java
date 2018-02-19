package logic;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import map.Cell;

public class HumanPlayer extends Player {
	/*
	 * This class is used to represent Player objects that are controlled by a
	 * human (as opposed to the computer).
	 */

	/*
	 * This class extends the Player class (inheritance). The changeDirection
	 * method is inherited, allowing the HumanPlayer object to change direction
	 * in response to key presses.
	 */

	/*
	 * The key codes for the keys that, when pressed, should attempt to change
	 * the player’s direction to UP, DOWN, LEFT, and RIGHT respectively.
	 */
	private final int upKey, downKey, leftKey, rightKey;

	/*
	 * Constructor.
	 */
	public HumanPlayer(Cell startCell, Cell endCell, double baseVel,
			double toleranceConstant, Color color, String name,
			double playerProportionOfCellDimensions, int numCheckpointsToReach,
			int upKey, int downKey, int leftKey, int rightKey) {
		/*
		 * The superclass's constructor must be called first.
		 */
		super(startCell, endCell, baseVel, toleranceConstant, color, name,
				playerProportionOfCellDimensions, numCheckpointsToReach);
		this.upKey = upKey;
		this.downKey = downKey;
		this.leftKey = leftKey;
		this.rightKey = rightKey;
	}

	/*
	 * Called when a key is pressed. If the key code of the key pressed is one
	 * of upKey, downKey, leftKey, or rightKey, attempts to change the direction
	 * of the player to the respective direction by calling the changeDirection
	 * method of the superclass Player.
	 */
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == upKey) {
			changeDirection(Direction.UP);
		} else if (keyCode == downKey) {
			changeDirection(Direction.DOWN);
		} else if (keyCode == rightKey) {
			changeDirection(Direction.RIGHT);
		} else if (keyCode == leftKey) {
			changeDirection(Direction.LEFT);
		}
	}
}
