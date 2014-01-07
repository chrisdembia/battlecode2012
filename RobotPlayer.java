package team140;

import team140.controller.*;
import battlecode.common.RobotController;

// TODO IMPORTANT!!!!! MAKE SURE ALL TEAM002 IMPORTS ARE REMOVED!!!!!!!

/**
 * 
 * There's a different class for each robot type. All the action occurs in the
 * takeOneTurn() method for that class.
 * 
 * TODO: rename the packages to team<team_number>, once we register a team and
 * get a number.
 */
public class RobotPlayer {

	public static void run(RobotController rc) {

		while (true) {

			switch (rc.getType()) {
			case ARCHON:
				new Archon(rc).run();
				break;
			case SOLDIER:
				new Soldier(rc).run();
				break;
			case SCOUT:
				new Scout(rc).run();
				break;
			case DISRUPTER:
				new Disrupter(rc).run();
				break;
			case SCORCHER:
				new Scorcher(rc).run();
				break;
			default:
				throw new Error("Unsupported type " + rc.getType());

			}
		}
	}
}
