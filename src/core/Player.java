package core;

import java.awt.Color;
import java.awt.Graphics;

public abstract class Player extends Entity {
	/*
	 * This class is an abstract class which the HumanPlayer and AIPlayer
	 * classes extend (inheritance). It has base attributes and methods that are
	 * shared by subclasses (e.g. methods to change the direction of the player)
	 */

	/*
	 * This class extends the Entity class (inheritance) as each player has a
	 * physical position on the screen and dimensions. The update and render
	 * methods need to be implemented (polymorphism).
	 */

	/*
	 * The player’s colour. Used for rendering purposes only.
	 */
	private final Color color;

	/*
	 * The player’s name. Used for announcing the winner.
	 */
	private final String name;

	/*
	 * The base velocity of the player. This is how many pixels the player will
	 * move in the direction its moving per game cycle.
	 */
	private final double baseVel;

	/*
	 * The tolerance constant for the player. Used in the changeDirection
	 * method.
	 */
	private final double toleranceConstant;

	/*
	 * The Cell object that the player is currently occupying. This is
	 * aggregation as the Player class has a HAS-A relationship with the Cell
	 * class but the currentCell object will not be destroyed if the Player
	 * object is destroyed.
	 */
	protected Cell currentCell;

	/*
	 * The Cell object that the player is meant to end up at. This is
	 * aggregation as the Player class has a HAS-A relationship with the Cell
	 * class but the endCell object will not be destroyed if the Player object
	 * is destroyed.
	 */
	private final Cell endCell;

	/*
	 * The direction that the player is currently moving in. This is aggregation
	 * as the Player class has a HAS-A relationship with the Direction enum type
	 * but the currentDirection object will not be destroyed if the Player
	 * object is destroyed.
	 */
	protected Direction currentDirection;

	/*
	 * The direction that the player wants to move in but cannot yet (because a
	 * wall is in the way). This is aggregation as the Player class has a HAS-A
	 * relationship with the Direction enum type but the queuedDirection object
	 * will not be destroyed if the Player object is destroyed.
	 */
	private Direction queuedDirection;

	/*
	 * The size of the player graphic relative to the size of a Cell object in
	 * the maze. Used for rendering purposes only.
	 */
	private final double playerProportionOfCellDimensions;

	/*
	 * The number of checkpoint Cell objects that the player has visited so far.
	 */
	private int numCheckpointsReached;

	/*
	 * The number of checkpoint Cell objects in the maze.
	 */
	private final int numCheckpointsToReach;

	/*
	 * Constructor.
	 */
	public Player(Cell startCell, Cell endCell, double baseVel,
			double toleranceConstant, Color color, String name,
			double playerProportionOfCellDimensions, int numCheckpointsToReach) {
		super(startCell.x, startCell.y, startCell.width, startCell.height);
		this.color = color;
		this.name = name;
		this.baseVel = baseVel;
		this.toleranceConstant = toleranceConstant;
		this.playerProportionOfCellDimensions = playerProportionOfCellDimensions;
		this.numCheckpointsToReach = numCheckpointsToReach;
		numCheckpointsReached = 0;
		this.endCell = endCell;

		/*
		 * The player starts at startCell.
		 */
		currentCell = startCell;

		/*
		 * The direction chosen is arbitrary.
		 */
		currentDirection = Direction.RIGHT;

		/*
		 * Because checkCheckpoint is usually called whenever the player moves
		 * into a new Cell object, it means that if the player starts on a Cell
		 * object, the method won't be called for that Cell object. Therefore,
		 * we call it here.
		 */
		checkCheckpoint();
	}

	/*
	 * Attempts to change the current direction of the player to
	 * targetDirection. If it is not possible (i.e. if there is a wall in the
	 * way), it queues targetDirection by storing it in the queuedDirection
	 * variable.
	 */
	protected void changeDirection(Direction targetDirection) {
		/*
		 * The speed multiplier of the cell (determined by the Surface) is used
		 * to get an adjusted velocity for calculating the tolerance.
		 */
		double adjustedVel = baseVel * currentCell.getSpeedMultiplier();

		/*
		 * We need to check whether the Cell object that is in the direction of
		 * targetDirection from currentCell can be moved into (i.e. whether the
		 * Cell object is adjacent to currentCell).
		 */
		Cell neighbouringCell = currentCell
				.getNeighbouringCell(targetDirection);
		boolean adjacent = currentCell.isAdjacentTo(neighbouringCell);

		/*
		 * If the neighbouring Cell object in the direction of targetDirection
		 * from currentCell is not adjacent cannot be moved into (yet), then
		 * targetDirection needs to be queued, which means that it will be
		 * stored in the queuedDirection variable and this method will be called
		 * again with targetDirection as the parameter in the next update cycle.
		 */
		if (!adjacent) {
			queuedDirection = targetDirection;
		} else {
			/*
			 * The tolerance is a value determining the maximum amount of the
			 * player’s body that may not be in the Cell object such that the
			 * player may still be allowed to turn. It is proportional to the
			 * velocity of the player because the faster a player is moving, the
			 * more it moves per game update.
			 */
			double tolerance = (adjustedVel) * toleranceConstant;

			switch (targetDirection) {
			case UP:
			case DOWN:

				/*
				 * If the target direction is vertical, then the x-coordinate of
				 * the player need to be within the tolerable bounds for the
				 * player to be able to change direction.
				 */
				if (x >= currentCell.getX() - tolerance
						* currentCell.getWidth()
						&& x <= currentCell.getX() + tolerance
								* currentCell.getWidth()) {

					/*
					 * This causes the player to "jump" a small distance
					 * horizontally so that it is fully within its current Cell
					 * object and so can change direction without its body
					 * moving through a wall corner
					 */
					x = currentCell.getX();

					currentDirection = targetDirection;
					queuedDirection = null;
				}

				/*
				 * If the x-coordinate of the player are not within the
				 * tolerable bounds, then the player cannot change direction
				 * (yet), so targetDirection is queued.
				 */
				else {
					queuedDirection = targetDirection;
				}
				break;
			case LEFT:
			case RIGHT:

				/*
				 * If the target direction is horizontal, then the y-coordinate
				 * of the player need to be within the tolerable bounds for the
				 * player to be able to change direction
				 */
				if (y >= currentCell.getY() - tolerance
						* currentCell.getHeight()
						&& y <= currentCell.getY() + tolerance
								* currentCell.getHeight()) {

					/*
					 * This causes the player to "jump" a small distance
					 * vertically so that it is fully within its current Cell
					 * object and so can change direction without its body
					 * moving through a wall corner
					 */
					y = currentCell.getY();

					currentDirection = targetDirection;
					queuedDirection = null;
				}

				/*
				 * If the y-coordinate of the player are not within the
				 * tolerable bounds, then the player cannot change direction
				 * (yet), so targetDirection is queued.
				 */
				else {
					queuedDirection = targetDirection;
				}
				break;
			}
		}
	}

