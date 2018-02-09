package user_interface;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public interface Screen {
	public void update();
	public void render(Graphics g);
	public void keyPressed(KeyEvent e);
}
