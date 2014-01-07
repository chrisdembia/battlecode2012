package team140.controller;

import team140.model.IncomingQueue;
import team140.util.Youtil;
import team140.util.RobotFilter.is;
import team140.actions.Fighter;
import team140.actions.Mover;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;

public class Soldier extends Brain {

  private final Fighter fighter;

  private boolean inBattleGroup;
  private int archonTooFar = 2;
  float fleeRatio = 1f;
  private final int disbandDist = 7;

  public Soldier(RobotController rc) {
    super(rc);
    this.fighter = new Fighter(rc, sensor, pathfinder);
    archonTooClose = 2;
  }

  protected Mover getMover() {
    return fighter;
  }

  @Override
  public void takeOneTurn() throws GameActionException {
    
    decideStrategy();

    /**
     * Strategy SPRINT // TODO create decideStrategy(), etc.
     *    1. Move toward closest capturable power node.
     *    2. Attack in place.
     *    
     *    (getting flux is done passively)
     */   
    switch (strategy) {
    case OFFENSIVE:

      //fighter.tryFluxTransfer(20);
      /*if (attackPrioritized()) {return;}
      if (stayWithArchon()) {return;}
      break;*/

      /*if (takeTurnDefendNode()) { rc.setIndicatorString(1, "3: Defending adjacent node");return; }
     if (takeTurnAttack()) { rc.setIndicatorString(1, "1: Attacking"); return; }
     //if (takeTurnRunToDefenders()) { rc.setIndicatorString(1, "Defending self"); return; }
     if (takeTurnDefendingSelf()) { rc.setIndicatorString(1, "Defending self"); return; }
     if (fighter.takeTurnMoveToClosestCapturableNode()) { rc.setIndicatorString(1, "2: Moving to closest node"); return; }
     break;*/

      //if (takeTurnRunToDefenders()) {rc.setIndicatorString(1, "running to defenders");  return; }
      if (takeTurnDefendingSelf()) { rc.setIndicatorString(1, "Defending self"); return; }
      if (fighter.faceAndAttackFirst()) {rc.setIndicatorString(1, "face and attack first");  return; }
      if (stayWithArchon()) {return;}
      //if (fighter.takeTurnMoveToClosestCapturableNode()) {rc.setIndicatorString(1, "move to cloesst node"); return; }
      if (takeTurnDefendNode()) {rc.setIndicatorString(1, "defend node"); return; }
      //if (chaseAndAttackTowers()) {rc.setIndicatorString(1, "attacking tower"); return; }
      break;

    case DEFENSIVE:
      fighter.tryFluxTransfer(20, 20);
      if (takeTurnAttack()) { rc.setIndicatorString(1, "1: Attacking"); return; }
      if (attackingCommanded()) { rc.setIndicatorString(1, "rotating attack commanded"); return;}
      if (takeTurnRunToDefenders()) { rc.setIndicatorString(1, "2: Defending self"); return; }
      //if (takeTurnDefendingSelf()) { rc.setIndicatorString(1, "Defending self"); return; }
      if (takeTurnMoveToClosestAlliedArchon()) { rc.setIndicatorString(1, "3: Moving to closest allied archon"); return; }
      if (takeTurnDefendNode()) { rc.setIndicatorString(1, "4: Defending adjacent node");return; }
      break;

    case BATTLEGROUP:
      fighter.fireAtWill();  
      fighter.tryFluxTransfer(20, 20);      
      //if (actor.rotateTowardsEnemies()) { rc.setIndicatorString(1, "1: rotate towards enemy"); return; }
      if (attackingCommanded()) { rc.setIndicatorString(1, "rotating attack commanded"); return;}
      if (goingToCommanded()) { rc.setIndicatorString(1, "moving to command location"); return; }
      if (turningToCommanded()) { rc.setIndicatorString(1, "rotating to command direction"); return; }
      //if (takeTurnDefendingSelf()) { rc.setIndicatorString(1, "Defending self"); return; }
      break;
    default:
      return;
    }
  }



