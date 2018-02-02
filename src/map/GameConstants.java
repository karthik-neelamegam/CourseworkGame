package map;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class GameConstants {
	
	public static final Color PLAYER1_COLOR = Color.GREEN;
	public static final Color PLAYER2_COLOR = Color.RED;
	public static final int PLAYER1_UP = KeyEvent.VK_W;
	public static final int PLAYER1_DOWN = KeyEvent.VK_S;
	public static final int PLAYER1_LEFT = KeyEvent.VK_A;
	public static final int PLAYER1_RIGHT = KeyEvent.VK_D;
	public static final int PLAYER2_UP = KeyEvent.VK_UP;
	public static final int PLAYER2_DOWN = KeyEvent.VK_DOWN;
	public static final int PLAYER2_LEFT = KeyEvent.VK_LEFT;
	public static final int PLAYER2_RIGHT = KeyEvent.VK_RIGHT;
	public static final double PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS = 0.05;
	public static final double PLAYER_PROPORTION_OF_CELL_DIMENSIONS = 7d/8d;
	public static final String PLAYER1_DEFAULT_NAME = "GREEN";
	public static final String PLAYER2_DEFAULT_NAME = "RED";

	public static final Color CHECKPOINT_COLOR = Color.YELLOW;
	public static final double CHECKPOINT_PROPORTION_OF_CELL_DIMENSIONS = 7d/8d;
	public static final Color GROUND_COLOR = Color.WHITE;
	public static final Color WALL_COLOR = Color.BLACK;
	public static final double WALL_PROPORTION_OF_CELL_DIMENSIONS = 0.1;
	public static final Color SLOW_SURFACE_COLOR = new Color(165, 42, 42, 127);
	public static final Color NORMAL_SURFACE_COLOR = GROUND_COLOR;
	public static final Color FAST_SURFACE_COLOR = new Color(0, 255, 255, 127);
}
