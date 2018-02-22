package core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.EnumMap;
import java.util.Random;

import javax.swing.JFrame;

public class Application {
	/*
	 * This class is where the window (JFrame) is set up and the ScreenDisplayer
	 * object is started.
	 */

	/*
	 * This global random number generator will be used whenever a random number
	 * is required. This makes testing more consistent as the same seed can be
	 * given to this random number generator and all the random numbers
	 * generated will be outputted in the same order every time.
	 */
	public static Random randomNumberGenerator;

	/*
	 * Constructor. gameHz dictates the number of game cycles per second.
	 * screenHeightFraction dictates the proportion of the screen's height taken
	 * up by the window. aspectRatio determines the ratio between the width of
	 * the window and the height of the window. randomSeed is used for rng.
	 */
	public Application(double screenHeightFraction, double aspectRatio,
			int gameHz, long randomSeed) {
		randomNumberGenerator = new Random(randomSeed);
		ScreenDisplayer screenDisplayer = new ScreenDisplayer(gameHz);

		/*
		 * The window upon which screenDisplayer is displayed.
		 */
		JFrame frame = new JFrame("CourseworkGame");

		/*
		 * Sets the screen displayed on screenDisplayer to the main menu screen.
		 */
		screenDisplayer.setScreen(new MainMenuScreen(screenDisplayer));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenHeight = screenSize.getHeight();
		int windowHeight = (int) (screenHeight * screenHeightFraction);
		int windowWidth = (int) (windowHeight * aspectRatio);

		/*
		 * Adds screenDisplayer (a JPanel) to frame
		 */
		frame.add(screenDisplayer);
		frame.pack();
		frame.setSize(windowWidth, windowHeight);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		// frame.setFocusable(true);

		/*
		 * Starts the game loop in the screenDisplayer object.
		 */
		screenDisplayer.run();

		/*
		 * Only once everything is set up, frame is set to be visible, otherwise
		 * the user may see visual hitches while things are being set up.
		 */
		frame.setVisible(true);
	}

	/*
	 * The main method. Creates a new Application object such that the window is
	 * square (because the mazes will be square for simplicity, although they
	 * can easily be rectangular) and takes up 90% of the screen height (so that
	 * the window is large and the game is clear). The gameHz parameter is 60
	 * (as most monitors only display 60 frames per second) and the random seed
	 * is the current time (so that it is different every time the user runs the
	 * game, giving different mazes every time). Named constants have been used
	 * for clarity.
	 */
	public static void main(String[] args) {
		new Application(GameConstants.WINDOW_SCREEN_HEIGHT_FRACTION,
				GameConstants.WINDOW_ASPECT_RATIO, GameConstants.GAME_HZ,
				System.currentTimeMillis());
	}

	/*
	 * Tests.
	 */

	public static Maze generateRandomMaze(double deadEndProbability) {
		/*
		 * Randomly set the maze dimensions to ensure that the test results are
		 * not biased.
		 */
		int numCellsWide = 5 + randomNumberGenerator.nextInt(100);
		int numCellsHigh = 5 + randomNumberGenerator.nextInt(100);

		/*
		 * Randomly select the maze generation algorithm used to generate the
		 * maze to ensure that both algorithms work properly and that the test
		 * results are not biased.
		 */
		MazeType mazeType = randomNumberGenerator.nextBoolean() ? MazeType.KRUSKAL
				: MazeType.DFS;

		/*
		 * Randomly select the number of checkpoints in the maze to ensure that
		 * the test results are not biased. The upper limit is the square root
		 * of the number of Cell objects in the maze (excluding the endpoint
		 * Cell objects), otherwise the tests would take too long.
		 */
		int numCheckpointsExcludingEndpoints = randomNumberGenerator
				.nextInt((int) (Math.sqrt(numCellsWide * numCellsHigh - 2))) + 2;

		/*
		 * Generate a random distribution for the surfaceRatios map to ensure
		 * that the test results are not biased.
		 */
		EnumMap<Surface, Double> surfaceRatios = new EnumMap<Surface, Double>(
				Surface.class) {
			{
				put(Surface.SLOW, randomNumberGenerator.nextDouble());
				put(Surface.NORMAL, randomNumberGenerator.nextDouble());
				put(Surface.FAST, randomNumberGenerator.nextDouble());
			}
		};

		/*
		 * These parameters are cosmetic do not matter for logical tests.
		 */
		double cellSideLength = 1;
		double x = 0;
		double y = 0;
		double wallProportionOfCellDimensions = 1;
		double checkpointProportionOfCellDimensions = 1;
		Color checkpointColor = GameConstants.CHECKPOINT_COLOR;
		Color wallColor = GameConstants.WALL_COLOR;
		Color groundColor = GameConstants.GROUND_COLOR;

		/*
		 * Returns a maze with the above values as arguments.
		 */
		return new Maze(numCellsWide, numCellsHigh, x, y, cellSideLength,
				deadEndProbability, wallProportionOfCellDimensions,
				checkpointProportionOfCellDimensions,
				numCheckpointsExcludingEndpoints, surfaceRatios,
				checkpointColor, wallColor, groundColor, mazeType);

	}

