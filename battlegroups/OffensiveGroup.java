package team140.battlegroups;

import team140.model.OutgoingQueue;
import team140.model.Sensor;
import team140.pathfinder.Delta;
import team140.pathfinder.Pathfinder;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class OffensiveGroup extends BattleGroup {
  public OffensiveGroup(RobotController rc, Sensor sensor, Pathfinder pathfinder,
      OutgoingQueue outBox) {
    super(rc, sensor, pathfinder, outBox);
  }

  @Override
  protected int getRosterLength() {
    return 5;
  }

  @Override
  protected boolean getFixedOrientation() {
    return false;
  }
  
  @Override
  public boolean isMobile() {
    return true;
  }
  
  @Override
  public RobotType[] getRoster() {
    return new RobotType[] {
        RobotType.SCOUT,
        RobotType.SOLDIER,
        RobotType.SOLDIER,
        RobotType.SOLDIER,
        RobotType.SOLDIER,
        //RobotType.SOLDIER,
        //RobotType.SOLDIER,
    };
  }

  @Override
  public Delta[] getFormation() {
    return new Delta[] {
        new Delta(0, 0),
        new Delta(-1, 0),
        new Delta(1, 0),
        new Delta(-1, -1),
        new Delta(1, -1),
        //new Delta(-1, -2),
        //new Delta(1, -2)
    };
  }

  @Override
  public Direction[] getDirections() {
    return  new Direction[] {
        Direction.NORTH,
        Direction.NORTH,
        Direction.NORTH,
        Direction.NORTH,
        Direction.NORTH,
        //Direction.NORTH,
        //Direction.NORTH
    };
  }

  @Override
  protected boolean isOffMapOK() {
    return false;
  }

  @Override
  protected Direction getTowerDirection() {
    // TODO Auto-generated method stub
    return null;
  }
}
