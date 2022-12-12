import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

public class LevelEditor implements Const {
	private static JFrame editor;
	private static JButton close, save, load, newMap;
	
	private static JRadioButton wallRb, emptyWallRb, foodRb, bigfoodRb, emptyFoodRb, 
								upsideWall, leftsideWall, downsideWall, rightsideWall, uppelLeftCorner, lowerLeftCorner, lowerRightCorner, upperRightCorner, flap,
								setPacmanPos, redGhost, orangeGhost, pinkGhost, inkGhost, deleteCreature,
	                            availablePath, ghostPath, portalCell, clearCell;
	
	private static JTextField nameOfMap;
	
	// For larger representation in MapEditor
	private static int IMG_W     = 2 * Const.IMG_W,
					   IMG_H     = 2 * Const.IMG_H,
					   CELL_SIZE = 2 * Const.CELL_SIZE;
	
	// For correct representation of food and creatures
	double x_dx = IMG_W / MAP_W,
		   y_dy = IMG_H / MAP_H;
	
	private static BufferedImage img   = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_RGB);
	private static Graphics      imgGr = img.getGraphics();
	
	private static EditorCanvas editorCanvasPanel = new EditorCanvas();
	
	private static Level level;
	
	private static int[][]          mapOfWall  = new int[MAP_H][MAP_W];
	private static CellOfRoute[][]  mapOfRoute = new CellOfRoute[MAP_H - 1][MAP_W - 1];
	private static Creature[]       creature   = new Creature[5];
	
	private static int x, y; // For mapOfRoute[][]
	
	LevelEditor() {
		editor = new JFrame("Map editor");
		editor.setSize(IMG_W + 450, IMG_H + 95);
		editor.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocation();
		
		editor.getContentPane().add(initContent());
		
		// Set default settings
		setDefaultColour();
		initDefaultMapImage();
		initDefaultMapsState();
		drawGrid();
		unleashRadioButtons();
		clearArrays();

		editor.setVisible(true);
	}
	
	public void setLocation() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		editor.setLocation((int)(dim.width / 8), dim.height / 18);
	}
	
	public JPanel initContent() {
		JPanel main = new JPanel(new BorderLayout());
		
		///*****************Buttons*****************
		JPanel buttons = new JPanel();
		
		close = new JButton("Close");
		close.addActionListener(new ButtonListener());
		buttons.add(close);
		
		save = new JButton("Save map");
		save.addActionListener(new ButtonListener());
		buttons.add(save);
		
		load = new JButton("Load map");
		load.addActionListener(new ButtonListener());
		buttons.add(load);
		///******************************************
		
		// All JRadioButtons are here
		JPanel tools = new JPanel(new GridLayout(2, 2));

		initRadioButtonGroup();
		
		TitledBorder border = BorderFactory.createTitledBorder("Environment");
		
		///*****************Environment*****************
		Box environment = Box.createVerticalBox();
		
		border.setTitleJustification(TitledBorder.CENTER);
		environment.setBorder(border);
		
		environment.add(wallRb);
		environment.add(emptyWallRb);
		environment.add(foodRb);
		environment.add(bigfoodRb);
		environment.add(emptyFoodRb);
		///*********************************************
		
		border = BorderFactory.createTitledBorder("Ghost environment");
		
		///*****************Ghost environment**************
		Box ghostEnvironment = Box.createVerticalBox();
		
		border.setTitleJustification(TitledBorder.CENTER);
		ghostEnvironment.setBorder(border);
		
		ghostEnvironment.add(upsideWall);
		ghostEnvironment.add(leftsideWall);
		ghostEnvironment.add(downsideWall);
		ghostEnvironment.add(rightsideWall);
		
		ghostEnvironment.add(uppelLeftCorner);
		ghostEnvironment.add(lowerLeftCorner);
		ghostEnvironment.add(lowerRightCorner);
		ghostEnvironment.add(upperRightCorner);
		
		ghostEnvironment.add(flap);
		///************************************************
		
		border = BorderFactory.createTitledBorder("Creatures");
		
		///*****************Creatures**************
		Box creatures = Box.createVerticalBox();
		
		border.setTitleJustification(TitledBorder.CENTER);
		creatures.setBorder(border);
		
		creatures.add(setPacmanPos);
		creatures.add(redGhost);
		creatures.add(orangeGhost);
		creatures.add(pinkGhost);
		creatures.add(inkGhost);
		creatures.add(deleteCreature);
		///****************************************
		
		border = BorderFactory.createTitledBorder("Movement");
		
		///*****************Movement**************
		Box movement = Box.createVerticalBox();
		
		border.setTitleJustification(TitledBorder.CENTER);
		movement.setBorder(border);
		
		movement.add(availablePath);
		movement.add(ghostPath);
		movement.add(portalCell);
		movement.add(clearCell);
		///****************************************
		
		///*****************Name of map**************
		border = BorderFactory.createTitledBorder("Name of map");
		
		Box textField = Box.createVerticalBox();
		
		border.setTitleJustification(TitledBorder.CENTER);
		textField.setBorder(border);
		
		textField.add(nameOfMap = new JTextField());
		///******************************************
		
		editorCanvasPanel.addMouseMotionListener(new MotionListener());
		editorCanvasPanel.addMouseListener(new ClickListener());
		
		tools.add(environment);
		tools.add(ghostEnvironment);
		tools.add(creatures);
		tools.add(movement);
		
		// Main east panel
		Box eastSide = Box.createVerticalBox();
		
		eastSide.add(tools);
		eastSide.add(textField);
		eastSide.add(newMap = new JButton("Clear all map"));
		newMap.addActionListener(new ButtonListener());
		
		eastSide.add(Box.createVerticalStrut(350));
		
		main.add(editorCanvasPanel, BorderLayout.CENTER);
		main.add(buttons, BorderLayout.SOUTH);
		main.add(eastSide, BorderLayout.EAST);
		
		return main;
	}
	
	private void initRadioButtonGroup() {
		ButtonGroup group = new ButtonGroup();
		
		group.add(wallRb = new JRadioButton("Wall"));
		group.add(emptyWallRb = new JRadioButton("Delete wall"));
		group.add(foodRb = new JRadioButton("Food"));
		group.add(bigfoodRb = new JRadioButton("Big food"));
		group.add(emptyFoodRb = new JRadioButton("Delete food"));
		
		group.add(upsideWall = new JRadioButton("Upside wall"));
		group.add(leftsideWall = new JRadioButton("Leftside wall"));
		group.add(downsideWall = new JRadioButton("Downside wall"));
		group.add(rightsideWall = new JRadioButton("Rightside wall"));
		group.add(uppelLeftCorner = new JRadioButton("Left upper corner"));
		group.add(lowerLeftCorner = new JRadioButton("Left lower corner"));
		group.add(lowerRightCorner = new JRadioButton("Right lower corner"));
		group.add(upperRightCorner = new JRadioButton("Right upper corner"));
		group.add(flap = new JRadioButton("Flap"));
		
		group.add(setPacmanPos = new JRadioButton("Set pacman position"));
		group.add(redGhost = new JRadioButton("Blinky (red ghost)"));
		group.add(orangeGhost = new JRadioButton("Clyde (orange ghost)"));
		group.add(pinkGhost = new JRadioButton("Pinky (pink ghost)"));
		group.add(inkGhost = new JRadioButton("Inky (ink ghost)"));
		group.add(deleteCreature = new JRadioButton("Delete creature"));
		
		group.add(availablePath = new JRadioButton("Available cell without food"));
		group.add(ghostPath = new JRadioButton("Ghost's cell at home"));
		group.add(portalCell = new JRadioButton("Portal cell"));
		group.add(clearCell = new JRadioButton("Clear cell"));
	}
	
	private void initDefaultMapImage() {
		imgGr.setColor(wallCellColor);
		
		imgGr.fillRect(0, 0, IMG_W, CELL_SIZE);
		imgGr.fillRect(0, 0, CELL_SIZE, IMG_H);
		
		imgGr.fillRect(IMG_W - CELL_SIZE, 0, CELL_SIZE, IMG_H);
		imgGr.fillRect(0, IMG_H - CELL_SIZE, IMG_W, CELL_SIZE);

		editorCanvasPanel.repaint();
	}
	
	private void clearMapOfWall() {
		mapOfWall = new int[MAP_H][MAP_W];
	}
	
	private void clearCreatures() {
		creature = new Creature[5];
	}
	
	// Set default state for mapOfWall and mapOfRoute
	private void initDefaultMapsState() {
		for(int y = 0; y < MAP_H - 1; y++) 
			for(int x = 0; x < MAP_W - 1; x++) 
				mapOfRoute[y][x] = new CellOfRoute(false, 0);
			
		
		for(int x = 0; x < MAP_W; x++) mapOfWall[0][x] = 1;
		for(int x = 0; x < MAP_W; x++) mapOfWall[MAP_H - 1][x] = 1;
		for(int y = 0; y < MAP_H; y++) mapOfWall[y][0] = 1;
		for(int y = 0; y < MAP_H; y++) mapOfWall[y][MAP_W - 1] = 1;
	}
	
	public void setDefaultColour() {
		imgGr.setColor(Color.LIGHT_GRAY);
		imgGr.fillRect(0, 0, IMG_W, IMG_H);
	}
	
	public void drawGrid() {
		imgGr.setColor(Color.WHITE);
		for(int y = CELL_SIZE; y < IMG_H - CELL_SIZE; y += CELL_SIZE) {
			for(int x = CELL_SIZE; x < IMG_W - CELL_SIZE; x += CELL_SIZE) {
				imgGr.drawRect(x, y, CELL_SIZE, CELL_SIZE);
			}
		}
		
		editorCanvasPanel.repaint();
	}
	
	static class EditorCanvas extends JComponent {
		public void paintComponent(Graphics g) {
			g.drawImage(img, 4, 4, img.getWidth(), img.getHeight(), null);
		}
	}
	
	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == close) editor.dispose();
			
			if(e.getSource() == save) {
				level = new Level(mapOfWall, mapOfRoute, creature, nameOfMap.getText());
				
				JFileChooser saveMap = new JFileChooser();
				
				if(saveMap.showSaveDialog(editor) == JFileChooser.APPROVE_OPTION) {
					File file = saveMap.getSelectedFile();
					try {
						ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
						oos.writeObject(level);
						oos.flush();
						oos.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			
			if(e.getSource() == load) {
				JFileChooser openMap = new JFileChooser();
				
				if(openMap.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION) {
					File file = openMap.getSelectedFile();
					
					try {
						ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
						
						level = (Level) ois.readObject();
						ois.close();
						
						drawLoadedMap();
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(editor, "File incorrect", "Error", JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace();
					}
				}
			}
			
			if(e.getSource() == newMap) {
				setDefaultColour();
				initDefaultMapImage();
				initDefaultMapsState();
				drawGrid();
				unleashRadioButtons();
				clearArrays();
			}
		}
	}
	
	// TODO: Add pacman pos
	public void drawLoadedMap() {
		// Set default settings
		setDefaultColour();
		initDefaultMapImage();
		initDefaultMapsState();
		drawGrid();
		unleashRadioButtons();
		clearArrays();
		
		mapOfWall  = level.getMapOfWall();
		mapOfRoute = level.getMapOfRoute(); 
		creature   = level.getCreature();
		
		// Drawing walls
		imgGr.setColor(wallCellColor);
		
		for(int y = 0; y < MAP_H; y++) {
			for(int x = 0; x < MAP_W; x++) {
				switch(mapOfWall[y][x]) {
					case 1: imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE + 1); break;
					case 2: imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE / 3); break;
					case 3: imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1); break;
					case 4: imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, CELL_SIZE + 1, CELL_SIZE / 3); break;
					case 5: imgGr.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1); break;
					
					case 6:
						imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE / 3);
						imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
					break;
					
					case 7:
						imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
						imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, CELL_SIZE + 1, CELL_SIZE / 3);
					break;
					
					case 8:
						imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, CELL_SIZE + 1, CELL_SIZE / 3);
						imgGr.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
					break;
					
					case 9:
						imgGr.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
						imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE / 3);
					break;
					
					case 10:
						imgGr.setColor(flapCellColor);
						imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE / 3);
						imgGr.setColor(wallCellColor);
					break;
				}
			}
		}
		
		// Drawing food and portal cells
		imgGr.setColor(foodCellColor);
		for(int y = 0; y < MAP_H - 1; y++) {
			for(int x = 0; x < MAP_W - 1; x++) {
				CellOfRoute cell = mapOfRoute[y][x];
				
				switch(cell.getType()) {
					case 0:
						if(cell.getAccess() == false) break;
						
						imgGr.setColor(Color.WHITE);
						imgGr.fillOval((int) (x * x_dx + CELL_SIZE) - 4, (int)(y * y_dy + CELL_SIZE) - 4, 8, 8);
						imgGr.setColor(foodCellColor);
					break;
					
					case 1: imgGr.fillOval((int) (x * x_dx + CELL_SIZE) - 4, (int)(y * y_dy + CELL_SIZE) - 4, 8, 8);   break;
					case 2: imgGr.fillOval((int) (x * x_dx + CELL_SIZE) - 7, (int)(y * y_dy + CELL_SIZE) - 7, 14, 14); break;
					
					case 3: 
						imgGr.setColor(Color.MAGENTA);
						imgGr.fillRect((int) ((x + 1) * x_dx) - 5, (int)((y + 1) * y_dy) - 5, 10, 10);
						imgGr.setColor(foodCellColor);
					break;
				}
			}
		}
		
		// Drawing creatures
		for(int i = 0; i < creature.length; i++) {
			if(creature[i] == null) continue;
			
			if(creature[i] instanceof Pacman) {
				setPacmanPos.setEnabled(false);
				
				imgGr.setColor(pacmanCellColor);
				imgGr.fillOval(creature[i].getXCorner() * 4 + CELL_SIZE, creature[i].getYCorner() * 4 + CELL_SIZE, 2 * CELL_SIZE, 2 * CELL_SIZE);
				
				continue;
			}
			
			if(creature[i] instanceof Blinky) redGhost.setEnabled(false);
			if(creature[i] instanceof Pinky)  pinkGhost.setEnabled(false);
			if(creature[i] instanceof Inky)   inkGhost.setEnabled(false);
			if(creature[i] instanceof Clyde)  orangeGhost.setEnabled(false);
			
			imgGr.drawImage( ((Ghost)(creature[i])).getImage().getImage(), 
									  (creature[i].getXCorner() * 4 + 5) + CELL_SIZE + 1, creature[i].getYCorner() * 4 + CELL_SIZE + 1,
									  2 * CELL_SIZE - 1, 2 * CELL_SIZE - 1,
									  null);
		}
		
		editorCanvasPanel.repaint();
	}
	
	public void unleashRadioButtons() {
		setPacmanPos.setEnabled(true);
		redGhost.setEnabled(true);
		orangeGhost.setEnabled(true);
		pinkGhost.setEnabled(true);
		inkGhost.setEnabled(true);
	}
	
	public void clearArrays() {
		clearMapOfWall();
		initDefaultMapsState();
		clearCreatures();
	}
	
	public void processSelection() {
		// For ignoring boundaries
		if( (x == 0 || y == 0 || x == MAP_W - 1 || y == MAP_H - 1) && 
			(portalCell.isSelected() == false) && (clearCell.isSelected() == false)) return;
		
		try {
///////////// Wall painting
			if(wallRb.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE + 1);
				
				mapOfWall[y][x] = 1;
			}
			
///////////// Upside wall painting
			if(upsideWall.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE / 3);
				
				mapOfWall[y][x] = 2;
			}
			
