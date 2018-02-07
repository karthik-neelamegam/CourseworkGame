package user_interface;
import java.awt.Graphics;
import java.awt.event.KeyListener;

public interface Screen extends KeyListener{
	public void update();
	public void render(Graphics g);
}
