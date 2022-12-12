import javax.swing.ImageIcon;

public class Clyde extends Ghost {
	public Clyde(int xFocus, int yFocus) {
		super(xFocus, yFocus);

		setImage(new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/orangeGhost.png"));
		setStrategy(new BlinkyStrategy());
		setName("Clyde");
	}
}