///////////// Left side wall painting
			if(leftsideWall.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
				
				mapOfWall[y][x] = 3;
			}
			
///////////// Down side wall painting
			if(downsideWall.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, CELL_SIZE + 1, CELL_SIZE / 3);
				
				mapOfWall[y][x] = 4;
			}
			
///////////// Right side wall painting 
			if(rightsideWall.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
				
				mapOfWall[y][x] = 5;
			}
			
///////////// Left upper corner painting
			if(uppelLeftCorner.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE / 3);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
				
				mapOfWall[y][x] = 6;
			}
			
///////////// Left lower corner painting
			if(lowerLeftCorner.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, CELL_SIZE + 1, CELL_SIZE / 3);
				
				mapOfWall[y][x] = 7;
			}
			
///////////// Right lower corner painting
			if(lowerRightCorner.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, CELL_SIZE + 1, CELL_SIZE / 3);
				imgGr.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
				
				mapOfWall[y][x] = 8;
			}
			
///////////// Right upper corner painting
			if(upperRightCorner.isSelected()) {
				imgGr.setColor(wallCellColor);
				imgGr.fillRect(x * CELL_SIZE + CELL_SIZE - CELL_SIZE / 3 + 1, y * CELL_SIZE, CELL_SIZE / 3, CELL_SIZE + 1);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE / 3);
				
				mapOfWall[y][x] = 9;
			}
			