	public static void isolatedSectionsTest() {
		randomNumberGenerator = new Random(System.currentTimeMillis());
		/*
		 * 1000 is a large enough sample size to be confident that the test
		 * results are reliable and valid.
		 */
		int numTests = 1000;

		/*
		 * Keeps track of the number of mazes generated with isolated sections.
		 */
		int numMazesWithIsolatedSections = 0;

		/*
		 * This loop generates a number of mazes equal to numTests and checks if
		 * they have isolated sections, incrementing the above variable if they
		 * do.
		 */
		for (int i = 0; i < numTests; i++) {

			/*
			 * Randomly set the dead-end probability to ensure that the test
			 * results are not biased.
			 */
			double deadEndProbability = randomNumberGenerator.nextDouble();

			/*
			 * Generate a random maze with this dead-end probability to ensure
			 * that the test results are not biased.
			 */
			Maze maze = generateRandomMaze(deadEndProbability);

			/*
			 * Calls the hasNoIsolatedSections method of maze to check if the
			 * maze has isolated sections and increments
			 * numMazesWithIsolatedSections if it does.
			 */
			if (!maze.hasNoIsolatedSections()) {
				numMazesWithIsolatedSections++;
			}
		}

		/*
		 * Output the results of the test.
		 */
		System.out.println("Number of mazes with isolated sections: "
				+ numMazesWithIsolatedSections + " / " + numTests);
	}

	public static void cyclesTest() {
		randomNumberGenerator = new Random(System.currentTimeMillis());
		/*
		 * 1000 is a large enough sample size to be confident that the test
		 * results are reliable and valid.
		 */
		int numTests = 1000;

		/*
		 * Keeps track of the number of mazes generated whos graph
		 * representations had no cycles.
		 */
		int numMazesWithNoCycles = 0;

		/*
		 * This loop generates a number of mazes equal to numTests and checks if
		 * their graph representations have no cycles, incrementing the above
		 * variable if they do.
		 */
		for (int i = 0; i < numTests; i++) {

			/*
			 * Randomly set the dead-end probability to ensure that the test
			 * results are not biased.
			 */
			double deadEndProbability = randomNumberGenerator.nextDouble();

			/*
			 * Generate a random maze with this dead-end probability to ensure
			 * that the test results are not biased.
			 */
			Maze maze = generateRandomMaze(deadEndProbability);

			/*
			 * Calls the hasNoCycles method of maze to check if the maze's graph
			 * representation has cycles, and increments numMazesWithNoCycles
			 * and outputs deadEndProbability (as a possible cause) if it
			 * doesn't.
			 */
			if (!maze.hasCycles()) {
				numMazesWithNoCycles++;
				System.out
						.println("Dead-end probability of a maze with no cycles: "
								+ deadEndProbability);
			}
		}

		/*
		 * Output the results of the test.
		 */
		System.out.println("Number of mazes with no cycles: "
				+ numMazesWithNoCycles + " / " + numTests);
	}

