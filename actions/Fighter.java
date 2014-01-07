package team140.actions;

import team140.util.Youtil;
import team140.model.Sensor;
import team140.pathfinder.Pathfinder;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class Fighter extends Fluxer {

  public Fighter(RobotController rc, Sensor sensor, Pathfinder pathfinder) {
    super(rc, sensor, pathfinder);
  }

  /**
   * Basic attack method.
   */
  public boolean fireAt(RobotInfo enemy) throws GameActionException {
    if (enemy == null || !rc.canAttackSquare(enemy.location)) { 
      return false; 
    } 
    if (!rc.isAttackActive()) {       
      rc.attackSquare(enemy.location, enemy.robot.getRobotLevel());
    }
    return true;
  }
  
  /**
   * Basic attack method.
   */
  public boolean fireAt(MapLocation location, RobotLevel level) throws GameActionException {
    if (!rc.canAttackSquare(location)) { 
      return false; 
    } 
    if (!rc.isAttackActive()) {       
      rc.attackSquare(location, level);
    }
    return true;
  }
  
  /**
   * Second-level attack method.
   * @return
   * @throws GameActionException
   */
  public boolean fireAtWill() throws GameActionException {  
    return sensor.existNearbyEnemies() && fireAt(sensor.getNearbyEnemies()[0]);
  }
  
  public boolean fireAtGroundEnemies() throws GameActionException {
    return sensor.existNearbyGroundEnemies() && fireAt(sensor.getNearbyGroundEnemies()[0]);
  }
  
  /**
   * Rotates and looks for enemies to attack. Uses a timer.
   * @param delay
   * @return
   * @throws GameActionException
   */
  public boolean rotatingAttack(int delay) throws GameActionException {
    if (!sensor.existNearbyEnemies() && Youtil.isNthRound(delay) && tryRotateLeft()) {
      return false;
    }
    return faceAndAttackFirst();       
  }

  /**
   * Rotates and looks for enemies to attack, very wasteful speed of rotation.
   */
  public boolean rotatingAttack() throws GameActionException {
    //filter
    if (!sensor.existNearbyEnemies()) { 
      tryRotateLeft();
    }
    return faceAndAttackFirst();       
  }
    
  /**
   * Attacks and pursues first nearby robot
   */
  public boolean chaseAndAttackFirst() throws GameActionException {  
    if (!sensor.existNearbyEnemies()) { return false; }
    if(!fireAt(sensor.getNearbyEnemies()[0])) {
      rc.setIndicatorString(2, "chaseAndAttackFirst(): cannot attack enemy, moving");
      moveTo(sensor.getNearbyEnemies()[0]);
      return true;
    }
    return true;
  }
  
  public boolean chaseAndAttack(RobotInfo enemy) throws GameActionException {
    if (!fireAt(enemy)) {
      moveTo(enemy);
    }
    return true;
  }
  
  public boolean chaseAndAttack(MapLocation location) throws GameActionException {
    if (!fireAt(location, RobotLevel.ON_GROUND)) {
      moveTo(location);
    }
    return true;
  }
  
  public boolean faceAndAttackFirst() throws GameActionException {  
    if (!sensor.existNearbyEnemies()) { return false; }
    
    final RobotInfo[] enemies = sensor.getNearbyEnemies();
    
    //try to shoot at anyone we can
    for (int i=0; i<enemies.length; i++) {
      if( fireAt(enemies[i]) ) {
        return true;
      }
    }
    
    //rotate towards someone in range
    for (int i=0; i<enemies.length; i++) {
      if (rc.getLocation().distanceSquaredTo(sensor.getNearbyEnemies()[0].location) <= rc.getType().attackRadiusMaxSquared) {
        rc.setIndicatorString(2, "faceAndAttackFirst(): cannot attack enemy but is within attack range, rotating");
        setDirection(rc.getLocation().directionTo(sensor.getNearbyEnemies()[0].location));
        return true;
      }
    }
    return false;
  }
  
  public boolean faceAndAttack(RobotInfo enemy) throws GameActionException {
    if(!fireAt(enemy)) {
      if (enemy != null && rc.getLocation().distanceSquaredTo(enemy.location) <= rc.getType().attackRadiusMaxSquared ) {
        // rotating will be effective
        setDirection(rc.getLocation().directionTo(enemy.location));
        return true;
      } else {
        return false;
       // rotatingAttack();
      }
    }
    return true;
  }
  
  public boolean faceAndAttack(MapLocation location) throws GameActionException {
    if(!fireAt(location, RobotLevel.ON_GROUND)
        && rc.getLocation().distanceSquaredTo(location) <= rc.getType().attackRadiusMaxSquared ) {
      setDirection(rc.getLocation().directionTo(location));
    }
    return true;
  }
  
  /**
   * TODO Implemented!
   * @return
   * @throws GameActionException
   */
  public boolean faceAndAttackWeakest() throws GameActionException {  

    // TODO make this actually work
    //return chase(new wounded(rc.getTeam(), 0.95));
    if (!sensor.existNearbyEnemyNonTowers()) { return false; }

    RobotInfo info = sensor.getNearbyWeakestNonTower();
    
    if(!fireAt(info) && info != null && rc.getLocation().distanceSquaredTo(info.location) <= rc.getType().attackRadiusMaxSquared) {
      setDirection(rc.getLocation().directionTo(info.location));
      return true;
    }
    return false;
  }
  
  /**
   * @return
   * @throws GameActionException
   */
  public boolean chaseAndAttackWeakest() throws GameActionException {  

    // TODO make this actually work
    //return chase(new wounded(rc.getTeam(), 0.95));
    if (!sensor.existNearbyEnemyNonTowers()) { return false; }

    if(!fireAt(sensor.getNearbyWeakestNonTower())) {
      moveTo(sensor.getNearbyWeakestNonTower());
      return true;
    }
    return true;
  }

  /**
   * rotate in place to face towards any enemies
   */
  public boolean rotateTowardsEnemies() throws GameActionException {
    
    if (!sensor.existNearbyEnemies() || rc.isMovementActive()) return false;

    rc.setDirection( Youtil.straightDirectionBetween(rc.getLocation(), sensor.getNearbyEnemies()[0].location) );
    return true;
  }
}
