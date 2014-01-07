package team140.model;

import team140.controller.Brain;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class OutgoingQueue extends Messages {

  public static final int maxStringsPerMessage = 20;
  
  private final Sensor sensor;
  
  private Message messageToSend;
  private String[] localMsgs;
  private int numMessages = 0;

  public OutgoingQueue(RobotController rc, Sensor sensor) {
    super(rc);
    this.sensor = sensor;
  }

  public void StartOutgoingQueue() throws GameActionException {
    messageToSend = new Message();
    messageToSend.ints = new int[1];
    numMessages = 0;
    localMsgs = new String[maxStringsPerMessage];
    
    QueueOutRobotLocations();
  }

  private void QueueOutRobotLocations() throws GameActionException {

    Brain.tic("before queuing");
    
    final RobotInfo[] robots = sensor.getNearbyRobots();
    
    Brain.tic("sense nearby robots");
    
    int numArchonMsgs = 0;
    int numFighterMsgs = 0;
    
    for (int i = 0; i < robots.length; i++) 
    {  
      if (robots[i].team != sensor.myTeam 
          && robots[i].type == RobotType.ARCHON 
          && numArchonMsgs++ < maxEnemyArchonMsgs) 
      {
        localMsgs[numMessages++] = "a" + MapLocation2String(robots[i].location);
      }
      if (robots[i].team != sensor.myTeam && 
          (robots[i].type == RobotType.SOLDIER || robots[i].type == RobotType.DISRUPTER || robots[i].type == RobotType.SCORCHER || robots[i].type == RobotType.TOWER)
         && numFighterMsgs++ < maxEnemyFighterMsgs) 
      {
        localMsgs[numMessages++] = "f" + MapLocation2String(robots[i].location);
      }
    }
  }

  public void FocusFire(MapLocation ml) {
    localMsgs[numMessages++] = "k" + MapLocation2String(ml);
  }
  
  
 
  public final void AddLocationAndDirectionCommand(final int id, final MapLocation ml, final Direction dir) {
    
    localMsgs[numMessages++] = String.format("d%c%d.%d-%d,", Direction2Char(dir), id, ml.x, ml.y);    
  }

  /**
   * To be called at the end of each turn. If called multiple times in a turn,
   * memory of messages created before the subsequent calls is erased.
   * 
   * @throws GameActionException
   */
  public boolean SendQueue() throws GameActionException {

    if (numMessages > 0) {

      // Initialize message
      messageToSend.strings = new String[numMessages + numMetaStrings];
      messageToSend.strings[0] = encrypt();
      messageToSend.strings[1] = address(); //TODO
      messageToSend.strings[2] = senderInfo2String();

      System.arraycopy(localMsgs, 0, messageToSend.strings, numMetaStrings, numMessages);

      // Send Message
      if (rc.getFlux() > messageToSend.getFluxCost()) {
        rc.broadcast(messageToSend);
        return true;
      } else {
        return false;
      }
    }
    return true;
  }

}
