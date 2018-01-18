package logic;

import user_interface.Application;

public abstract class Sprite extends Entity {

	protected int vel;
	protected Direction directionFacing;

	public Sprite(int x, int y, int w, int h, double angle, double turningRate) {
		super(x, y, w, h);
		int rand = Application.rng.nextInt(Direction.values().length);
		int i = 0;
		for (Direction direction : Direction.values()) {
			if (rand == i) {
				direction = directionFacing;
				break;
			}
			i++;
		}
	}

	// use of enum to restrict options at compile time and avoid need to check
	// for abnormals
	public void move(Direction dir) {
		switch (dir) {
		case NORTH:
			setY(getY() - vel);
		case SOUTH:
			setY(getY() + vel);
		case EAST:
			setX(getX() + vel);
		case WEST:
			setX(getX() - vel);
		}
	}

}
