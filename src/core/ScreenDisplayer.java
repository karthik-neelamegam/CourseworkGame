package core;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

public class ScreenDisplayer extends JPanel implements KeyListener {
	/*
	 * This class will update and display an object implementing the Screen
	 * interface. A screen is something (e.g. a main menu screen or a game
	 * screen) displayed using a window.
	 */

	/*
	 * This class extends JPanel (inheritance), which is a class in Java's Swing
	 * library. The paintComponent method is inherited and overriden
	 * (polymorphism), which handles the lower level aspects of drawing to a GUI
	 * panel. The addKeyListener method is also inherited, allowing key actions
	 * to be detected.
	 */

	/*
	 * This class also implements KeyListener, which is an interface in Java's
	 * AWT library. The keyPressed, keyReleased, and keyTyped abstract methods
	 * need to be implemented (polymorphism). These will need to contain code to
	 * be executed when key actions occur.
	 */

	/*
	 * The Screen object that is currently being updated and displayed. This is
	 * aggregation as the ScreenDisplayer class has a HAS-A relationship with
	 * the Screen interface but the currentScreen object will not be destroyed
	 * if the ScreenDisplayer object is destroyed.
	 */
	private Screen currentScreen;

	/*
	 * Whether the game is running. This will be true during the running of the
	 * game.
	 */
	private boolean running;

	/*
	 * The target number of game cycles per second. Used for consistent
	 * gameplay. This is calculated as the reciprocal of the
	 * desiredGameCycleFrequency parameter passed into the constructor.
	 */
	private final double desiredTimePerGameCycle;

	/*
	 * Constructor.
	 */
	public ScreenDisplayer(int desiredGameCycleFrequency) {
		/*
		 * The superclass's constructor must be called first.
		 */
		super();

		/*
		 * Allows the panel to be focused on (by clicking on it), after which
		 * key actions are listened for. This method is Inherited from JPanel.
		 */
		setFocusable(true);

		/*
		 * This method is inherited from JPanel and adds this ScreenDisplayer
		 * object (which implements KeyListener) as a KeyListener for the
		 * underlying JPanel (which this class extends), allowing the
		 * keyPressed, keyReleased, and keyTyped methods in this class to
		 * actually be called when key actions occur.
		 */
		addKeyListener(this);

		running = false;

		/*
		 * desiredGameLoopCycleFrequency refers to the number of game cycles per
		 * second. Therefore, to get the number of second per game cycle, we
		 * take the reciprocal of desiredGameCycleFrequency (i.e. 1 /
		 * desiredGameCycleFrequency). Because the methods used require time in
		 * nanoseconds, we multiply by 1e9 (i.e. 1,000,000,000) to convert
		 * seconds to nanoseconds.
		 */
		desiredTimePerGameCycle = 1e9 / desiredGameCycleFrequency;
	}

	/*
	 * Creates and starts a thread that runs the game loop.
	 */
	public void run() {
		Thread loop = new Thread() {
			public void run() {
				gameLoop();
			}
		};
		running = true;
		loop.start();
	}

