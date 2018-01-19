package logic;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class HumanPlayer extends Player implements KeyListener {

	public HumanPlayer(double x, double y, double width, double height,
			double startEnergy, double baseVel, Color color) {
		super(x, y, width, height, startEnergy, baseVel, color);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			setActionFlag(Action.MOVE_NORTH, true);
			break;
		case KeyEvent.VK_DOWN:
			setActionFlag(Action.MOVE_SOUTH, true);
			break;
		case KeyEvent.VK_RIGHT:
			setActionFlag(Action.MOVE_EAST, true);
			break;
		case KeyEvent.VK_LEFT:
			setActionFlag(Action.MOVE_WEST, true);
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			setActionFlag(Action.MOVE_NORTH, false);
			break;
		case KeyEvent.VK_DOWN:
			setActionFlag(Action.MOVE_SOUTH, false);
			break;
		case KeyEvent.VK_RIGHT:
			setActionFlag(Action.MOVE_EAST, false);
			break;
		case KeyEvent.VK_LEFT:
			setActionFlag(Action.MOVE_WEST, false);
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
