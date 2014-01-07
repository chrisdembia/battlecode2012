package team140.controller;

import team140.actions.Healer;
import team140.actions.Mover;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Scout extends Brain {

  private final Healer healer;
  private boolean inBattleGroup;
  
  public Scout(RobotController rc) {
    super(rc);

    this.healer = new Healer(rc, sensor, pathfinder);
  }
  
  protected Mover getMover() {return healer;}

  @Override
  public void takeOneTurn() throws GameActionException {

    decideStrategy();


    switch (strategy){
    case DEFENSIVE:
      if (   inBox.commandLocation != null 
      && healer.moveTo(inBox.commandLocation)) { rc.setIndicatorString(1, "1: moveToCommanded");}
      healer.healAtWill();
      healer.tryFluxTransfer(20, 20);
      break;
    case OFFENSIVE:
      if (healer.moveTo(sensor.getClosestAlliedArchon())) {return;}
      if (healer.chaseAndHealUnits()) {return;}

      break;
    case BATTLEGROUP:
      if (   inBox.commandLocation != null 
      && healer.moveTo(inBox.commandLocation)) { rc.setIndicatorString(1, "1: moveToCommanded");}
      healer.healAtWill();
      healer.tryFluxTransfer(20, 20);
      if (goingToCommanded()) { rc.setIndicatorString(1, "moving to command location"); return; }
      if (turningToCommanded()) { rc.setIndicatorString(1, "rotating to command direction"); return; }
      break;
    }
  }


  @Override
  public void decideStrategy() throws GameActionException {
    if (inBattleGroup) {
      strategy = Strategy.BATTLEGROUP;
      return;
    }
    if (inBox.commandLocation != null) {
      inBattleGroup = true;
      strategy = Strategy.BATTLEGROUP;
    } else {
      strategy = Strategy.OFFENSIVE;   
    }
  }

  @Override
  protected boolean shouldReceiveMessages() {
    return true;
  }

  @Override
  protected boolean shouldSendMessages() {
    return false;
  }

}