  public void decideStrategy() throws GameActionException {
    // TODO need a way to release soldiers from the battlegroup as well.
    if (inBattleGroup) {
      if (sensor.getClosestAlliedArchon() == null
          || sensor.myDistanceTo(sensor.getClosestAlliedArchon()) > disbandDist) {
        strategy = Strategy.OFFENSIVE;
      } else {
        strategy = Strategy.BATTLEGROUP;
      }
      return;
    }
    if (inBox.commandLocation != null) {
      inBattleGroup = true;
      rc.setIndicatorString(0, "BATTLEGROUP");
      strategy = Strategy.BATTLEGROUP;
    } else {
      rc.setIndicatorString(0, "OFFENSIVE");
      strategy = Strategy.OFFENSIVE;   
    }
  }

  /**
   * Scott's doing.
   * @return
   * @throws GameActionException
   */
  private boolean takeTurnAttack() throws GameActionException {
    //rc.setIndicatorString(0, Float.toString(sensor.getAllyToEnemyRatio()));
    if (sensor.getAllyToEnemyRatio() < fleeRatio) {
      return attackPrioritized();
    }
    return false;
    //System.out.println("enemy ally ratio switch" + rc.getLocation().toString() + sensor.getAllyToEnemyRatio());
    //return false;
  }

  private boolean stayWithArchon() throws GameActionException {
    if (rc.isMovementActive()) return false;
    if (sensor.getClosestAlliedArchon().distanceSquaredTo(rc.getLocation()) > archonTooFar*archonTooFar) {
      return fighter.moveTo(sensor.getClosestAlliedArchon());
    }
    return false;
  }

  /**
   *  Attacks enemies with the priority: attack archons, closest nontowers, then towers.
   *  
   *  TODO this could also be broken down into 3 attack methods to be individually called
   *  by Soldier. So is this code not useable by fighters other than Soliders?
   *  TODO I think this should be moved to Soldier eventually.
   * @return
   * @throws GameActionException
   */
  public boolean attackPrioritized() throws GameActionException {
    //attacks enemy fighters if they are around, only attacks towers if there are no other enemies

    if (!sensor.existNearbyEnemies()) { return false; }

    rc.setIndicatorString(2, "nearby enemies exist");
    if (sensor.existNearbyEnemyArchons()) {
      fighter.faceAndAttack(sensor.getLowestEnergonRobot(sensor.getNearbyEnemyArchons()));
      return true;
    }

    if (inBox.numEnemyArchons > 0) {
      return fighter.faceAndAttack(inBox.enemyArchons[0]);
    }

    rc.setIndicatorString(2, "no nearby enemy archons");
    if (sensor.existNearbyEnemyNonTowers()) {
      return fighter.faceAndAttack(sensor.getLowestEnergonRobot(sensor.getNearbyEnemyNonTowers()));
    }

    if (inBox.numEnemyFighters > 0) {
      return fighter.faceAndAttack(inBox.enemyFighters[0]);
    }

    rc.setIndicatorString(2, "no nearby enemy nontowers");

    return chaseAndAttackTowers();
  }

  public boolean chaseAndAttackTowers() throws GameActionException {
    if (sensor.getNearbyEnemyTowers() !=null) {
      rc.setIndicatorString(2, "there is an enemy tower");
      // need to move toward towers
      return fighter.chaseAndAttackFirst();
    }
    return false;
  }

