package core;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

public class Maze extends Entity {
	/*
	 * This class consists of the maze Cell objects and the maze generation
	 * algorithms.
	 */

	/*
	 * This class extends the Entity class (inheritance) because each cell has a
	 * physical position on the screen and dimensions. The update and render
	 * methods need to be implemented (polymorphism).
	 */

	/*
	 * A matrix storing all the Cell objects in the maze, representing the
	 * arrangement of cells in the rectangular grid maze. This is aggregation as
	 * the Maze class has a HAS-A relationship with the Cell class but the Cell
	 * objects in the cells matrix will not be destroyed if the Maze object is
	 * destroyed.
	 */
	private final Cell[][] cells;

	/*
	 * The base colour of the maze. This is the effectively the default colour
	 * of the Cell objects before the colour of any surface is applied. Used for
	 * rendering purposes only.
	 */
	private final Color groundColor;

	/*
	 * The number of checkpoint cells in the maze.
	 */
	private final int numCheckpoints;

	/*
	 * Constructor. Generates the maze, including the setting of checkpoints,
	 * using the other methods. mazeType is the style of maze that is to be
	 * generated and determines the algorithm used to generate the maze (i.e.
	 * randomised Kruskal’s or depth-first search).
	 */
	public Maze(int numCellsWide, int numCellsHigh, double x, double y,
			double cellSideLength, double deadEndProbability,
			double wallProportionOfCellDimensions,
			double checkpointProportionOfCellDimensions,
			int numCheckpointsExcludingEndpoints,
			EnumMap<Surface, Double> surfaceRatios, Color checkpointColor,
			Color wallColor, Color groundColor, MazeType mazeType) {
		/*
		 * The superclass's constructor must be called first. The width of the
		 * maze is equal to the side length per cell multiplied by the number of
		 * cells in one row. The height of the maze is equal to the side length
		 * per cell multiplied by the number of cells in one column.
		 */
		super(x, y, numCellsWide * cellSideLength, numCellsHigh
				* cellSideLength);

		this.groundColor = groundColor;
		this.numCheckpoints = numCheckpointsExcludingEndpoints + 2;
		/*
		 * The Cell objects must be created first before the maze generation
		 * algorithms are applied, otherwise runtime errors will arise as there
		 * would be no Cell objects in the cells matrix for the algorithms to
		 * operate on.
		 */
		cells = initCells(numCellsWide, numCellsHigh, cellSideLength,
				wallProportionOfCellDimensions, wallColor,
				checkpointProportionOfCellDimensions, checkpointColor,
				surfaceRatios);

		switch (mazeType) {
		/*
		 * If mazeType is KRUSKAL, then the perfect maze should be generated
		 * using the randomised Kruskal's algorithm (which is done in the
		 * initKruskalPerfectMaze method)
		 */
		case KRUSKAL:
			initKruskalPerfectMaze();
			break;

		/*
		 * If mazeType is DFS, then the perfect maze should be generated using
		 * the depth-first search algorithm (which is done in the
		 * initDepthFirstSearchPerfectMaze method)
		 */
		case DFS:
			initDepthFirstSearchPerfectMaze();
			break;
		}

		/*
		 * The dead ends should be removed after the perfect maze has been
		 * generated, otherwise glitches will occur.
		 */
		removeDeadEnds(deadEndProbability);

		/*
		 * Randomly sets a given number of Cell objects as checkpoints.
		 */
		placeCheckpoints(numCheckpointsExcludingEndpoints);
	}