	public static void validGreedyCheckpointVertexRouteTest() {
		randomNumberGenerator = new Random(System.currentTimeMillis());
		/*
		 * 1000 is a large enough sample size to be confident that the test
		 * results are reliable and valid.
		 */
		int numTests = 1000;

		/*
		 * Keeps track of the number of cases where the greedy checkpoint vertex
		 * route generated was invalid.
		 */
		int numCasesWithInvalidRoutes = 0;

		/*
		 * This loop generates a number of cases equal to numTests and checks if
		 * the generated greedy checkpoint vertex route is invalid, incrementing
		 * the above variable if it is.
		 */
		for (int i = 0; i < numTests; i++) {
			/*
			 * Randomly set the dead-end probability to ensure that the test
			 * results are not biased.
			 */
			double deadEndProbability = randomNumberGenerator.nextDouble();

			/*
			 * Generate a random maze with this dead-end probability to ensure
			 * that the test results are not biased.
			 */
			Maze maze = generateRandomMaze(deadEndProbability);
			ReducedGraph reducedGraph = new ReducedGraph(maze.getStartCell());

			/*
			 * Generate an AIPlayer with default parameters.
			 */
			AIPlayer aiPlayer = new AIPlayer(
					maze.getEndCell(),
					maze.getStartCell(),
					maze.getCellSideLength()
							* GameConstants.AI_PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER2_COLOR,
					GameConstants.PLAYER2_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS, maze
							.getNumCheckpoints(), reducedGraph);

			/*
			 * Calls the isGreedyCheckpointVertexRouteValid method of aiPlayer
			 * to check if the generated greedy checkpoint vertex route is
			 * valid, and increments numCasesWithInvalidRoutes if it isn't.
			 */
			if (!aiPlayer.isGreedyCheckpointVertexRouteValid(
					reducedGraph.getVertex(maze.getEndCell()),
					reducedGraph.getVertex(maze.getStartCell()))) {
				numCasesWithInvalidRoutes++;
			}
		}

		/*
		 * Output the results of the test.
		 */
		System.out
				.println("Number of cases with invalid generated greedy checkpoint vertex routes: "
						+ numCasesWithInvalidRoutes + " / " + numTests);
	}

	public static void mergeSortTest() {
		randomNumberGenerator = new Random(System.currentTimeMillis());
		/*
		 * 1000 is a large enough sample size to be confident that the test
		 * results are reliable and valid.
		 */
		int numTests = 1000;

		/*
		 * Keeps track of the number of cases where the mergeSort method failed.
		 */
		int numCasesWhereMergeSortFailed = 0;

		/*
		 * This loop generates a number of cases equal to numTests and checks if
		 * the mergeSort method fails, incrementing the above variable if it
		 * does.
		 */
		for (int i = 0; i < numTests; i++) {
			/*
			 * Randomly set the dead-end probability to ensure that the test
			 * results are not biased.
			 */
			double deadEndProbability = randomNumberGenerator.nextDouble();

			/*
			 * Generate a random maze with this dead-end probability to ensure
			 * that the test results are not biased.
			 */
			Maze maze = generateRandomMaze(deadEndProbability);
			ReducedGraph reducedGraph = new ReducedGraph(maze.getStartCell());

			/*
			 * Generate an AIPlayer with default parameters.
			 */
			AIPlayer aiPlayer = new AIPlayer(
					maze.getEndCell(),
					maze.getStartCell(),
					maze.getCellSideLength()
							* GameConstants.AI_PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER2_COLOR,
					GameConstants.PLAYER2_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS, maze
							.getNumCheckpoints(), reducedGraph);

			/*
			 * Calls the isMergeSortFunctional method of aiPlayer to check if
			 * the mergeSort method fails, and increments
			 * numCasesWhereMergeSortFailed if it does.
			 */
			if (!aiPlayer.isMergeSortFunctional()) {
				numCasesWhereMergeSortFailed++;
			}
		}

		/*
		 * Output the results of the test.
		 */
		System.out
				.println("Number of cases where the mergeSort method failed: "
						+ numCasesWhereMergeSortFailed + " / " + numTests);

	}