  /**
   *  Attacks enemies with the priority: attack archons, closest nontowers, then towers.
   *  
   *  TODO this could also be broken down into 3 attack methods to be individually called
   *  by Soldier. So is this code not useable by fighters other than Soliders?
   *  TODO I think this should be moved to Soldier eventually.
   * @return
   * @throws GameActionException
   */
  public boolean chaseAndAttackPrioritized() throws GameActionException {
    //attacks enemy fighters if they are around, only attacks towers if there are no other enemies

    if (!sensor.existNearbyEnemies()) { return false; }

    rc.setIndicatorString(2, "nearby enemies exist");
    if (sensor.existNearbyEnemyArchons()) {
      return fighter.chaseAndAttack(sensor.getClosestRobot(sensor.getNearbyEnemyArchons()));
    }

    if (inBox.numEnemyArchons > 0) {
      return fighter.chaseAndAttack(inBox.enemyArchons[0]);
    }

    rc.setIndicatorString(2, "no nearby enemy archons");
    if (sensor.existNearbyEnemyNonTowers()) {   
      return fighter.chaseAndAttack(sensor.getClosestRobot(sensor.getNearbyEnemyNonTowers()));
    }

    if (inBox.numEnemyFighters > 0) {
      return fighter.chaseAndAttack(inBox.enemyFighters[0]);
    }

    rc.setIndicatorString(2, "no nearby enemy nontowers");
    if (sensor.getNearbyEnemyTowers() != null) {
      rc.setIndicatorString(2, "there is an enemy tower");
      return fighter.chaseAndAttackFirst();
    }
    return false;
  }

  /**
   * TODO if we introduce code in sensor that checks messages for who's nearby then this
   * will need to be modified. The idea is that this action does not get triggered if
   * your health goes down you can sense an enemy.
   * @return
   * @throws GameActionException
   */
  private boolean takeTurnDefendingSelf() throws GameActionException {
    if (sensor.attackedRecently() && !(sensor.existNearbyEnemyFighters())) {
      //!(RobotFilter.filter(nearbyRobots, new isNot(RobotType.ARCHON, rc.getTeam())).length == 0)) {
      //rc.setIndicatorString(1, "Attacked from behind");
      //System.out.println("Attacked from behind" + rc.getLocation());
      return fighter.rotatingAttack();
    }
    return false;
  }

  /**
   * This is the other option if we're not doing takeTurnDefendingSelf(). Scott had this one.
   * @return
   * @throws GameActionException
   */
  private boolean takeTurnRunToDefenders() throws GameActionException {
    if (sensor.attackedRecently() && !sensor.existNearbyEnemyFighters()) {
      //rc.setIndicatorString(1, "Attacked from behind");
      //System.out.println("Attacked from behind" + rc.getLocation());
      RobotInfo[] soldiers = sensor.filterNearbyRobots(new is(RobotType.SOLDIER, rc.getTeam()));
      if (sensor.existRobots(soldiers)) {
        rc.setIndicatorString(2, "Defending myself - run to soldier");
        return fighter.moveBackTo(sensor.getClosestRobot(soldiers));
      }

      return fighter.moveBackTo(sensor.getClosestAlliedArchon());
    }
    return false;
  }

  /**
   * Defend node by rotating until seeing an enemy.
   * @return
   * @throws GameActionException
   */
  private boolean takeTurnDefendNode() throws GameActionException {
    return fighter.rotatingAttack(5);
  }

  /**
   * Moves to closest allied archon.
   * @return
   * @throws GameActionException
   */
  private boolean takeTurnMoveToClosestAlliedArchon() throws GameActionException {
    //rc.setIndicatorString(1, Integer.toString(rc.getLocation().distanceSquaredTo(closestCapturableNode)));
    if (Youtil.manhattanDist(rc.getLocation(), sensor.getClosestAlliedArchon()) <= 2) {

      // Soldiers were preventing archons from getting to towers, so this
      //tries to make them make a hole for the archons. It's mildly successful
      //if (fleeNearbyAlliedArchon()) {return true;}
      return false;
    }
    return fighter.moveNear(sensor.getClosestAlliedArchon());      
  }

  //doesn't belong here
  public boolean attackingCommanded() throws GameActionException {
    if (inBox.numEnemyFighters > 0) {
      MapLocation closestEnemy = inBox.enemyFighters[Youtil.indexOfMin(rc.getLocation(), inBox.enemyFighters)];
      if (rc.getLocation().distanceSquaredTo(closestEnemy) <= rc.getType().attackRadiusMaxSquared) {
        // only start spinning if the enemy is within attack radius.
        return fighter.rotatingAttack();
      }
    }
    return false;
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

