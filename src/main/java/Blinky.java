import javax.swing.ImageIcon;

public class Blinky extends Ghost {
	public Blinky(int xFocus, int yFocus) {
		super(xFocus, yFocus);

		setImage(new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/redGhost.png"));
		setStrategy(new BlinkyStrategy());
		setName("Blinky");
	}
}
