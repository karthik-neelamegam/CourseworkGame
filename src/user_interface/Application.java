package user_interface;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Random;

import javax.swing.JFrame;

public class Application {
	public static Random rng;
	//private JFrame frame;
	public Application(double screenHeightFraction, double aspectRatio, int gameHz, long randomSeed) {
		rng = new Random(randomSeed);
		JFrame frame = new JFrame("CourseworkGame");
		ScreenDisplayer screenDisplayer = new ScreenDisplayer(gameHz);
		Screen menu = new MainMenuScreen(screenDisplayer);
		screenDisplayer.setScreen(menu);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenHeight = screenSize.getHeight();
		int windowHeight = (int)(screenHeight*screenHeightFraction);
		int windowWidth = (int)(windowHeight*aspectRatio);
		frame.add(screenDisplayer);
		frame.pack();
		frame.setSize(windowWidth, windowHeight);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		screenDisplayer.run();
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new Application(0.75, 1, 60, 1);
	}

}
