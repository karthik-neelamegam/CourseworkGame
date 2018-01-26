package map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import dsa.DisjointSet;
import logic.Direction;
import logic.Entity;
import user_interface.Application;

public class Maze extends Entity {

	private Cell[][] cells;
	//private List<Wall> wallsBetweenCells;
	//private List<Wall> borderWalls;
	private Cell startCell, endCell;
	public Maze(int numCellsWide, int numCellsHigh, int x, int y, int maxWidth,
			int maxHeight, DeadEndProbability deadEndProbability, double wallProportionOfCellDimensions, int numCheckpoints,
			int numCheckpointAttempts, SurfacePicker surfacePicker, Color checkpointColor, int startCellColumn, int startCellRow, int endCellColumn, int endCellRow, Color wallColor, MazeType mazeType) {
		super(x, y, numCellsWide
				* Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide),
				numCellsHigh
						* Math.min(maxHeight / numCellsHigh, maxWidth
								/ numCellsWide));
		initCells(numCellsWide, numCellsHigh, maxWidth, maxHeight, wallColor, wallProportionOfCellDimensions, surfacePicker);
		System.out.println("Cells initialised");
		startCell = cells[startCellColumn][startCellRow];
		endCell = cells[endCellColumn][endCellRow];
		switch(mazeType) {
		case KRUSKAL:
			initKruskalPerfectMaze();
			break;
		case DFS:
			initDepthFirstPerfectMaze();
		}
		System.out.println("Generated perfect maze");
		removeDeadEnds(deadEndProbability);
		System.out.println("Removed dead ends");
		placeCheckpoints(numCheckpoints,
				numCheckpointAttempts, checkpointColor);
		System.out.println("Checkpoints placed");
		//temp
		int numVertices = 0, nCheckpoints = 0;
		for(int i = 0; i < cells.length; i++) {
			for(int j = 0; j < cells[i].length; j++) {
				Cell cell = cells[i][j];
				if(cell.getOrder() != 2 || cell.hasCheckpoint()) {
					numVertices++;
					if(cell.hasCheckpoint()) {
						nCheckpoints++;
					}
				}
			}
		}
		System.out.println("RequiredNumberOfVertices: " + numVertices);
		System.out.println("RequiredNumberOfCheckpoints: " + nCheckpoints);
	}

	public Cell getStartCell() {
		return startCell;
	}
	
	public Cell getEndCell() {
		return endCell;
	}
	
	private void initCells(int numCellsWide, int numCellsHigh, int maxWidth, int maxHeight, Color wallColor, double wallProportionOfCellDimensions, SurfacePicker surfacePicker) {
		cells = new Cell[numCellsWide][numCellsHigh];
		//wallsBetweenCells = new ArrayList<Wall>();
		int cellSide = Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide);
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = new Cell(x + i * cellSide, y + j * cellSide, cellSide, surfacePicker.getRandomSurface(), wallProportionOfCellDimensions, wallColor);
				cells[i][j] = currentCell;
