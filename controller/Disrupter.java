package team140.controller;

import team140.actions.Mover;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Disrupter extends Soldier {
	
	public Disrupter(RobotController rc) {
		super(rc);
	}

	@Override
	public void takeOneTurn() throws GameActionException {
		super.takeOneTurn();
	}

  @Override
  public void decideStrategy() throws GameActionException {
    super.decideStrategy();
  }

  @Override
  protected Mover getMover() {
    return super.getMover();
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
