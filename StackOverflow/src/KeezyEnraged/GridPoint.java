/*
 * File:    GridPoint.java
 * Package: KeezyEnraged
 * Author:  Zachary Gill
 */

package KeezyEnraged;

import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.Map;

public class GridPoint {
    
    public enum Direction {
        NORTH(0, 1),
        WEST(-1, 0),
        SOUTH(0, -1),
        EAST(1, 0);
        
        int lon;
        
        int lat;
        
        Direction(int lon, int lat) {
            this.lon = lon;
            this.lat = lat;
        }
    }
    
    private Point point;
    
    private Map<Direction, GridPoint> adjacent = new LinkedHashMap<>();
    
    public GridPoint(int x, int y) {
        point = new Point(x, y);
    }
    
    public void populateAdjacent() {
        for (Direction dir : Direction.values()) {
            adjacent.put(dir, Grid.getGridPoint(point.x + dir.lat, point.y + dir.lon));
        }
    }
    
}
