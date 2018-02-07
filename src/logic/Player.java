package logic;

import java.awt.Color;
import java.awt.Graphics;

import map.Cell;

public class Player extends Entity {
	private final Color color;
	private final String name;
	private final double baseVel;
	private final double toleranceConstant;
	protected Cell currentCell;
	private final Cell endCell;
	protected Direction currentDirection;
	private Direction queuedDirection;
	private final double playerProportionOfCellDimensions;
	private int numCheckpointsReached;
	private final int numCheckpointsToReach;
	
	public Player(Cell startCell, Cell endCell, double baseVel, double toleranceConstant, Color color, String name, double playerProportionOfCellDimensions, int numCheckpointsToReach) {
		super(startCell.x, startCell.y, startCell.width, startCell.height);
		this.color = color;
		this.name = name;
		this.baseVel = baseVel;
		this.toleranceConstant = toleranceConstant;
		this.playerProportionOfCellDimensions = playerProportionOfCellDimensions;
		this.numCheckpointsToReach = numCheckpointsToReach;
		numCheckpointsReached = 0;
		this.endCell = endCell;
		currentCell = startCell;
		currentDirection = Direction.RIGHT;
		checkCheckpoint();
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}

	protected void changeDirection(Direction targetDirection) {
		double adjustedVel = baseVel*currentCell.getSpeedMultiplier();
		Cell neighbouringCell = currentCell.getNeighbouringCell(targetDirection);
		boolean adjacent = currentCell.isAdjacentTo(neighbouringCell);
		if(!adjacent) {
			queuedDirection = targetDirection;
		} else {
			double tolerance = (adjustedVel)*toleranceConstant;
			switch(targetDirection) {
			case UP: case DOWN:
				if(x >= currentCell.getX() - tolerance*currentCell.getWidth() && x <= currentCell.getX() + tolerance*currentCell.getWidth()) {
					x = currentCell.getX();
					currentDirection = targetDirection;
					queuedDirection = null;
				} else {
					queuedDirection = targetDirection;
				} break;
			case LEFT: case RIGHT:
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
		
	private void move() {
		Cell neighbouringCell = currentCell.getNeighbouringCell(currentDirection);
		boolean adjacent = currentCell.isAdjacentTo(neighbouringCell);
		double adjustedVel = baseVel*currentCell.getSpeedMultiplier();
		//assumes you +/- vel*delta doesn't make you skip an entire cell (which should be true)
		switch (currentDirection) {
		case UP:
			if (!adjacent && y - adjustedVel < currentCell.getY()) {
				y = currentCell.getY();
			} else {
				y -= adjustedVel;
			}
			break;
		case DOWN:
			if (!adjacent && y + adjustedVel > currentCell.getY()) {
				y = currentCell.getY();
			} else {
				y += adjustedVel;
			}
			break;
		case RIGHT:
			if (!adjacent && x + adjustedVel > currentCell.getX()) {
				x = currentCell.getX();
			} else {
				x += adjustedVel;
			}
			break;
		case LEFT:
			if (!adjacent && x - adjustedVel < currentCell.getX()) {
				x = currentCell.getX();
			} else {
				x -= adjustedVel;
			}
			break;
		}
		if (adjacent) {
			if (neighbouringCell.isInCell(getCentreX(), getCentreY())) {
				currentCell = neighbouringCell;
				checkCheckpoint();
			}
		}
	}
	
	private void checkCheckpoint() {
		if(currentCell.isCheckpoint()) {
			if(currentCell.addEncounteredPlayer(this)) {
				numCheckpointsReached++;
			}
		}
	}
	
	public boolean finished() {
		return numCheckpointsReached == numCheckpointsToReach && currentCell == endCell;
	}

	@Override
	public void update() {
		if(queuedDirection != null) {
			changeDirection(queuedDirection);
		}
		move();
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
