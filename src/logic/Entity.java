package logic;

import java.awt.Graphics;

public abstract class Entity {
	/*
	 * This class is as an abstract class from which physical game objects (i.e.
	 * those that have a position and dimensions) can extend and inherit common
	 * attributes and methods (inheritance). It contains abstract methods
	 * (update and render) which must be implemented by any class that extends
	 * this class (polymorphism).
	 */

	/*
	 * The pixel x-coordinate and y-coordinate of the entity.
	 */
	protected double x, y;

	/*
	 * The width and height of the entity in pixels.
	 */
	protected final double width, height;

	/*
	 * Constructor.
	 */
	public Entity(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/*
	 * Abstract method that must be implemented by subclasses (polymorphism).
	 * Called every game cycle. Updates the variables that need to be updated
	 * every cycle.
	 */
	public abstract void update();

	/*
	 * Abstract method that must be implemented by subclasses (polymorphism).
	 * Called every game cycle. Draws the entity onto the screen using the
	 * Graphics object.
	 */
	public abstract void render(Graphics graphics);

	/*
	 * Returns the pixel x-coordinate of the centre of the entity (assuming it
	 * is a rectangle, otherwise this method can be overridden by inheriting
	 * subclasses).
	 */
	public double getCentreX() {
		return x + width / 2;
	}

	/*
	 * Returns the pixel y-coordinate of the centre of the entity (assuming it
	 * is a rectangle, otherwise this method can be overridden by inheriting
	 * subclasses).
	 */
	public double getCentreY() {
		return y + height / 2;
	}

	/*
	 * Returns the pixel distance between the centre co-ordinates of this Entity
	 * object and those of the other parameter.
	 */
	public double getDistanceBetweenCentres(Entity other) {
		return Math.sqrt(Math.pow(getCentreX() - other.getCentreX(), 2)
				+ Math.pow(getCentreY() - other.getCentreY(), 2));
	}

	/*
	 * Getters and setters.
	 */

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}
}
