package logic;

public abstract class Sprite extends Entity {

	protected double turningRate;
	protected int vel;
	protected double angle;

	public Sprite(int x, int y, int w, int h, double angle, double turningRate) {
		super(x, y, w, h);
		this.angle = angle;
		this.turningRate = turningRate;
	}

	// use of enum to restrict options at compile time and avoid need to check
	// for abnormals
	public void move(Direction dir) {
		if (dir == Direction.ANTICLOCKWISE) {
			angle += turningRate;
		} else if (dir == Direction.CLOCKWISE) {
			angle -= turningRate;
		}
		setX(getX() + (int)(Math.round(vel * Math.cos(angle))));
		setY(getY() + (int)(Math.round(vel * Math.sin(angle))));
	}

}
