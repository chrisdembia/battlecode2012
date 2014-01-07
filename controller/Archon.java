package team140.controller;

import team140.battlegroups.BattleGroup;
import team140.battlegroups.DefensiveGroup;
import team140.battlegroups.DiamondDaggerGroup;
import team140.controller.Brain;
import team140.util.Youtil;
import team140.actions.Mover;
import team140.actions.Spawner;
import battlecode.common.*;

public class Archon extends Brain {

  private final Spawner spawner;
  private final DiamondDaggerGroup battleGroup;
  private final DefensiveGroup defensiveGroup;

  private int myArchonID;

  private boolean startedToBuild = false;
  private boolean stopMovingToNode = false;
  private MapLocation nextStep = null;
  
  //these fields used in InitialSpread
  private static final int maxBackAndForth = 2;
  private static final int numOldLocations = 3;
  private final MapLocation[] oldLocations = new MapLocation[numOldLocations];
  private int numBackAndForth;
  private MapLocation currentLocation;
  
  private int tryingToGetInFormation = 0;

  // PARAMETERS FOR DECISIONS
  static final float fluxSustainThreshold = 15; // 15
  static final float desiredFluxDonation = 30;
  static final int enemyTooClose = 99;

  public Archon(RobotController rc) {
    super(rc);
    spawner = new Spawner(rc, sensor, pathfinder);
    archonTooClose = 6;
    bgTooClose = 6;
    battleGroup = new DiamondDaggerGroup(rc, sensor, pathfinder, outBox);
    defensiveGroup = new DefensiveGroup(rc, sensor, pathfinder, outBox);
    
    currentLocation = rc.getLocation();
  }
  
  protected Mover getMover() {return spawner;}

  @Override
  public void takeOneTurn() throws GameActionException {
    
    if (Clock.getRoundNum() == 0) {
        roundOneIdentification();
    }

    decideStrategy();

    tic("decide strategy");

    if (startedToBuild) {
      //don't decide what direction to build until we've spread out a bit
      battleGroup.setFacing(decideFacingDirection());
    }
    
    switch (strategy) {
    case BATTLEGROUP:
      
      if (inBox.enemyArchons.length > 0) {
        spawner.spinOrSpawn(RobotType.SCORCHER);
        rc.setIndicatorString(1, "SPAWNING CRAZY SCORCHER");
        outBox.FocusFire(inBox.enemyArchons[0]);
      }
      
      if (sensor.existNearbyEnemyArchons()) {
        spawner.spinOrSpawn(RobotType.SCORCHER);
        rc.setIndicatorString(1, "SPAWNING CRAZY SCORCHER");
        outBox.FocusFire(sensor.getNearbyEnemyArchons()[0].location);
      }
      
      rc.setIndicatorString(0, "I'm in a battlegroup bitches");
      if (!startedToBuild && initialBGSpread()) { rc.setIndicatorString(1, "initialBGspread"); return; }
      if (battleGroup.isMobile() && waitingToBuildTower()) {
        rc.setIndicatorString(1, "waiting to build tower");
        spawner.tryBuildTower();
      }
      if (ensureAndFluxFullGroup(battleGroup)) { rc.setIndicatorString(1, "ensuring and fluxing group"); return; }
      //if (fleeAlliedArchons()) {rc.setIndicatorString(1, "fleeing allied archons"); return;}
      

      
      break;
    case DEFENSIVE:
      rc.setIndicatorString(0, "I'm in defensive mode bitches");
      if (!rc.getLocation().equals(sensor.getClosestAlliedNode().add(Direction.SOUTH))) {
        spawner.moveTo(sensor.getClosestAlliedNode().add(Direction.SOUTH));
      } else {
        ensureAndFluxFullGroup(defensiveGroup);
      }
      if (inBox.enemyArchons.length > 0) {
        spawner.spinOrSpawn(RobotType.SCORCHER);
        rc.setIndicatorString(1, "SPAWNING CRAZY SCORCHER");
        outBox.FocusFire(inBox.enemyArchons[0]);
      }
      
      if (sensor.existNearbyEnemyArchons()) {
        spawner.spinOrSpawn(RobotType.SCORCHER);
        rc.setIndicatorString(1, "SPAWNING CRAZY SCORCHER");
        outBox.FocusFire(sensor.getNearbyEnemyArchons()[0].location);
      }
      break;

    default:
      System.out.println("WARNING: no archon strategy");
    }
  }

