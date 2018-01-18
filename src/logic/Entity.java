package logic;

import java.awt.Graphics;

public abstract class Entity {
	
	protected int x, y, width, height;

	public Entity(int x, int y, int width, int height) {
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
	
	public int getCentreX() {
		return x + width/2;
	}
	
	public int getCentreY() {
		return y + height/2;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
