package map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

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
	public static final double AI_PLAYER_BASE_VELOCITY_PROPORTION_OF_CELL_DIMENSIONS = 0.04;

	public static final double PLAYER_TOLERANCE_CONSTANT = 0.02;
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
	
	public static final Color MENU_TEXT_COLOR = Color.WHITE;
	public static final String MENU_TEXT_FONT_TYPE = Font.SANS_SERIF;
	public static final int MENU_TEXT_FONT_STYLE = Font.PLAIN;
	public static final double MENU_TEXT_FONT_PROPORTION_OF_SCREEN_HEIGHT = 0.03;
	public static Font getMenuFont(int screenDisplayerHeight) {
		return new Font(MENU_TEXT_FONT_TYPE, MENU_TEXT_FONT_STYLE, (int)(screenDisplayerHeight*MENU_TEXT_FONT_PROPORTION_OF_SCREEN_HEIGHT));
	}

	public static final Color TITLE_TEXT_COLOR = Color.YELLOW;
	public static final String TITLE_TEXT_FONT_TYPE = Font.SERIF;
	public static final int TITLE_TEXT_FONT_STYLE = Font.ITALIC;
	public static final double TITLE_TEXT_FONT_PROPORTION_OF_SCREEN_HEIGHT = 0.07;
	public static Font getTitleFont(int screenDisplayerHeight) {
		return new Font(TITLE_TEXT_FONT_TYPE, TITLE_TEXT_FONT_STYLE, (int)(screenDisplayerHeight*TITLE_TEXT_FONT_PROPORTION_OF_SCREEN_HEIGHT));
	}

	public static final Image MENU_BACKGROUND_IMAGE = getMenuBackgroundImage();
	private static Image getMenuBackgroundImage() {
		Image menuBackgroundImage = null;
		try {
			menuBackgroundImage = ImageIO.read(new File("Background.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return menuBackgroundImage;
	}
	
	public static final Color GAME_OVERLAY_COLOR = new Color(0, 0, 0, 0.7f);
}