	public static void greedyVsRandomCheckpointVertexRouteTest() {
		randomNumberGenerator = new Random(System.currentTimeMillis());
		/*
		 * 1000 is a large enough sample size to be confident that the test
		 * results are reliable and valid.
		 */
		int numTests = 1000;

		/*
		 * Keeps track of the number of cases where the greedy weight (i.e. the
		 * weight of the checkpoint route generated by the greedy algorithm) was
		 * greater than the random weight (i.e. the average weight of the
		 * randomly generated checkpoint routes).
		 */
		int numCasesWhereGreedyWeightGreaterThanRandomWeight = 0;

		/*
		 * Keeps track of the sum of the percentage differences between the
		 * greedy weights and the random weights.
		 */
		double sumOfPercentageDifferencesBetweenRandomWeightAndGreedyWeight = 0;

		/*
		 * This loop generates a number of cases equal to numTests and finds the
		 * percentage difference between the greedy weight and random weight,
		 * adding it to the
		 * sumOfPercentageDifferencesBetweenRandomWeightAndGreedyWeight
		 * variable, and incrementing the
		 * numCasesWhereGreedyWeightGreaterThanRandomWeight variable if the
		 * greedy weight is greater than the random weight.
		 */
		for (int i = 0; i < numTests; i++) {
			/*
			 * Randomly set the dead-end probability to ensure that the test
			 * results are not biased.
			 */
			double deadEndProbability = randomNumberGenerator.nextDouble();

			/*
			 * Generate a random maze with this dead-end probability to ensure
			 * that the test results are not biased.
			 */
			Maze maze = generateRandomMaze(deadEndProbability);
			ReducedGraph reducedGraph = new ReducedGraph(maze.getStartCell());

			/*
			 * Generate an AIPlayer with default parameters.
			 */
			AIPlayer aiPlayer = new AIPlayer(
					maze.getEndCell(),
					maze.getStartCell(),
					maze.getCellSideLength()
							* GameConstants.AI_PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER2_COLOR,
					GameConstants.PLAYER2_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS, maze
							.getNumCheckpoints(), reducedGraph);

			/*
			 * Calls the percentageDifferenceBetweenRandomWeightAndGreedyWeight
			 * method of aiPlayer to find the percentage difference between the
			 * greedy weight and the random weight.
			 */
			double percentageDifferenceBetweenRandomWeightAndGreedyWeight = aiPlayer
					.calculatePercentageDifferenceBetweenRandomWeightAndGreedyWeight(
							reducedGraph.getVertex(maze.getEndCell()),
							reducedGraph.getVertex(maze.getStartCell()));

			/*
			 * If the greedy weight is greater than the random weight, we
			 * increment the relevant variable and output the difference (to see
			 * how significant the difference is).
			 */
			if (percentageDifferenceBetweenRandomWeightAndGreedyWeight < 0) {
				numCasesWhereGreedyWeightGreaterThanRandomWeight++;
				System.out
						.println("Percentage difference if greedy weight greater than random weight: "
								+ percentageDifferenceBetweenRandomWeightAndGreedyWeight);
			}

			/*
			 * We add the difference to the
			 * sumOfPercentageDifferencesBetweenRandomWeightAndGreedyWeight
			 * variable so that we can find the average at the end of the for
			 * loop.
			 */
			sumOfPercentageDifferencesBetweenRandomWeightAndGreedyWeight += percentageDifferenceBetweenRandomWeightAndGreedyWeight;
		}

		/*
		 * The average percentage difference between the random weight and the
		 * greedy weight is found by dividing the sum of all the percentage
		 * differences by the number of test cases.
		 */
		double averagePercentageDifferenceBetweenRandomWeightAndGreedyWeight = sumOfPercentageDifferencesBetweenRandomWeightAndGreedyWeight
				/ numTests;

		/*
		 * Output the results of the test.
		 */
		System.out
				.println("Number of cases where the weight of the checkpoint route generated "
						+ "by the greedy algorithm was greater than the average weight of the "
						+ "randomly generated checkpoint routes: "
						+ numCasesWhereGreedyWeightGreaterThanRandomWeight
						+ " / " + numTests);
		System.out
				.println("Average percentage difference between the greedy weight and random weight: "
						+ averagePercentageDifferenceBetweenRandomWeightAndGreedyWeight);
	}

