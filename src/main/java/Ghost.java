import javax.swing.ImageIcon;

public abstract class Ghost extends Creature {
	private ImageIcon img;
	private Strategy strategy;
	private String name;
	
	public Ghost(int xFocus, int yFocus) {
		super(xFocus, yFocus);
	}
	
	public ImageIcon getImage() {
		return img;
	}
	
	public void setImage(ImageIcon img) {
		this.img = img;
	}
	
	public Strategy getStrategy() {
		return strategy;
	}
	
	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
