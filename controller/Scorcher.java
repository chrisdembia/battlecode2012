package team140.controller;

import team140.actions.Fighter;
import team140.actions.Mover;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;

public class Scorcher extends Brain {

  private final Fighter fighter;
  //private MapLocation[] blindSpots = new MapLocation[]
  private Strategy myStrategy;
  private boolean imOffensive = false;
  private boolean inBattlegroup = false;



  public Scorcher(RobotController rc) {
    super(rc);
    this.fighter = new Fighter(rc, sensor, pathfinder);
    archonTooClose = 2;
  }


  @Override
  public void takeOneTurn() throws GameActionException {

    decideStrategy();

    switch (myStrategy) {
    case BATTLEGROUP:
      fighter.tryFluxTransfer(20, 20);

      if (goingToCommanded()) { rc.setIndicatorString(1, "moving to command location");
      //updateBlindSpots();
      return; }
      if (turningToCommanded()) { rc.setIndicatorString(1, "rotating to command direction");
      //updateBlindSpots();
      return; }

      // only shoot at enemies if we are already in position
      // to minimize friendly fire
      //actor.fireAtGroundEnemies();

      if (attackRangeClearOfArchons() && attackRangeClearOfTowers()) {
        if (inBox.numEnemyFighters > 0) {
          fighter.fireAt(inBox.enemyFighters[0], RobotLevel.ON_GROUND);
        }

        fighter.fireAtWill();
      }
      break;
    case OFFENSIVE:
      rc.setIndicatorString(2, "IM A CRAZY SCORCHER");
      //if it's in range, fire, if not, move to it.
     if (inBox.focusFire != null) {
      if (rc.canAttackSquare(inBox.focusFire) 
       && attackRangeClearOfArchons() 
       && attackRangeClearOfTowers()) {
        fighter.fireAt(inBox.focusFire, RobotLevel.ON_GROUND);
        fighter.moveTo(inBox.focusFire);
      } else {
        fighter.moveTo(inBox.focusFire);
      }
      break;
    
    } fighter.fireAtWill();
    
     break;
    }
  }


  @Override
  public void decideStrategy() throws GameActionException {

    if (inBattlegroup || inBox.commandLocation != null) {
      inBattlegroup = true;
      myStrategy = Strategy.BATTLEGROUP;
    } else if (imOffensive || inBox.focusFire != null) {
      imOffensive = true;
      myStrategy = Strategy.OFFENSIVE;
    } else {
      myStrategy = Strategy.BATTLEGROUP;
    }
  }


  @Override
  protected Mover getMover() {
    return fighter;
  }

  /*
   * FINAL PUSH: scorchers don't kill archons. can later be extended to include other important robots.
   */
  public boolean attackRangeClearOfArchons() throws GameActionException {

    // if archon is in sensor range, (may want to extend to any friendlies in sensor range),
    // then we're not clear
    if (sensor.existNearbyAlliedArchons()) { return false; }

    // alright now lets check our blind spots for archons.
    // the blind spots are only updated when the scorcher moves.

    // we're just going to check for the closest archon.
    if (rc.getLocation().distanceSquaredTo(sensor.getClosestAlliedArchon()) > RobotType.SCORCHER.attackRadiusMaxSquared) {
      return true;
    }

    if (rc.canAttackSquare(sensor.getClosestAlliedArchon())) {
      return false;
    }
    /*for (int i = 0; i < blindSpots.length; i++ ) { 
      if (sensor.getClosestAlliedArchon().equals(blindSpots[i])) {
        return false;
      }
    }*/
    return true;
  }

  public boolean attackRangeClearOfTowers() throws GameActionException {
    if (sensor.getClosestAlliedNode() != null && rc.canAttackSquare(sensor.getClosestAlliedNode())) {
      return false;
    }
    return true;
  }

  private void updateBlindSpots() {

  }
  @Override
  protected boolean shouldReceiveMessages() {
    return true;
  }


  @Override
  protected boolean shouldSendMessages() {
    return false;
  }
}