  public void decideStrategy() {
    if (myArchonID == 0) {
      strategy = Strategy.DEFENSIVE;
    } else {
      strategy = Strategy.BATTLEGROUP;
    }
  }

  public boolean initialBGSpread() throws GameActionException {
    
    if (tooManyBackAndForth()) {
      rc.setIndicatorString(1, "too many back and forth, so starting to build");
      startedToBuild = true;
      return false;
    }

    if (spreadTowardsNode()) {
      rc.setIndicatorString(1, "spread towards node");
      return false;
    }

    if (Clock.getRoundNum() < 17) {
      rc.setIndicatorString(1, "spread from point");
      spawner.spreadFromPoint(rc.sensePowerCore().getLocation());
      return true;
    } else if (findSpotToCamp()) {
      return true;
    } else {
      rc.setIndicatorString(1, "start to build");
      startedToBuild = true;
    }
    return false;
  }
  
  private final boolean spreadTowardsNode() throws GameActionException {
    final MapLocation[] capturable = rc.senseCapturablePowerNodes();
    for (int i = 0; i < capturable.length; i++) {
      if (myArchonID == 5-i && !stopMovingToNode) {
        if (Youtil.manhattanDist(capturable[i], rc.getLocation()) <= 2) {
          stopMovingToNode = true;
          return true;
        } else {
        
          getMover().moveTo(capturable[i]);
          return true;
        }
      }
    }
    return false;
  }

  /**
   * counts consecutive repeating the same spot
   */
  private boolean tooManyBackAndForth() {
    if (!rc.getLocation().equals(currentLocation)) {
 
      boolean repeat = false;
      for (int i=0; i < oldLocations.length; i++) {
        repeat |= oldLocations[i] != null && oldLocations[i].equals(rc.getLocation());
      } 
      if (repeat) {
        numBackAndForth++;
      } else {
        numBackAndForth = 0;
      }
      for (int i=0; i < oldLocations.length-1; i++) {
        oldLocations[i] = oldLocations[i+1];
      }
      oldLocations[oldLocations.length-1] = currentLocation;
      currentLocation = rc.getLocation();
    }
    return numBackAndForth > maxBackAndForth;
  }

  public boolean findSpotToCamp() throws GameActionException {
    if (sensor.alliedArchonsTooClose(bgTooClose) && !startedToBuild) {
      rc.setIndicatorString(1, "allied archons too close");
      spawner.moveVaguely(null, sensor.getAlliedArchonLocations()); //rc.senseCapturablePowerNodes()
      return true;
    }
    if (!battleGroup.roomForGroup() && (sensor.getAdjacentVoids().length > 0)) {
      rc.setIndicatorString(1, "adjacent voids too close");
      spawner.moveVaguely(null, sensor.getAdjacentImpassables());
      return true;
    }
    return false;
  }

  private boolean waitingToBuildTower() throws GameActionException {
    // TODO will still wait to build a tower if enemies are in sight
    if ( sensor.nextToAndFacingTower() && !sensor.existNearbyEnemyFighters()) { // && !enemiesInSight() ) {   
      final GameObject object = rc.senseObjectAtLocation(sensor.getFrontLocation(), RobotType.TOWER.level);
      if (object == null || rc.senseRobotInfo((Robot) object).type != RobotType.TOWER) {
        return true;
      }
    }
    return false;
  }
  
