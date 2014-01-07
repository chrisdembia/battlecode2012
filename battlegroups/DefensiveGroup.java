package team140.battlegroups;

import team140.model.OutgoingQueue;
import team140.model.Sensor;
import team140.pathfinder.Delta;
import team140.pathfinder.Pathfinder;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class DefensiveGroup extends BattleGroup {
  public DefensiveGroup(RobotController rc, Sensor sensor, Pathfinder pathfinder,
      OutgoingQueue outBox) {
    super(rc, sensor, pathfinder, outBox);
  }

  @Override
  protected int getRosterLength() {
    return 11;
  }
  
  @Override
  protected boolean getFixedOrientation() {
    return true;
  }
  
  @Override
  public boolean isMobile() {
    return false;
  }

  @Override
  public RobotType[] getRoster() {
    return new RobotType[] {
        RobotType.SCOUT,
        RobotType.SCORCHER,
        RobotType.SOLDIER,
        RobotType.SOLDIER,
        RobotType.SCORCHER,
        RobotType.SCORCHER,
        RobotType.SOLDIER,
        RobotType.SOLDIER,
        RobotType.SCORCHER,
        RobotType.SCOUT,
        RobotType.SCOUT

    };
  }

  @Override
  public Delta[] getFormation() {
    return new Delta[] {
        new Delta(0, 0),
        new Delta(0, -2),
        new Delta(1, -1),
        new Delta(-1, -1),
        new Delta(2,-1),
        new Delta(-2,-1),
        new Delta(1, 0),
        new Delta(-1, 0),
        new Delta(0, 1),
        new Delta(1, -1), 
        new Delta(-1, -1),   
        
    };
  }

  @Override
  public Direction[] getDirections() {
    return  new Direction[] {
        Direction.SOUTH,
        Direction.NORTH,
        Direction.EAST,
        Direction.WEST,
        Direction.EAST,
        Direction.WEST,
        Direction.EAST,
        Direction.WEST,
        Direction.SOUTH,
        Direction.SOUTH,
        Direction.SOUTH
       
       
    };
  }

  @Override
  protected boolean isOffMapOK() {
    return true;
  }

  @Override
  protected Direction getTowerDirection() {
    // TODO Auto-generated method stub
    return null;
  }

}
