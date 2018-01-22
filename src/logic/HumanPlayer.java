package logic;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import map.Cell;

public class HumanPlayer extends Player implements KeyListener {

	public HumanPlayer(Cell startCell, double baseVel, Color color, int numCheckpointsToReach, double tolerance) {
		super(startCell, baseVel, color, numCheckpointsToReach, tolerance);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			changeDirection(Direction.NORTH);
			break;
		case KeyEvent.VK_DOWN:
			changeDirection(Direction.SOUTH);
			break;
		case KeyEvent.VK_RIGHT:
			changeDirection(Direction.EAST);
			break;
		case KeyEvent.VK_LEFT:
			changeDirection(Direction.WEST);
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

}
