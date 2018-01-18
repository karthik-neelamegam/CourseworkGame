package map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import logic.Entity;

public class Wall extends Entity {
	public Wall(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public static Wall createWall(Cell cell1, Cell cell2, double proportionOfCellDimensions) {
		int x = 0, y = 0, width = 0, height = 0;
		if(cell1.getX() != cell2.getX()) {
			x = Math.max(cell1.getX(), cell2.getX());
			y = cell1.getY();
			width = (int)(cell1.getWidth()*proportionOfCellDimensions) + (int)(cell1.getHeight()*proportionOfCellDimensions/2);
			height = cell1.getHeight();
			x -= (int)(((float)width)/2);
		}
		else if(cell1.getY() != cell2.getY()) {
			y = Math.max(cell1.getY(), cell2.getY());
			x = cell1.getX();
			height = (int)(cell1.getHeight()*proportionOfCellDimensions) + (int)(cell1.getWidth()*proportionOfCellDimensions/2);
			width = cell1.getWidth();
			y -= (int)(((float)height)/2);
		}
		return new Wall(x, y, width, height);
	}
	
	@Override
	public void update(double delta) {
		
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(getX(),getY(),getWidth(),getHeight());
	}
}
