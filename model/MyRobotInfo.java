package team140.model;

import battlecode.common.*;

/**
 * We exclude the Robot object, since that cannot be described by a primitive type
 * that can be translated back and forth through a string.
 * @author fitze
 *
 */
public class MyRobotInfo {

  public final MapLocation location;
  public final double energon;
  public final double flux;
  public final Direction direction;
  public final RobotType type;
  public final Team team;
  public final RobotLevel level;

  public MyRobotInfo(MapLocation location,
      double energon,
      double flux,
      Direction direction,
      RobotType type,
      Team team,
      RobotLevel level) {


    this.location = location;
    this.energon = energon;
    this.flux = flux;
    this.direction = direction;
    this.type = type;
    this.team = team;
    this.level = level;
  }
}
