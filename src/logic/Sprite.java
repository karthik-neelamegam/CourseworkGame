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
			y -= vel;
			directionFacing = Direction.NORTH;
			break;
		case SOUTH:
			y += vel;
			directionFacing = Direction.SOUTH;
			break;
		case EAST:
			x += vel;
			directionFacing = Direction.EAST;
			break;
		case WEST:
			x -= vel;
			directionFacing = Direction.WEST;
			break;
		}
	}

}
