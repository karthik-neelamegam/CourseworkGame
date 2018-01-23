package logic;

import java.awt.Color;
import java.awt.Graphics;

import map.Cell;

public class Player extends Sprite {
	protected Color color;
	protected double baseVel, currentVel;
	protected Cell currentCell;
	protected Cell targetCell;
	protected Direction queuedDirection;
	protected int numCheckpointsReached;
	protected int numCheckpointsToReach;
	//protected double tolerance; //should be twice the ratio between max velocity and cellside for consistency 
	public Player(Cell startCell, double baseVel, Color color, int numCheckpointsToReach, double tolerance) {
		super(startCell.x, startCell.y, startCell.width, startCell.height, baseVel);
		this.color = color;
		//this.tolerance = tolerance;
		this.baseVel = baseVel;
		currentCell = startCell;
		this.numCheckpointsToReach = numCheckpointsToReach;
		numCheckpointsReached = 0;
		if(startCell.hasCheckpoint()) {
			if(currentCell.getCheckpoint().addEncounteredPlayer(this)) {
				numCheckpointsReached++;
			}
		}
		direction = Direction.EAST;
	}
	
	public Color getColor() {
		return color;
	}

	protected void changeDirection(Direction direction) {
		double adjustedVel = vel*currentCell.getSpeedMultiplier();
		Cell adjacentCell = currentCell.getAdjacentCell(direction);
		if(adjacentCell == null) {
			switch(direction) {
			case NORTH: case SOUTH:
				if(y == currentCell.getY()) {
					System.out.println("A");
					queuedDirection = direction;
				} else {
					System.out.println("B");

					this.direction = direction;
					queuedDirection = null;
				} break;
			case WEST: case EAST:
				if(x == currentCell.getX()) {
					System.out.println("C");

					queuedDirection = direction;
				} else {
					System.out.println("D");

					this.direction = direction;
					queuedDirection = null;
				} break;
			}
		} else {
			double tolerance = 0;
			switch(direction) {
			case NORTH: case SOUTH:
				tolerance = (adjustedVel/currentCell.getWidth())*2;
				if(x >= currentCell.getX() - tolerance*currentCell.getWidth() && x <= currentCell.getX() + tolerance*currentCell.getWidth()) {
					System.out.println("E");
					x = currentCell.getX();
					this.direction = direction;
					queuedDirection = null;
				} else {
					System.out.println("F");
					queuedDirection = direction;
				} break;
			case WEST: case EAST:
				tolerance = (adjustedVel/currentCell.getHeight())*2;
				if(y >= currentCell.getY() - tolerance*currentCell.getHeight() && y <= currentCell.getY() + tolerance*currentCell.getHeight()) {
					System.out.println("G");
					y = currentCell.getY();
					this.direction = direction;
					queuedDirection = null;
				} else {
					System.out.println("H");
					queuedDirection = direction;
				} break;
			}
		}
	}
	
	protected void move2(Cell toCell, double delta) {
		
	}
	
	protected void move(double delta) {
		Cell adjacentCell = currentCell.getAdjacentCell(direction);
		if(adjacentCell != null) {
			//System.out.println("NOT NULL");
			//System.out.println(adjacentCell.x + " " + adjacentCell.y);
		} 
		double adjustedVel = vel*currentCell.getSpeedMultiplier();
		//assumes you +/- vel*delta doesn't make you skip an entire cell (which should be true)
		switch (direction) {
		case NORTH:
			if (adjacentCell == null && y - vel * delta < currentCell.getY()) {
				y = currentCell.getY();
			} else {
				y -= adjustedVel * delta;
			}
			break;
		case SOUTH:
			if (adjacentCell == null && y + vel * delta > currentCell.getY()) {
				y = currentCell.getY();
			} else {
				y += adjustedVel * delta;
			}
			break;
		case EAST:
			if (adjacentCell == null && x + vel * delta > currentCell.getX()) {
				x = currentCell.getX();
			} else {
				x += adjustedVel * delta;
			}
			break;
		case WEST:
			if (adjacentCell == null && x - vel * delta < currentCell.getX()) {
				x = currentCell.getX();
			} else {
				x -= adjustedVel * delta;
			}
			break;
		}
		if (adjacentCell != null) {
			if (adjacentCell.isInCell(getCentreX(), getCentreY())) {
				currentCell = adjacentCell;
				if(currentCell.hasCheckpoint()) {
					if(currentCell.getCheckpoint().addEncounteredPlayer(this)) {
						numCheckpointsReached++;
					}
				}	
			}
		}
	}

	@Override
	public void update(double delta) {
		if(queuedDirection != null) {
			changeDirection(Direction.valueOf(queuedDirection.toString()));
		}
		//System.out.println();
		//System.out.println(currentCell.getX()+ ", " + currentCell.getY());
		//System.out.println("Direction: " + direction.toString());
		//System.out.println("QueuedDirection: " + ((queuedDirection != null) ? queuedDirection.toString() : "NULL"));
		vel = baseVel;
		move(delta);
	}

	@Override
	public void render(Graphics g) {
		g.setColor(color);
		g.fillOval((int) (x+width/16), (int) (y+height/16), (int) (14*width/16), (int) (14*height/16));
		g.setColor(Color.BLACK);
		g.drawString(""+numCheckpointsReached, (int) (x+width/2), (int) (y+height/2));
		g.drawOval((int) (x+width/16), (int) (y+height/16), (int) (14*width/16), (int) (14*height/16));
	}
}
