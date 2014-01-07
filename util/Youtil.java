package team140.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import team140.pathfinder.Delta;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;


public class Youtil {

  public static final float SQRT_2 = 1.41421f;
  public static final float SQRT_2_BY_2 = 0.707f;

  
  public static final boolean isNthRound(final int numRounds) {
    return Clock.getRoundNum() % numRounds == 0;
  }
  
  public static float shortestDistBetween(MapLocation loc0, MapLocation loc1) {
    return shortestDistBetween(loc0.x, loc0.y, loc1.x, loc1.y);
  }

  public static float shortestDistBetween(int x0, int y0, int x1, int y1) {
    int dx = x0 - x1;
    int dy = y0 - y1;
    dx = dx < 0 ? -dx : dx; //absolute value
    dy = dy < 0 ? -dy : dy; //absolute value

    final int min = dx < dy ? dx : dy;
    final int max = dx > dy ? dx : dy; 
    final int straight = max - min;

    // go diagonel for the min diff; go straight for the remainder of the max diff
    return SQRT_2 * min + straight;
  }

  public static Direction straightDirectionBetween(final MapLocation alice, final MapLocation bob) {
    return new Delta(alice.x - bob.x, alice.y - bob.y).getDirection();
  }


  public static String getExceptionAsString(Exception e )
  {
    StringWriter w = new StringWriter();
    e.printStackTrace(new PrintWriter(w));
    return w.toString().substring(0,120);
    //w.toString().substring(1, w.toString().contains("at") ? 
    // w.toString().indexOf("at")-1 : Math.min(w.toString().length(), 100));
  } 

  public static int indexOfMin(MapLocation me, MapLocation[] ml) {
    int index = 0;
    int dist = Integer.MAX_VALUE; 
    for (int i = 0; i < ml.length; i++) {
      if (manhattanDist(me, ml[i]) < dist) {
        index = i;
      }
    }
    return index;
  }

  public static int[] sortIndices(MapLocation me, MapLocation[] ml) {

    int length = ml.length;
    int[] values = new int[length];
    int[] indices = new int[length];

    int temp = 0;
    for (int i = 0; i < length; i++) {
      values[i] = me.distanceSquaredTo(ml[i]);
      indices[i] = i;
    }

    for (int i = 0; i < length; i++) {
      temp = values[i];
      int j = i;
      while (j > 0 && values[j-1] > temp) {
        values[j] = values[j-1];
        indices[j] = j -1;
        j--;

      }
      values[j] = temp;
    }

    //System.out.println(indices);
    return indices;
  }

  /**
   * @return abs(dx) + abs(dy)
   */
  public static final int manhattanDist(final MapLocation loc0, final MapLocation loc1) {
    final int dx = loc0.x - loc1.x;
    final int dy = loc0.y - loc1.y;
    return (dx > 0 ? dx : -dx) + (dy > 0 ? dy : -dy);
  }

  public static final List<Direction> MOVEABLE_DIRECTIONS = Arrays.asList(
      Direction.SOUTH,
      Direction.SOUTH_EAST,
      Direction.EAST,
      Direction.NORTH_EAST,
      Direction.NORTH,
      Direction.NORTH_WEST,
      Direction.WEST,
      Direction.SOUTH_WEST
      );

  public static boolean isUniqueLocation(MapLocation[] mls, MapLocation ml) {
    for (int i = 0; i < mls.length; i++) {
      if (mls[i] != null &&mls[i].equals(ml)) {
        return false;
      }
    }
    return true;
  }

  public static float myCos(int angle) {
    // could also make this just take in a direction
    switch (angle % 8) {
    case 0:
      return 1f;
    case 1:
      return SQRT_2_BY_2;
    case 2:
      return 0f;
    case 3:
      return -SQRT_2_BY_2;
    case 4:
      return -1f;
    case 5:
      return -SQRT_2_BY_2;
    case 6:
      return 0f;
    case 7:
      return SQRT_2_BY_2;
    }
    throw new Error("No clue how you got here");
  }

  public static float mySin(int angle) {
    switch (angle % 8) {
    case 0:
      return 0f;
    case 1:
      return SQRT_2_BY_2;
    case 2:
      return 1f;
    case 3:
      return SQRT_2_BY_2;
    case 4:
      return 0f;
    case 5:
      return -SQRT_2_BY_2;
    case 6:
      return -1f;
    case 7:
      return -SQRT_2_BY_2;
    }
    throw new Error("No clue how you got here");
  }

  public static Direction Int2Direction(int dirint) { 
    switch (dirint % 8) {
    case 0:
      return Direction.NORTH;
    case 1:
      return Direction.NORTH_EAST;
    case 2:
      return Direction.EAST;
    case 3:
      return Direction.SOUTH_EAST;
    case 4:
      return Direction.SOUTH;
    case 5:
      return Direction.SOUTH_WEST;
    case 6:
      return Direction.WEST;
    case 7:
      return Direction.NORTH_WEST;
    default:
      return null; //malformed direction string
    }
  }
  
  public static int Direction2Int(Direction dir) {
    switch (dir) {
    case NORTH:
      return 0;
    case NORTH_EAST:
      return 1;
    case EAST:
      return 2;
    case SOUTH_EAST:
      return 3;
    case SOUTH:
      return 4;
    case SOUTH_WEST:
      return 5;
    case WEST:
      return 6;
    case NORTH_WEST:
      return 7;
    default:
      throw new Error("WHAT WHY DID YOU ENDUP HERE?");
    }
  }
  
  /**
   * use for looping through directions
   * @return
   */
  public static Direction[] getDirections() {
    return new Direction[] {
        Direction.NORTH,
        Direction.NORTH_EAST,
        Direction.EAST,
        Direction.SOUTH_EAST,
        Direction.SOUTH,
        Direction.SOUTH_WEST,
        Direction.WEST,
        Direction.NORTH_WEST,
    };
  }
  
  public static final Delta[] directionsToDeltas(final Direction[] dirs) {
    Delta[] deltas = new Delta[dirs.length];
    for (int i=0; i<dirs.length; i++) {
      deltas[i] = directionToDelta(dirs[i]);
    }
    return deltas;
  }
  
  private static final Delta directionToDelta(final Direction direction) {
    switch (direction) {
    case WEST: return new Delta(-1, 0);
    case SOUTH: return new Delta(0, 1);
    case EAST: return new Delta(1, 0);
    case NORTH: return new Delta(0, -1);
    case NORTH_EAST: return new Delta(1, -1);
    case NORTH_WEST: return new Delta(-1, -1);
    case SOUTH_EAST: return new Delta(1, 1);
    case SOUTH_WEST: return new Delta(-1, 1);
    default: return new Delta(0,0);
    }
  }

  public static Delta rotateDelta(Delta delta, int angle) {

    return new Delta(
        java.lang.Math.round(delta.dx * myCos(angle) - delta.dy * mySin(angle)),
        java.lang.Math.round(delta.dx * mySin(angle) + delta.dy * myCos(angle)));
  }


}















