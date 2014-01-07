
package team140.model;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public abstract class Messages {

  public static final int maxEnemyFighterMsgs = 6;
  public static final int maxEnemyArchonMsgs = 1;
  
  protected final RobotController rc;

  protected final int ageTreshold = 2;
  protected final int numMetaStrings = 3;

  public Messages(RobotController rc) {
    this.rc = rc;
  }

  protected enum MessageType {
    ENEMY_FIGHTER_LOCATION,
    ENEMY_ARCHON_LOCATION,
    FOCUS_FIRE,
    LOC_AND_DIR_COMMAND, 
    UNKNOWN
  }

  protected static final MessageType getMessageType(final char type_id) {
    switch (type_id) {
    case 'f':
      return MessageType.ENEMY_FIGHTER_LOCATION;
    case 'a':
      return MessageType.ENEMY_ARCHON_LOCATION;
    case 'k':
      return MessageType.FOCUS_FIRE;
    case 'd':
      return MessageType.LOC_AND_DIR_COMMAND;
    default:
      return MessageType.UNKNOWN;
    }
  }

  protected static final String encrypt() {

    return Double.toString(Math.log(Clock.getRoundNum()));

  }

  /**
   * Bit string, should be ordered the same as the RobotType enum
   * @return
   */
  protected String address() {
    return "11111";
  }

  protected String senderInfo2String() {
    final MapLocation ml = rc.getLocation();
    return Integer.toString(rc.getType().ordinal()) + "!" +
    Integer.toString(rc.getRobot().getID()) + 
    MapLocation2String(ml);
  }

  protected final static String MapLocation2String(final MapLocation ml) {
    return "." + ml.x + "-" + ml.y + ",";
  }

  protected MapLocation String2MapLocation(final String str) {
    final int period = str.indexOf(".");
    final int hyphen = str.indexOf("-");
    final int comma = str.indexOf(",");
    if (period < 0 || hyphen < 0 || comma < 0) return null;

    final Integer x = Integer.parseInt( str.substring(period+1, hyphen) );
    final Integer y = Integer.parseInt( str.substring(hyphen+1, comma) );
    if (x == null || y == null) return null;

    return new MapLocation(x, y);
  }

  protected static final char Direction2Char(final Direction dir) {
    switch (dir) {
    case NORTH:       return '0';
    case NORTH_EAST:  return '1';
    case EAST:        return '2';
    case SOUTH_EAST:  return '3';
    case SOUTH:       return '4';
    case SOUTH_WEST:  return '5';
    case WEST:        return '6';
    case NORTH_WEST:  return '7';
    default:
      throw new RuntimeException("can't charify direction: " + dir);
    }
  }

  protected static final Direction Char2Direction(final char c) {
    switch(c) {
    case '0': return Direction.NORTH;
    case '1': return Direction.NORTH_EAST;
    case '2': return Direction.EAST;
    case '3': return Direction.SOUTH_EAST;
    case '4': return Direction.SOUTH;
    case '5': return Direction.SOUTH_WEST;
    case '6': return Direction.WEST;
    case '7': return Direction.NORTH_WEST;
    default: return null;
    }
  }

  protected final boolean decrypt(final String toValidate) {
    return Clock.getRoundNum() - Math.pow(Math.E, Double.valueOf(toValidate)) < this.ageTreshold; 
  }
}
