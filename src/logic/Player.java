package logic;

import java.awt.Color;
import java.awt.Graphics;

import map.Cell;

public class Player extends Sprite {
	protected Color color;
	protected double baseVel;
	protected Cell currentCell;
	protected Direction queuedDirection;
	public Player(Cell startCell, double baseVel, Color color) {
		super(startCell.x, startCell.y, startCell.width, startCell.height, baseVel);
		this.color = color;
		this.baseVel = baseVel;
		currentCell = startCell;
		direction = Direction.EAST;
	}

	private void move(double delta) {
		Cell adjacentCell = currentCell.getAdjacentCell(direction);
		if(adjacentCell != null) {
			System.out.println("NOT NULL");
			System.out.println(adjacentCell.x + " " + adjacentCell.y);
		} 
		//assumes you +/- vel*delta doesn't make you skip an entire cell (which should be true)
		switch (direction) {
		case NORTH:
			//System.out.println("NORTH");
			if (adjacentCell == null && y - vel * delta < currentCell.getY()) {
				y = currentCell.getY();
			} else {
				y -= vel * delta;
			}
			break;
		case SOUTH:
			//System.out.println("SOUTH");
			//System.out.println((y + vel*delta) + ";   " + currentCell.getY());
			if (adjacentCell == null && y + vel * delta > currentCell.getY()) {
				y = currentCell.getY();
			} else {
				y += vel * delta;
			}
			break;
		case EAST:
			//System.out.println("EAST");
			if (adjacentCell == null && x + vel * delta > currentCell.getX()) {
				x = currentCell.getX();
			} else {
				x += vel * delta;
			}
			break;
		case WEST:
			//System.out.println("WEST");
			if (adjacentCell == null && x - vel * delta < currentCell.getX()) {
				x = currentCell.getX();
			} else {
				x -= vel * delta;
			}
			break;
		}
		if (adjacentCell != null) {
			if (adjacentCell.isInCell(getCentreX(), getCentreY())) {
				currentCell = adjacentCell;
			}
		}
	}

	@Override
	public void update(double delta) {
		vel = baseVel;
		move(delta);
	}

	@Override
	public void render(Graphics g) {
		g.setColor(color);
		g.fillOval((int) (x+width/16), (int) (y+height/16), (int) (14*width/16), (int) (14*height/16));
		g.setColor(Color.BLACK);
		g.drawOval((int) (x+width/16), (int) (y+height/16), (int) (14*width/16), (int) (14*height/16));
	}
}