///////////// Flap wall painting
			if(flap.isSelected()) {
				imgGr.setColor(flapCellColor);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE / 3);
				
				mapOfWall[y][x] = 10;
			}
			
///////////// Wall deleting
			if(emptyWallRb.isSelected()) {
				if(mapOfWall[y][x] == 0) return;
				
				imgGr.setColor(Color.LIGHT_GRAY);
				imgGr.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE + 1, CELL_SIZE + 1);
				imgGr.setColor(Color.WHITE);
				imgGr.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				
				mapOfWall[y][x] = 0;
			}
			
///////////// Food painting
			if(foodRb.isSelected()) {
				if(x == 1 || y == 1) return;
				
				imgGr.setColor(foodCellColor);
				imgGr.fillOval((int) (x * x_dx) - 4, (int)(y * y_dy) - 4, 8, 8);

				mapOfRoute[y - 1][x - 1] = new CellOfRoute(true, 1);
			}
			
///////////// Big food painting
			if(bigfoodRb.isSelected()) {
				if(x == 1 || y == 1) return;
				
				double x_dx = IMG_W / MAP_W,
			       	   y_dy = IMG_H / MAP_H;
				
				imgGr.setColor(foodCellColor);
				imgGr.fillOval((int) (x * x_dx) - 7, (int)(y * y_dy) - 7, 14, 14);
				
				mapOfRoute[y - 1][x - 1] = new CellOfRoute(true, 2);
			}
			
