/*
 * File:    Grid.java
 * Package: KeezyEnraged
 * Author:  Zachary Gill
 */

package KeezyEnraged;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grid {
    
    private static final int WIDTH = 4;
    
    private static final int HEIGHT = 4;
    
    private static final Map<Point, GridPoint> gridPoints = new HashMap<>();
    
    private static final List<GridPoint> gridPointsAsList = new ArrayList<>();
    
    public Grid() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                gridPointsAsList.add(getGridPoint(x, y));
            }
        }
        gridPointsAsList.forEach(GridPoint::populateAdjacent);
    }
    
    public static void main(String[] args) {
        Grid grid = new Grid();
    }
    
    public static GridPoint getGridPoint(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return null;
        }
        
        Point point = new Point(x, y);
        if (gridPoints.containsKey(point)) {
            return gridPoints.get(point);
        } else {
            GridPoint gridPoint = new GridPoint(x, y);
            gridPoints.put(point, gridPoint);
            return gridPoint;
        }
    }
    
}
