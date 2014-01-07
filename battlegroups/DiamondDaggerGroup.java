package team140.battlegroups;

import team140.model.OutgoingQueue;
import team140.model.Sensor;
import team140.pathfinder.Delta;
import team140.pathfinder.Pathfinder;
import team140.util.Youtil;
import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * oh wow what a cool name
 * 
 * should face away from starting power node
 */
public class DiamondDaggerGroup extends BattleGroup {

  public Direction facing;
  
  public DiamondDaggerGroup(RobotController rc, Sensor sensor, Pathfinder pathfinder,
      OutgoingQueue outBox) {
    super(rc, sensor, pathfinder, outBox);
    
    this.facing = Direction.NORTH;
  }
  
  public void setFacing(Direction facing) {
    this.facing = facing;
    
    //need to override directions and deltas now that facing is set
    this.directions = getDirections();
    this.deltas = getFormation();
    this.towerDirection = getTowerDirection();
  }

  @Override
  protected final int getRosterLength() {
    return 8;
  }

  protected final Direction getTowerDirection() {
    return facing.opposite();
  }
  
  @Override
  public final RobotType[] getRoster() {    
    return new RobotType[] {
        RobotType.SCORCHER,
        RobotType.SOLDIER,
        RobotType.SOLDIER,
        RobotType.SCOUT,
        RobotType.SCORCHER,
        RobotType.SCORCHER,
        RobotType.DISRUPTER,
        RobotType.DISRUPTER,
    };
  }

  @Override
  public final Delta[] getFormation() {
    return Youtil.directionsToDeltas(getDirections());
  }

  @Override
  public Direction[] getDirections() {  
    if (facing == null) { facing = Direction.NORTH; } 
    
   switch (facing) {
      
      case NORTH:
      case EAST:
      case NORTH_EAST:
        return new Direction[] {
            Direction.NORTH_EAST,
            Direction.WEST,
            Direction.SOUTH,
            Direction.SOUTH,
            Direction.NORTH_WEST,
            Direction.SOUTH_EAST,
            Direction.NORTH,
            Direction.EAST,
        };
      
      case SOUTH:
      case SOUTH_EAST:
        return new Direction[] {
            Direction.SOUTH_EAST, 
            Direction.WEST,
            Direction.NORTH,
            Direction.NORTH,
            Direction.SOUTH_WEST,
            Direction.NORTH_EAST,
            Direction.SOUTH,
            Direction.EAST,
        };
        
      case WEST:
      case SOUTH_WEST:
        return new Direction[] {
            Direction.SOUTH_WEST,
            Direction.EAST,
            Direction.NORTH,
            Direction.NORTH,
            Direction.SOUTH_EAST,
            Direction.NORTH_WEST,
            Direction.SOUTH,
            Direction.WEST,
      };
   }
    
    //default to north west
    return new Direction[] {
        Direction.NORTH_WEST,
        Direction.EAST,
        Direction.SOUTH,
        Direction.SOUTH,
        Direction.NORTH_EAST,
        Direction.SOUTH_WEST,
        Direction.NORTH,
        Direction.WEST,
      };
  }

  @Override
  protected boolean getFixedOrientation() {
    return true;
  }

  //ready to move once there's at least 6 units
  //with > 10 flux
  public boolean isMobile() {
    int numReady = 0;
    for (int i=0; i<members.length; i++) {
      if (members[i] != null && members[i].flux > 10) {
        numReady++;
      }
    }
    return numReady >= 6;
  }

  @Override
  protected boolean isOffMapOK() {
    return true;
  }

}
