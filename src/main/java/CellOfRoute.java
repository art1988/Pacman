import java.io.Serializable;

public class CellOfRoute implements Serializable {
	private boolean availability; // Is this cell available for unit ?
	private int type;             // 0 - Empty (without food); 1 - Food; 2 - Energizer (Big food); 3 - Portal
	private boolean visited;      // Is this cell visited ? Need for Strategy class (wave algorithm)
	private int waveValue;		  // Wave value. Need for Strategy class (wave algorithm)
	
	public CellOfRoute(boolean access, int type) {
		availability = access;
		this.type = type;
	}
	
	public boolean getAccess() {
		return availability;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public boolean isVisited() {
		return visited;
	}
	
	public void setVisited(boolean visit) {
		visited = visit;
	}
	
	public int getWaveValue() {
		return waveValue;
	}
	
	public void setWaveValue(int value) {
		waveValue = value;
	}
}