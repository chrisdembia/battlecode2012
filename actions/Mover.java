package team140.actions;

import team140.model.Sensor;
import team140.pathfinder.Pathfinder;
import team140.util.RobotFilter;
import team140.util.Youtil;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public abstract class Mover {
  
  private static final float MOVE_VAGUELY_DISTANCE = 5;
  private final Pathfinder pathfinder;
  protected final RobotController rc;
  protected final Sensor sensor;
  
  public Mover(RobotController rc, Sensor sensor, Pathfinder pathfinder) {
    this.rc = rc;
    this.sensor = sensor;
    this.pathfinder = pathfinder;
  }
  
  public boolean setDirection(Direction dir) throws GameActionException {
    if (rc.isMovementActive() || dir == rc.getDirection()) return false;
    rc.setDirection(dir);
    return true;
  }
  
  /**
   * Moves to closest capturable node, but will not sit right on it, should just try to stay
   * in proximity.
   * @return
   * @throws GameActionException
   */
  public boolean takeTurnMoveToClosestCapturableNode() throws GameActionException {
    //rc.setIndicatorString(1, Integer.toString(rc.getLocation().distanceSquaredTo(closestCapturableNode)));
    if (Youtil.manhattanDist(rc.getLocation(), sensor.getClosestCapturableNode()) <= 2) {

      // Soldiers were preventing archons from getting to towers, so this
      //tries to make them make a hole for the archons. It's mildly successful
      //if (fleeNearbyAlliedArchon()) {return true;}
      return false;
    }
    return moveNear(sensor.getClosestCapturableNode());
    //return actor.moveBackTo(sensor.getClosestCapturableNode());
  }
  
  /**
   * returns false if movement is active
   * returns true if it turns to dir, or if it is already facing dir
   * 
   */
  public boolean faceDirection(Direction dir) throws GameActionException {
    if (dir == rc.getDirection()) {return true;}
    else if (!rc.isMovementActive()){ rc.setDirection(dir); return true;}
    return false;
  }
  
  
  public boolean tryRotateLeft() throws GameActionException {
    if (!rc.isMovementActive()) {
      rc.setDirection(rc.getDirection().rotateLeft().rotateLeft());
      return true;
    }
    return false;
  }
  
  public boolean rotateLeft45() throws GameActionException {
    if (!rc.isMovementActive()) {
      rc.setDirection(rc.getDirection().rotateLeft());
      return true;
    }
    return false;
  }
  
  public boolean tryRotateRight() throws GameActionException {
    if (!rc.isMovementActive()) {
      rc.setDirection(rc.getDirection().rotateRight().rotateRight());
      return true;
    }
    return false;
  }
  
  public boolean rotateRight45() throws GameActionException {
    if (!rc.isMovementActive()) {
      rc.setDirection(rc.getDirection().rotateRight());
      return true;
    }
    return false;
  }
  
  public boolean tryRotate180() throws GameActionException {
    if (!rc.isMovementActive()) {
      rc.setDirection(rc.getDirection().opposite());
      return true;
    }
    return false;
  }
  
  /**
   * Takes in RobotInfo instead, to do null error checking (if robot is not there).
   */
  public boolean moveTo(RobotInfo robot) throws GameActionException {
    if (robot == null) { return false; }
    return moveTo(robot.location);
  }
  
  /**
   * Takes in RobotInfo instead, to do null error checking (if robot is not there).
   */
  public boolean moveBackTo(RobotInfo robot) throws GameActionException {
    if (robot == null) { return false; }
    return moveBackTo(robot.location);
  }
  
  public boolean moveTo(MapLocation target, boolean dodgeUnits) throws GameActionException {
    if (rc.getLocation().equals(target) || rc.isMovementActive()) return false;
    
    Direction dir = pathfinder.getDirectionTowards(target, dodgeUnits);
    
    if (dir != rc.getDirection()) {
      // change direction
      rc.setDirection(dir);
      
    } else if (rc.canMove(dir)
        && rc.getFlux() > rc.getType().moveCost + 1) {
      // move towards target
      rc.moveForward();
      return true;
    }
    return false;
  }
  
  public boolean moveTo(MapLocation target) throws GameActionException {
    return moveTo(target, true);
  }
    
  /**
   * Move backwards?
   * @param target
   * @return
   * @throws GameActionException
   */
  public boolean moveBackTo(MapLocation target) throws GameActionException {
    if ((rc.canSenseSquare(target) && rc.getLocation().equals(target)) || 
        rc.isMovementActive()) return false;
    
    Direction dir = rc.getLocation().directionTo(target).opposite();
    
    if (dir != rc.getDirection()) {
      // change direction
      rc.setDirection(dir);
      
    } else if (rc.canMove(dir.opposite())) {
      if (rc.getFlux() > rc.getType().moveCost) {
        rc.moveBackward();
        return true;
      }
    } else {
      rotateRight45();
      
    }
    return false;
  }
  
  /**
   * Takes in a RobotInfo, for error checking.
   */
  public boolean moveAdjacent(RobotInfo robot) throws GameActionException {
    if (robot == null) { return false; }
    return moveAdjacent(robot.location);
  }
  
  /**
   * Takes in a MapLocation
   * @param target
   * @return
   * @throws GameActionException
   */
  public boolean moveAdjacent(MapLocation target) throws GameActionException {
    if ((rc.canSenseSquare(target) && rc.getLocation().isAdjacentTo(target)) ||
      rc.isMovementActive()) {
      return false;
    }
    Direction dir = pathfinder.getDirectionTowards(target, true);
    
    if (dir != rc.getDirection()) {
      // change direction
      rc.setDirection(dir);
      
    } else if (rc.canMove(dir) && rc.getFlux() > rc.getType().moveCost) {
      // move towards target
      rc.moveForward();
      return true;
    }
    return false;
  }
  
  /**
   * Takes in a RobotInfo, for null error checking
   */
  public boolean moveNear(RobotInfo robot) throws GameActionException {
    if (robot == null) { return false; }
    return moveNear(robot.location);
  }
  
  /**
   * Takes in MapLocation
   * @param target
   * @return
   * @throws GameActionException
   */
  public boolean moveNear(MapLocation target) throws GameActionException {
    if (sensor.getFrontLocation().equals(target) || rc.getLocation().isAdjacentTo(target)) {
      // if in front of me IS where i want to not sit, don't go there.
      return false;
    }
    MapLocation adjloc;
    for (Direction dir : Youtil.MOVEABLE_DIRECTIONS) {
      adjloc = target.add(dir);
      if (rc.canSenseSquare(adjloc))  {
        if (rc.senseObjectAtLocation(adjloc, rc.getType().level) != null) {
          target = adjloc;
          break;
        }
        // also want to minimize distance
      }
    }
    return moveTo(target);
  }
  
  public boolean spreadFromPoint(MapLocation point) throws GameActionException {
  //  Direction dir = pathfinder.getDirectionTowards(point, false);
    Direction dir = rc.getLocation().directionTo(point);
    MapLocation target = rc.getLocation().subtract(dir);
    return moveTo(target);
  }
  
  public boolean fleeFromPoint(MapLocation point) throws GameActionException {
    //  Direction dir = pathfinder.getDirectionTowards(point, false);
      Direction dir = rc.getLocation().directionTo(point);
      MapLocation target = rc.getLocation().subtract(dir);
      return moveBackTo(target);
    }
  
  /**
   * look for a robot matching the filter, and chase it.
   * 
   * @return whether found a robot matching the filter
   */
  public boolean chase(RobotFilter filter) throws GameActionException {
         
    for (int i=0; i<sensor.getNearbyRobots().length; i++) {
      if (filter.accept(sensor.getNearbyRobots()[i])) {
        return moveTo(sensor.getNearbyRobots()[i]);
      }
    }
    return false;
  }
  
  public boolean flee(RobotFilter filter) throws GameActionException {
    RobotInfo[] matches = RobotFilter.filter(sensor.getNearbyRobots(), filter);
    MapLocation[] repulsars = new MapLocation[matches.length];
    for (int i=0; i<matches.length; i++) {
      repulsars[i] = matches[i].location;
    }
    return moveVaguely(null, repulsars);
  }
  
  /**
   * this method tries generally to move towards attractors and away from repulsors.
   * either list can be empty or null
   */
  public boolean moveVaguely(MapLocation[] attractors, MapLocation[] repulsors) throws GameActionException {    
    final int x = rc.getLocation().x;
    final int y = rc.getLocation().y;
    
    float x_force = 0f;
    float y_force = 0f;
    
    if (attractors != null) {
      for (int i=0; i < attractors.length; i++) {
        
        //okay, squaring each of the diffs independently isn't exactly what we want
        // but doing it properly (tip to tail?  I remember highschool physics!) would take longer
        float x_dist = attractors[i].x - x;
        float y_dist = attractors[i].y - y;
        if (x_dist != 0) {
          x_force += 1 / (x_dist * x_dist) * (x_dist > 0 ? 1 : -1); //1 / x_dist^2 * its sign
        }
        if (y_dist != 0) {
          y_force += 1 / (y_dist * y_dist) * (y_dist > 0 ? 1 : -1); //1 / y_dist^2 * its sign
        }
      }
    }
    
    if (repulsors != null) {
      for (int i=0; i < repulsors.length; i++) {
        float x_dist = x - repulsors[i].x;
        float y_dist = y - repulsors[i].y;
        if (x_dist != 0) {
          x_force += 1 / (x_dist * x_dist) * (x_dist > 0 ? 1 : -1);
        }
        if (y_dist != 0) {
          y_force += 1 / (y_dist * y_dist) * (y_dist > 0 ? 1 : -1);
        }
      }
    }
    
    // now scale the (x_force, y_force) vector to length MOVE_VAGUELY_DISTANCE
    final float partial = (float) Math.sqrt(x_force*x_force + y_force*y_force);
    if (partial == 0) {
      return false;
    }
    final float scaling = MOVE_VAGUELY_DISTANCE / partial;
    final int dx = (int) (x_force * scaling + 0.5);
    final int dy = (int) (y_force * scaling + 0.5);    
    
    //go towards a passable point near the target
    MapLocation target = pathfinder.nearestReachablePoint(x+dx, y+dy);
    
    return moveTo(target);
  }
}
