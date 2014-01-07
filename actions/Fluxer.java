package team140.actions;

import team140.model.Sensor;
import team140.pathfinder.Pathfinder;
import team140.util.RobotFilter;
import team140.util.RobotFilter.lowFlux;
import team140.util.RobotFilter.onOrAdjacent;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Fluxer extends Mover {
  
  public Fluxer(RobotController rc, Sensor sensor, Pathfinder pathfinder) {
    super(rc, sensor, pathfinder);
  }


  public void tryFluxTransfer(float minFlux, float desiredDonation) throws GameActionException {    
    final RobotInfo[] adjacent = RobotFilter.filter(sensor.getNearbyRobots(), new onOrAdjacent(rc.getLocation(), rc.getTeam()));
    final RobotInfo[] targets = RobotFilter.filter(adjacent, new lowFlux(rc.getTeam(), minFlux));

    for (int i=0; i < targets.length && rc.getFlux() > 0; i++) {

      double canGive = rc.getFlux();
      double wantToGive = desiredDonation - targets[i].flux + 5;  //TODO remove the +5 slop once decideMinFlux is correct
      double amount = canGive < wantToGive ? canGive : wantToGive; //min

      rc.transferFlux(targets[i].location, targets[i].type.level, amount);
    } 
  }
  
  public void tryFluxTransfer(RobotInfo info, float minFlux) throws GameActionException {    

    if (!info.location.isAdjacentTo(rc.getLocation())) { return; }


    double canGive = rc.getFlux();
    double wantToGive = minFlux - info.flux + 5;  //TODO remove the +5 slop once decideMinFlux is correct
    double amount = canGive < wantToGive ? canGive : wantToGive; //min

    rc.transferFlux(info.location, info.type.level, amount);
  }

}
