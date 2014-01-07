package team140.model;

import team140.util.Youtil;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

// ben's refactoring todo
//  - wrap arrays
//  - shortcut decoding when approppriate
//
// maybe:
//  - checksums for uniqueness && non-alteration checking
//    => wouldn't need hashed turn num
//
//  - lazy parsing of message content

public class IncomingQueue extends Messages {
  
  public MapLocation focusFire = null;
  public MapLocation commandLocation = null;
  public Direction commandDirection = null;
  public MapLocation[] enemyFighters = new MapLocation[0];
  public MapLocation[] enemyArchons = new MapLocation[0];
  public boolean existEnemyMessages = false;
  public int numEnemyArchons = 0;
  public int numEnemyFighters = 0;
  
  private MapLocation[] rawEnemyFighters;
  private MapLocation[] rawEnemyArchons;

  public IncomingQueue(RobotController myrc) {
    super(myrc);
  }

  /**
   * Called at the beginning of each round for each robot
   */
  public void GetTurnsMessages() {

    final Message[] incoming = rc.getAllMessages();
    
    focusFire = null;
    commandLocation = null;
    commandDirection = null;
    enemyFighters = new MapLocation[0];
    enemyArchons = new MapLocation[0];
    numEnemyFighters = 0;
    numEnemyArchons = 0;
    existEnemyMessages = false;

    if (incoming.length == 0) return; 
    
    rawEnemyFighters = new MapLocation[maxEnemyFighterMsgs];
    rawEnemyArchons = new MapLocation[maxEnemyArchonMsgs];

    for (int i = 0; i < incoming.length; i++) {
      
      if (   incoming[i].strings == null 
          || incoming[i].strings.length == 0
          || !decrypt(incoming[i].strings[0]) ) {
        
        //message isn't ours
        existEnemyMessages = true;
        
        continue;
      }

      // TODO Get address and sender ID and sender type information here.

      for (int j = numMetaStrings; j < incoming[i].strings.length; j++) {
        MessageType type = Messages.getMessageType( incoming[i].strings[j].charAt(0) );
        if (type != MessageType.UNKNOWN) {
          decomposeString(type, incoming[i].strings[j]);
        }
      }
      
    }
  
    //TODO this is happening when other archons recieve an archon's message
//    if (existEnemyMessages) {
//      System.out.println("Incoming Queue debug: recieved enemy message");   
//    }

    enemyFighters = new MapLocation[numEnemyFighters];
    enemyArchons = new MapLocation[numEnemyArchons];
    System.arraycopy(rawEnemyFighters, 0, enemyFighters, 0, numEnemyFighters);
    System.arraycopy(rawEnemyArchons, 0, enemyArchons, 0, numEnemyArchons);
  }



  public final void decomposeString(final MessageType type, final String str) {
    
    final MapLocation loc = String2MapLocation(str);
    if (loc == null) return;
    
    switch (type) {
    
    case ENEMY_FIGHTER_LOCATION:
      if (numEnemyFighters < maxEnemyFighterMsgs) {
        rawEnemyFighters[numEnemyFighters++] = loc;
      }
      return;
    case ENEMY_ARCHON_LOCATION:
      if (numEnemyArchons < maxEnemyArchonMsgs) {
        rawEnemyArchons[numEnemyArchons++] = loc;
      }
      return;
    case FOCUS_FIRE:
      focusFire = loc;
      return;
    case LOC_AND_DIR_COMMAND:
      final String id = str.substring(2, str.indexOf("."));
      if ( Integer.toString(rc.getRobot().getID()).equals(id) ) {
        commandLocation = loc;
        commandDirection = Char2Direction(str.charAt(1));
      }
      return;
    default:
      System.out.println("Warning: cannot decompose str: " + str);
    }
  }
  
  public void debug() {
//  if (enemyFighterLocations.length != 0) {
//System.out.println("Printing out enemy fighter locations: ");
//for (int i = 0; i < enemyFighterLocations.length; i++) {
//  System.out.println(enemyFighterLocations[i].toString());
//}
//}
//if (enemyArchonLocations.length != 0) {
//System.out.println("Printing out enemy archon locations: ");
//for (int i = 0; i < enemyArchonLocations.length; i++) {
//  System.out.println(enemyArchonLocations[i].toString());
//}
//}
//if (voidLocations.length != 0) {
//System.out.println("Printing out void locations: ");
//for (int i = 0; i < voidLocations.length; i++) {
//  System.out.println(voidLocations[i].toString());
//}
//}
//if (commandLocations.length != 0) {
//System.out.println("Printing out locDirCommands");
//for (int i = 0; i < commandLocations.length; i++) {
//  if (commandLocations[i] != null) {
//    System.out.println(commandLocations[i].toString() + " and " + 
//        commandDirections[i].toString());
//  } else {
//    System.out.println("commandLocations is null");
//
//  }
//}
//}

}

}