///////////// Food deleting
			if(emptyFoodRb.isSelected()) {
				if(x == 1 || y == 1) return;
				
				imgGr.setColor(Color.LIGHT_GRAY);
				imgGr.fillOval((int) (x * x_dx) - 7, (int)(y * y_dy) - 7, 14, 14);
				
				imgGr.setColor(Color.WHITE);
				// White cross
				imgGr.drawLine((int) (x * x_dx), (int) (y * y_dy) - 7, (int) (x * x_dx), (int) (y * y_dy) + 7);
				imgGr.drawLine((int) (x * x_dx) - 7, (int) (y * y_dy), (int) (x * x_dx) + 7, (int) (y * y_dy));

				mapOfRoute[y - 1][x - 1] = new CellOfRoute(false, 0);
			}
			
///////////// Pacman painting
			if(setPacmanPos.isSelected()) {
				if(x == 1 || y == 1) return;
				
				int xPacmanFocus = (int) (x * x_dx) - CELL_SIZE,
				    yPacmanFocus = (int) (y * y_dy) - CELL_SIZE;
				
				for(int i = 0; i < creature.length; i++) {
					if(creature[i] == null) {
						creature[i] = new Pacman(xPacmanFocus / 4, yPacmanFocus / 4);
						
						creature[i].setXCorner(creature[i].getXFocus() - CELL_SIZE / 4);
						creature[i].setYCorner(creature[i].getYFocus() - CELL_SIZE / 4);
						
						creature[i].setDirection(Direction.NONE);
						
						break;
					}
				}
				
				imgGr.setColor(pacmanCellColor);

				imgGr.fillOval((int) (x * x_dx) - CELL_SIZE, (int) (y * y_dy) - CELL_SIZE, 2 * CELL_SIZE, 2 * CELL_SIZE);
				
				setPacmanPos.setEnabled(false);
				emptyWallRb.setSelected(true);
			}
			
