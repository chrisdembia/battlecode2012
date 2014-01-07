package team140.pathfinder;

import java.util.Arrays;
import java.util.List;

import team140.util.Youtil;

import battlecode.common.*;

public class Pathfinder {

  /**
   * Most codes to try pathfinding before switching to wall hugging
   */
  public final static int MAX_PATHFINDER_BYTECODE = 6000;
  
  /**
   * Max manhattan distance to attempt pathfinding instead of wallhugging
   */
  public final static int MAX_PATHFINDER_DISTANCE = 10;

  public static final List<Direction> MOVEABLE_DIRECTIONS = Arrays.asList(
      Direction.SOUTH, Direction.SOUTH_EAST, Direction.EAST,
      Direction.NORTH_EAST, Direction.NORTH, Direction.NORTH_WEST,
      Direction.WEST, Direction.SOUTH_WEST);

  public static final int MAX_X = GameConstants.MAP_MAX_WIDTH * 2;
  public static final int MAX_Y = GameConstants.MAP_MAX_HEIGHT * 2;
  
  public static final int EXPLORED_INDEX_SIZE = (MAX_X+2) * MAX_Y;

  private final RobotController rc;
  
  // allowed one external obstacle in the main pathfinder
  // usually the closest allied power node
  private MapLocation obstacle;

  public Pathfinder(RobotController rc) {
    this.rc = rc;
  }
  
  //treats voids & off-maps as impassable
  public final boolean isPassable(final MapLocation loc) {
    
    final boolean squareOK = !rc.canSenseSquare(loc) || rc.senseTerrainTile(loc) == TerrainTile.LAND;
    final boolean noObstacle = obstacle == null || obstacle.x != loc.x || obstacle.y != loc.y;
    
    return squareOK && noObstacle;
  }
  
  public final boolean isPassable(final int x, final int y) {
    return (isPassable(new MapLocation(x, y)));
  }
  
  /**
   * Finds the shortest path to the target square, optimistically assuming that
   * all unknown tiles are passable. Returns the first direction along that
   * path.
   * 
   * please let me (ben) code review any changes here before you commit them.  thanks!
   */
  public Direction getDirectionTowards(MapLocation targetLocation, boolean shouldDodgeUnits) {    
    final MapLocation startLocation = rc.getLocation();
   
    if (Youtil.manhattanDist(startLocation, targetLocation) > MAX_PATHFINDER_DISTANCE) {
//      System.out.println("INFO: distance was too far; switched to wall hugging");
      return wallHugPath(targetLocation);
    }
    
    final XY start = new XY(startLocation.x, startLocation.y) ;
    final XY target = new XY(targetLocation.x, targetLocation.y);

    if (start.equals(target)) {
      System.out.println("Warning: already at target");
      return Direction.NONE;
    }

    //treat the nearest allied power node 
    // or open power node as an obstacle
    // unless it is the target square
    obstacle = null;
    final PowerNode[] nodes = rc.senseAlliedPowerNodes();
    int bestDist = Integer.MAX_VALUE;
    for (int i=0; i < nodes.length; i++) {
      int dist = Youtil.manhattanDist(startLocation, nodes[i].getLocation());
      if (dist < bestDist) {
        bestDist = dist;
        obstacle = nodes[i].getLocation();
      }
    }
    final MapLocation[] capturable = rc.senseCapturablePowerNodes();
    for (int i=0; i < capturable.length; i++) {
      int dist = Youtil.manhattanDist(startLocation, capturable[i]);
      if (!capturable[i].equals(target) && dist < bestDist) {
        bestDist = dist;
        obstacle = capturable[i];
      }
    }
    
    final PriorityQueue open = new PriorityQueue(MAX_X * MAX_Y);
    final boolean[] explored = new boolean[EXPLORED_INDEX_SIZE];

    // add neighbors of starting tile
    for (Direction dir : MOVEABLE_DIRECTIONS) {
      XY xy = new XY(start.x + dir.dx, start.y + dir.dy, dir, distance(dir), target);

      if (isPassable(xy.x, xy.y) && (!shouldDodgeUnits || rc.canMove(dir))) {

        if (xy.equals(target)) {
          //at goal already
          return dir;
        }

        open.add(xy);
        explored[xy.index] = true;
      }
    }

    while (open.count > 0 && Clock.getBytecodeNum() < MAX_PATHFINDER_BYTECODE) {

      XY current = open.removeMin();

      // add neighbors of this tile
      // keep the same starting direction
      for (int i = 0; i < 8; i++) {
        Delta d = Delta.MOVEABLE_DELTAS[i];

        XY xy = new XY(current.x + d.dx, current.y + d.dy, current.startDir,
            current.dist + d.dist, target);
        if (!explored[xy.index] && isPassable(xy.x, xy.y)) {

          // check for goal, or straight path to goal
          if (xy.equals(target) || straightPath(xy, target)) {
            return current.startDir;
          }

          open.add(xy);
          explored[xy.index] = true;
        }
      }
    }

    // couldn't find a path in time, so switch to wall hugging
//    System.out.println("INFO: couldn't find a path in time; switched to wall-hugging");

    return wallHugPath(targetLocation);
  }
  
