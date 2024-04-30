import java.awt.*;

public class Inky extends Ghost {
	public Inky(int xFocus, int yFocus) {
		super(xFocus, yFocus);

		setImage(new SerializableImage(Toolkit.getDefaultToolkit().getImage(System.getProperty("user.dir") + "/src/main/resources/inkGhost.png")));
		setStrategy(new BlinkyStrategy());
		setName("Inky");
	}
}