///////////// Blinky painting (Red ghost)
			if(redGhost.isSelected()) {
				if(x == 1 || y == 1) return;
				
				int xGhostFocus = (int) (x * x_dx) - CELL_SIZE,
					yGhostFocus = (int) (y * y_dy) - CELL_SIZE;
				
				for(int i = 0; i < creature.length; i++) {
					if(creature[i] == null) {
						creature[i] = new Blinky(xGhostFocus / 4, yGhostFocus / 4);
						
						creature[i].setXCorner(creature[i].getXFocus() - CELL_SIZE / 4);
						creature[i].setYCorner(creature[i].getYFocus() - CELL_SIZE / 4);
						
						creature[i].setDirection(Direction.NONE);
						
						imgGr.drawImage( ((Ghost)(creature[i])).getImage().getImage(), 
								         (int) (x * x_dx) - CELL_SIZE + 1,
								         (int) (y * y_dy) - CELL_SIZE + 1,
								         2 * CELL_SIZE - 1, 2 * CELL_SIZE - 1,
								         null);
						
						break;
					}
				}
				
				redGhost.setEnabled(false);
				emptyWallRb.setSelected(true);
			}
			
///////////// Pinky painting (Pink ghost)
			if(pinkGhost.isSelected()) {
				if(x == 1 || y == 1) return;
				
				int xGhostFocus = (int) (x * x_dx) - CELL_SIZE,
					yGhostFocus = (int) (y * y_dy) - CELL_SIZE;
				
				for(int i = 0; i < creature.length; i++) {
					if(creature[i] == null) {
						creature[i] = new Pinky(xGhostFocus / 4, yGhostFocus / 4);
						
						creature[i].setXCorner(creature[i].getXFocus() - CELL_SIZE / 4);
						creature[i].setYCorner(creature[i].getYFocus() - CELL_SIZE / 4);
						
						//TODO:
						creature[i].setDirection(Direction.NONE);
						
						imgGr.drawImage( ((Ghost)(creature[i])).getImage().getImage(), 
								         (int) (x * x_dx + 5) - CELL_SIZE + 1, // +5 - small displacement for correct representation
								         (int) (y * y_dy) - CELL_SIZE + 1,
								         2 * CELL_SIZE - 1, 2 * CELL_SIZE - 1,
								         null);
						
						break;
					}
				}
				
				pinkGhost.setEnabled(false);
				emptyWallRb.setSelected(true);
			}
			
