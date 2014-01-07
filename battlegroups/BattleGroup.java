package team140.battlegroups;

import team140.controller.Brain;
import team140.model.OutgoingQueue;
import team140.model.Sensor;
import team140.pathfinder.Delta;
import team140.pathfinder.Pathfinder;
import team140.util.Youtil;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TerrainTile;

public abstract class BattleGroup {

  public final RobotController rc;
  public final Sensor sensor;
  public final Pathfinder pathfinder;
  public final OutgoingQueue outBox;

  public final RobotInfo[] members;
  public final int rosterLength;
  public final int[] memberIDs;
  public final RobotType[] roster;
  public Direction[] directions;
  public Delta[] deltas;
  public Direction towerDirection; //direction left empty for towers
  
  public int delayOrthognal = 0;
  public int delayDiagnal = 0;

  public final boolean fixedOrientation;
  
  public BattleGroup(RobotController rc, Sensor sensor, Pathfinder pathfinder, OutgoingQueue outBox) {
    this.rc = rc;
    this.sensor = sensor;
    this.pathfinder = pathfinder;
    this.outBox = outBox;

    this.rosterLength = getRosterLength();
    this.members = new RobotInfo[rosterLength];
    this.memberIDs = new int[rosterLength];
    this.deltas = getFormation();
    this.roster = getRoster();
    this.directions = getDirections();
    this.towerDirection = getTowerDirection();

    for (RobotType bot: getRoster()) {
    	this.delayDiagnal = Math.max(this.delayDiagnal, bot.moveDelayDiagonal);
    	this.delayOrthognal = Math.max(this.delayOrthognal, bot.moveDelayOrthogonal);
    }
    
    this.fixedOrientation = getFixedOrientation();
  }
  
  // these methods are overwritten in subclass
  // if you want to access them externally, just grab the field directly
  protected abstract Direction getTowerDirection();
  protected abstract RobotType[] getRoster();
  protected abstract Delta[] getFormation();
  protected abstract Direction[] getDirections();
  protected abstract int getRosterLength();
  protected abstract boolean getFixedOrientation();
  protected abstract boolean isOffMapOK();
  
  public abstract boolean isMobile();

  // TODO RESPAWNING, and picking up stragglers to fit in what the group needs

  public boolean registerMember(RobotInfo robot) {
    for (int i = 0; i < rosterLength; i++) {
      if (robot.type.equals(roster[i]) && vacant(i)) {
        int id = robot.robot.getID();
        for (int j = 0; j < rosterLength; j++) {
          if (id == memberIDs[j]) {
            // already registered
            return false;
          }
        }
        members[i] = robot;
        memberIDs[i] = id;
        return true;
      }
    }
    return false;
  }

  public RobotType typeToSpawn() {
    for (int i = 0; i < rosterLength; i++) {

      if (vacant(i)) return roster[i];
    }
    return null;
  }

  public Direction directionToSpawn() {
    for (int i = 0; i < rosterLength; i++) {

      if (vacant(i)) return directions[i];
    }
    return null;
  }

  public boolean allAlive() {
    for (int i = 0; i < rosterLength; i++) {
      
      if (vacant(i)) return false;
    }
    return true;
  }

  public void update() throws GameActionException {
    for (int i = 0; i < rosterLength; i++) {
      if (members[i] != null) {
        if (rc.canSenseObject(members[i].robot)) {
          members[i] = rc.senseRobotInfo(members[i].robot);
        } else {
          members[i] = null;
          rc.setIndicatorString(1, "BattleGroup: Detected dead member");
        }
      }

    }
  }

  public boolean inFormation() {
    for (int i = 0; i < rosterLength; i++) {
      if (members[i] != null && 
          (  pathfinder.isPassable( Delta2MapLocation(rc.getLocation(), deltas[i]) )
             && ( !members[i].location.equals( Delta2MapLocation(rc.getLocation(), deltas[i]) ) 
                  || !members[i].direction.equals(directions[i]) ) ) ) 
      { 
        return false;
      }
    }
    return true;
  }

  public void updateFormationAndDirection() {
    Direction leaderDir = rc.getDirection();
    int newdirint = Youtil.Direction2Int(leaderDir);
    for (int i = 0; i < rosterLength; i ++) {
      deltas[i] = Youtil.rotateDelta(deltas[i], newdirint);
      directions[i] = Youtil.Int2Direction(directions[i].ordinal() + newdirint);
    }
  }

  public boolean orderToLocation() {

    if (!fixedOrientation) {
      updateFormationAndDirection();
    }
    MapLocation ml;
    for (int i = 0; i < rosterLength; i++) {
      ml = Delta2MapLocation(rc.getLocation(), deltas[i]);
      if (members[i] != null) {
        outBox.AddLocationAndDirectionCommand(memberIDs[i], ml, directions[i]);
      }
    }
    return false;
  }

  public boolean orderToInitialFormation() {
    MapLocation ml;
    for (int i = 0; i < rosterLength; i++) {
      ml = Delta2MapLocation(rc.getLocation(), deltas[i]);
      if (members[i] != null 
          && (members[i].location != ml || members[i].direction != directions[i])) {
        outBox.AddLocationAndDirectionCommand(memberIDs[i], ml, directions[i]);
        
        Brain.tic("loc and dir " + i);

      }
    }
    
    Brain.tic("command loop");

    return false;
  }

  public boolean orderToLocation(MapLocation loc) {
    if (!fixedOrientation) {
      updateFormationAndDirection();
    }
    MapLocation ml;
    for (int i = 0; i < rosterLength; i++) {
      ml = Delta2MapLocation(loc, deltas[i]);
      if (members[i] != null 
          && (members[i].location != ml || members[i].direction != directions[i])) {
        outBox.AddLocationAndDirectionCommand(memberIDs[i], ml, directions[i]);
        
      }
    }    
    return false;
  }

  private static final MapLocation Delta2MapLocation(final MapLocation loc, final Delta delta) {
    return loc.add(delta.dx, delta.dy);
  }

  /**
   * a position in the formation is vacant if:
   *    - there's no member there
   *    - the tile is available
   *    
   *    TODO remember off-map positions for stationary battlegroups
   */
  private final boolean vacant(final int i) {
    
    if (members[i] != null) return false;
    
    final TerrainTile tile = rc.senseTerrainTile(Delta2MapLocation(rc.getLocation(), deltas[i]));
    return tile.isTraversableAtHeight(roster[i].level);
  }
  
  public boolean roomForGroup() throws GameActionException {
    final int x = rc.getLocation().x;
    final int y = rc.getLocation().y;
    final boolean offmap = isOffMapOK();
    
    for (int i = 0; i < rosterLength; i++) {
      MapLocation loc = new MapLocation(x + deltas[i].dx, y + deltas[i].dy);
      TerrainTile tile = rc.senseTerrainTile(loc);
      if ( (!offmap || tile != TerrainTile.OFF_MAP) 
          && !tile.isTraversableAtHeight(roster[i].level) ) {
        return false;
      }
    }
    return true;
  }
  
  public boolean roomForGroup(MapLocation location) throws GameActionException {
    final int x = location.x;
    final int y = location.y;
    final boolean offmap = isOffMapOK();
    
    for (int i = 0; i < rosterLength; i++) {
      MapLocation loc = new MapLocation(x + deltas[i].dx, y + deltas[i].dy);
      TerrainTile tile = rc.senseTerrainTile(loc);
      if ( (!offmap || tile != TerrainTile.OFF_MAP) 
          && !tile.isTraversableAtHeight(roster[i].level) ) {
        return false;
      }
    }
    return true;
  }
  
}