  public boolean ensureAndFluxFullGroup(BattleGroup group) throws GameActionException {
    group.update();
    tic("group.update");
    
    if (Youtil.isNthRound(5)) {

      if (group.isMobile() && tryingToGetInFormation++ > 25) {
        //give up and keep moving
        tryingToGetInFormation = 0;
        tryMoveGroup(group);
      }
      else if (nextStep == null && !group.inFormation()) {
        group.orderToInitialFormation();
        tic("group.order to formation");
        rc.setIndicatorString(2, "group.order to formation");
      } else {
        tryingToGetInFormation = 0;
        tryMoveGroup(group);
      }
    }
    
    if (group.typeToSpawn() == null) {
      spawner.tryFluxTransfer(fluxSustainThreshold, desiredFluxDonation);
      return false;
    }
    
    manageBattleGroup(group);
    tic("manage battle group");
    return true;
  }
  
  private Direction decideFacingDirection() {
    // away from base
    return Youtil.straightDirectionBetween(rc.sensePowerCore().getLocation(), rc.getLocation());
  }

  public void manageBattleGroup(BattleGroup group) throws GameActionException {
    //this method is safe for defensive groups too now
    spawner.spawnBattleGroup(group);
    
    RobotType type = group.typeToSpawn();
    if (type != null) {
      Robot robot = (Robot) rc.senseObjectAtLocation(sensor.getFrontLocation(), type.level);
      if (robot != null && rc.canSenseObject(robot)) {
        group.registerMember(rc.senseRobotInfo(robot));
      }
    }
    
    tic("registers");
    spawner.tryFluxTransfer(fluxSustainThreshold, desiredFluxDonation);
    
    tic("try flux");
  }
  
  private boolean tryMoveGroup(BattleGroup group) throws GameActionException {
    if (!group.isMobile()) return false;
    
    if (nextStep != null && nextStep.equals(rc.getLocation())) {
      //made it one square.  pause and regroup.
      nextStep = null;
      return false;
    } else if (nextStep != null) {
      //already have a target.  keep trying to get there
      spawner.moveTo(nextStep, false); //don't break formation by dodging units
      return true;
    }
    
    //find next square to move to
    rc.setIndicatorString(2, "is mobile, checking to see if we should move");
    
    final MapLocation node = sensor.getClosestArchonlessNode();
    if (node == null) return false;
    
    final MapLocation eventualTarget = node.add(group.towerDirection.opposite());
    if (eventualTarget == null)  return false;
    
    if (rc.getLocation().equals(eventualTarget)) {
      rc.setIndicatorString(2, "k i'm adjacent. trying to turn to "+ group.towerDirection.toString());
      spawner.setDirection(group.towerDirection);
      return true;
    }
    
    Direction dir = pathfinder.getFatDirectionTowards(eventualTarget);
    if (dir == null) return false;
    
    nextStep = rc.getLocation().add(dir);
    group.orderToLocation(nextStep);
    rc.setIndicatorString(2, "not adjacent to tower, let's move this group to " + dir);
    return true;
  }
  
  private boolean fleeAlliedArchons() throws GameActionException {
    if (sensor.existNearbyAlliedArchons()) { 
      MapLocation archonLoc = sensor.getClosestRobot(sensor.getNearbyAlliedArchons()).location;
      //if (sensor.myDistanceTo(archonLoc) < archonTooClose)
      if (rc.getLocation().distanceSquaredTo(archonLoc) < archonTooClose*archonTooClose)
        // return actor.fleeFromPoint(archonLoc);
        return spawner.spreadFromPoint(archonLoc);
    }
    return false;
  }

  public void roundOneIdentification() throws GameActionException {
    
      // Assign numbers to archons to use for deciding which archon does what.
      int myID = rc.getRobot().getID();

      int[] theirIDs = new int[6];
      int j = 0;
      for (MapLocation loc : rc.senseAlliedArchons()) {
        theirIDs[j] = rc.senseObjectAtLocation(loc, RobotLevel.ON_GROUND).getID();
        j++;
      }

      java.util.Arrays.sort(theirIDs);

      for (int i = 0; i < 6; i++) {
        if (myID > theirIDs[i]) {
          myArchonID++;
        } else {
          break;
        }
      }
    
  }

  @Override
  protected boolean shouldReceiveMessages() {
    return false;
  }

  @Override
  protected boolean shouldSendMessages() {
    return true;
  }
}