///////////// Inky painting (Ink ghost)
			if(inkGhost.isSelected()) {
				if(x == 1 || y == 1) return;
				
				int xGhostFocus = (int) (x * x_dx) - CELL_SIZE,
					yGhostFocus = (int) (y * y_dy) - CELL_SIZE;
				
				for(int i = 0; i < creature.length; i++) {
					if(creature[i] == null) {
						creature[i] = new Inky(xGhostFocus / 4, yGhostFocus / 4);
						
						creature[i].setXCorner(creature[i].getXFocus() - CELL_SIZE / 4);
						creature[i].setYCorner(creature[i].getYFocus() - CELL_SIZE / 4);
						
						//TODO:
						creature[i].setDirection(Direction.NONE);
						
						imgGr.drawImage( ((Ghost)(creature[i])).getImage().getImage(), 
								         (int) (x * x_dx + 5) - CELL_SIZE + 1, // +5 - small displacement for correct representation
								         (int) (y * y_dy) - CELL_SIZE + 1,
								         2 * CELL_SIZE - 1, 2 * CELL_SIZE - 1,
								         null);
						
						break;
					}
				}
				
				inkGhost.setEnabled(false);
				emptyWallRb.setSelected(true);
			}
			
///////////// Clyde painting (Orange ghost)
			if(orangeGhost.isSelected()) {
				if(x == 1 || y == 1) return;
				
				int xGhostFocus = (int) (x * x_dx) - CELL_SIZE,
					yGhostFocus = (int) (y * y_dy) - CELL_SIZE;
				
				for(int i = 0; i < creature.length; i++) {
					if(creature[i] == null) {
						creature[i] = new Clyde(xGhostFocus / 4, yGhostFocus / 4);
						
						creature[i].setXCorner(creature[i].getXFocus() - CELL_SIZE / 4);
						creature[i].setYCorner(creature[i].getYFocus() - CELL_SIZE / 4);
						
						//TODO:
						creature[i].setDirection(Direction.NONE);
						
						imgGr.drawImage( ((Ghost)(creature[i])).getImage().getImage(), 
								         (int) (x * x_dx + 5) - CELL_SIZE + 1, // +5 - small displacement for correct representation
								         (int) (y * y_dy) - CELL_SIZE + 1,
								         2 * CELL_SIZE - 1, 2 * CELL_SIZE - 1,
								         null);
						
						break;
					}
				}
				
				orangeGhost.setEnabled(false);
				emptyWallRb.setSelected(true);
			}
			
			// TODO: 
			if(deleteCreature.isSelected()) {
				
			}
			
