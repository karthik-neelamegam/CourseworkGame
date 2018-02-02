package logic;

import java.awt.Color;
import java.awt.Graphics;

import map.Cell;

public class Player extends Entity {
	private Color color;
	private String name;
	private final double baseVel;
	protected Cell currentCell;
	private Cell endCell;
	protected Direction currentDirection;
	private Direction queuedDirection;
	private double playerProportionOfCellDimensions;
	private int numCheckpointsReached, numCheckpointsToReach;
	//protected double tolerance; //should be twice the ratio between max velocity and cellside for consistency 
	public Player(Cell startCell, Cell endCell, double baseVel, Color color, String name, double playerProportionOfCellDimensions, int numCheckpointsToReach) {
		super(startCell.x, startCell.y, startCell.width, startCell.height);
		this.color = color;
		this.name = name;
		//this.tolerance = tolerance;
		this.baseVel = baseVel;
		this.playerProportionOfCellDimensions = playerProportionOfCellDimensions;
		this.numCheckpointsToReach = numCheckpointsToReach;
		numCheckpointsReached = 0;
		this.endCell = endCell;
		currentCell = startCell;
		if(startCell.hasCheckpoint()) {
			if(currentCell.getCheckpoint().addEncounteredPlayer(this)) {
				numCheckpointsReached++;
			}
		}
		currentDirection = Direction.EAST;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}

	protected void changeDirection(Direction targetDirection) {
		double adjustedVel = baseVel*currentCell.getSpeedMultiplier();
		Cell adjacentCell = currentCell.getAdjacentCell(targetDirection);
		if(adjacentCell == null) {
			switch(targetDirection) {
			case NORTH: case SOUTH:
				if(y == currentCell.getY()) {
					queuedDirection = targetDirection;
				} else {
					currentDirection = targetDirection;
					queuedDirection = null;
				} break;
			case WEST: case EAST:
				if(x == currentCell.getX()) {
					queuedDirection = targetDirection;
				} else {
					currentDirection = targetDirection;
					queuedDirection = null;
				} break;
			}
		} else {
			double tolerance = 0;
			switch(targetDirection) {
			case NORTH: case SOUTH:
				tolerance = (adjustedVel/currentCell.getWidth())*2;
				if(x >= currentCell.getX() - tolerance*currentCell.getWidth() && x <= currentCell.getX() + tolerance*currentCell.getWidth()) {
					x = currentCell.getX();
					currentDirection = targetDirection;
					queuedDirection = null;
				} else {
					queuedDirection = targetDirection;
				} break;
			case WEST: case EAST:
				tolerance = (adjustedVel/currentCell.getHeight())*2;
				if(y >= currentCell.getY() - tolerance*currentCell.getHeight() && y <= currentCell.getY() + tolerance*currentCell.getHeight()) {
					y = currentCell.getY();
					currentDirection = targetDirection;
					queuedDirection = null;
				} else {
					queuedDirection = targetDirection;
				} break;
			}
		}
	}
		
	private void move(double delta) {
		Cell adjacentCell = currentCell.getAdjacentCell(currentDirection);
		double adjustedVel = baseVel*currentCell.getSpeedMultiplier();
		//assumes you +/- vel*delta doesn't make you skip an entire cell (which should be true)
		switch (currentDirection) {
		case NORTH:
			if (adjacentCell == null && y - baseVel * delta < currentCell.getY()) {
				y = currentCell.getY();
			} else {
				y -= adjustedVel * delta;
			}
			break;
		case SOUTH:
			if (adjacentCell == null && y + baseVel * delta > currentCell.getY()) {
				y = currentCell.getY();
			} else {
				y += adjustedVel * delta;
			}
			break;
		case EAST:
			if (adjacentCell == null && x + baseVel * delta > currentCell.getX()) {
				x = currentCell.getX();
			} else {
				x += adjustedVel * delta;
			}
			break;
		case WEST:
			if (adjacentCell == null && x - baseVel * delta < currentCell.getX()) {
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
	
	public boolean finished() {
		return numCheckpointsReached == numCheckpointsToReach && currentCell == endCell;
	}

	@Override
	public void update(double delta) {
		if(queuedDirection != null) {
			changeDirection(Direction.valueOf(queuedDirection.toString()));
		}
		move(delta);
	}

	@Override
	public void render(Graphics g) {
		Color lastColor = g.getColor();
		g.setColor(color);
		g.fillOval((int) (x+width*(1-playerProportionOfCellDimensions)/2), (int) (y+height*(1-playerProportionOfCellDimensions)/2), (int) (width*playerProportionOfCellDimensions), (int) (height*playerProportionOfCellDimensions));
		g.setColor(Color.BLACK);
		g.drawOval((int) (x+width*(1-playerProportionOfCellDimensions)/2), (int) (y+height*(1-playerProportionOfCellDimensions)/2), (int) (width*playerProportionOfCellDimensions), (int) (height*playerProportionOfCellDimensions));
		g.setColor(lastColor);
	}
}
