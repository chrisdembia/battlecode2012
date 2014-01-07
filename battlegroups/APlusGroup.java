package team140.battlegroups;

import team140.model.OutgoingQueue;
import team140.model.Sensor;
import team140.pathfinder.Delta;
import team140.pathfinder.Pathfinder;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class APlusGroup extends BattleGroup {
  public APlusGroup(RobotController rc, Sensor sensor, Pathfinder pathfinder,
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
    return false;
  }
  
  @Override
  public RobotType[] getRoster() {
    return new RobotType[] {
        RobotType.SCOUT,
        RobotType.SOLDIER,
        RobotType.SCORCHER,
        RobotType.SOLDIER,
        RobotType.SCORCHER

    };
  }

  @Override
  public Delta[] getFormation() {

 //   if (rc.getDirection().equals(Direction.SOUTH)) {
      return new Delta[] {
          new Delta(0, 0),
          new Delta(-1, 0),
          new Delta(0, 1),
          new Delta(1, 0),
          new Delta(0, -1)
      };
//    } else {
//      return new Delta[] {
//          new Delta(0, 0),
//          new Delta(-1, 0),
//          new Delta(0, -1),
//          new Delta(1, 0),
//          new Delta(0, 1)
//      };
//    }
  }

  @Override
  public Direction[] getDirections() {
  //  if (rc.getDirection().equals(Direction.SOUTH)) { 
    return  new Direction[] {
        Direction.SOUTH,
        Direction.WEST,
        Direction.SOUTH,
        Direction.EAST,
        Direction.NORTH
    };
//    } else {
//      return  new Direction[] {
//          Direction.SOUTH,
//          Direction.WEST,
//          Direction.NORTH,
//          Direction.EAST,
//          Direction.SOUTH
//      };
 //   }
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
