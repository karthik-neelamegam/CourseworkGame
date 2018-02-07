package logic;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import map.Cell;

public class HumanPlayer extends Player implements KeyListener {
	private final int upKey, downKey, leftKey, rightKey;
	public HumanPlayer(Cell startCell, Cell endCell, double baseVel, double toleranceConstant, Color color, String name, double playerProportionOfCellDimensions, int numCheckpointsToReach, int upKey, int downKey, int leftKey, int rightKey) {
		super(startCell, endCell, baseVel, toleranceConstant, color, name, playerProportionOfCellDimensions, numCheckpointsToReach);
		this.upKey = upKey;
		this.downKey = downKey;
		this.leftKey = leftKey;
		this.rightKey = rightKey;
	}
	
	@Override
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

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
