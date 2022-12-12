import javax.swing.ImageIcon;

public class Inky extends Ghost {
	public Inky(int xFocus, int yFocus) {
		super(xFocus, yFocus);

		setImage(new ImageIcon(System.getProperty("user.dir") + "/src/main/resources/inkGhost.png"));
		setStrategy(new BlinkyStrategy());
		setName("Inky");
	}
}