/*				if (i == 0) {
					borderWalls.add(Wall.createWall(currentCell, null, Direction.WEST,
							wallProportionOfCellDimensions));
				} else if (i == cells.length - 1) {
					borderWalls.add(Wall.createWall(currentCell, null, Direction.EAST,
							wallProportionOfCellDimensions));
				}
				if (j == 0) {
					borderWalls.add(Wall.createWall(currentCell, null, Direction.NORTH,
							wallProportionOfCellDimensions));
				} else if (j == cells[i].length - 1) {
					borderWalls.add(Wall.createWall(currentCell, null, Direction.SOUTH,
							wallProportionOfCellDimensions));
				}
*/				if (j > 0) {
					Cell neighbouringCell = cells[i][j - 1];
					currentCell.addNeighbouringCell(neighbouringCell, Direction.NORTH);
				}
				if (i > 0) {
					Cell neighbouringCell = cells[i - 1][j];
					currentCell.addNeighbouringCell(neighbouringCell, Direction.WEST);
				}
			}
		}
	}

	// this is graph traversal, cells are nodes, lack of walls are edges
	private void initDepthFirstPerfectMaze() {
		Stack<Cell> stack = new Stack<Cell>();
		Set<Cell> visitedCells = new HashSet<Cell>();
		Cell currentCell = startCell;
		visitedCells.add(currentCell);
		do {
			List<Cell> unvisitedNeighbouringCells = new ArrayList<Cell>();
			for(Cell neighbouringCell : currentCell.getNeighbouringCells()) {
				if(!visitedCells.contains(neighbouringCell)) {
					unvisitedNeighbouringCells.add(neighbouringCell);
				}
			}
			if(!unvisitedNeighbouringCells.isEmpty()) {
				Cell randomUnvisitedNeighbouringCell = unvisitedNeighbouringCells.get(Application.rng.nextInt(unvisitedNeighbouringCells.size()));
				stack.push(currentCell);
				currentCell.setAdjacentTo(randomUnvisitedNeighbouringCell);
				currentCell = randomUnvisitedNeighbouringCell;
				visitedCells.add(randomUnvisitedNeighbouringCell);
			} else if (!stack.isEmpty()) {
				currentCell = stack.pop();
			}
		} while (!stack.isEmpty());
	}	
	
	private void initKruskalPerfectMaze() {
		DisjointSet<Cell> cellDisjointSet = new DisjointSet<Cell>();
		Set<Wall> wallsSet = new HashSet<Wall>();
		for(int i = 0; i < cells.length; i++) {
			for(int j = 0; j < cells[i].length; j++) {
				Cell cell = cells[i][j];
				cellDisjointSet.add(cell);
				wallsSet.addAll(cell.getWalls());
			}
		}
		List<Wall> wallsList = new ArrayList<Wall>(wallsSet);
		Collections.shuffle(wallsList, Application.rng);
		List<Wall> wallsToKeep = new ArrayList<Wall>();
		for(Wall wall : wallsList) {
			Cell cell1 = wall.getCell1();
			Cell cell2 = wall.getCell2();
			if(cell1 == null || cell2 == null) {
				System.out.println("NULL");
			}
			if(!cellDisjointSet.areJoined(cell1, cell2)) {
				cell1.setAdjacentTo(cell2);
				cellDisjointSet.union(cell1, cell2);
			} else {
				wallsToKeep.add(wall);
			}
		}
	}
	
	private void removeDeadEnds(DeadEndProbability deadEndProbability) {
		List<Cell> cellsList = new ArrayList<Cell>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cellsList.add(cells[i][j]);
			}
		}
		Collections.shuffle(cellsList, Application.rng);
		for (Cell cell : cellsList) {
			if (cell.getOrder() <= 1) {
				System.out.println("REMOVING");
				if (Application.rng.nextFloat() < deadEndProbability.getProbability()) {
					Cell walledCell = cell.getRandomNeighbouringWalledCell();
					cell.setAdjacentTo(walledCell);
				}
			}
		}
	}

	// REMEMBER TO MAKE START AND END CHECKPOINTS AS WELL!!!
	private void placeCheckpoints(int numCheckpoints, int numAttempts, Color checkpointColor) {
		startCell.addCheckpoint(checkpointColor);
		endCell.addCheckpoint(checkpointColor);
		for (int i = 0; i < numCheckpoints; i++) {
			int x = Application.rng.nextInt(cells.length);
			int y = Application.rng.nextInt(cells[x].length);
			Cell checkpointCell = cells[x][y];
			if(checkpointCell.hasCheckpoint()) {
				i--;
			} else {
				checkpointCell.addCheckpoint(checkpointColor);
			}
		}
	}

	public double getCellSide() {
		return startCell.getHeight();
	}

	@Override
	public void update(double delta) {

	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect((int) x, (int) y, (int) width, (int) height);
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = cells[i][j];
				currentCell.render(g);
			}
		}
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = cells[i][j];
				currentCell.renderWalls(g);
			}
		}

/*		for (Wall wall : wallsBetweenCells) {
			wall.render(g);
		}
*//*		for (Wall wall : borderWalls) {
			wall.render(g);
		}
*/	}

}
