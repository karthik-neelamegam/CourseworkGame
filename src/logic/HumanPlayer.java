package logic;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import map.Cell;

public class HumanPlayer extends Player implements KeyListener {

	public HumanPlayer(Cell startCell, double baseVel, Color color) {
		super(startCell, baseVel, color);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			direction = Direction.NORTH;
			break;
		case KeyEvent.VK_DOWN:
			direction = Direction.SOUTH;
			break;
		case KeyEvent.VK_RIGHT:
			direction = Direction.EAST;
			break;
		case KeyEvent.VK_LEFT:
			direction = Direction.WEST;
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