  /**
   * Very fast & stupid pathfinding
   * tries to go towards the target; if there's an obstruction, tries to go one degree counterclockwise, etc.
   * this works ok most of the time
   */
  public final Direction wallHugPath(final MapLocation target) {
    
    int i = 0;
    Direction dir = Youtil.straightDirectionBetween(rc.getLocation(), target);
    while (!rc.canMove(dir) && i++ < 8) {
      dir = dir.rotateLeft();
    }
    return dir;
  }
  

  // picks a straight path and checks if every spot along the path is passable
  // optimized for speed as best as I can
  private final boolean straightPath(final XY current, final XY target) {

    final int dx = target.x - current.x;
    final int dy = target.y - current.y;

    final int abs_dx = dx > 0 ? dx : -dx;
    final int abs_dy = dy > 0 ? dy : -dy;

    int sign_dx = dx > 0 ? 1 : (dx == 0 ? 0 : -1);
    int sign_dy = dy > 0 ? 1 : (dy == 0 ? 0 : -1);

    final int min = abs_dx < abs_dy ? abs_dx : abs_dy;
    final int max = abs_dx > abs_dy ? abs_dx : abs_dy;

    int x = current.x;
    int y = current.y;
    boolean obstacles = false;

    // run diagonel for min steps
    // good to keep the branch logic out of the for loop?
    // don't need to bounds check because both start and target are in bounds
    int step = 0;
    while (step++ < min) {
      x += sign_dx;
      y += sign_dy;
      obstacles |= !isPassable(x,y);
    }
    if (obstacles) return false;

    // run straight for the remaining steps
    // by setting the sign bit of the smaller one to 0
    sign_dx = abs_dx > abs_dy ? sign_dx : 0;
    sign_dy = abs_dy > abs_dx ? sign_dy : 0;
    while (step++ < max) {
      x += sign_dx;
      y += sign_dy;
      obstacles |= !isPassable(x,y);
    }
    return !obstacles;
  }

  // distance of moving one square in this direction
  private final static float distance(final Direction dir) {
    return dir == Direction.NORTH_WEST || dir == Direction.NORTH_EAST
        || dir == Direction.SOUTH_WEST || dir == Direction.SOUTH_EAST 
        ? Youtil.SQRT_2 : 1;
  }

  // TODO: should try to preserve distance when pushing into bounds
  // otherwise, if getting chased straight off the map, we'll get stuck
  public MapLocation nearestReachablePoint(int x, int y) {

    // short-circuit check if point is passable, to avoid BFS
    if (isPassable(x,y)) {
      return new MapLocation(x, y);
    }

    // breadth first search
    int dx = 0;
    int dy = 1;
    boolean lastChangeToX = false;
    while (!isPassable(x + dx, y + dy)) {
      if (lastChangeToX) {
        // change y
        dy = (dy > 0 ? -dy : 1 - dy);
      } else {
        // change x
        dx = (dx > 0 ? -dx : 1 - dx);
      }
      lastChangeToX = !lastChangeToX;
    }
    return new MapLocation(x + dx, y + dy);
  }

  // get direction for a fat battlegroup
  // try not to get too close to walls if possible
  // never enough bytecode for A*, so don't even try
  //
  // TODO make this method very clever
  // right now it's the same as the normal wallhugger, without dodging units
  // which is very very stupid
  public final Direction getFatDirectionTowards(final MapLocation target) {
    
    int i = 0;
    Direction dir = Youtil.straightDirectionBetween(rc.getLocation(), target);
    while (!isPassable(rc.getLocation().add(dir)) && i++ < 8) {
      dir = dir.rotateLeft();
    }
    return dir;
  }
}
