import java.awt.Color;

public interface Const {
	int CELL_SIZE = 10;
	
	int MAP_W = 29;
	int MAP_H = 32;
	
	int IMG_W = CELL_SIZE * MAP_W;
	int IMG_H = CELL_SIZE * MAP_H;
	
	int IND_W = 85;
	int IND_H = 30;
	
	// For correct representation of food and creatures
	double DX = IMG_W / MAP_W,
		   DY = IMG_H / MAP_H;
	
	int INCREMENT_OF_MOTION = 1;
	
	int SPEED_OF_PACMAN = 40; // 40
	
	Color foodCellColor   = new Color(249, 246, 173);
	Color pacmanCellColor = new Color(255, 247, 50);
	Color wallCellColor   = new Color(56, 10, 187);
	Color flapCellColor   = new Color(241, 231, 205);
	Color ghostCellColor  = new Color(213, 17, 58);
}