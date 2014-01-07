package team140.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

/**
 * A function that accepts or declines particular types of robots.
 * 
 */
public abstract class RobotFilter {

  public abstract boolean accept(RobotInfo robot);
  
  /**
   * @return an array of items that match the filter.
   */
  public static RobotInfo[] filter(RobotInfo[] robots, RobotFilter filter) {
    int count = 0;
    for (int i=0; i<robots.length; i++) {
      count +=  filter.accept(robots[i]) ? 1 : 0;
    }
    RobotInfo[] matches = new RobotInfo[count];
    int m = 0;
    for (int i=0; i<robots.length; i++) {
      if (filter.accept(robots[i])) {
        matches[m++] = robots[i];
      }
    }
    return matches;
  }
  
  /**
   * Looks for robots of a particular type and team.
   * 
   * to match all types or all teams, pass in null for that argument.
   */
  public static class is extends RobotFilter {
    private final RobotType type;
    private final Team team;
    
    public is(RobotType type, Team team) { 
      this.team = team; 
      this.type = type;
    }
    
    public boolean accept(RobotInfo robot) { 
      return (type == null || robot.type == type) && (team == null || robot.team == team); 
    }
  }
  
  public static class isNot extends RobotFilter {
    private final RobotType type;
    private final Team team;
    
    public isNot(RobotType type, Team team) { 
      this.team = team; 
      this.type = type;
    }
    
    public boolean accept(RobotInfo robot) { 
      return (type == null || robot.type != type) && (team == null || robot.team != team); 
    }
  }
  
  /**
   * ground-level robots
   * 
   * pass any parameter null if you don't want to filter based on it
   */
  public static class ground extends RobotFilter {
    private final Team team;
    private final RobotType type;
    
    public ground(RobotType type, Team team) {
      this.team = team;
      this.type = type;
    }

    @Override
    public boolean accept(final RobotInfo robot) {
      return    (team == null || robot.team == team)
             && (type == null || robot.type == type)
             && (!robot.type.isAirborne());
    }
  }
  
  /**
   * Looks for wounded robots.
   */
  public static class wounded extends RobotFilter {
    private final Team team;  // required
    private final double threshold; // required
    private RobotType type = null; // optional
    
    public wounded(Team team, double threshold) {
      this.team = team;
      this.threshold = threshold;
    }
    
    public wounded(Team team, double percent_wounded_thresh, RobotType type) {
      this.team = team;
      this.threshold = percent_wounded_thresh;
      this.type = type;
    }
    
    public boolean accept(RobotInfo robot) {
      return robot.team == team && robot.energon <= threshold*robot.type.maxEnergon && (type == null || robot.type == type);
    }
  }
  
  /**
   * Looks for non-archon, non-tower units with flux < threshold.
   */
  public static class lowFlux extends RobotFilter {
    private final Team team;
    private final double thresh;
    
    public lowFlux(Team team, double threshold) {
      this.team = team;
      this.thresh = threshold;
    }
    
    public boolean accept(RobotInfo robot) {
      return robot.team == team && robot.type != RobotType.ARCHON && robot.type != RobotType.TOWER && robot.flux < thresh;
    }
  }
  
  /**
   * Looks for units adjacent to the current position.
   */
  public static class adjacent extends RobotFilter {
    private final MapLocation loc;
    private final Team team;
    
    public adjacent(MapLocation loc, Team team) {
      this.loc = loc;
      this.team = team;
    }
    
    @Override
    public boolean accept(RobotInfo robot) {
      return robot.team == team && robot.location.isAdjacentTo(loc);
    }
  }
  
  
  public static class onOrAdjacent extends RobotFilter {
    private final MapLocation loc;
    private final Team team;
    
    public onOrAdjacent(MapLocation loc, Team team) {
      this.loc = loc;
      this.team = team;
    }
    
    @Override
    public boolean accept(RobotInfo robot) {
      return robot.team == team && (robot.location.isAdjacentTo(loc) || robot.location.equals(loc));
    }
  }
}
