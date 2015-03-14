package team031;

import battlecode.common.*;

public class RobotPlayer 
{
	
	
	public static void run(RobotController myRC) 
	{
		//this runs once when the robot is initialized
		MapLocation prevLocation = myRC.getLocation();
		Direction prevDirection = myRC.getDirection();
		boolean isBugging = false;
		BugDirection bugDir = BugDirection.RIGHTWALL;
		MapLocation bugStart = myRC.getLocation();
		int buggerCount = 0;
		
		while (true) 
		{
			try 
			{
				RobotType rt = myRC.getType();
				switch(rt)
				{
					case ARCHON:
						ArchonControl ac = new ArchonControl(myRC,prevLocation,prevDirection,isBugging,bugDir,bugStart,buggerCount);
						ac.Go();
						prevLocation = ac.prevLoc;
						prevDirection = ac.prevDir;
						isBugging = ac.isBugging;
						bugDir = ac.bugDir;
						bugStart = ac.bugStart;
						buggerCount = ac.buggerCount;
						myRC.yield();
						break;
					case SCOUT:
						ScoutControl scoutc = new ScoutControl(myRC,prevLocation,prevDirection,isBugging,bugDir,bugStart,buggerCount);
						scoutc.Go();
						prevLocation = scoutc.prevLoc;
						prevDirection = scoutc.prevDir;
						isBugging = scoutc.isBugging;
						bugDir = scoutc.bugDir;
						bugStart = scoutc.bugStart;
						buggerCount = scoutc.buggerCount;
						myRC.yield();
						break;
					case SOLDIER:
						SoldierControl soldc = new SoldierControl(myRC,prevLocation,prevDirection,isBugging,bugDir,bugStart,buggerCount);
						soldc.Go();
						prevLocation = soldc.prevLoc;
						prevDirection = soldc.prevDir;
						isBugging = soldc.isBugging;
						bugDir = soldc.bugDir;
						bugStart = soldc.bugStart;
						buggerCount = soldc.buggerCount;
						myRC.yield();
						break;
					case DISRUPTER:
						DisrupterControl dc = new DisrupterControl(myRC,prevLocation,prevDirection,isBugging,bugDir,bugStart,buggerCount);
						dc.Go();
						prevLocation = dc.prevLoc;
						prevDirection = dc.prevDir;
						isBugging = dc.isBugging;
						bugDir = dc.bugDir;
						bugStart = dc.bugStart;
						buggerCount = dc.buggerCount;
						myRC.yield();
						break;
					case SCORCHER:
						ScorcherControl scc = new ScorcherControl(myRC,prevLocation,prevDirection,isBugging,bugDir,bugStart,buggerCount);
						scc.Go();
						prevLocation = scc.prevLoc;
						prevDirection = scc.prevDir;
						isBugging = scc.isBugging;
						bugDir = scc.bugDir;
						bugStart = scc.bugStart;
						buggerCount = scc.buggerCount;
						myRC.yield();
						break;
				}
			} 
			catch (Exception e) 
			{
				System.out.println("caught exception:");
				e.printStackTrace();
				myRC.setIndicatorString(2,"exception: " + e.toString());
			}
		}
	}
}