	/*
	 * Selects a random Surface enum. This Surface will be present in the
	 * surfaceRatios map. The Surface selected will randomly selected based on
	 * the distribution declared in the surfaceRatios map. The Double value
	 * associated with a particular Surface key in the dictionary divided by the
	 * sum of all the Double values in the dictionary gives the probability of
	 * that particular Surface key being selected.
	 */
	private Surface getRandomSurface(EnumMap<Surface, Double> surfaceRatios) {
		/*
		 * Calculates the total of the Double values in the surfaceRatios map by
		 * iterating over the entries in the map.
		 */
		double totalRatio = 0;
		for (Entry<Surface, Double> entry : surfaceRatios.entrySet()) {
			double ratio = entry.getValue();
			totalRatio += ratio;
		}

		/*
		 * Selects a random double between 0 and totalRatio
		 */
		double rand = Application.randomNumberGenerator.nextDouble()
				* totalRatio;

		/*
		 * Iterates over the map, adding the Double values to cumulativeRatios
		 * until cumulativeRatios exceeds rand, at which point the random
		 * Surface has been selected. This means that probability of a certain
		 * Surface in the key set being selected is equal to the Double value
		 * associated with that Surface key divided by totalRatio.
		 */
		double cumulativeRatios = 0;
		for (Entry<Surface, Double> entry : surfaceRatios.entrySet()) {
			cumulativeRatios += entry.getValue();
			if (cumulativeRatios >= rand) {
				return entry.getKey();
			}
		}

		/*
		 * The program should never reach here unless there are no entries in
		 * the dictionary, which should not happen. Thus, there is a logical
		 * error elsewhere in the program, so a RuntimeException is thrown to
		 * quit the program and make debugging and tracing the error easier. A
		 * try-catch block would not be useful here as the issue cannot be fixed
		 * without changing the code.
		 */
		throw new RuntimeException();
	}

	/*
	 * Creates a Cells matrix with NumCellsWide columns and NumCellsHigh rows.
	 * Populates it with Cell objects with surfaces chosen randomly according to
	 * a distribution declared in the SurfaceRatios map. Cell objects
	 * neighbouring (i.e. next to) other Cell objects in the Cells matrix (which
	 * represents the arrangement of cells in the maze) are set as neighbours of
	 * the Cell objects. Every Cell object initially has no adjacent Cell
	 * objects (i.e. there are walls between every pair of neighbouring Cell
	 * objects).
	 */
	private Cell[][] initCells(int numCellsWide, int numCellsHigh,
			double cellSideLength, double wallProportionOfCellDimensions,
			Color wallColor, double checkpointProportionOfCellDimensions,
			Color checkpointColor, EnumMap<Surface, Double> surfaceRatios) {
		/*
		 * Creates a cells matrix with numCellsWide rows and numCellsHigh
		 * columns
		 */
		Cell[][] cells = new Cell[numCellsWide][numCellsHigh];

		for (int column = 0; column < cells.length; column++) {
			for (int row = 0; row < cells[column].length; row++) {

				/*
				 * These are the pixel coordinates representing the new Cell
				 * object's position on the screen. The matrix position of the
				 * Cell object should be the same as the position of the Cell
				 * object on the screen relative to the other Cell objects.
				 */
				double cellX = x + column * cellSideLength;
				double cellY = y + row * cellSideLength;

				/*
				 * A random surface is selected for the Cell object based on the
				 * probability distribution given in the surfaceRatios map using
				 * the getRandomSurface method.
				 */
				Surface surface = getRandomSurface(surfaceRatios);
				Cell currentCell = new Cell(cellX, cellY, cellSideLength,
						surface, wallProportionOfCellDimensions, wallColor,
						checkpointProportionOfCellDimensions, checkpointColor);
				cells[column][row] = currentCell;

				/*
				 * If the Cell object is not in the first row, then we can set
				 * it as a neighbour of the Cell object in the same column but
				 * in the row above.
				 */
				if (row > 0) {
					Cell neighbouringCell = cells[column][row - 1];
					currentCell.addNeighbouringCell(neighbouringCell,
							Direction.UP);
				}

				/*
				 * If the Cell object is not in the first column, then we can
				 * set it as a neighbour of the Cell object in the same row but
				 * in the column to the left.
				 */
				if (column > 0) {
					Cell neighbouringCell = cells[column - 1][row];
					currentCell.addNeighbouringCell(neighbouringCell,
							Direction.LEFT);
				}

				/*
				 * We do not need to check if there is a Cell object in the same
				 * row and in the column to the right or if there is a Cell
				 * object in the same column and in the row below because these
				 * Cell objects will be made neighbours when the for loop gets
				 * to them.
				 */
			}
		}
		return cells;
	}