	public static void beforeVsAfterTwoOptCheckpointVertexRouteTest() {
		randomNumberGenerator = new Random(System.currentTimeMillis());
		/*
		 * 1000 is a large enough sample size to be confident that the test
		 * results are reliable and valid.
		 */
		int numTests = 1000;

		/*
		 * Keeps track of the number of times the twoOpt method increases the
		 * weight of the greedy checkpoint route that it is applied to.
		 */
		int numCasesWhereWeightIncreasesAfterTwoOpt = 0;

		/*
		 * Keeps track of the sum of the percentage drops in the weight of the
		 * greedy checkpoint route from before the twoOpt method is applied to
		 * after.
		 */
		double sumOfPercentageDropsInWeightAfterTwoOpt = 0;

		/*
		 * This loop generates a number of cases equal to numTests and finds the
		 * percentage drop in the weight of the greedy checkpoint route from
		 * before the twoOpt method is applied to after, adding it to the
		 * sumOfPercentageDropsInWeightAfterTwoOpt variable.
		 */
		for (int i = 0; i < numTests; i++) {
			/*
			 * Randomly set the dead-end probability to ensure that the test
			 * results are not biased.
			 */
			double deadEndProbability = randomNumberGenerator.nextDouble();

			/*
			 * Generate a random maze with this dead-end probability to ensure
			 * that the test results are not biased.
			 */
			Maze maze = generateRandomMaze(deadEndProbability);
			ReducedGraph reducedGraph = new ReducedGraph(maze.getStartCell());

			/*
			 * Generate an AIPlayer with default parameters.
			 */
			AIPlayer aiPlayer = new AIPlayer(
					maze.getEndCell(),
					maze.getStartCell(),
					maze.getCellSideLength()
							* GameConstants.AI_PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS,
					GameConstants.PLAYER_TOLERANCE_CONSTANT,
					GameConstants.PLAYER2_COLOR,
					GameConstants.PLAYER2_DEFAULT_NAME,
					GameConstants.PLAYER_PROPORTION_OF_CELL_DIMENSIONS, maze
							.getNumCheckpoints(), reducedGraph);

			/*
			 * Calls the calculatePercentageDropInWeightAfterTwoOpt method of
			 * aiPlayer to find the percentage drop in the weight of the greedy
			 * checkpoint route from before the twoOpt method is applied to
			 * after.
			 */
			double percentageDropInWeightAfterTwoOpt = aiPlayer
					.calculatePercentageDropInWeightAfterTwoOpt(
							reducedGraph.getVertex(maze.getEndCell()),
							reducedGraph.getVertex(maze.getStartCell()));

			/*
			 * If the greedy weight is greater than the random weight, we
			 * increment the relevant variable.
			 */
			if (percentageDropInWeightAfterTwoOpt < 0) {
				numCasesWhereWeightIncreasesAfterTwoOpt++;
			}

			/*
			 * We add the percentage drop to the
			 * sumOfPercentageDropsInWeightAfterTwoOpt variable so that we can
			 * find the average at the end of the for loop.
			 */
			sumOfPercentageDropsInWeightAfterTwoOpt += percentageDropInWeightAfterTwoOpt;
		}

		/*
		 * The average percentage drop in the weight of the greedy checkpoint
		 * route from before the twoOpt method is applied to after is found by
		 * dividing the sum of all the percentage drops by the number of test
		 * cases.
		 */
		double averagePercentageDropInWeightAfterTwoOpt = sumOfPercentageDropsInWeightAfterTwoOpt
				/ numTests;

		/*
		 * Output the results of the test.
		 */
		System.out
				.println("Number of cases where the weight increases after the twoOpt method is applied: "
						+ numCasesWhereWeightIncreasesAfterTwoOpt
						+ " / "
						+ numTests);
		System.out
				.println("Average percentage drop in weight after the twoOpt method is applied: "
						+ averagePercentageDropInWeightAfterTwoOpt);
	}

}
