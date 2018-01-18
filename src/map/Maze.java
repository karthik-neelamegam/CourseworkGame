package map;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import logic.Entity;
import user_interface.Application;

public class Maze extends Entity {

	private ArrayList<ArrayList<Cell>> cells;
	private List<Wall> walls;
		
	public Maze(int numCellsWide, int numCellsHigh, int x, int y,
			int maxWidth, int maxHeight, double deadEndProbability,
			int numCheckpoints, int numCheckpointAttempts, SurfacePicker surfacePicker) {
		super(x, y, numCellsWide
				* Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide),
				numCellsHigh
						* Math.min(maxHeight / numCellsHigh, maxWidth
								/ numCellsWide));
		int cellSide = Math.min(maxHeight / numCellsHigh, maxWidth / numCellsWide);
		cells = initCellMatrix(numCellsWide, numCellsHigh, cellSide, surfacePicker);
		System.out.println("Cell matrix initialised");
		generateDepthFirstPerfectMaze(cells);
		System.out.println("Generated perfect maze");
		removeDeadEnds(cells, deadEndProbability);
		System.out.println("Removed dead ends");
		walls = initWallsList(cells);
		System.out.println("Physical walls initialised");
		Cell topLeftCell = cells.get(0).get(0);
		Cell bottomRightCell = cells.get(cells.size()-1).get(cells.get(cells.size()-1).size()-1);
		placeCheckpoints(cells, topLeftCell, bottomRightCell, numCheckpoints, numCheckpointAttempts);
		System.out.println("Checkpoints placed");
	}

	private ArrayList<ArrayList<Cell>> initCellMatrix(int numCellsWide, int numCellsHigh, int cellSide, SurfacePicker surfacePicker) {
		ArrayList<ArrayList<Cell>> cells = new ArrayList<ArrayList<Cell>>(); 
		for (int i = 0; i < numCellsWide; i++) {
			cells.add(new ArrayList<Cell>());
			for (int j = 0; j < numCellsHigh; j++) {
				Cell currentCell = new Cell(getX() + i * cellSide, getY() + j
						* cellSide, cellSide, surfacePicker.getRandomSurface());
				cells.get(i).add(currentCell);
				if (j > 0) {
					Cell neighbouringCell = cells.get(i).get(j-1);
					currentCell.addNeighbouringCell(neighbouringCell);
				}
				if (i > 0) {
					Cell neighbouringCell = cells.get(i-1).get(j);
					currentCell.addNeighbouringCell(neighbouringCell);
				}
			}
		}
		return cells;
	}

	// this is graph traversal, cells are nodes, lack of walls are edges
	private void generateDepthFirstPerfectMaze(ArrayList<ArrayList<Cell>> cells) {
		Stack<Cell> stack = new Stack<Cell>();
		Cell currentCell = cells.get(0).get(0);
		int unvisitedCells = cells.size() * cells.get(0).size();
		currentCell.setVisited();
		unvisitedCells--;
		do {
			Cell neighbouringCell;
			if ((neighbouringCell = currentCell
					.getRandomUnvisitedNeighbouringCell()) != null) {
				stack.push(currentCell);
				currentCell.removeWall(neighbouringCell);
				currentCell = neighbouringCell;
				neighbouringCell.setVisited();
				unvisitedCells--;
			} else if (!stack.isEmpty()) {
				currentCell = stack.pop();
			}
		} while (unvisitedCells > 0);
	}
	
	private void generateKruskalPerfectMaze() {
		//TODO
	}

	private void removeDeadEnds(ArrayList<ArrayList<Cell>> cells, double deadEndProbability) {
		List<Cell> cellsList = new ArrayList<Cell>();
		for (int i = 0; i < cells.size(); i++) {
			for (int j = 0; j < cells.get(i).size(); j++) {
				cellsList.add(cells.get(i).get(j));
			}
		}
		Collections.shuffle(cellsList, Application.rng);
		for (Cell cell : cellsList) {
			if (cell.getOrder() <= 1) {
				System.out.println("REMOVING");
				if (Application.rng.nextFloat() < deadEndProbability) {
					Cell adjacentCell = cell.getRandomAdjacentCell();
					cell.removeWall(adjacentCell);
				}
			}
		}
	}

	private List<Wall> initWallsList(ArrayList<ArrayList<Cell>> cells) {
		List<Wall> walls = new ArrayList<Wall>();
		for (int i = 0; i < cells.size(); i++) {
			for (int j = 0; j < cells.get(i).size(); j++) {
				Cell currentCell = cells.get(i).get(j);
				if (j > 0) {
					Cell leftCell = cells.get(i).get(j-1);
					if (!currentCell.isAdjacent(leftCell)) {
						walls.add(Wall.createWall(currentCell, leftCell, 0.1));
					}
				}
				if (i > 0) {
					Cell upCell = cells.get(i-1).get(j);
					if (!currentCell.isAdjacent(upCell)) {
						walls.add(Wall.createWall(currentCell, upCell, 0.1));
					}
				}
			}
		}
		return walls;
	}

	//REMEMBER TO MAKE START AND END CHECKPOINTS AS WELL!!!
	private void placeCheckpoints(ArrayList<ArrayList<Cell>> cells, Cell startCell, Cell endCell, int numCheckpoints, int numAttempts) {
		startCell.addCheckpoint();
		endCell.addCheckpoint();
		for (int i = 0; i < numCheckpoints; i++) {
			int x = Application.rng.nextInt(cells.size());
			int y = Application.rng.nextInt(cells.get(x).size());
			Cell checkpointCell = cells.get(x).get(y);
			checkpointCell.addCheckpoint();
		}
	}
				
	@Override
	public void update(double delta) {

	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(getX(), getY(), getWidth(), getHeight());
		for (List<Cell> cellsSubList : cells) {
			for (Cell cell : cellsSubList) {
				cell.render(g);
			}
		}
		for (Wall wall : walls) {
			wall.render(g);
		}
	}

}
