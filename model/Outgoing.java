package team140.model;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public interface Outgoing {

	public void StartOutgoingQueue() throws GameActionException;

	public void AddBroadcast();

	public void QueueVoidLocations();

	public void QueueOutRobotLocations() throws GameActionException;

	public void AddRobotLocation(RobotInfo robot);

	public void AddEnemyFighterLocation(RobotInfo robot);

	public void AddEnemyArchonLocation(RobotInfo robot);

	public void FocusFire(MapLocation ml);

	public void AddVoidLocation(MapLocation ml);

	public void ManageAddedMessage();

	public boolean SendQueue() throws GameActionException;
}