	/*
	 * While the game is running, updates and displays currentScreen a fixed
	 * number of times a second, equal to the constructor parameter
	 * desiredGameLoopCycleFrequency, in order to keep the speed of the gameplay
	 * consistent regardless of how fast the user’s computer is (unless it is
	 * extremely slow, in which case the game would not be enjoyable anyway).
	 */
	private void gameLoop() {
		double previousCycleTime = System.nanoTime();
		while (running) {
			double currentTime = System.nanoTime();

			/*
			 * If the time difference between now and the previous cycle is
			 * greater than the desired time per game loop cycle, then that
			 * means the game is running behind schedule. To ensure that
			 * gameplay consistent, we need to update the game repeatedly
			 * (without rendering the game, to save time) until it is back on
			 * schedule, which is what this loop does.
			 */
			while (currentTime - previousCycleTime > desiredTimePerGameCycle) {
				/*
				 * If currentScreen is pointing to a null reference, then
				 * calling the update method would lead to a
				 * NullPointerException.
				 */
				if (currentScreen != null) {
					/*
					 * This is dynamic polymorphism as currentScreen can be an
					 * object of any class implementing the Screen interface and
					 * the update method can contain different code depending on
					 * which class is implementing the Screen interface.
					 */
					currentScreen.update();
				}
				previousCycleTime += desiredTimePerGameCycle;
			}

			/*
			 * Calls the paintComponent method, which renders CurrentScreen.
			 */
			repaint();

			/*
			 * If the time difference between now and the previous cycle is less
			 * than the desired time per game loop cycle, then that means the
			 * game is running faster than needed. To ensure that gameplay is
			 * consistent, this means that we should wait until it is time for
			 * the next cycle, allowing us to free up CPU resources while
			 * waiting. This is what this loop does.
			 */
			while (currentTime - previousCycleTime < desiredTimePerGameCycle) {

				/*
				 * A try-catch block is used because threads can be interrupted,
				 * leading to exceptions which can be handled. If this thread is
				 * interrupted, it doesn't matter much so exiting the program is
				 * not required and the program can still continue.
				 */
				try {
					/*
					 * Causes the game thread to sleep for a very short time (1
					 * millisecond) to free up CPU resources.
					 */
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				currentTime = System.nanoTime();
			}
		}
	}

	/*
	 * This method is inherited and overridden from a GUI panel superclass
	 * (polymorphism). The repaint method is used to call this method and render
	 * the screen.
	 */
	@Override
	protected void paintComponent(Graphics graphics) {

		/*
		 * Sets antialiasing on in order to make curved and diagonal lines look
		 * smoother. A Graphics2D cast is done because the regular Graphics
		 * class does not have the following setRenderingHint method.
		 */
		((Graphics2D) graphics).setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		/*
		 * Executes the code in the inherited method.
		 */
		super.paintComponent(graphics);
		/*
		 * If currentScreen is pointing to a null reference, then calling the
		 * render method would lead to a NullPointerException.
		 */
		if (currentScreen != null) {
			/*
			 * This is dynamic polymorphism as currentScreen can be an object of
			 * any class implementing the Screen interface and the render method
			 * can contain different code depending on which class is
			 * implementing the Screen interface.
			 */
			currentScreen.render(graphics);
		}
	}

	/*
	 * Methods from the KeyListener interface that need to be implemented
	 * (polymorphism). These methods contain the code to be executed when key
	 * actions occur.
	 */

	/*
	 * Called when a key is pressed. Executes code that the currentScreen object
	 * requires to be executed when a key, corresponding to the keyEvent
	 * argument, is pressed.
	 */
	@Override
	public void keyPressed(KeyEvent keyEvent) {
		/*
		 * If currentScreen is pointing to a null reference, then calling the
		 * keyPressed method would lead to a NullPointerException.
		 */
		if (currentScreen != null) {
			/*
			 * Executes code that the currentScreen object requires to be
			 * executed when a key, corresponding to the keyEvent argument, is
			 * pressed. This is dynamic polymorphism as currentScreen can be an
			 * object of any class implementing the Screen interface and the
			 * keyPressed method can contain different code depending on which
			 * class is implementing the Screen interface.
			 */
			currentScreen.keyPressed(keyEvent);
		}
	}

	/*
	 * Nothing needs to be done when a key is released.
	 */
	@Override
	public void keyReleased(KeyEvent keyEvent) {

	}

	/*
	 * Nothing needs to be done when a key is typed.
	 */
	@Override
	public void keyTyped(KeyEvent keyEvent) {

	}

	/*
	 * Setters.
	 */

	/*
	 * The Screen object being updated and displayed is changed by using this
	 * method. Allows transitions between the different screens in the game
	 * (e.g. changing from MainMenuScreen to GameScreen will involve calling the
	 * SetScreen method and passing GameScreen as a parameter).
	 */
	public void setScreen(Screen nextScreen) {
		currentScreen = nextScreen;
	}

}