	/*
	 * Moves the player in its current direction if it is possible (i.e. if
	 * there is no wall in the way).
	 */
	private void move() {

		/*
		 * The speed multiplier of the Cell object (determined by the Surface)
		 * is used to get an adjusted velocity for calculations.
		 */
		double adjustedVel = baseVel * currentCell.getSpeedMultiplier();

		/*
		 * We need to check whether the Cell object that is in the direction of
		 * currentDirection from currentCell can be moved into (i.e. whether the
		 * Cell object is adjacent to currentCell).
		 */
		Cell neighbouringCell = currentCell
				.getNeighbouringCell(currentDirection);
		boolean adjacent = currentCell.isAdjacentTo(neighbouringCell);

		switch (currentDirection) {

		/*
		 * In each case, we need to check if the neighbouring Cell object in the
		 * current direction is not adjacent to the player’s current Cell object
		 * (i.e. if there is a wall in the way) and if the player’s future
		 * position (were it to move in the current direction at its current
		 * velocity) would cross such a wall. If both these conditions are true,
		 * then we need to set the player’s position just before the wall (to
		 * prevent it from moving through the wall). Otherwise we can just move
		 * the player in its current direction at its current velocity.
		 */

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

			/*
			 * We need to check if the player has moved to NeighbouringCell
			 * (i.e. if the centre of the player is in Neighbouringcell) and
			 * update the player’s current Cell object if it has and also check
			 * if it an unvisited checkpoint.
			 */
			if (neighbouringCell.isInCell(getCentreX(), getCentreY())) {
				currentCell = neighbouringCell;
				checkCheckpoint();
			}
		}
	}

	/*
	 * Checks if the player has visited an unvisited checkpoint Cell object and
	 * increments the numCheckpointsReached variable by 1 if it has.
	 */
	private void checkCheckpoint() {
		if (currentCell.isCheckpoint()) {

			/*
			 * The addEncounteredPlayer method returns true if this Player
			 * object has not already visited currentCell.
			 */
			boolean unvisited = currentCell.addEncounteredPlayer(this);
			if (unvisited) {
				numCheckpointsReached++;
			}
		}
	}

	/*
	 * Returns whether the player has finished the round (i.e. if they have
	 * visited all the checkpoints and have reached the end cell).
	 */
	public boolean finished() {
		return numCheckpointsReached == numCheckpointsToReach
				&& currentCell == endCell;
	}

	/*
	 * Methods from the Entity abstract class that need to be implemented
	 * (polymorphism).
	 */

	/*
	 * Called every game cycle. It moves the player and if there is a queued
	 * direction, then it attempts to change direction.
	 */
	@Override
	public void update() {
		if (queuedDirection != null) {
			changeDirection(queuedDirection);
		}
		move();
	}

	/*
	 * Renders the player as a coloured circle.
	 */
	@Override
	public void render(Graphics g) {
		/*
		 * The colour used in the graphics object before this method is called
		 * needs to be stored so that it can be restored at the end of the
		 * method (see below). This prevents side effects when the graphics
		 * object is used again.
		 */
		Color lastColor = g.getColor();

		/*
		 * Draws the coloured circle representing the player.
		 */
		g.setColor(color);
		g.fillOval((int) (x + width * (1 - playerProportionOfCellDimensions)
				/ 2), (int) (y + height
				* (1 - playerProportionOfCellDimensions) / 2),
				(int) (width * playerProportionOfCellDimensions),
				(int) (height * playerProportionOfCellDimensions));

		/*
		 * Draws the outline of the circle.
		 */
		g.setColor(Color.BLACK);
		g.drawOval((int) (x + width * (1 - playerProportionOfCellDimensions)
				/ 2), (int) (y + height
				* (1 - playerProportionOfCellDimensions) / 2),
				(int) (width * playerProportionOfCellDimensions),
				(int) (height * playerProportionOfCellDimensions));

		g.setColor(lastColor);
	}

	/*
	 * Getters.
	 */

	public Color getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

}
