package team140.pathfinder;

import battlecode.common.Direction;
import team140.util.Youtil;

/**
 *  more lightweight than using a Direction
 *  this is used in the inner pathfinding loop
 *
 */
public class Delta {
  public final int dx;
  public final int dy;
  final float dist;
  
  //used in pathfinder
  public Delta(int dx, int dy, float dist) {
    this.dx = dx;
    this.dy = dy;
    this.dist = dist;
  }
  
  //used for general utility
  public Delta(int dx, int dy) {
    this.dx = dx;
    this.dy = dy;
    this.dist = -1f;
  }
  
  private static final float SQRT_2 = Youtil.SQRT_2;
  
  public static Delta[] MOVEABLE_DELTAS= {
    new Delta(-1, -1, SQRT_2), // north west
    new Delta(-1, 0, 1f), // west
    new Delta(-1, 1, SQRT_2), // south west
    new Delta(0, 1, 1f), // south
    new Delta(1, 1, SQRT_2), // south east
    new Delta(1, 0, 1f), // east
    new Delta(1, -1, SQRT_2), // north east
    new Delta(0, -1, 1f) // north
  };

  public final Direction getDirection() {
    if (dx == 0 && dy < 0) return Direction.SOUTH;
    if (dx > 0 && dy < 0) return Direction.SOUTH_WEST;
    if (dx > 0 && dy == 0) return Direction.WEST;
    if (dx > 0 && dy > 0) return Direction.NORTH_WEST;
    if (dx == 0 && dy > 0) return Direction.NORTH;
    if (dx < 0 && dy > 0) return Direction.NORTH_EAST;
    if (dx < 0 && dy == 0) return Direction.EAST;
    if (dx < 0 && dy < 0) return Direction.SOUTH_EAST;
      
    return Direction.NONE;
  }
}
