import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Timer;
import java.util.TimerTask;

public class PacmanGame implements Const {
	private static JFrame window;
	private static JButton start, load, editor, exit;
	
	private static BufferedImage background     = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_RGB),
								 picture        = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_RGB),
	                             pacmanMouthOn  = new BufferedImage(2 * CELL_SIZE, 2 * CELL_SIZE, BufferedImage.TYPE_INT_BGR),
	                             pacmanMouthOff = new BufferedImage(2 * CELL_SIZE, 2 * CELL_SIZE, BufferedImage.TYPE_INT_BGR),
	                             life           = new BufferedImage(IND_W, IND_H, BufferedImage.TYPE_INT_RGB);
	
	private static Graphics imgBackground  = background.getGraphics(),
	                        imgPicture     = picture.getGraphics(),
	                        imgPacMouthOn  = pacmanMouthOn.getGraphics(),
	                        imgPacMouthOff = pacmanMouthOff.getGraphics(),
	                        imgLife		   = life.getGraphics();
	
	private PacmanCanvas        canvasPanel   = new PacmanCanvas();
	private PacmanLifeIndicator lifeIndicator = new PacmanLifeIndicator();
	
	// From file
	private static Level level;
	
	// From level
	private static int[][]          mapOfWall;
	private static CellOfRoute[][]  mapOfRoute;
	private static Creature[]       creature;    // Pacman and ghosts
	
	// Creatures
	private static Pacman pacman;
	private static Ghost  blinky, pinky, inky, clyde;

	// Start state of creatures for new round
	private static Point pacmanStartFocus, pacmanStartCorner,
	                     blinkyStartFocus, blinkyStartCorner,
	                     pinkyStartFocus,  pinkyStartCorner,
						 inkyStartFocus,   inkyStartCorner,
						 clydeStartFocus,  clydeStartCorner;
	
	
	private static Timer pacmanTimer = new Timer(),
					     blinkyTimer = new Timer(),
					     pinkyTimer  = new Timer(),
					     inkyTimer   = new Timer(),
					     clydeTimer  = new Timer();
	
	
	private static TimerTask pacmanMotionTask,
							 blinkyMotionTask,
							 pinkyMotionTask,
							 inkyMotionTask,
							 clydeMotionTask;
	
	
	private static GhostLauncher ghostLauncher;
	
	
	private static int countOfFood, countOfBigFood, countOfPacmanLife;
	

	private static Direction currentDirection = Direction.NONE,  // Current actual pacman's direction
							 newDirection     = Direction.NONE,  // New pacman's direction
							 nextDirection    = Direction.NONE;  // Next direction for turn
							 
	
	private static boolean mouthOn       = true,  // Pacman's mouth state
	                       pacmanMotion  = false; // State of pacman's moving
	
	
	private static boolean pinkyAtHome = true,
	                       inkyAtHome  = true,
	                       clydeAtHome = true;
	
	// Coordinates of portal cell's
	private static int xPortal1, yPortal1,
					   xPortal2, yPortal2;
	
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		
		new PacmanGame();
	}
	
	PacmanGame() {
		window = new JFrame("Pacman");
		window.setSize(IMG_W + 90, IMG_H + 25);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setLocation();
		
		window.getContentPane().add(initContent());
		window.setVisible(true);
	}
	
	
	public void setLocation() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation((int)(dim.width / 2.5), dim.height / 3);
	}
	
	
	public JPanel initContent() {
		JPanel main = new JPanel(new BorderLayout());
		
		Box buttons = Box.createVerticalBox();
		
		start = new JButton("Start game");
		start.setFocusable(false);
		start.addActionListener(new ButtonListener());
		buttons.add(start);
		
		editor = new JButton("Level editor");
		editor.setFocusable(false);
		editor.addActionListener(new ButtonListener());
		buttons.add(editor);
		
		load = new JButton("Load level");
		load.setFocusable(false);
		load.addActionListener(new ButtonListener());
		buttons.add(load);
		
		exit = new JButton("Exit");
		
		exit.setFocusable(false);
		exit.addActionListener(new ButtonListener());
		buttons.add(exit);
		
		buttons.add(Box.createVerticalStrut(165));
		lifeIndicator.setBorder(BorderFactory.createTitledBorder(""));
		buttons.add(lifeIndicator);

		canvasPanel.setBorder(new TitledBorder(""));
		canvasPanel.setFocusable(true);
		canvasPanel.addKeyListener(new PacmanKeyListener());
		
		// Clear imgBackground
		imgBackground.setColor(Color.LIGHT_GRAY);
		imgBackground.fillRect(0, 0, IMG_W, IMG_H);
		
		main.add(canvasPanel, BorderLayout.CENTER);
		main.add(buttons, BorderLayout.EAST);
		
		return main;
	}
	
	
	class PacmanCanvas extends JComponent {
		public void update(Graphics g) {
			paint(g);
		}
		
		public void paint(Graphics g) {
			// Drawing background on picture 
			imgPicture.drawImage(background, 0, 0, null);
			
			// TODO: Delete this line after append all ghosts
			if(creature == null) return;
			
			// Drawing creatures
			for(int i = 0; i < creature.length; i++) 
				drawCreature(creature[i]);
			
			BufferedImage subImg = picture.getSubimage(CELL_SIZE - 2, CELL_SIZE - 2, IMG_W - 2 * CELL_SIZE + 4, IMG_H - 2 * CELL_SIZE + 4);
				
			g.drawImage(subImg, 0, 0, subImg.getWidth(), subImg.getHeight(), null);
		}
	}
	
	
	private void fillOutLifeIndicator() {
		imgLife.setColor(pacmanCellColor);
		
		switch(countOfPacmanLife) {
			case 3:
				imgLife.fillArc(1 + 4 * CELL_SIZE, 1, 2 * CELL_SIZE, 2 * CELL_SIZE, 225, 270);
				imgLife.fillArc(1 + 2 * CELL_SIZE, 1, 2 * CELL_SIZE, 2 * CELL_SIZE, 225, 270);
				imgLife.fillArc(1, 1, 2 * CELL_SIZE, 2 * CELL_SIZE, 225, 270);
			break;
		
			case 2:
				imgLife.fillArc(1 + 4 * CELL_SIZE, 1, 2 * CELL_SIZE, 2 * CELL_SIZE, 225, 270);
				imgLife.fillArc(1 + 2 * CELL_SIZE, 1, 2 * CELL_SIZE, 2 * CELL_SIZE, 225, 270);
			break;
			
			case 1:
				imgLife.fillArc(1 + 4 * CELL_SIZE, 1, 2 * CELL_SIZE, 2 * CELL_SIZE, 225, 270);
			break;
			
			case 0:
				imgLife.setColor(window.getBackground());
				imgLife.fillRect(0, 0, IND_W, IND_H);
			break;
		}
	}
	
	
	class PacmanLifeIndicator extends JComponent {
		public void paintComponent(Graphics g) {
			imgLife.setColor(window.getBackground());
			imgLife.fillRect(0, 0, IND_W, IND_H);
			
			fillOutLifeIndicator();
			
			g.drawImage(life, 0, 0, null);
		}
	}
	
	
	public void loadLevel(File from) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(from)));
			level = (Level) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void parseWallState(int wall[][], int y, int x) {
		switch(wall[y][x]) {
			case 1: imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE); break;
			case 2: imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE / 3); break;
			case 3: imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE); break;
			case 4: imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3, CELL_SIZE, CELL_SIZE / 3); break;
			case 5: imgBackground.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE); break;
			
			case 6:
				imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE , CELL_SIZE / 3);
				imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE);
			break;
			
			case 7:
				imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE);
				imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3, CELL_SIZE, CELL_SIZE / 3);
			break;
			
			case 8:
				imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3, CELL_SIZE, CELL_SIZE / 3);
				imgBackground.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE);
			break;
			
			case 9:
				imgBackground.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE);
				imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE / 3);
			break;
	
			case 10: 
				imgBackground.setColor(flapCellColor);
				imgBackground.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE / 3);
				imgBackground.setColor(wallCellColor);
			break;
		}
	}

	
	public void initStartingLevelState() {
		countOfPacmanLife = 3;
		
		mapOfWall   = level.getMapOfWall();
		mapOfRoute  = level.getMapOfRoute();
		creature    = level.getCreature();

		// Walls
		imgBackground.setColor(wallCellColor);
		
		for(int y = 0; y < MAP_H; y++) 
			for(int x = 0; x < MAP_W; x++) 
				parseWallState(mapOfWall, y, x);

		// Routes
		imgBackground.setColor(foodCellColor);
		
		for(int y = 0; y < MAP_H - 1; y++) {
			for(int x = 0; x < MAP_W - 1; x++) {
				CellOfRoute cell = mapOfRoute[y][x];
				
				switch(cell.getType()) {
					case 1: imgBackground.fillOval((int) ((x+1) * DX) - 2, (int)((y+1) * DY) - 2, 4, 4); countOfFood++;    break;
					case 2: imgBackground.fillOval((int) ((x+1) * DX) - 4, (int)((y+1) * DY) - 4, 8, 8); countOfBigFood++; break;

					case 3: 
						if(xPortal1 == 0) {    // First portal 
							xPortal1 = x;
							yPortal1 = y;
						} else {			   // Second portal
							xPortal2 = x;
							yPortal2 = y;
						}
					break;
				}
			}
		}

		// Initializing creatures
		for(int i = 0; i < creature.length; i++) {
			if(creature[i] instanceof Pacman) {
				pacman = (Pacman) creature[i];
				
				pacmanStartFocus  = new Point(pacman.getXFocus(), pacman.getYFocus());
				pacmanStartCorner = new Point(pacman.getXCorner(), pacman.getYCorner());
			}
			
			if(creature[i] instanceof Blinky) {
				blinky = (Blinky) creature[i]; 
				
				blinkyStartFocus  = new Point(blinky.getXFocus(), blinky.getYFocus());
				blinkyStartCorner = new Point(blinky.getXCorner(), blinky.getYCorner());
				
				blinky.getStrategy().setWaveMap(mapOfRoute);
			}
			
			if(creature[i] instanceof Pinky) {
				pinky = (Pinky) creature[i];
				
				pinkyStartFocus  = new Point(pinky.getXFocus(), pinky.getYFocus());
				pinkyStartCorner = new Point(pinky.getXCorner(), pinky.getYCorner()); 
				
				pinky.getStrategy().setWaveMap(mapOfRoute);
			}
			
			if(creature[i] instanceof Inky) {
				inky = (Inky) creature[i];

				inkyStartFocus  = new Point(inky.getXFocus(), inky.getYFocus());
				inkyStartCorner = new Point(inky.getXCorner(), inky.getYCorner());

				inky.getStrategy().setWaveMap(mapOfRoute);
			}
			
			if(creature[i] instanceof Clyde) {
				clyde = (Clyde) creature[i];

				clydeStartFocus  = new Point(clyde.getXFocus(), clyde.getYFocus());
				clydeStartCorner = new Point(clyde.getXCorner(), clyde.getYCorner());

				clyde.getStrategy().setWaveMap(mapOfRoute);
			}
		}
		
		// Draw background on picture
		imgPicture.drawImage(background, 0, 0, null);
		
		// Drawing creatures
		for(int i = 0; i < creature.length; i++) 
			drawCreature(creature[i]);
		
		lifeIndicator.repaint();
		canvasPanel.repaint();
	}
	

	public void startGame() {
		ghostLauncher = new GhostLauncher();
		ghostLauncher.start();
		
		pacmanMotion = true;
		
		pacmanTimer.schedule(pacmanMotionTask = new PacmanMotion(), 0, SPEED_OF_PACMAN);

		pinky.setDirection(Direction.DOWN); // TODO: Add it in Editor
		pinkyTimer.schedule(pinkyMotionTask = new HomeMotion(pinky), 0, SPEED_OF_PACMAN);
		
		inky.setDirection(Direction.DOWN);
		inkyTimer.schedule(inkyMotionTask = new HomeMotion(inky), 0, SPEED_OF_PACMAN);
		
		clyde.setDirection(Direction.DOWN);
		clydeTimer.schedule(clydeMotionTask = new HomeMotion(clyde), 0, SPEED_OF_PACMAN);
	}
	
	
	public void preparePictureForCreature(Creature creature) {
		if(creature.getDirection() == null) return; // For correct work with Strategy
		
		moveCreature(creature);
		
		updateCornerCoords(creature);
		
		if(creature instanceof Pacman) {
			checkAccessOfNextDirection();
			pacmanEating();
		}
		
		checkNextStep(creature);
		
		canvasPanel.repaint();
	}
	
	
	
	// TODO: Complete method
	public void moveGhostAtHome(Ghost ghost) {
		moveCreature(ghost);
		updateCornerCoords(ghost);
		
		int x = ghost.getXFocus() / 5 + 1,
		    y = ghost.getYFocus() / 5;
		
		switch(ghost.getDirection()) {
			case DOWN:
			case UP:
				y += 1;
				break;
		}
		
		if(mapOfWall[y][x] == 4) { // Downside wall
			ghost.setDirection(Direction.UP);   
			return;
		}
		
		//TODO: Think about it
		if(mapOfWall[y][x] == 2 || mapOfWall[y][x] == 10) { // Upside wall or flap
			ghost.setDirection(Direction.DOWN);
			return; 
		}
	}
	
	
	// Move creature's focus
	public void moveCreature(Creature creature) {
		switch(creature.getDirection()) {
			case LEFT:  creature.setXFocus(creature.getXFocus() - INCREMENT_OF_MOTION); break;
			case RIGHT: creature.setXFocus(creature.getXFocus() + INCREMENT_OF_MOTION); break;
			case UP:    creature.setYFocus(creature.getYFocus() - INCREMENT_OF_MOTION); break;
			case DOWN:  creature.setYFocus(creature.getYFocus() + INCREMENT_OF_MOTION); break;
		}
	}
	
	
	// Draw creature using by xCorner and yCorner coordinates
	public void drawCreature(Creature creature) {
		if(creature instanceof Pacman) {
			setMouthImage();
			
			if(mouthOn == true) {
				imgPicture.drawImage(pacmanMouthOn, pacman.getXCorner() * 2 + CELL_SIZE, pacman.getYCorner() * 2 + CELL_SIZE, null);
				mouthOn = false;
				return;
			}
			
			if(mouthOn == false) {
				imgPicture.drawImage(pacmanMouthOff, pacman.getXCorner() * 2 + CELL_SIZE, pacman.getYCorner() * 2 + CELL_SIZE, null);
				mouthOn = true;
				return;
			}
		}
		
		if(creature instanceof Ghost) {
			if(creature instanceof Blinky) {
				imgPicture.drawImage( ((Ghost)(creature)).getImage().getImage(), 
						                       creature.getXCorner() * 2 + CELL_SIZE,
						   					   creature.getYCorner() * 2 + CELL_SIZE, 
						   					   2 * CELL_SIZE, 
						   					   2 * CELL_SIZE, 
						   					   null);
			}
			
			if(creature instanceof Pinky) {
				if(pinkyAtHome) {
					imgPicture.drawImage( ((Ghost)(creature)).getImage().getImage(),
												   creature.getXCorner() * 2 + CELL_SIZE + 5,
            				                       creature.getYCorner() * 2 + CELL_SIZE, 2 * CELL_SIZE, 2 * CELL_SIZE, null);
				} else {
					imgPicture.drawImage( ((Ghost)(creature)).getImage().getImage(),
		                   				           creature.getXCorner() * 2 + CELL_SIZE,
		                   				           creature.getYCorner() * 2 + CELL_SIZE, 2 * CELL_SIZE, 2 * CELL_SIZE, null);
				}
			}

			if(creature instanceof Inky) {
				if(inkyAtHome) {
					imgPicture.drawImage( ((Ghost)(creature)).getImage().getImage(),
							creature.getXCorner() * 2 + CELL_SIZE + 5,
							creature.getYCorner() * 2 + CELL_SIZE, 2 * CELL_SIZE, 2 * CELL_SIZE, null);
				} else {
					imgPicture.drawImage( ((Ghost)(creature)).getImage().getImage(),
							creature.getXCorner() * 2 + CELL_SIZE,
							creature.getYCorner() * 2 + CELL_SIZE, 2 * CELL_SIZE, 2 * CELL_SIZE, null);
				}
			}

			if(creature instanceof Clyde) {
				if(clydeAtHome) {
					imgPicture.drawImage( ((Ghost)(creature)).getImage().getImage(),
							creature.getXCorner() * 2 + CELL_SIZE + 5,
							creature.getYCorner() * 2 + CELL_SIZE, 2 * CELL_SIZE, 2 * CELL_SIZE, null);
				} else {
					imgPicture.drawImage( ((Ghost)(creature)).getImage().getImage(),
							creature.getXCorner() * 2 + CELL_SIZE,
							creature.getYCorner() * 2 + CELL_SIZE, 2 * CELL_SIZE, 2 * CELL_SIZE, null);
				}
			}
		}
	}
	
	
	public void updateCornerCoords(Creature creature) {
		if(creature.getDirection().equals(Direction.LEFT) || creature.getDirection().equals(Direction.RIGHT)) 
			creature.setXCorner(creature.getXFocus() - CELL_SIZE / 2);
		
		if(creature.getDirection().equals(Direction.UP) || creature.getDirection().equals(Direction.DOWN))
			creature.setYCorner(creature.getYFocus() - CELL_SIZE / 2);
	}

	
	public void setMouthImage() {
		imgPacMouthOn.setColor(Color.LIGHT_GRAY);
		imgPacMouthOn.fillRect(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE);
		imgPacMouthOn.setColor(pacmanCellColor);
		
		imgPacMouthOff.setColor(Color.LIGHT_GRAY);
		imgPacMouthOff.fillRect(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE);
		imgPacMouthOff.setColor(pacmanCellColor);
		
		switch(pacman.getDirection()) {
			case NONE:
				imgPacMouthOn.fillOval(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE);
				imgPacMouthOff.fillOval(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE);
			break;
		
			case LEFT:
				imgPacMouthOn.fillArc(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE, 225, 270);
				imgPacMouthOff.fillArc(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE, 180, 360);
			break;
			
			case UP: 
				imgPacMouthOn.fillArc(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE, 135, 270);
				imgPacMouthOff.fillArc(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE, 90, 360);
			break;
			
			case RIGHT: 
				imgPacMouthOn.fillArc(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE, 45, 270);
				imgPacMouthOff.fillArc(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE, 0, 360);
			break;
			
			case DOWN: 
				imgPacMouthOn.fillArc(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE, 315, 270);
				imgPacMouthOff.fillArc(0, 0, 2 * CELL_SIZE, 2 * CELL_SIZE, 270, 360);
			break;
		}
	}
	
	
	public void checkNextStep(Creature creature) {
		int x = creature.getXFocus() / 5,
		    y = creature.getYFocus() / 5;
		
		switch(creature.getDirection()) {
			case LEFT:  x = (creature.getXFocus() - INCREMENT_OF_MOTION) / 5; break;
			case RIGHT: x = (creature.getXFocus() + CELL_SIZE / 2)       / 5; break;
			case UP:    y = (creature.getYFocus() - INCREMENT_OF_MOTION) / 5; break;
			case DOWN:  y = (creature.getYFocus() + CELL_SIZE / 2)       / 5; break;
		}
		
		// TODO: creature.setYFocus(yPortal2 * 5);   -   (???)
		// Think about y coordinate
		
		// It's portal cell
		if(mapOfRoute[y][x].getType() == 3) {
			if(x == xPortal1) creature.setXFocus(xPortal2 * 5);
			if(x == xPortal2) creature.setXFocus(xPortal1 * 5);
		}
		
		if(mapOfRoute[y][x].getAccess() == false) {
			if(creature instanceof Pacman) {
				pacmanMotion = false;
				//pacmanTimer.cancel();
				pacmanMotionTask.cancel();
			}
		}
		
		// Set correct coordinates for Strategy
		switch(creature.getDirection()) {
			case LEFT:  x += 1; break;
			case RIGHT: x -= 1; break;
			case UP:    y += 1; break;
			case DOWN:  y -= 1; break;
		}
		
		// Set new direction from Strategy
		if(creature instanceof Ghost) {
			Direction dir = (((Ghost) creature).getStrategy()).pointsOfTurn.get(new Point(x, y));
			
			if(dir != null) {
				creature.setDirection(dir);
				(((Ghost) creature).getStrategy()).pointsOfTurn.remove(new Point(x, y));
			}
		}
	}

	public boolean checkBorderLines(Creature creature, Direction dir) {
		// Checking wrong creature's position (for example, passing through wall in special case)
		if(creature.getXFocus() % 5 != 0 || creature.getYFocus() % 5 != 0) return false;
		
		int x = creature.getXFocus() / 5,
		    y = creature.getYFocus() / 5;

		switch(dir) {
			case UP:    y--; break;
			case DOWN:  y++; break;
			case LEFT:  x--; break;
			case RIGHT: x++; break;
		}

		if(mapOfRoute[y][x].getAccess() == false) return false;
		
		return true;
	}
	
	
	// Can we set nextDirection ? (For pacman)
	public void checkAccessOfNextDirection() {
		if(nextDirection.equals(Direction.NONE)) return;
		
		int x = pacman.getXFocus() / 5,
			y = pacman.getYFocus() / 5;
		
		switch(nextDirection) {
			case UP:    y--; break;
			case DOWN:  y++; break;
			case LEFT:  x--; break;
			case RIGHT: x++; break;
		}

		if(currentDirection.equals(Direction.RIGHT) || currentDirection.equals(Direction.LEFT)) {
			if(pacman.getXFocus() % 5 == 0 && mapOfRoute[y][x].getAccess() == true) {
				pacman.setDirection(nextDirection);
				currentDirection = nextDirection;
				nextDirection = Direction.NONE;
				return;
			}
		}
		
		if(currentDirection.equals(Direction.UP) || currentDirection.equals(Direction.DOWN)) {
			if(pacman.getYFocus() % 5 == 0 && mapOfRoute[y][x].getAccess() == true) {
				pacman.setDirection(nextDirection);
				currentDirection = nextDirection;
				nextDirection = Direction.NONE;
				return;
			}
		}
	}
	
	
	public void pacmanEating() {
		int x = pacman.getXFocus() / 5,
	    	y = pacman.getYFocus() / 5;
		
		// Skip eating this cell when pacman passing through portal cell (for correct picture)
		if(mapOfRoute[y][x].getType() == 3) return;
		
		if(pacman.getXFocus() % 5 == 0 || pacman.getYFocus() % 5 == 0) {
			if(mapOfRoute[y][x].getType() == 0) return;
			
			imgBackground.setColor(Color.LIGHT_GRAY);
			imgBackground.fillOval((int) ((x+1) * DX) - 4, (int)((y+1) * DY) - 4, 8, 8);

			switch(mapOfRoute[y][x].getType()) {
				case 1: mapOfRoute[y][x].setType(0); countOfFood--;    break;
				case 2:	mapOfRoute[y][x].setType(0); countOfBigFood--; break;
			}
		}
	}
	
	
	class ButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == start)
			{
				startGame();
				start.setEnabled(false);
			}
			
			if(e.getSource() == load)
			{
				// TODO: (!)
				JFileChooser loadMap = new JFileChooser();

				if(loadMap.showOpenDialog(window) == JFileChooser.APPROVE_OPTION)
				{
					loadLevel(loadMap.getSelectedFile());
					initStartingLevelState();
				}
			}
			
			if(e.getSource() == editor) new LevelEditor();

			if(e.getSource() == exit) System.exit(0);
		}
	}
	

	class PacmanKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if(start.isEnabled()) return;
			
			if(e.getKeyChar() == 'w' || e.getKeyCode() == KeyEvent.VK_UP)    newDirection = Direction.UP;
			if(e.getKeyChar() == 'a' || e.getKeyCode() == KeyEvent.VK_LEFT)  newDirection = Direction.LEFT;
			if(e.getKeyChar() == 's' || e.getKeyCode() == KeyEvent.VK_DOWN)  newDirection = Direction.DOWN;
			if(e.getKeyChar() == 'd' || e.getKeyCode() == KeyEvent.VK_RIGHT) newDirection = Direction.RIGHT;
			
			if(! checkBorderLines(pacman, newDirection)) {
				nextDirection = newDirection;
				return;
			}
			
			pacman.setDirection(newDirection);
			
			currentDirection = pacman.getDirection();
			
			if(pacmanMotion == false) {
				pacmanTimer.schedule(pacmanMotionTask = new PacmanMotion(), 0, SPEED_OF_PACMAN);
				pacmanMotion = true;
			}
		}
	}
	
	// TODO: Complete
	private void removePacmanLife(Ghost ghost) {
		countOfPacmanLife--;
		lifeIndicator.repaint();

		// Kill motion tasks
		blinkyTimer.cancel(); blinkyTimer.purge();
		pinkyTimer.cancel(); pinkyTimer.purge();
		inkyTimer.cancel(); inkyTimer.purge();
		clydeTimer.cancel(); clydeTimer.purge();

		ghostLauncher.stop();

		// TODO: Restore for new game. Button 'Start'
		if(checkLose()) {
			//
			//
			//
			return;
		}


		// Restoring to start state for new round
		pacman.setXFocus(pacmanStartFocus.x);
		pacman.setYFocus(pacmanStartFocus.y);
		pacman.setXCorner(pacmanStartCorner.x);
		pacman.setYCorner(pacmanStartCorner.y);
		pacman.setDirection(Direction.NONE);

		blinky.setXFocus(blinkyStartFocus.x);
		blinky.setYFocus(blinkyStartFocus.y);
		blinky.setXCorner(blinkyStartCorner.x);
		blinky.setYCorner(blinkyStartCorner.y);
		blinky.getStrategy().clearVariables();
		blinky.getStrategy().execution = false;
		blinky.setDirection(Direction.NONE);
		blinkyTimer = new Timer();

		pinky.setXFocus(pinkyStartFocus.x);
		pinky.setYFocus(pinkyStartFocus.y);
		pinky.setXCorner(pinkyStartCorner.x);
		pinky.setYCorner(pinkyStartCorner.y);
		pinky.getStrategy().clearVariables();
		pinky.getStrategy().execution = false;
		pinkyAtHome = true;
		pinky.setDirection(Direction.DOWN);
		pinkyTimer = new Timer();
		pinkyTimer.schedule(pinkyMotionTask = new HomeMotion(pinky), 0, SPEED_OF_PACMAN);

		inky.setXFocus(inkyStartFocus.x);
		inky.setYFocus(inkyStartFocus.y);
		inky.setXCorner(inkyStartCorner.x);
		inky.setYCorner(inkyStartCorner.y);
		inky.getStrategy().clearVariables();
		inky.getStrategy().execution = false;
		inkyAtHome = true;
		inky.setDirection(Direction.DOWN);
		inkyTimer = new Timer();
		inkyTimer.schedule(inkyMotionTask = new HomeMotion(inky), 0, SPEED_OF_PACMAN);

		clyde.setXFocus(clydeStartFocus.x);
		clyde.setYFocus(clydeStartFocus.y);
		clyde.setXCorner(clydeStartCorner.x);
		clyde.setYCorner(clydeStartCorner.y);
		clyde.getStrategy().clearVariables();
		clyde.getStrategy().execution = false;
		clydeAtHome = true;
		clyde.setDirection(Direction.DOWN);
		clydeTimer = new Timer();
		clydeTimer.schedule(clydeMotionTask = new HomeMotion(clyde), 0, SPEED_OF_PACMAN);

		canvasPanel.repaint();

		String message = "You have been eaten by " + ghost.getName() + " !"+ "\nPrepare for new round !";
		JOptionPane.showMessageDialog(window, message, "Oops !", JOptionPane.INFORMATION_MESSAGE);

		ghostLauncher = new GhostLauncher();
		ghostLauncher.start();
	}
	

	private void checkPacmanCatch(Ghost ghost)
	{
		boolean caught = false;

		// Skip check when pacman passing through portal cell
		if(mapOfRoute[pacman.getYFocus() / 5][pacman.getXFocus() / 5].getType() == 3) return;

		// From above
		if(ghost.getXFocus() == pacman.getXFocus() && ghost.getYFocus() >= pacman.getYCorner() && ghost.getYFocus() <= pacman.getYFocus())
			caught = true;
		// From left
		if(ghost.getYFocus() == pacman.getYFocus() && ghost.getXFocus() >= pacman.getXCorner() && ghost.getXFocus() <= pacman.getXFocus())
			caught = true;
		// From down
		if(ghost.getXFocus() == pacman.getXFocus() && ghost.getYFocus() >= pacman.getYFocus() && ghost.getYFocus() <= pacman.getYCorner() + CELL_SIZE)
			caught = true;
		// From right
		if(ghost.getYFocus() == pacman.getYFocus() && ghost.getXFocus() >= pacman.getXFocus() && ghost.getXFocus() <= pacman.getXCorner() + CELL_SIZE)
			caught = true;
		
		if(caught == true) removePacmanLife(ghost);
	}
	
	
	private void checkWin() {
		if(countOfFood == 0 && countOfBigFood == 0) {
			//TODO: Think about it
			pacmanMotionTask.cancel();
			blinkyMotionTask.cancel();
			
			JOptionPane.showMessageDialog(window, "You won !\nNext level.", "Winner !", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	
	private boolean checkLose() {
		if(countOfPacmanLife == 0) { 
			JOptionPane.showMessageDialog(window, "Game over.", "Game over.", JOptionPane.INFORMATION_MESSAGE);
			
			// TODO: Continue
			// Continue with pacman state
			blinkyTimer.cancel();
			pinkyTimer.cancel();
			inkyTimer.cancel();
			clydeTimer.cancel();
			//3.
			
			return true;
		}
		
		return false;
	}
	
	
	class PacmanMotion extends TimerTask
	{
		public void run()
		{
			preparePictureForCreature(pacman);
			checkWin();
		}
	}
	
	
	class BlinkyMotion extends TimerTask
	{
		public void run()
		{
			preparePictureForCreature(blinky);
			checkGhostStrategy(blinky);
			checkPacmanCatch(blinky);
			updateStrategy(blinky);
		}
	}


	class PinkyMotion extends TimerTask
	{
		public void run()
		{
			preparePictureForCreature(pinky);
			checkGhostStrategy(pinky);
			checkPacmanCatch(pinky);
			updateStrategy(pinky);
		}
	}

	class InkyMotion extends TimerTask
	{
		public void run()
		{
			preparePictureForCreature(inky);
			checkGhostStrategy(inky);
			checkPacmanCatch(inky);
			updateStrategy(inky);
		}
	}

	class ClydeMotion extends TimerTask
	{
		public void run()
		{
			preparePictureForCreature(clyde);
			checkGhostStrategy(clyde);
			checkPacmanCatch(clyde);
			updateStrategy(clyde);
		}
	}
	
	
	// Motion of ghost at home
	class HomeMotion extends TimerTask  {
		private Ghost ghost;
		
		public HomeMotion(Ghost g) {
			ghost = g;
		}
		
		public void run() {
			moveGhostAtHome(ghost);
		}
	}
	
	// Set order of exiting of ghosts
	class GhostLauncher extends Thread
	{
		private int counter;
		
		public void run()
		{
			while(true)
			{
				// Blinky out right now !
				if(counter == 0) {
					blinkyTimer = new Timer();
					blinkyTimer.schedule(blinkyMotionTask = new BlinkyMotion(), 0, SPEED_OF_PACMAN);
				}
				
				if(counter == 2) { // Pinky out !
					pinkyAtHome = false;

					pinkyTimer.cancel();      // Kill previous Timer...
					pinkyTimer.purge();
					pinkyTimer = new Timer(); // Get new instance...
					pinkyTimer.schedule(pinkyMotionTask = new PinkyMotion(), 0, SPEED_OF_PACMAN); // ... And set new Task (with 3 parameters !)
				}
				
				if(counter == 7) { // Inky out !
					inkyAtHome = false;

					inkyTimer.cancel();
					inkyTimer.purge();
					inkyTimer = new Timer();
					inkyTimer.schedule(inkyMotionTask = new InkyMotion(), 0, SPEED_OF_PACMAN);
				}
				
				if(counter == 12) { // Clyde out !
					clydeAtHome = false;

					clydeTimer.cancel();
					clydeTimer.purge();
					clydeTimer = new Timer();
					clydeTimer.schedule(clydeMotionTask = new ClydeMotion(), 0, SPEED_OF_PACMAN);
					break;
				}

				try {
					sleep(1000); // Sleep one second
				} catch (InterruptedException e) {  }
				
				counter++;
			}
			
			// Stop this thread
			stop();
		}
	}
	
	
	private void checkGhostStrategy(Ghost ghost) {
		if(ghost.getStrategy().pointsOfTurn.isEmpty() == true) {
			ghost.getStrategy().clearVariables();
			ghost.getStrategy().execution = false;
		}
	}
	
	
	private void updateStrategy(Ghost ghost) {
		if(ghost.getStrategy().execution == false) {
			ghost.getStrategy().launchWave(pacman.getXFocus() / 5, pacman.getYFocus() / 5, ghost.getXFocus() / 5, ghost.getYFocus() / 5);
			ghost.getStrategy().layPath(ghost.getXFocus() / 5, ghost.getYFocus() / 5);
			
			ghost.setDirection(ghost.getStrategy().pointsOfTurn.get(new Point(ghost.getXFocus() / 5, ghost.getYFocus() / 5)));
			ghost.getStrategy().pointsOfTurn.remove(new Point(ghost.getXFocus() / 5, ghost.getYFocus() / 5));
			
			ghost.getStrategy().execution = true;
		}
	}
}
