package map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import logic.Direction;
import logic.Entity;
import user_interface.Application;

public class Maze extends Entity {

	private Cell[][] cells;
	private List<Wall> walls;

	public Maze(int numCellsWide, int numCellsHigh, int x, int y, int maxWidth,
			int maxHeight, double deadEndProbability, double wallProportionOfCellDimensions, int numCheckpoints,
			int numCheckpointAttempts, SurfacePicker surfacePicker) {
		super(x, y, numCellsWide
				* Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide),
				numCellsHigh
						* Math.min(maxHeight / numCellsHigh, maxWidth
								/ numCellsWide));
		cells = new Cell[numCellsWide][numCellsHigh];
		int cellSide = Math.min(maxHeight / numCellsHigh, maxWidth
				/ numCellsWide);
		initCellMatrix(cells, cellSide, surfacePicker);
		System.out.println("Cell matrix initialised");
		generateDepthFirstPerfectMaze(cells);
		System.out.println("Generated perfect maze");
		removeDeadEnds(cells, deadEndProbability);
		System.out.println("Removed dead ends");
		walls = initWallsList(cells, wallProportionOfCellDimensions);
		System.out.println("Physical walls initialised");
		Cell topLeftCell = cells[0][0];
		Cell bottomRightCell = cells[numCellsWide-1][numCellsHigh-1];
		placeCheckpoints(cells, topLeftCell, bottomRightCell, numCheckpoints,
				numCheckpointAttempts);
		System.out.println("Checkpoints placed");
	}

	private void initCellMatrix(Cell[][] cells, int cellSide, SurfacePicker surfacePicker) {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = new Cell(x + i * cellSide, y + j * cellSide,
						cellSide, surfacePicker.getRandomSurface());
				cells[i][j] = currentCell;
				if (j > 0) {
					Cell neighbouringCell = cells[i][j - 1];
					currentCell.addNeighbouringCell(neighbouringCell, Direction.WEST);
				}
				if (i > 0) {
					Cell neighbouringCell = cells[i - 1][j];
					currentCell.addNeighbouringCell(neighbouringCell, Direction.NORTH);
				}
			}
		}
	}

	// this is graph traversal, cells are nodes, lack of walls are edges
	private void generateDepthFirstPerfectMaze(Cell[][] cells) {
		Stack<Cell> stack = new Stack<Cell>();
		Cell currentCell = cells[0][0];
		int unvisitedCells = cells.length*cells[0].length;
		currentCell.setVisited();
		unvisitedCells--;
		do {
			Cell neighbouringCell;
			if ((neighbouringCell = currentCell
					.getRandomUnvisitedNeighbouringCell()) != null) {
				stack.push(currentCell);
				currentCell.setAdjacentTo(neighbouringCell);
				currentCell = neighbouringCell;
				neighbouringCell.setVisited();
				unvisitedCells--;
			} else if (!stack.isEmpty()) {
				currentCell = stack.pop();
			}
		} while (unvisitedCells > 0);
	}

	private void generateKruskalPerfectMaze() {
		// TODO
	}

	private void removeDeadEnds(Cell[][] cells,
			double deadEndProbability) {
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

	private List<Wall> initWallsList(Cell[][] cells, double proportionOfCellDimensions) {
		List<Wall> walls = new ArrayList<Wall>();
		
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				Cell currentCell = cells[i][j];
				if (i == 0) {
					walls.add(Wall.createWall(currentCell, Direction.WEST,
							proportionOfCellDimensions));
				} else if (i == cells.length - 1) {
					walls.add(Wall.createWall(currentCell, Direction.EAST,
							proportionOfCellDimensions));
				}
				if (j == 0) {
					walls.add(Wall.createWall(currentCell, Direction.NORTH,
							proportionOfCellDimensions));
				} else if (j == cells[i].length - 1) {
					walls.add(Wall.createWall(currentCell, Direction.SOUTH,
							proportionOfCellDimensions));
				}
				if (j > 0) {
					Cell westCell = cells[i][j-1];
					if (!currentCell.isAdjacentTo(westCell)) {
						walls.add(Wall.createWall(currentCell, Direction.WEST,
								proportionOfCellDimensions));
					}
				}
				if (i > 0) {
					Cell northCell = cells[i-1][j];
					if (!currentCell.isAdjacentTo(northCell)) {
						walls.add(Wall.createWall(currentCell, Direction.NORTH,
								proportionOfCellDimensions));
					}
				}
			}
		}
		return walls;
	}

	// REMEMBER TO MAKE START AND END CHECKPOINTS AS WELL!!!
	private void placeCheckpoints(Cell[][] cells,
			Cell startCell, Cell endCell, int numCheckpoints, int numAttempts) {
		startCell.addCheckpoint();
		endCell.addCheckpoint();
		for (int i = 0; i < numCheckpoints; i++) {
			int x = Application.rng.nextInt(cells.length);
			int y = Application.rng.nextInt(cells[x].length);
			Cell checkpointCell = cells[x][y];
			checkpointCell.addCheckpoint();
		}
	}

	public double getCellSide() {
		return cells[0][0].getHeight();
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
		for (Wall wall : walls) {
			wall.render(g);
		}
	}

}
