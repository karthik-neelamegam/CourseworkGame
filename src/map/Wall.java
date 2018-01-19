package map;

import java.awt.Color;
import java.awt.Graphics;

import logic.Direction;
import logic.Entity;

public class Wall extends Entity {
	public Wall(double x, double y, double width, double height) {
		super(x, y, width, height);
	}

	public static Wall createWall(Cell cell1, Cell cell2,
			double proportionOfCellDimensions) {
		double x = 0, y = 0, width = 0, height = 0;
		if (cell1.getX() != cell2.getX()) { //implies the wall is vertical
			x = Math.max(cell1.getX(), cell2.getX());
			y = cell1.getY();
			width = cell1.getWidth() * proportionOfCellDimensions;
			y -= cell1.getHeight() * proportionOfCellDimensions / 2;
			height = cell1.getHeight() + cell1.getHeight()
					* proportionOfCellDimensions;
			x -= width / 2;
		} else if (cell1.getY() != cell2.getY()) { //implies the wall is horizontal
			y = Math.max(cell1.getY(), cell2.getY());
			x = cell1.getX();
			height = cell1.getHeight() * proportionOfCellDimensions;
			x -= cell1.getWidth() * proportionOfCellDimensions / 2;
			width = cell1.getWidth() + cell1.getWidth()
					* proportionOfCellDimensions;
			y -= height / 2;
		}
		return new Wall(x, y, width, height);
	}
	
	public static Wall createWall(Cell cell, Direction dir,
			double proportionOfCellDimensions) {
		double x = 0, y = 0, width = 0, height = 0;
		if(dir == Direction.WEST || dir == Direction.EAST) {
			if(dir == Direction.WEST) {
				x = cell.getX();
			} else {
				x = cell.getX() + cell.getWidth();
			}
			y = cell.getY();
			width = cell.getWidth() * proportionOfCellDimensions;
			y -= cell.getHeight() * proportionOfCellDimensions / 2;
			height = cell.getHeight() + cell.getHeight()
					* proportionOfCellDimensions;
			x -= width / 2;
		} else if(dir == Direction.NORTH || dir == Direction.SOUTH) {
			if(dir == Direction.NORTH) {
				y = cell.getY();
			} else {
				y = cell.getY() + cell.getHeight();
			}
			x = cell.getX();
			height = cell.getHeight() * proportionOfCellDimensions;
			x -= cell.getWidth() * proportionOfCellDimensions / 2;
			width = cell.getWidth() + cell.getWidth()
					* proportionOfCellDimensions;
			y -= height / 2;
		}
		return new Wall(x, y, width, height);
	}


	@Override
	public void update(double delta) {

	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect((int) x, (int) y, (int) width, (int) height);
	}
}
