package team140.controller;

//import team002.pathfinding.Pathfinder;
import team140.util.RobotFilter.*;
import team140.model.IncomingQueue;
import team140.model.OutgoingQueue;
import team140.model.Sensor;
import team140.pathfinder.Pathfinder;
import team140.util.Youtil;
import team140.actions.*;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * This class uses the message information and any sensor information to
 * determine what robot does
 * 
 * @author
 * 
 */
public abstract class Brain {

  // BIG THINGS
  protected final RobotController rc;
  protected final Sensor sensor;
  protected final Pathfinder pathfinder;
  protected final IncomingQueue inBox;
  protected final OutgoingQueue outBox;

  //temp for debugging
  public static int[] tics;
  public static String[] events;
  public static int numtics;
  
  public enum Strategy {
    OFFENSIVE,
    DEFENSIVE,
    BATTLEGROUP,
    OFFENSIVEGROUP
  }
  Strategy strategy;
  
  // PARAMETERS USED BY MULTIPLE BOT TYPES
  protected int archonTooClose;
  protected int bgTooClose;

  public Brain(RobotController rc) {

    this.pathfinder = new Pathfinder(rc);
        
    this.rc = rc;
    this.inBox = new IncomingQueue(rc);
    this.sensor = new Sensor(rc, this.inBox);
    this.outBox = new OutgoingQueue(rc, sensor);
    this.archonTooClose = 2;
  }

  public abstract void takeOneTurn() throws GameActionException;
  
  // override these to return true or false depending on state, etc.
  // having both set to "true" is very dangerous in terms of bytecode
  protected abstract boolean shouldReceiveMessages();
  protected abstract boolean shouldSendMessages();
  
  // so that subclasses of Brain can use a subclass of move
  protected abstract Mover getMover();

  public abstract void decideStrategy() throws GameActionException;
  
  /**
   * This method loops forever, calling takeOneTurn() every turn (hopefully)
   * and logging any top-level exceptions.
   */
  public final void run() {
    while (true) {
      try {
        
        tics = new int[99];
        events = new String[99];
        numtics = 0;
        
        int round = Clock.getRoundNum();
        tic("start");
        beginTurn();
        takeOneTurn();
        endTurn();
        
//        int rounds = Clock.getRoundNum() - round;
//        if (rounds > 0) {
//          System.out.println("maxed out bytecode.");
//          System.out.println("num enemies: " + sensor.getNumNearbyAlliesAndEnemies()[1] + ", num allies: " + sensor.getNumNearbyAlliesAndEnemies()[0]);
//          printTics();
//        }
        
        rc.yield();

      } 
      catch (Exception e) {
        System.out.println("Caught exception in " + this + ":");
        rc.setIndicatorString(2, Youtil.getExceptionAsString(e));
        e.printStackTrace();
      }
    }
  }
  
  //for bytecode debug
  public static void tic(String event) {
    events[numtics] = event;
    tics[numtics++] = Clock.getBytecodeNum();
  }
  private void printTics() {
    for (int i=0; i<numtics; i++) {
      System.out.println("\t" + events[i] + ": " + tics[i]);
    }
  }

  /**
   * 
   * @throws GameActionException
   */
  public void beginTurn() throws GameActionException {


    
    sensor.reset();
    
    tic("sensor reset");
    
    if (shouldReceiveMessages()) {
      inBox.GetTurnsMessages();
    }
    
    tic("get turns messages");
    
    if (shouldSendMessages()) {
      outBox.StartOutgoingQueue();
    }
    
    tic("start outgoing queue");
  }

  public void endTurn() throws GameActionException {

    outBox.SendQueue();
    
    tic("send queue");
  }

  public boolean goingToCommanded() throws GameActionException {
    if (inBox.commandLocation != null) {
    rc.setIndicatorString(2, "commanded to go to " + inBox.commandLocation.toString());
    return getMover().moveTo(inBox.commandLocation, false);
    }
    return false;
  }

  public boolean turningToCommanded() throws GameActionException {
    return inBox.commandDirection != null 
        && getMover().setDirection(inBox.commandDirection);
  }

  /**
   * Are any archons too close? this will flee any Archons in the range of range (lol)
   * 
   * returns true if it flees
   * returns false if there are no archons within the range or if it cannot move
   * @return
   * @throws GameActionException
   */
  protected boolean fleeNearbyAlliedArchon(int range) throws GameActionException {

    if (sensor.alliedArchonsTooClose(range)) {
      return getMover().flee(new is(RobotType.ARCHON, rc.getTeam()));
    }
    return false;
  }
  
  public String toString() {
    return rc.getType() + " with ID " + rc.getRobot().getID();
  }
}
