package core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cell extends Entity {
	/*
	 * This class consists of the attributes and methods associated with the
	 * cells of the maze.
	 */

	/*
	 * This class extends the Entity class (inheritance) because each cell has a
	 * physical position on the screen and dimensions. The update and render
	 * methods need to be implemented (polymorphism).
	 */

	/*
	 * A list of Neighbouring objects consisting of a Cell object which
	 * neighbours this Cell object and the direction of that Cell object to this
	 * Cell object. This is effectively an adjacency list for an unweighted,
	 * undirected graph (the neighbouring cells graph) where the vertices are
	 * all the Cell objects in the Cells matrix in the Maze object, and edges
	 * exist between Cell objects if they neighbour each other (i.e. are next to
	 * each other). The List interface is used rather than a concrete class such
	 * as ArrayList because it separates the actual implementation of the List
	 * interface from this class's use of the interface's methods, allowing the
	 * implementation to change (say, from ArrayList to LinkedList) in the
	 * future. This is composition as the Cell class has a HAS-A relationship
	 * with the Neighbouring class and the Neighbouring objects in the
	 * neighbourings list will be destroyed if the Cell object is destroyed.This
	 * is composition as the Cell class has a HAS-A relationship with the
	 * Neighbouring class and the Neighbouring objects in the neighbourings list
	 * will be destroyed if the Cell object is destroyed.
	 */
	private final List<Neighbouring> neighbourings;

	/*
	 * A list of the Cell objects which neighbour and have no wall shared with
	 * this Cell object. This is effectively an adjacency list for a weighted,
	 * undirected graph (the adjacent cells graph), where the vertices are all
	 * of the Cell objects in the Cells matrix in the Maze object, and edges
	 * exist between Cell objects if they are adjacent to each other (they
	 * neighbour each other and there is no wall dividing the cells). The weight
	 * of an edge is the weighted distance between the centres of the square
	 * bounds of the two Cell objects connected by the edge. The List interface
	 * is used rather than a concrete class such as ArrayList for the same
	 * reasons as above. This is recursive aggregation as the Cell class has a
	 * HAS-A relationship with the Cell class but the Cell objects in the
	 * adjacentCells list will be destroyed if the Cell object is destroyed.
	 */
	private final List<Cell> adjacentCells;

	/*
	 * Whether this Cell object is a checkpoint.
	 */
	private boolean isCheckpoint;

	/*
	 * The Player objects which have visited this Cell object. Used if this Cell
	 * object is a checkpoint. The List interface is used rather than a concrete
	 * class such as ArrayList for the same reasons as above. This is
	 * aggregation as the Cell class has a HAS-A relationship with the Player
	 * class but the Player objects in the encounteredPlayers list will not be
	 * destroyed if the Cell object is destroyed.
	 */
	private List<Player> encounteredPlayers;

	/*
	 * The Surface of this Cell object. Changes the speed of Player objects that
	 * move over this cell according to the speedMultiplier value of the Surface
	 * enum. This is aggregation as the Cell class has a HAS-A relationship with
	 * the Surface enum type but the Surface enum will not be destroyed if the
	 * Cell object is destroyed.
	 */
	private Surface surface;

	/*
	 * The thickness of this Cell object’s wall relative to the dimensions of
	 * the Cell object. A value of 1 means that the wall is as thick as the Cell
	 * object. A value of 0 means that the wall has 0 thickness. Used for
	 * rendering purposes only.
	 */
	private final double wallProportionOfCellDimensions;

	/*
	 * The size of the checkpoint indicator (if this Cell object is a
	 * checkpoint) relative to the dimensions of the Cell object. Used for
	 * rendering purposes only.
	 */
	private final double checkpointProportionOfCellDimensions;

	/*
	 * The colour of the walls. Used for rendering purposes only.
	 */
	private final Color wallColor;

	/*
	 * The colour of the checkpoint indicators. Used for rendering purposes
	 * only.
	 */
	private final Color checkpointColor;

	/*
	 * Constructor.
	 */
	public Cell(double x, double y, double sideLength, Surface surface,
			double wallProportionOfCellDimensions, Color wallColor,
			double checkpointProportionOfCellDimensions, Color checkpointColor) {
		/*
		 * The superclass's constructor must be called first.
		 */
		super(x, y, sideLength, sideLength);

		/*
		 * ArrayList implementations are used because they are efficient with
		 * respect to memory and iteration time complexity.
		 */
		neighbourings = new ArrayList<Neighbouring>();
		adjacentCells = new ArrayList<Cell>();
		isCheckpoint = false;
		this.surface = surface;
		this.wallProportionOfCellDimensions = wallProportionOfCellDimensions;
		this.checkpointProportionOfCellDimensions = checkpointProportionOfCellDimensions;
		this.wallColor = wallColor;
		this.checkpointColor = checkpointColor;
	}

	/*
	 * Makes this Cell object a checkpoint. Sets isCheckpoint to true and
	 * initialises the encounteredPlayers list.
	 */
	public void setCheckpoint() {
		/*
		 * If isCheckpoint is already true, then we do not want to create a new
		 * encounteredPlayers list (in case it contains anything in it already,
		 * even though it shouldn't).
		 */
		if (!isCheckpoint) {
			isCheckpoint = true;

			/*
			 * An ArrayList implementation is used because it is efficient with
			 * respect to memory and iteration time complexity.
			 */
			encounteredPlayers = new ArrayList<Player>();
		}
	}

	/*
	 * Adds the encounteredPlayer parameter to the encounteredPlayers list if it
	 * is not already in it. If it is, returns false, otherwise returns true.
	 */
	public boolean addEncounteredPlayer(Player encounteredplayer) {
		if (!encounteredPlayers.contains(encounteredplayer)) {
			encounteredPlayers.add(encounteredplayer);
			return true;
		}
		return false;
	}

	/*
	 * Adds a Neighbouring object consisting of the parameters to the
	 * neighbourings list. Also adds a Neighbouring object consisting of this
	 * Cell object and the opposite direction to
	 * directionFromThisCellToNeighbouringCell to the neighbourings list of
	 * neighbouringCell (as this is effectively an undirected graph, so all
	 * neighbourings must be two-way).
	 */
	public void addNeighbouringCell(Cell neighbouringCell,
			Direction directionFromThisCellToNeighbouringCell) {
		neighbourings.add(new Neighbouring(neighbouringCell,
				directionFromThisCellToNeighbouringCell));
		neighbouringCell.neighbourings.add(new Neighbouring(this,
				directionFromThisCellToNeighbouringCell.getOpposite()));
	}

	/*
	 * Returns the direction from this Cell object to neighbouringCell if it is
	 * a neighbour. Otherwise returns null.
	 */
	public Direction getDirectionToNeighbouringCell(Cell neighbouringCell) {
		for (Neighbouring neighbouring : neighbourings) {
			if (neighbouring.getNeighbouringCell() == neighbouringCell) {
				return neighbouring.getDirectionToNeighbouringCell();
			}
		}
		return null;
	}

	/*
	 * Returns the neighbouring Cell object that is in the direction of the
	 * parameter.
	 */
	public Cell getNeighbouringCell(Direction direction) {
		/*
		 * Iterates over the neighbourings list to find the Neighbouring object
		 * with the same direction as the one in the parameter and then returns
		 * the Cell object in that Neighbouring object.
		 */
		for (Neighbouring neighbouring : neighbourings) {
			if (neighbouring.getDirectionToNeighbouringCell() == direction) {
				/*
				 * Exits the loop early and returns the Cell object once it has
				 * found the neighbouring Cell object.
				 */
				return neighbouring.getNeighbouringCell();
			}
		}

		/*
		 * Returns null if there is no neighbouring Cell object in that
		 * direction.
		 */
		return null;
	}

	/*
	 * Adds adjacentCell to the adjacentCells list and adds this Cell object to
	 * the adjacentCells list of adjacentCell (as this is effectively an
	 * undirected graph, so all adjacencies must be two-way).
	 */
	public void setAdjacentTo(Cell adjacentCell) {
		adjacentCells.add(adjacentCell);
		adjacentCell.adjacentCells.add(this);
	}

	/*
	 * Returns how many Cell objects are adjacent to this one (i.e. the size of
	 * the adjacentCells list).
	 */
	public int getOrder() {
		return adjacentCells.size();
	}

	/*
	 * Returns a random neighbouring Cell object that is not adjacent to this
	 * Cell object (i.e. a random Cell object that is not in the adjacentCells
	 * list but is in a Neighbouring object in the neighbourings list)
	 */
	public Cell getRandomNeighbouringNonAdjacentCell() {

		/*
		 * A list is created to store all the neighbouring Cell objects not
		 * adjacent to this Cell object.
		 */
		List<Cell> neighbouringNonAdjacentCells = new ArrayList<Cell>();

		/*
		 * Iterates over the neighbourings list to find Neighbouring objects
		 * which are not adjacent to this Cell object and adds the Cell objects
		 * in those Neighbouring objects to the neighbouringNonAdjacentCells
		 * list.
		 */
		for (Neighbouring neighbouring : neighbourings) {
			Cell cell = neighbouring.getNeighbouringCell();
			if (!isAdjacentTo(cell)) {
				neighbouringNonAdjacentCells.add(cell);
			}
		}
		/*
		 * The Cell object in a random index of the neighbouringNonAdjacentCells
		 * list is then returned.
		 */
		int randIndex = Application.randomNumberGenerator
				.nextInt(neighbouringNonAdjacentCells.size());
		return neighbouringNonAdjacentCells.get(randIndex);
	}

	/*
	 * Returns a random Cell object from the adjacentCells list.
	 */
	public Cell getRandomAdjacentCell() {
		int randIndex = Application.randomNumberGenerator.nextInt(adjacentCells
				.size());
		return adjacentCells.get(randIndex);
	}

	/*
	 * Returns the speed multiplier of the Surface object of the Cell object.
	 */
	public double getSpeedMultiplier() {
		return surface.getSpeedMultiplier();
	}

	/*
	 * Returns whether the otherCell object is in the adjacentCells list.
	 */
	public boolean isAdjacentTo(Cell otherCell) {
		return adjacentCells.contains(otherCell);
	}

	/*
	 * Returns whether the pixel coordinates in the parameters are within the
	 * bounds of the square bounds of this Cell object.
	 */
	public boolean isInCell(double pointX, double pointY) {
		return pointX >= x && pointX <= x + width && pointY >= y
				&& pointY <= y + height;
	}

	/*
	 * Returns the weighted distance between the centre of the square bounds of
	 * this Cell object and the centre of the square bounds of adjacentCell.
	 */
	public double getWeightedDistanceToAdjacentCell(Cell adjacentCell) {

		/*
		 * If adjacentCell is not adjacent to this Cell object (which should not
		 * happen), then it means that there are logical errors elsewhere in the
		 * code, so a RuntimeException is thrown to quit the program and make
		 * debugging and tracing the error easier. A try-catch block would not
		 * be useful here as the issue cannot be fixed without changing the
		 * code.
		 */
		if (!isAdjacentTo(adjacentCell)) {
			throw new RuntimeException();
		}

		return 0.5
				* height
				* (1 / getSpeedMultiplier() + 1 / adjacentCell
						.getSpeedMultiplier());
	}

	/*
	 * Methods from the Entity abstract class that need to be implemented
	 * (polymorphism).
	 */

	/*
	 * Does nothing as nothing in this class needs to be updated every cycle.
	 */
	@Override
	public void update() {
	}

	/*
	 * Renders the base cell, the surface, and the checkpoint indicator if the
	 * cell is a checkpoint.
	 */
	@Override
	public void render(Graphics graphics) {
		/*
		 * The colour used in the graphics object before this method is called
		 * needs to be stored so that it can be restored at the end of the
		 * method (see below). This prevents side effects when the graphics
		 * object is used again.
		 */
		Color lastColor = graphics.getColor();

		/*
		 * Draws the square base cell with the colour of the surface object.
		 */
		graphics.setColor(surface.getColor());
		graphics.fillRect((int) x, (int) y, (int) width, (int) height);

		/*
		 * Draws the checkpoint indicator if the Cell object is a checkpoint
		 */
		if (isCheckpoint) {
			graphics.setColor(checkpointColor);
			/*
			 * Draws the base checkpoint indicator circle with the checkpoint
			 * color.
			 */
			graphics.fillOval((int) (x + width
					* (1 - checkpointProportionOfCellDimensions) / 2),
					(int) (y + height
							* (1 - checkpointProportionOfCellDimensions) / 2),
					(int) (width * checkpointProportionOfCellDimensions),
					(int) (height * checkpointProportionOfCellDimensions));

			/*
			 * For each Player object that is in the encounteredPlayers list, an
			 * equally-sized sector of the checkpoint indicator circle is filled
			 * with the same colour as the respective Player object.
			 */
			int numPlayersEncountered = encounteredPlayers.size();
			double startAngle = 0;
			double arcAngle = 360 / (numPlayersEncountered + 1);
			for (Player player : encounteredPlayers) {
				graphics.setColor(player.getColor());
				graphics.fillArc((int) (x + width
						* (1 - checkpointProportionOfCellDimensions) / 2),
						(int) (y + height
								* (1 - checkpointProportionOfCellDimensions)
								/ 2),
						(int) (width * checkpointProportionOfCellDimensions),
						(int) (height * checkpointProportionOfCellDimensions),
						(int) startAngle, (int) arcAngle);
				startAngle += arcAngle;
			}

			/*
			 * Draws the black outline of the checkpoint indicator.
			 */
			graphics.setColor(Color.BLACK);
			graphics.drawOval((int) (x + width
					* (1 - checkpointProportionOfCellDimensions) / 2),
					(int) (y + height
							* (1 - checkpointProportionOfCellDimensions) / 2),
					(int) (width * checkpointProportionOfCellDimensions),
					(int) (height * checkpointProportionOfCellDimensions));
		}

		graphics.setColor(lastColor);
	}

	public void renderWalls(Graphics graphics) {

		/*
		 * A Graphics2D cast is done because the regular Graphics class does not
		 * have the setStroke method used below.
		 */
		Graphics2D graphics2D = (Graphics2D) graphics;

		/*
		 * The colour and stroke used in the graphics and graphics2D objects
		 * before this method is called needs to be stored so that it can be
		 * restored at the end of the method (see below). This prevents side
		 * effects when the graphics object is used again.
		 */
		Color lastColor = graphics.getColor();
		Stroke oldStroke = graphics2D.getStroke();

		graphics.setColor(wallColor);

		/*
		 * The strokeWeight is how thick the wall (which is drawn as a line)
		 * will be.
		 */
		double strokeWeight = wallProportionOfCellDimensions * height;

		/*
		 * Using BasicStroke.CAP_ROUND means that the walls will appear rounded,
		 * which is a more pleasant look.
		 */
		Stroke stroke = new BasicStroke((int) strokeWeight,
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		graphics2D.setStroke(stroke);
		graphics.setColor(wallColor);

		/*
		 * Walls can only be drawn between this Cell object and neighbouring
		 * Cell objects, so the neighbourings list is iterated over.
		 */
		for (Neighbouring neighbouring : neighbourings) {
			Cell cell = neighbouring.getNeighbouringCell();

			/*
			 * If this Cell object is not adjacent to cell, then this means that
			 * there is a wall between the two Cell objects and so a wall needs
			 * to be drawn.
			 */
			if (!isAdjacentTo(cell)) {
				Direction direction = getDirectionToNeighbouringCell(cell);
				/*
				 * The starting y-coordinate of the wall (drawn as a line) will
				 * be the same as the y-coordinate of this Cell object unless
				 * the wall is being drawn between this Cell object and a
				 * neighbouring Cell object which is below this Cell object, in
				 * which case the starting x-coordinate of the wall will be the
				 * same as the y-coordinate of the bottom side of this Cell
				 * object, which is equal to y + height.
				 */
				double wallY = (direction != Direction.DOWN) ? y : y + height;

				/*
				 * The starting x-coordinate of the wall (drawn as a line) will
				 * be the same as the x-coordinate of this Cell object unless
				 * the wall is being drawn between this Cell object and a
				 * neighbouring Cell object which is to the right of this Cell
				 * object, in which case the starting x-coordinate of the wall
				 * will be the same as the x-coordinate of the right side of
				 * this Cell object, which is equal to x + width.
				 */
				double wallX = (direction != Direction.RIGHT) ? x : x + width;

				/*
				 * Draws a horizontal line along the edge of the top or bottom
				 * edges of the Cell object if the neighbouring Cell object is
				 * above or below this Cell object.
				 */
				if (direction == Direction.UP || direction == Direction.DOWN) {
					graphics.drawLine((int) wallX, (int) wallY,
							(int) (wallX + width), (int) wallY);
				}
				/*
				 * Draws a vertical line along the edge of the left or right
				 * edges of the Cell object if the neighbouring Cell object is
				 * to the left or to the right this Cell object.
				 */
				else {
					graphics.drawLine((int) wallX, (int) wallY, (int) (wallX),
							(int) (wallY + height));
				}

			}
		}
		graphics2D.setStroke(oldStroke);
		graphics.setColor(lastColor);
	}

	/*
	 * Getters.
	 */

	public boolean isCheckpoint() {
		return isCheckpoint;
	}

	public List<Cell> getAdjacentCells() {
		/*
		 * An unmodifiable version is returned so that the list cannot be
		 * accidentally altered, reducing the risk of side-effects.
		 */
		return Collections.unmodifiableList(adjacentCells);
	}

	public List<Neighbouring> getNeighbourings() {
		/*
		 * An unmodifiable version is returned so that the list cannot be
		 * accidentally altered, reducing the risk of side-effects.
		 */
		return Collections.unmodifiableList(neighbourings);
	}

	/*
	 * Tests.
	 */

	public String testID = Integer.toString((int) x)
			+ Integer.toString((int) y);

	private void renderTestID(Graphics graphics) {
		graphics.drawString(testID, (int) (x + width / 2),
				(int) (y + width / 2));
	}
}
