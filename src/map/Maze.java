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
	private List<Wall> wallsBetweenCells;
	private List<Wall> borderWalls;
	private Cell startCell, endCell;
	public Maze(int numCellsWide, int numCellsHigh, int x, int y, int maxWidth,
			int maxHeight, double deadEndProbability, double wallProportionOfCellDimensions, int numCheckpoints,
			int numCheckpointAttempts, SurfacePicker surfacePicker, Color checkpointColor, int startCellColumn, int startCellRow, int endCellColumn, int endCellRow) {
		super(x, y, numCellsWide
				* Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide),
				numCellsHigh
						* Math.min(maxHeight / numCellsHigh, maxWidth
								/ numCellsWide));
		initCells(numCellsWide, numCellsHigh, maxWidth, maxHeight, wallProportionOfCellDimensions, surfacePicker);
		System.out.println("Cells initialised");
		startCell = cells[startCellColumn][startCellRow];
		endCell = cells[endCellColumn][endCellRow];
		initKruskalPerfectMaze();
		System.out.println("Generated perfect maze");
		//removeDeadEnds(cells, deadEndProbability);
		System.out.println("Removed dead ends");
		placeCheckpoints(numCheckpoints,
				numCheckpointAttempts, checkpointColor);
		System.out.println("Checkpoints placed");
	}

	public Cell getStartCell() {
		return startCell;
	}
	
	public Cell getEndCell() {
		return endCell;
	}
	
	private void initCells(int numCellsWide, int numCellsHigh, int maxWidth, int maxHeight, double proportionOfCellDimensions, SurfacePicker surfacePicker) {
		cells = new Cell[numCellsWide][numCellsHigh];
		wallsBetweenCells = new ArrayList<Wall>();
		borderWalls = new ArrayList<Wall>();
		int cellSide = Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide);
		
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = new Cell(x + i * cellSide, y + j * cellSide, cellSide, surfacePicker.getRandomSurface());
				cells[i][j] = currentCell;
				if (i == 0) {
					borderWalls.add(Wall.createWall(currentCell, Direction.WEST,
							proportionOfCellDimensions));
				} else if (i == cells.length - 1) {
					borderWalls.add(Wall.createWall(currentCell, Direction.EAST,
							proportionOfCellDimensions));
				}
				if (j == 0) {
					borderWalls.add(Wall.createWall(currentCell, Direction.NORTH,
							proportionOfCellDimensions));
				} else if (j == cells[i].length - 1) {
					borderWalls.add(Wall.createWall(currentCell, Direction.SOUTH,
							proportionOfCellDimensions));
				}
				if (j > 0) {
					Cell neighbouringCell = cells[i][j - 1];
					currentCell.addNeighbouringCell(neighbouringCell, Direction.NORTH);
					wallsBetweenCells.add(Wall.createWall(currentCell, neighbouringCell, proportionOfCellDimensions));
				}
				if (i > 0) {
					Cell neighbouringCell = cells[i - 1][j];
					currentCell.addNeighbouringCell(neighbouringCell, Direction.WEST);
					wallsBetweenCells.add(Wall.createWall(currentCell, neighbouringCell, proportionOfCellDimensions));
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
			for(Cell neighbouringCell : currentCell.getNonAdjacentCells()) {
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
		for(int i = 0; i < cells.length; i++) {
			for(int j = 0; j < cells[i].length; j++) {
				cellDisjointSet.add(cells[i][j]);
			}
		}
		Collections.shuffle(wallsBetweenCells, Application.rng);
		List<Wall> wallsToKeep = new ArrayList<Wall>();
		for(Wall wall : wallsBetweenCells) {
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
		wallsBetweenCells = new ArrayList<Wall>();
		for(Wall wall : wallsToKeep) {
			wallsBetweenCells.add(wall);
		}
	}
	
	private void removeDeadEnds(double deadEndProbability) {
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
				if (Application.rng.nextFloat() < deadEndProbability) {
					Cell adjacentCell = cell.getRandomAdjacentCell();
					cell.setAdjacentTo(adjacentCell);
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
			checkpointCell.addCheckpoint(checkpointColor);
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
		for (Wall wall : wallsBetweenCells) {
			wall.render(g);
		}
		for (Wall wall : borderWalls) {
			wall.render(g);
		}
	}

}