	/*
	 * Generates a perfect maze using the depth-first search algorithm. This
	 * changes which Cell objects in the cells matrix are adjacent to each other
	 * (i.e. which neighbouring cells have walls between them) so that there is
	 * a unique path of adjacent Cell objects between any two Cell objects in
	 * the Cells matrix.
	 */
	private void initDepthFirstSearchPerfectMaze() {
		/*
		 * This stack keeps track of which Cell objects need to be backtracked
		 * to in the depth first traversal.
		 */
		Stack<Cell> cellStack = new Stack<Cell>();

		/*
		 * This set keeps track of Cell objects that have been visited, so that
		 * walls of Cell objects that have already been visited are not carved
		 * (to prevent cycles from being made in the maze).
		 */
		Set<Cell> visitedCells = new HashSet<Cell>();
		Cell currentCell = cells[0][0];
		visitedCells.add(currentCell);
		do {

			/*
			 * The algorithm needs to carve a wall of an unvisited neighbouring
			 * Cell object to prevent cycles from being made in the maze, so this
			 * section of code tries to select an unvisited neighbouring Cell
			 * object if there is one.
			 */

			List<Cell> unvisitedNeighbouringCells = new ArrayList<Cell>();

			/*
			 * This loop populates a list of neighbouring Cell objects that have
			 * not been visited yet.
			 */
			for (Neighbouring neighbouring : currentCell.getNeighbourings()) {
				Cell neighbouringCell = neighbouring.getNeighbouringCell();
				if (!visitedCells.contains(neighbouringCell)) {
					unvisitedNeighbouringCells.add(neighbouringCell);
				}
			}

			if (!unvisitedNeighbouringCells.isEmpty()) {

				/*
				 * The algorithm needs to pick a random unvisited neighbouring
				 * Cell object so that the maze is random, otherwise it will
				 * tend to pick Cell objects in a limited number of directions,
				 * creating a maze with a biased structure.
				 */
				Cell randomUnvisitedNeighbouringCell = unvisitedNeighbouringCells
						.get(Application.randomNumberGenerator
								.nextInt(unvisitedNeighbouringCells.size()));

				/*
				 * The current Cell object is pushed onto the stack so that the
				 * algorithm can backtrack to it when it reaches a dead end.
				 */
				cellStack.push(currentCell);

				/*
				 * Setting the Cell objects adjacent to each other effectively
				 * “carves” through the wall between them.
				 */
				currentCell.setAdjacentTo(randomUnvisitedNeighbouringCell);
				currentCell = randomUnvisitedNeighbouringCell;

				/*
				 * The next iteration of the loop will visit
				 * randomUnvisitedNeighbouringCell, so it is put in the
				 * visitedCells list.
				 */
				visitedCells.add(randomUnvisitedNeighbouringCell);
				/*
				 * The loop repeats with the neighbouring Cell object as the
				 * next currentCell, the algorithm carries on like this until it
				 * reaches a Cell object with no unvisited neighbouring Cell
				 * objects, in which case it has reached a dead end and program
				 * flow will go to the else statement below.
				 */
			} else if (!cellStack.isEmpty()) {

				/*
				 * If the algorithm reaches here, then there are no unvisited
				 * neighbouring Cell objects, so a dead end is reached; the
				 * algorithm backtracks up the path it traversed through until
				 * it reaches a Cell object that has unvisited Cell objects and
				 * then continues the traversal as normal, carving through more
				 * walls. Backtracking is achieved here by popping the last Cell
				 * object off the stack and going back to that Cell object.
				 */
				currentCell = cellStack.pop();
			}

			/*
			 * The loop repeats until the algorithm backtracks all the way back
			 * to the initial cell, at which point the stack would be empty and
			 * every cell would have been visited.
			 */
		} while (!cellStack.isEmpty());
	}

