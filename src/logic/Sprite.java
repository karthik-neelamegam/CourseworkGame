package logic;

import user_interface.Application;

public abstract class Sprite extends Entity {

	protected double vel;
	protected Direction directionFacing;

	public Sprite(double x, double y, double width, double height, double vel) {
		super(x, y, width, height);
		this.vel = vel;
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
	public void move(Direction dir, double delta) {
		switch (dir) {
		case NORTH:
			y -= vel*delta;
			directionFacing = Direction.NORTH;
			break;
		case SOUTH:
			y += vel*delta;
			directionFacing = Direction.SOUTH;
			break;
		case EAST:
			x += vel*delta;
			directionFacing = Direction.EAST;
			break;
		case WEST:
			x -= vel*delta;
			directionFacing = Direction.WEST;
			break;
		}
	}

}
