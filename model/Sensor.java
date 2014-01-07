package team140.model;

import team140.util.RobotFilter;
import team140.util.Youtil;
import team140.util.RobotFilter.*;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.PowerNode;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class Sensor {

  private final RobotController rc;
  public final Team myTeam;
  public final Team opponentTeam;

  private MapLocation closestCapturableNode;
  private MapLocation closestArchonlessNode;
  private MapLocation farthestCapturableNode;
  private MapLocation closestAlliedArchon;
  private MapLocation closestAlliedNode;
  private MapLocation frontLocation;
  private MapLocation[] alliedArchonLocations;
  private TerrainTile[] adjacentTiles;
  private MapLocation[] adjacentVoids;
  private MapLocation[] adjacentImpassables;
  private MapLocation[] adjacentOffMaps;

  public RobotInfo[] nearbyRobots;
  public RobotInfo[] nearbyEnemies;
  public RobotInfo[] nearbyGroundEnemies;
  public RobotInfo[] nearbyEnemyArchons;
  public RobotInfo[] nearbyAlliedArchons;
  private RobotInfo[] nearbyEnemyNonTowers;
  private RobotInfo[] nearbyEnemyTowers;
  private RobotInfo[] nearbyEnemyFighters;
  private RobotInfo nearbyWeakestNonTower;
  private float allyToEnemyRatio;
  private double[] energonMemory = new double[6];

  public Sensor(RobotController rc, IncomingQueue inBox) {
    this.rc = rc;
    this.myTeam = rc.getTeam();
    this.opponentTeam = rc.getTeam().opponent();
  }

  // TODO Reset should also populate those lists
  /**
   * 
   */
  public void reset() {
    closestCapturableNode = null;
    farthestCapturableNode = null;
    closestArchonlessNode = null;
    closestAlliedArchon = null;
    closestAlliedNode = null;
    alliedArchonLocations = null;
    adjacentTiles = null;
    adjacentVoids = null;
    adjacentImpassables = null;
    adjacentOffMaps = null;
    frontLocation = null;
    nearbyRobots = null;
    nearbyEnemies = null;
    nearbyGroundEnemies = null;
    nearbyEnemyArchons = null;
    nearbyAlliedArchons = null;
    nearbyEnemyNonTowers = null;
    nearbyEnemyTowers = null;
    nearbyEnemyFighters = null;
    nearbyWeakestNonTower = null;
    allyToEnemyRatio = -1;

    updateEnergonHistory();
  }

  /**
   * 
   * Used to calculate all sensor information
   * 
   */
  public void calcAll() {
    try {
      reset();
      getNearbyRobots();
      getNearbyEnemies();
      getNearbyGroundEnemies();
      getNearbyEnemyArchons();
      getNearbyAlliedArchons();
      getAdjacentTiles();
      getAdjacentOffMaps();
      getAdjacentVoids();
      getAdjacentImpassables();
      getClosestCapturableNode();
      getClosestArchonlessNode();
      getClosestAlliedNode();
      getClosestAlliedArchon();
      getAlliedArchonLocations();
      getFrontLocation();
      getNearbyEnemyNonTowers();
      getNearbyEnemyTowers();
      getNearbyEnemyFighters();
      getNearbyWeakestNonTower();
    } catch (GameActionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  // ACTUAL METHODS
  /**
   * Takes in nothing (should be instance created for this robot) Returns
   * Nearby enemies
   */
  public MapLocation[] SenseNearbyUnits() {
    return null;

  }


  /**
   * I want us to use this method to get distance from us to another
   * maplocation.
   * 
   * @param loc1
   * @return
   */
  public int myDistanceTo(MapLocation loc1) {
    return Youtil.manhattanDist(rc.getLocation(), loc1);
  }

  // TODO get energon of nearby robots

  // TODO use message range to detect robots beyond sensing range.
  /**
   * manhattan distance is fast and good enough
   * 
   * @return
   */
  public MapLocation getClosestCapturableNode() throws GameActionException {
    if (closestCapturableNode == null) {
      if (rc.senseCapturablePowerNodes().length == 0) {
        closestCapturableNode = null;
      } else {
        /*closestCapturableNode =
				 rc.senseCapturablePowerNodes()[Youtil.indexOfMin(rc.getLocation(),
				 rc.senseCapturablePowerNodes())];*/
        int bestDist = Integer.MAX_VALUE;
        for (MapLocation node : rc.senseCapturablePowerNodes()) {
          int dist = Youtil.manhattanDist(node, rc.getLocation());
          if (dist < bestDist) {
            bestDist = dist;
            closestCapturableNode = node;
          }
        }
      }
    }
    return closestCapturableNode;
  }
  
  public MapLocation getClosestArchonlessNode() throws GameActionException {
    if (closestArchonlessNode == null) {
      if (rc.senseCapturablePowerNodes().length == 0) {
        closestArchonlessNode = null;
      } else {
        MapLocation[] Archons = rc.senseAlliedArchons();
        int bestDist = Integer.MAX_VALUE;
        for (MapLocation node : rc.senseCapturablePowerNodes()) {
          int dist = Youtil.manhattanDist(node, rc.getLocation());
          if (dist < bestDist) {
            boolean archonless = true;
            for (MapLocation archon : Archons) {
              if (!archon.equals(rc.getLocation()) && Youtil.manhattanDist(archon, node) <= 2) {
              //if (archon.isAdjacentTo(node)) {
                archonless = false;
                break;
              }
            }
            if (archonless) {
              bestDist = dist;
              closestArchonlessNode = node;
            }
          }
        }
      }
    }
    return closestArchonlessNode;
  }
  
  // farthest node from base
  public MapLocation getFarthestCapturableNode() {
    if (farthestCapturableNode == null) {
      final MapLocation core = rc.sensePowerCore().getLocation();
      int bestDist = -1;
      for (MapLocation node : rc.senseCapturablePowerNodes()) {
        int dist = Youtil.manhattanDist(core, node);
        if (dist > bestDist) {
          bestDist = dist;
          farthestCapturableNode = node;
        }
      }
    }
    return farthestCapturableNode;
  }

  public MapLocation getClosestAlliedArchon() {
    if (closestAlliedArchon == null) {
      if (rc.senseAlliedArchons().length > 0) {

        int bestDist = Integer.MAX_VALUE;
        for (MapLocation node : rc.senseAlliedArchons()) {
          if (node != rc.getLocation()) {
            int dist = Youtil.manhattanDist(node, rc.getLocation());
            if (dist < bestDist) {
              bestDist = dist;
              closestAlliedArchon = node;
            }
          }
        }
      }
    }
    return closestAlliedArchon;
  }

  public MapLocation[] getAlliedArchonLocations() throws GameActionException {
    if (alliedArchonLocations == null) {
      if (getNearbyAlliedArchons().length > 0) {
        alliedArchonLocations = new MapLocation[getNearbyAlliedArchons().length]; 
        for (int i = 0; i < nearbyAlliedArchons.length; i++) {
          alliedArchonLocations[i] = getNearbyAlliedArchons()[i].location;
        }
      }
    }
    return alliedArchonLocations;
  }

  public TerrainTile[] getAdjacentTiles() throws GameActionException {
    if (adjacentTiles == null) {
      MapLocation myLoc = rc.getLocation();
      adjacentTiles = new TerrainTile[8];
      for (int i=0;i<8;i++) {
        adjacentTiles[i] = rc.senseTerrainTile(myLoc.add(Youtil.getDirections()[i]));
      }
    }
    return adjacentTiles;
  }
  
  public MapLocation[] getAdjacentImpassables() throws GameActionException {
    if (adjacentImpassables == null) {
      int j = 0;
      adjacentImpassables = new MapLocation[8];
      for (int i = 0; i<8; i++) {
        if (getAdjacentTiles()[i] == TerrainTile.OFF_MAP || getAdjacentTiles()[i] == TerrainTile.VOID) {
          adjacentImpassables[j] = rc.getLocation().add(Youtil.getDirections()[i]);
          j++;
        }
      }
      adjacentImpassables = java.util.Arrays.copyOf(adjacentImpassables, j);
    }
    return adjacentImpassables;
  }

  public MapLocation[] getAdjacentOffMaps() throws GameActionException {
    if (adjacentOffMaps == null) {
      int j = 0;
      adjacentOffMaps = new MapLocation[8];
      for (int i = 0; i<8; i++) {
        if (getAdjacentTiles()[i] == TerrainTile.OFF_MAP) {
          adjacentOffMaps[j] = rc.getLocation().add(Youtil.getDirections()[i]);
          j++;
        }
      }
      adjacentOffMaps = java.util.Arrays.copyOf(adjacentOffMaps, j);
    }

    return adjacentOffMaps;
  }
  
  public MapLocation[] getAdjacentVoids() throws GameActionException {
    if (adjacentVoids == null) {
      int j = 0;
      adjacentVoids = new MapLocation[8];
      for (int i = 0; i<8; i++) {
        if (getAdjacentTiles()[i] == TerrainTile.VOID) {
          adjacentVoids[j] = rc.getLocation().add(Youtil.getDirections()[i]);
          j++;
        }
      }
      adjacentVoids = java.util.Arrays.copyOf(adjacentVoids, j);
    }
    return adjacentVoids;
  }

  public MapLocation getClosestAlliedNode() {
    if (closestAlliedNode == null) {
      if (rc.senseAlliedPowerNodes().length > 0) {

        int bestDist = Integer.MAX_VALUE;
        PowerNode[] Node = rc.senseAlliedPowerNodes();
        for (int i = 0; i < Node.length; i++) {
          if (Node[i].getLocation() != rc.getLocation()) {
            int dist = Youtil.manhattanDist(Node[i].getLocation(), rc.getLocation());
            if (dist < bestDist) {
              bestDist = dist;
              closestAlliedNode = Node[i].getLocation();
            }
          }
        }
      }
    }
    return closestAlliedNode;
  }

  public MapLocation getFrontLocation() {
    if (frontLocation == null) {
      final Direction dir = rc.getDirection();
      final MapLocation loc = rc.getLocation();
      frontLocation = new MapLocation(loc.x + dir.dx, loc.y + dir.dy);
    }
    return frontLocation;
  }

  /**
   * TODO need to update nearbyRobots with our extended sight coming from
   * broadcasting.
   * 
   * @return
   * @throws GameActionException
   */
  public RobotInfo[] getNearbyRobots() throws GameActionException {
    if (nearbyRobots == null) {
      final Robot[] bots = rc.senseNearbyGameObjects(Robot.class);
      nearbyRobots = new RobotInfo[bots.length];

      for (int i = 0; i < nearbyRobots.length; i++) {
        nearbyRobots[i] = rc.senseRobotInfo(bots[i]);
      }
    }
    return nearbyRobots;
  }

  /**
   * 
   */
  private void updateEnergonHistory() {
    for (int i = 5; i > 0; i--) {
      energonMemory[i] = energonMemory[i - 1];
    }
    energonMemory[0] = rc.getEnergon();
  }

  /**
   * 
   * @return
   */
  public boolean attackedRecently() {
    final double energonFiveRoundsAgo = energonMemory[5];
    if (energonFiveRoundsAgo > energonMemory[0]
        || energonFiveRoundsAgo > energonMemory[1]
            || energonFiveRoundsAgo > energonMemory[2]
                || energonFiveRoundsAgo > energonMemory[3]
                    || energonFiveRoundsAgo > energonMemory[4]) {
      return true;
    } else {
      return false;
    }
  }

  public boolean nextToAndFacingConnectedTower() throws GameActionException {
    return rc.senseObjectAtLocation(getFrontLocation(), RobotLevel.POWER_NODE) != null
        && rc.senseConnected((PowerNode) rc.senseObjectAtLocation(getFrontLocation(), RobotLevel.POWER_NODE));
  }
  
  public boolean nextToAndFacingTower() throws GameActionException {
    return rc.senseObjectAtLocation(getFrontLocation(), RobotLevel.POWER_NODE) != null;
  }

  /**
   * Cached, though this may lead to exceptions.
   * 
   * @return
   * @throws GameActionException
   */
  public RobotInfo[] getNearbyEnemies() throws GameActionException {
    if (nearbyEnemies == null) {
      nearbyEnemies = RobotFilter.filter(getNearbyRobots(), new is(null,
          opponentTeam));
    }
    return nearbyEnemies;
  }

  public RobotInfo[] getNearbyGroundEnemies() throws GameActionException {
    if (nearbyGroundEnemies == null) {
      nearbyGroundEnemies = RobotFilter.filter(getNearbyRobots(), new ground(null, opponentTeam));
    }
    return nearbyGroundEnemies;
  }

  public RobotInfo[] getNearbyEnemyArchons() throws GameActionException {
    if (nearbyEnemyArchons == null) {
      nearbyEnemyArchons = RobotFilter.filter(getNearbyRobots(), new is(
          RobotType.ARCHON, opponentTeam));
    }
    return nearbyEnemyArchons;
  }

  public RobotInfo[] getNearbyAlliedArchons() throws GameActionException {
    if (nearbyAlliedArchons == null) {
      nearbyAlliedArchons = RobotFilter.filter(getNearbyRobots(), new is(
          RobotType.ARCHON, myTeam));
    }
    return nearbyAlliedArchons;
  }

  /**
   * Returns a RobotInfo array using filter to get nearby enemies that are not
   * towers. Cached, though this may lead to exceptions.
   * 
   * @return
   * @throws GameActionException
   */
  public RobotInfo[] getNearbyEnemyNonTowers() throws GameActionException {
    if (nearbyEnemyNonTowers == null) {
      nearbyEnemyNonTowers = RobotFilter.filter(getNearbyRobots(),
          new isNot(RobotType.TOWER, myTeam));
    }
    return nearbyEnemyNonTowers;
  }

  /**
   * Returns a RobotInfo array using filter to get nearby enemies that are
   * fighters. Cached, though this may lead to exceptions.
   * 
   * @return
   * @throws GameActionException
   */
  public RobotInfo[] getNearbyEnemyFighters() throws GameActionException {
    if (nearbyEnemyFighters == null) {
      nearbyEnemyFighters = RobotFilter.filter(RobotFilter.filter(
          getNearbyRobots(), new isNot(RobotType.ARCHON, myTeam)),
          new isNot(RobotType.TOWER, null));
    }
    return nearbyEnemyFighters;
  }

  /**
   * Returns a RobotInfo array using a filter of nearby enemy towers. Cached,
   * though this may lead to exceptions.
   * 
   * @return
   * @throws GameActionException
   */
  public RobotInfo[] getNearbyEnemyTowers() throws GameActionException {
    if (nearbyEnemyTowers == null) {
      nearbyEnemyTowers = RobotFilter.filter(getNearbyRobots(), 
          new is(RobotType.TOWER, opponentTeam));
    }
    return nearbyEnemyTowers;
  }

  /**
   * Any enemies in sight?
   */
  public boolean existNearbyEnemies() throws GameActionException {
    return getNearbyEnemies().length != 0;
  }

  public boolean existNearbyGroundEnemies() throws GameActionException {
    return getNearbyGroundEnemies().length != 0;
  }

  // not cached
  // if we use it more than once, add it to the other cached ones
  public boolean existNearbyWoundedAllies() throws GameActionException {
    final double thresh = 0.99; // heal any wounds at all
    final RobotInfo[] woundedAllies = RobotFilter.filter(getNearbyRobots(), new wounded(rc.getTeam(), thresh));
    return woundedAllies.length > 0;
  }

  /**
   * Any enemy nontowers in sight?
   * 
   * @return
   * @throws GameActionException
   */
  public boolean existNearbyEnemyNonTowers() throws GameActionException {
    return getNearbyEnemyTowers().length != 0;
  }

  /**
   * Any enemy archons in sight?
   * 
   * @return
   * @throws GameActionException
   */
  public boolean existNearbyEnemyArchons() throws GameActionException {
    return getNearbyEnemyArchons().length > 0;
  }
  
  public boolean existNearbyAlliedArchons() throws GameActionException {
    return getNearbyAlliedArchons().length > 0;
  }

  /**
   * If enemy is in sight and the enemy is neither an archon or tower, true.
   * 
   * @return
   * @throws GameActionException
   */
  public boolean existNearbyEnemyFighters() throws GameActionException {
    return getNearbyEnemyFighters().length != 0;
  }

  /**
   * General method if you want to avoid using the specific methods above.
   * 
   * @param robots
   * @return
   * @throws GameActionException
   */
  public boolean existRobots(RobotInfo[] robots) throws GameActionException {
    return robots.length != 0;
  }

  /**
   * Caches result as compared to getLowestEnergonRobot.
   * 
   * @return
   * @throws GameActionException
   */
  public RobotInfo getNearbyWeakestNonTower() throws GameActionException {
    if (nearbyWeakestNonTower == null) {
      if (getNearbyEnemyNonTowers().length == 0) {
        nearbyWeakestNonTower = null;
      } else {
        int weakestNonTowerIndex = 0;
        double lowestEnergon = Double.MAX_VALUE;
        for (int i = 0; i < getNearbyEnemyNonTowers().length; i++) {
          if (getNearbyEnemyNonTowers()[i].energon < lowestEnergon) {
            weakestNonTowerIndex = i;
            lowestEnergon = getNearbyEnemyNonTowers()[i].energon;
          }
        }
        nearbyWeakestNonTower = getNearbyEnemyNonTowers()[weakestNonTowerIndex];
      }
    }
    return nearbyWeakestNonTower;
  }

  public RobotInfo getLowestEnergonRobot(RobotInfo[] robots)
      throws GameActionException {
    if (robots.length == 0) {
      return null;
      // throw new
      // Error("Cannot find closets robot: no robots in RobotInfo array!");
    }

    if (robots.length == 1) {
      return robots[0];
    }

    int weakestRobotIndex = 0;
    double lowestEnergon = Integer.MAX_VALUE;

    for (int i = 0; i < robots.length; i++) {
      if (robots[i].energon < lowestEnergon) {
        weakestRobotIndex = i;
        lowestEnergon = robots[i].energon;
      }
    }
    return robots[weakestRobotIndex];
  }

  public RobotInfo getClosestRobot(RobotInfo[] robots) {
    if (robots.length == 0) {
      return null;
      // throw new
      // Error("Cannot find closets robot: no robots in RobotInfo array!");
    }

    if (robots.length == 1) {
      return robots[0];
    }

    int closestRobotIndex = 0;
    int min = Integer.MAX_VALUE;

    for (int i = 0; i < robots.length; i++) {
      int dist = Youtil.manhattanDist(robots[i].location,
          rc.getLocation());
      if (dist < min) {
        closestRobotIndex = i;
        min = dist;
      }
    }
    return robots[closestRobotIndex];
  }

  public RobotInfo[] filterNearbyRobots(RobotFilter filter)
      throws GameActionException {
    return RobotFilter.filter(getNearbyRobots(), filter);
  }
  
  /**
   * sorry chris, I allow them to spawn on tower nodes :-)
   */
  public boolean roomToSpawn(RobotType type, MapLocation target) throws GameActionException {
    final TerrainTile tile = rc.senseTerrainTile(target);
    final GameObject object = rc.senseObjectAtLocation(target, type.level);

    return object == null && tile.isTraversableAtHeight(type.level);
  }

  public boolean roomToSpawn(RobotType type) throws GameActionException {
    return roomToSpawn(type, getFrontLocation());
  }

  /**
   * Cost of trip computation to know if fighter has enough flux to make a
   * trip.
   * 
   * @param pathLength
   * @return
   */
  public float costOfTrip(float pathLength) {
    return (float) rc.getType().moveCost * pathLength;
  }

  public int[] getNumNearbyAlliesAndEnemies() throws GameActionException {
    int numAllies = 0;
    int numEnemies = 0;
    // get number of local enemies from a message from an archon.
    for (int i = 0; i < getNearbyRobots().length; i++) {
      if (getNearbyRobots()[i].team.equals(myTeam)) {
        numAllies++;
      } else {
        numEnemies++;
      }
    }

    // TODO this could mean double counting
    /*		if (inBox.enemyFighterLocations != null) {
		  numEnemies = numEnemies + inBox.enemyFighterLocations.length;
		}*/
    return new int[] { numAllies, numEnemies };
  }

  public float getAllyToEnemyRatio() throws GameActionException {

    if (allyToEnemyRatio == -1) {
      allyToEnemyRatio = (float) getNumNearbyAlliesAndEnemies()[0]
          / (float) getNearbyRobots().length;
    }
    return allyToEnemyRatio;
  }

  //doesn't cache anything yet
  public boolean alliedArchonsTooClose(int range) throws GameActionException {    
    final MapLocation loc = getClosestAlliedArchon();
    if (loc == null) return false;
    return ( myDistanceTo(loc) <= range );
  }

 


}