///////////// Cell (without food) painting
			if(availablePath.isSelected()) {
				if(x == 1 || y == 1) return;
				
				imgGr.setColor(Color.WHITE);
				imgGr.fillOval((int) (x * x_dx) - 4, (int)(y * y_dy) - 4, 8, 8);

				mapOfRoute[y - 1][x - 1] = new CellOfRoute(true, 0);
			}
			
///////////// Cell for ghost (at home) // TODO: (???)
			if(ghostPath.isSelected()) {
				if(x == 1 || y == 1) return;
				
				imgGr.setColor(ghostCellColor);
				imgGr.fillOval((int) (x * x_dx) - 4, (int)(y * y_dy) - 4, 8, 8);
				
				mapOfRoute[y - 1][x - 1] = new CellOfRoute(true, 0);
			}
			
///////////// Portal cell painting
			if(portalCell.isSelected()) {
				imgGr.setColor(Color.MAGENTA);
				imgGr.fillRect((int) (x * x_dx) - 5, (int)(y * y_dy) - 5, 10, 10);
				
				mapOfRoute[y - 1][x - 1] = new CellOfRoute(true, 3);
			}
			
///////////// Clear cell
			if(clearCell.isSelected()) {
				imgGr.setColor(Color.LIGHT_GRAY);
				imgGr.fillOval((int) (x * x_dx) - 7, (int)(y * y_dy) - 7, 14, 14);
				
				imgGr.setColor(Color.WHITE);
				// White cross
				imgGr.drawLine((int) (x * x_dx), (int) (y * y_dy) - 7, (int) (x * x_dx), (int) (y * y_dy) + 7);
				imgGr.drawLine((int) (x * x_dx) - 7, (int) (y * y_dy), (int) (x * x_dx) + 7, (int) (y * y_dy));
				
				mapOfRoute[y - 1][x - 1] = new CellOfRoute(false, 0);
			}
		} catch(ArrayIndexOutOfBoundsException ex) {};
	}
	
	class MotionListener extends MouseMotionAdapter {
		public void mouseDragged(MouseEvent e) {
			x = e.getX() / CELL_SIZE;
			y = e.getY() / CELL_SIZE;
			
			processSelection();
			
			editorCanvasPanel.repaint();
		}
	}
	
	class ClickListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			x = e.getX() / CELL_SIZE;
			y = e.getY() / CELL_SIZE;
			
			processSelection();
			
			editorCanvasPanel.repaint();
		}
	}
}