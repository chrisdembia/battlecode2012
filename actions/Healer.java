package team140.actions;

import team140.util.RobotFilter.wounded;
import team140.model.Sensor;
import team140.pathfinder.Pathfinder;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class Healer extends Fluxer {

  public Healer(RobotController rc, Sensor sensor, Pathfinder pathfinder) {
    super(rc, sensor, pathfinder);
  }

  public boolean healAtWill() throws GameActionException {
    if (rc.getFlux() >= GameConstants.REGEN_COST && sensor.existNearbyWoundedAllies()) {
      rc.regenerate();
      return true;
    }
    return false;
  }
  
  public boolean chaseAndHealUnits() throws GameActionException {
    healAtWill();
    return chase(new wounded(rc.getTeam(), .99));
  }
  
 
}
