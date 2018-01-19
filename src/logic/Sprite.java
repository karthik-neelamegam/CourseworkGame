package logic;

import user_interface.Application;

public abstract class Sprite extends Entity {

	protected double vel;
	protected Direction direction;

	public Sprite(double x, double y, double width, double height, double vel) {
		super(x, y, width, height);
		this.vel = vel;
		direction = Direction.NORTH;
	}

	// use of enum to restrict options at compile time and avoid need to check
	// for abnormals
/*	public void move(double delta) {
		switch (direction) {
		case NORTH:
			y -= vel*delta;
			direction = Direction.NORTH;
			break;
		case SOUTH:
			y += vel*delta;
			direction = Direction.SOUTH;
			break;
		case EAST:
			x += vel*delta;
			direction = Direction.EAST;
			break;
		case WEST:
			x -= vel*delta;
			direction = Direction.WEST;
			break;
		}
	}*/

}
