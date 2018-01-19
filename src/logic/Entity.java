package logic;

import java.awt.Graphics;

public abstract class Entity {
	
	protected double x, y, width, height;

	public Entity(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public abstract void update(double delta);

	public abstract void render(Graphics g);
	
	public double getSquaredEuclideanDistanceBetweenCentres(Entity other) {
		return Math.pow(getCentreX() - other.getCentreX(), 2)
		+ Math.pow(getCentreY() - other.getCentreY(), 2);
	}
	
	public double getEuclideanDistanceBetweenCentres(Entity other) {
		return Math.sqrt(getSquaredEuclideanDistanceBetweenCentres(other));
	}
	
	public double getCentreX() {
		return x + width/2;
	}
	
	public double getCentreY() {
		return y + height/2;
	}
	
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

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(int h) {
		this.height = h;
	}
}
