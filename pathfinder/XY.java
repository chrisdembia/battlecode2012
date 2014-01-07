package team140.pathfinder;

import team140.util.Youtil;
import battlecode.common.Direction;

final class XY {
  final int x;
  final int y;
  final int index; //hash of x and y
  final float dist; // the distance we've come so far
  final float approxTotalDist; // optimistically, the total distance to the
                               // goal through this square
  final Direction startDir; // the starting direction of the path we've taken
                            // to get here

  XY(int x, int y, Direction dir, float dist, XY target) {
    this.x = x;
    this.y = y;
    this.startDir = dir;
    this.dist = dist;
    this.approxTotalDist = dist
        + Youtil.shortestDistBetween(x, y, target.x, target.y);
    this.index =  (x + y * (Pathfinder.MAX_X + 1)) % Pathfinder.EXPLORED_INDEX_SIZE;
  }

  XY(int x, int y) {
    this.x = x;
    this.y = y;
    this.dist = 0;
    this.approxTotalDist = 0;
    this.startDir = null;
    this.index = 0;
  }

  public final boolean equals(Object other) {
    return (other != null && other instanceof XY && this.x == ((XY) other).x && this.y == ((XY) other).y);
  }

  public String toString() {
    return "(" + x + "," + y + ", dir: " + startDir + ", dist: " + dist
        + ", total: " + approxTotalDist + ")";
  }
}