	/*
	 * Generates a perfect maze using the randomised Kruskal’s algorithm. This
	 * changes which Cell objects in the cells matrix are adjacent to each other
	 * (i.e. which neighbouring cells have walls between them) so that there is
	 * a unique path of adjacent Cell objects between any two Cell objects in
	 * the Cells matrix.
	 */
	private void initKruskalPerfectMaze() {

		/*
		 * This disjoint-set is needed to efficiently check if a path exists
		 * along adjacent Cell objects between Cell objects, when deciding
		 * whether to remove walls.
		 */
		DisjointSet<Cell> cellDisjointSet = new DisjointSet<Cell>();

		class Wall {
			/*
			 * This class consists of the two neighbouring Cell objects not
			 * adjacent to each other (i.e. divided by a wall).
			 */

			/*
			 * The two Cell objects divided by the wall. This is aggregation as
			 * the Wall class has a HAS-A relationship with the Cell class but
			 * the cell1 and cell2 objects will not be destroyed if the Wall
			 * object is destroyed.
			 */
			private final Cell cell1;
			private final Cell cell2;

			/*
			 * Constructor.
			 */
			public Wall(Cell c1, Cell c2) {
				cell1 = c1;
				cell2 = c2;
			}

			/*
			 * Generates the hash code for the Wall object. Always gives the
			 * same hash code given a Wall object with the same two Cell
			 * objects.
			 */
			@Override
			public int hashCode() {

				/*
				 * Multiplication is a commutative operation, the order of the
				 * Cell objects does not matter, so given the same two Cell
				 * objects, the hash code will always be the same, regardless of
				 * the order.
				 */
				return cell1.hashCode() * cell2.hashCode();
			}

			/*
			 * Checks if two Wall objects are equal (i.e. if they both have the
			 * same two Cell objects)
			 */
			@Override
			public boolean equals(Object o) {
				if (o instanceof Wall) {
					Wall other = (Wall) o;
					return (other.cell1 == cell2 && other.cell2 == cell1)
							|| (other.cell1 == cell1 && other.cell2 == cell2);
				}
				return false;
			}

			/*
			 * Getters.
			 */

			public Cell getCell1() {
				return cell1;
			}

			public Cell getCell2() {
				return cell2;
			}
		}

		Set<Wall> wallsSet = new HashSet<Wall>();

		/*
		 * This loop populates cellDisjointSet with the Cell objects in the
		 * cells matrix and populates wallsSet with all the Wall objects
		 * representing all the conceptual walls in the maze.
		 */
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell cell = cells[i][j];
				cellDisjointSet.add(cell);

				/*
				 * We search the neighbouring Cell objects of cell to look for
				 * neighbouring Cell objects that are not adjacent to cell (as
				 * walls must exist between these pairs of Cell objects)
				 */
				for (Neighbouring neighbouring : cell.getNeighbourings()) {
					Cell neighbouringCell = neighbouring.getNeighbouringCell();
					if (!cell.isAdjacentTo(neighbouringCell)) {
						/*
						 * Then there is a wall between the two Cell objects, so
						 * we create and add a Wall object for these two Objects
						 * to WallsSet.
						 */
						wallsSet.add(new Wall(cell, neighbouringCell));
					}
				}
			}
		}

		/*
		 * A list is created and shuffled so that the Wall objects can be
		 * iterated through in a random order (sets cannot be shuffled).
		 */
		List<Wall> wallsList = new ArrayList<Wall>(wallsSet);

		/*
		 * When iterating through the Wall objects, the Wall objects should be
		 * shuffled into a random order – otherwise, the walls would be deleted
		 * in a biased order, leading to the maze looking too structured.
		 */
		Collections.shuffle(wallsList, Application.randomNumberGenerator);

		for (Wall wall : wallsList) {
			Cell cell1 = wall.getCell1();
			Cell cell2 = wall.getCell2();

			/*
			 * This If statement checks if there exists a path (consisting of
			 * adjacent Cell objects) between the two Cell objects divided by
			 * the wall.
			 */
			if (!cellDisjointSet.areJoined(cell1, cell2)) {

				/*
				 * Effectively deletes the wall between the two Cell objects,
				 * setting the two Cell objects adjacent to each other.
				 */
				cell1.setAdjacentTo(cell2);

				/*
				 * There now exists a path between the two Cell objects, so the
				 * two Cell objects should be in the same subset in
				 * cellsDisjointSet.
				 */
				cellDisjointSet.join(cell1, cell2);
			}
		}
	}

	/*
	 * Randomly goes through each Cell object in the initialised cells matrix
	 * and, if it is a dead end, makes it not a dead end (with probability
	 * deadEndProbability) by making it adjacent to (i.e. removing the wall
	 * between it and) a random neighbouring cell.
	 */
	private void removeDeadEnds(double deadEndProbability) {

		/*
		 * A list is created so that the Cell objects can be iterated through in
		 * a random order and it is easier to randomly shuffle a list than it is
		 * to randomly iterate through the Cells matrix.
		 */
		List<Cell> cellsList = new ArrayList<Cell>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cellsList.add(cells[i][j]);
			}
		}

		/*
		 * When iterating through the Cell objects, the Cell objects should be
		 * shuffled into a random order – otherwise, the maze may have a biased
		 * structure with more dead ends in some areas than others.
		 */
		Collections.shuffle(cellsList, Application.randomNumberGenerator);

		for (Cell cell : cellsList) {

			/*
			 * A dead end is when a Cell object has at least three walls (i.e.
			 * it has at most one adjacent Cell object, so the order of the Cell
			 * object is at most one in graph theoretic terms) so this If
			 * statement checks if a Cell object is a dead end.
			 */
			if (cell.getOrder() <= 1) {

				/*
				 * The probability of randomly generating a real number between
				 * 0 and 1 under x (where x is between 0 and 1) is x, and the
				 * probability that a dead end should be removed should be 1 –
				 * deadEndProbability, which is why this works.
				 */
				if (Application.randomNumberGenerator.nextFloat() > deadEndProbability) {

					/*
					 * This removes the dead end by setting cell to a random
					 * neighbouring Cell object that cell is not adjacent to.
					 */
					cell.setAdjacentTo(cell
							.getRandomNeighbouringNonAdjacentCell());
				}
			}
		}
	}

	/*
	 * Makes a number (equal to numCheckpointsExlcudingEndpoints) of random Cell
	 * objects in the initialised cells matrix checkpoints. Also makes the
	 * endpoint cells checkpoints.
	 */
	private void placeCheckpoints(int numCheckpointsExcludingEndpoints) {
		getStartCell().setCheckpoint();
		getEndCell().setCheckpoint();
		for (int i = 0; i < numCheckpointsExcludingEndpoints; i++) {
			int x = Application.randomNumberGenerator.nextInt(cells.length);
			int y = Application.randomNumberGenerator.nextInt(cells[x].length);
			Cell checkpointCell = cells[x][y];
			if (checkpointCell.isCheckpoint()) {
				i--;
			} else {
				checkpointCell.setCheckpoint();
			}
		}
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
	 * Renders the maze by rendering each Cell object in the Cells matrix and
	 * then rendering the walls for each Cell object.
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
		 * Draws the background of the maze.
		 */
		g.setColor(groundColor);
		g.fillRect((int) x, (int) y, (int) width, (int) height);

		/*
		 * Draws all the base cells first.
		 */
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = cells[i][j];
				currentCell.render(g);
			}
		}

		/*
		 * Draws the walls after (so that the walls appear above the surfaces of
		 * the cells.
		 */
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = cells[i][j];
				currentCell.renderWalls(g);
			}
		}
		g.setColor(lastColor);
	}

	/*
	 * Returns the top left cell (i.e. on of the endpoint cells)
	 */
	public Cell getStartCell() {
		return cells[0][0];
	}

	/*
	 * Returns the bottom right cell (i.e. on of the endpoint cells)
	 */
	public Cell getEndCell() {
		return cells[cells.length - 1][cells[0].length - 1];
	}

	/*
	 * Getters.
	 */

	public double getCellSideLength() {
		return cells[0][0].getHeight();
	}

	public int getNumCheckpoints() {
		return numCheckpoints;
	}

	/*
	 * Tests
	 */

	public boolean hasNoIsolatedSections() {
		/*
		 * Basic depth-first traversal of the Cell objects in the cells matrix
		 * (i.e. all the Cell objects in the maze) implemented using a stack. It
		 * keeps track of how many unique Cell objects were visited using the
		 * visitedCells hash set. A set is used because it ensures no
		 * duplicates.
		 */
		Set<Cell> visitedCells = new HashSet<Cell>();
		Stack<Cell> cellsStack = new Stack<Cell>();
		Cell currentCell = getStartCell();
		cellsStack.push(currentCell);
		while (!cellsStack.isEmpty()) {
			currentCell = cellsStack.pop();
			if (!visitedCells.contains(currentCell)) {
				visitedCells.add(currentCell);
				for (Cell adjacentCell : currentCell.getAdjacentCells()) {
					if (adjacentCell != currentCell) {
						cellsStack.push(adjacentCell);
					}
				}
			}
		}

		/*
		 * Number of Cell objects in the maze.
		 */
		int numCellsInMaze = cells.length * cells[0].length;

		/*
		 * Number of Cell objects that were visited in the depth-first
		 * traversal.
		 */
		int numCellsVisitedInTraversal = visitedCells.size();

		/*
		 * If the above two variables are equal, then the depth-first traversal
		 * visited all the Cell objects in the maze and so there are no isolated
		 * sections in the maze. Otherwise, there are isolated sections.
		 */
		return numCellsInMaze == numCellsVisitedInTraversal;
	}

	public boolean hasCycles() {
		return hasCycles(new HashSet<Cell>(), getStartCell(), null);
	}

	public boolean hasCycles(Set<Cell> visitedCells, Cell currentCell,
			Cell previousCell) {
		/*
		 * Basic depth-first traversal of the Cell objects in the cells matrix
		 * (i.e. all the Cell objects in the maze) implemented using recursion.
		 * It keeps track of the Cell objects that are visited using the
		 * visitedCells hash set. A set is used because it ensures no
		 * duplicates.
		 */
		visitedCells.add(currentCell);
		for (Cell adjacentCell : currentCell.getAdjacentCells()) {
			if (adjacentCell != previousCell) {

				/*
				 * If adjacentCell has already been visited, then there must be
				 * a cycle.
				 */
				if (visitedCells.contains(adjacentCell)) {
					return true;
				}

				/*
				 * Otherwise, if the recursive call returns true, then there
				 * must be a cycle.
				 */
				if (hasCycles(visitedCells, adjacentCell, currentCell)) {
					return true;
				}
			}
		}

		/*
		 * Otherwise, there are no cycles.
		 */
		return false;
	}

	private void printSurfaceAndDeadEndRatios() {
		/*
		 * These variables are used for counting the number of Cell objects with
		 * each surface (Slow, Normal, Fast).
		 */
		int slows = 0, normals = 0, fasts = 0;

		/*
		 * This variable is to count the number of dead ends in the maze.
		 */
		int deadEnds = 0;

		/*
		 * Iterate over all the Cell objects in the maze.
		 */
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {

				/*
				 * Find which surface each Cell object has and increment the
				 * relevant counting variable.
				 */
				if (cells[i][j].getSpeedMultiplier() == Surface.SLOW
						.getSpeedMultiplier()) {
					slows++;
				} else if (cells[i][j].getSpeedMultiplier() == Surface.NORMAL
						.getSpeedMultiplier()) {
					normals++;
				} else if (cells[i][j].getSpeedMultiplier() == Surface.FAST
						.getSpeedMultiplier()) {
					fasts++;
				}

				/*
				 * If a cell has order 1, it is a dead end.
				 */
				if (cells[i][j].getOrder() == 1) {
					deadEnds++;
				}
			}
		}

		/*
		 * These variables store the percentage of Cell objects in the maze that
		 * have each surface.
		 */
		double slowsRatio = 100 * slows / (cells.length * cells[0].length);
		double normalsRatio = 100 * normals / (cells.length * cells[0].length);
		double fastsRatio = 100 * fasts / (cells.length * cells[0].length);

		/*
		 * This variable stores the percentage of Cell objects in the maze that
		 * are dead ends.
		 */
		double deadEndsRatio = 100 * deadEnds
				/ (cells.length * cells[0].length);

		/*
		 * Output the results.
		 */
		System.out.println("Slow surfaces: " + slowsRatio + "%");
		System.out.println("Normal surfaces: " + normalsRatio + "%");
		System.out.println("Fast surfaces: " + fastsRatio + "%");
		System.out.println("Dead ends: " + deadEndsRatio + "%");
	}

}
