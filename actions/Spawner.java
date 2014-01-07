package team140.actions;

import team140.battlegroups.BattleGroup;
import team140.model.Sensor;
import team140.pathfinder.Pathfinder;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.PowerNode;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;

public class Spawner extends Fluxer {


  // used in spawning battlegroups
  int battleGroupStep = 1;
  boolean done = false;



  public Spawner(RobotController rc, Sensor sensor, Pathfinder pathfinder) {
    super(rc, sensor, pathfinder);
  }



  /**
   * tries to spawn a unit of RobotType type
   *  
   *  TODO: if the location in front of it is occupied by a robot or a void, it will
   *  move to a different location (haven't figured out where it should move yet)
   * 
   *  returns false if movement is active, it does not have enough flux, or 
   *    if the space is occupied (though it still moves to a new location)
   *  returns true if it spawns 
   * @param type
   * @throws GameActionException 
   *
   */
  public boolean spinOrSpawn(RobotType type) throws GameActionException {
    if (!rc.isMovementActive()) {
      if (sensor.roomToSpawn(type)) {
        if (rc.getFlux() > type.spawnCost + 2) {
          rc.spawn(type);    
          return true;
        }
      } else {
        rc.setDirection(rc.getDirection().rotateLeft());
      }
    }
    return false;
  }

  /**
   * tries to spawn a unit of RobotType type
   *
   *  returns false if movement is active, it does not have enough flux, or 
   *    if the space is occupied (though it still moves to a new location)
   *  returns true if it spawns 
   *  
   * @param type
   * @throws GameActionException 
   *
   */
  public boolean trySpawn(RobotType type) throws GameActionException {
    if ( !rc.isMovementActive() 
        && sensor.roomToSpawn(type)
        && rc.getFlux() > type.spawnCost + 2) {
      rc.spawn(type);    
      return true;
    }
    return false;
  }  
  
  public boolean trySpawn(RobotType type, float minFlux) throws GameActionException {
    if ( !rc.isMovementActive() 
        && sensor.roomToSpawn(type)
        && rc.getFlux() > (type.spawnCost + minFlux)) {
      rc.spawn(type);    
      return true;
    }
    return false;
  }  
  
  //this method is safe for defensive groups too now
  public boolean spawnBattleGroup(BattleGroup battleGroup) throws GameActionException {
    RobotType type = battleGroup.typeToSpawn();
    Direction dir = battleGroup.directionToSpawn();
    if (dir == null || rc.getDirection() == dir || !sensor.roomToSpawn(type, rc.getLocation().add(dir))) {
      return type != null && spinOrSpawn(type);
    } else {
      return setDirection(dir);
    }
  }
  
  /*
   * used to have this check, now in roomToSpawn && rc.senseObjectAtLocation(sensor.getFrontLocation(), RobotLevel.POWER_NODE) == null
   */
  public void trySpawnSoldier(float minFlux) throws GameActionException {
    if (rc.getFlux() >= RobotType.SOLDIER.spawnCost + minFlux 
        && !rc.isMovementActive()
        && sensor.roomToSpawn(RobotType.SOLDIER)) 
    {
      rc.spawn(RobotType.SOLDIER);
    }
  }

  public void tryBuildTower() throws GameActionException {
    if (rc.getFlux() >= RobotType.TOWER.spawnCost
        && !rc.isMovementActive()
        && sensor.roomToSpawn(RobotType.TOWER)
        && sensor.nextToAndFacingTower()
        && rc.senseConnected((PowerNode) rc.senseObjectAtLocation(sensor.getFrontLocation(), RobotLevel.POWER_NODE))
        ) 
    {
      rc.setIndicatorString(2, "about to spawn!");
      rc.spawn(RobotType.TOWER);
    }
  }
}
