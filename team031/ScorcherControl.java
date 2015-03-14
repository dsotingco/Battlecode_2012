package team031;

import battlecode.common.*;
import java.math.*;
import java.util.*;

public class ScorcherControl extends InteractionControl
{
	//additional fields
	
	//constructor
	public ScorcherControl(RobotController myRC, MapLocation pl, Direction pd, boolean ib, BugDirection bd, MapLocation bs, int bc)
	{
		super(myRC,pl,pd,ib,bd,bs,bc);
	}
	
	//main method
	public void Go()
	{
		//set flux thresholds
		selfFluxThreshold = RobotType.SCOUT.maxFlux;
		receiveFluxThreshold = RobotType.SCOUT.maxFlux/5; //should be below creationFluxThreshold
		creationFluxThreshold = 25; //flux to give to a newly created unit
		sleepFluxThreshold = RobotType.SCORCHER.moveCost; 
		
		standardSense();
		
		if(myRC.getFlux()>sleepFluxThreshold)
		{
			if(containsCPN(myRC.getLocation()))	//if you're sitting on top of a capturable power node, go away
			{
				attackMove(getFarthest(cpn),2,SurroundDirection.OUTWARD);
			}
			else
			{
				attackMove(getNearest(cpn),2,SurroundDirection.OUTWARD);
				//surroundLocation(getNearest(cpn),2,SurroundDirection.OUTWARD);
			}
		}
	}
	
	public void attackMove(MapLocation goal, int offset, SurroundDirection sd)
	{
		RobotInfo[] ae = getAttackableEnemies(nearbyEnemyInfo);
		if(ae.length>0)
		{
			attack();
		}
		else if(nearbyEnemies.length>0)
		{
			//you see enemies, but they are not within attack range, so move towards the closest one
			move(getNearest(nearbyEnemyLocations));
		}
		else
		{
			//you see no enemies, so proceed towards the goal
			surroundLocation(goal,offset,sd);
		}
	}
	
	public void attack()
	{
		if(myRC.isAttackActive()==false)
		{
			RobotInfo[] ae = getAttackableEnemies(nearbyEnemyInfo);
			if(ae.length>0)
			{
				if(countAlliesOfType(nearbyAlliedInfo,RobotType.ARCHON)==0)	//don't want to ever attack our own archons
				{
					int numGroundAllies = nearbyAllies.length - countAlliesOfType(nearbyAlliedInfo,RobotType.SCOUT);
					int numGroundEnemies = nearbyEnemies.length - countAlliesOfType(nearbyEnemyInfo,RobotType.SCOUT);
					if(numGroundAllies==0 && numGroundEnemies>0)
					{
						//if there are no allies in range but there are enemies, then just attack
						//since scorchers have the splash damage thing, just attack the square in front of you to save computation
						try
						{
							myRC.attackSquare(myRC.getLocation().add(myRC.getDirection()),RobotLevel.ON_GROUND);
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					else if(numGroundEnemies>=(3*numGroundAllies))
					{
						//if enemies outnumber allies by 3 to 1, then fire anyway
						try
						{
							myRC.attackSquare(myRC.getLocation().add(myRC.getDirection()),RobotLevel.ON_GROUND);
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					else
					{
						//do nothing 
					}
				}
			}
		}
	}
	
}
