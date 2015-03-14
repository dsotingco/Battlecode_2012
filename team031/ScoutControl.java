package team031;

import battlecode.common.*;
import java.math.*;
import java.util.*;

public class ScoutControl extends InteractionControl
{
	//additional fields
	
	//constructor
	public ScoutControl(RobotController myRC, MapLocation pl, Direction pd, boolean ib, BugDirection bd, MapLocation bs, int bc)
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
		sleepFluxThreshold = RobotType.SCOUT.moveCost; 
		
		standardSense();
		
		if(myRC.getFlux()>sleepFluxThreshold && myRC.senseAlliedArchons().length>0)
		{
			regenAttackMove(getNearest(myRC.senseAlliedArchons()));
		}
		else if(myRC.getFlux()>sleepFluxThreshold)
		{
			regenAttackMove(getNearest(cpn));
		}
	}
	
	//regenerate-move command
	public void regenAttackMove(MapLocation goal)
	{
		RobotInfo[] ae = getAttackableEnemies(nearbyEnemyInfo);
		if(numAlliesDamagedInRange(nearbyAlliedInfo)>0)
		{
			regenerateIfAble(nearbyAlliedInfo);
		}
		else if(numAlliesDamaged(nearbyAlliedInfo)>0)
		{
			//move towards the nearest damaged ally
			move(getNearest(getDamagedAlliedLocations(nearbyAlliedInfo)));
		}
		else if(ae.length>0)
		{
			attack();
		}
		else if(nearbyEnemies.length>0)
		{
			move(getNearest(nearbyEnemyLocations));
		}
		else
		{
			//continue to your goal
			move(goal);
		}
	}
	
	//regenerate if able
	public void regenerateIfAble(RobotInfo[] alliedInfo)
	{
		if(numAlliesDamagedInRange(alliedInfo)>0 && myRC.getFlux()>GameConstants.REGEN_COST)
		{
			if(numAlliesDamaged(alliedInfo)>0)
			{
				try
				{
					myRC.regenerate();
				}
				catch (Exception e)
				{
					System.out.println("Caught exception: ");
					e.printStackTrace();
					myRC.setIndicatorString(2,"exception: " + e.toString());
				}
			}
		}
	}
	
	//attack command
	public void attack()
	{
		if(myRC.isAttackActive()==false)
		{
			RobotInfo[] ae = getAttackableEnemies(nearbyEnemyInfo);
			if(ae.length>0)
			{
				//attack the thing with the lowest flux that is greater than zero
				double[] enemyFlux = getEnemyFlux(ae);
				int theIndex = indexOfSmallestNonzero(enemyFlux);
				MapLocation theLocation = ae[theIndex].location;
				RobotType theType = ae[theIndex].type;
				RobotLevel theLevel = RobotLevel.ON_GROUND; 
				if(theType.isAirborne())
				{
					theLevel = RobotLevel.IN_AIR;
				}
				try
				{
					myRC.attackSquare(theLocation,theLevel);
				}
				catch (Exception e)
				{
					System.out.println("Caught exception: ");
					e.printStackTrace();
					myRC.setIndicatorString(2,"exception: " + e.toString());
				}
			}
		}
	}
	
	//Given a RobotInfo[] array of enemy info, returns a double[] array of enemy flux
	public double[] getEnemyFlux(RobotInfo[] enemyInfo)
	{
		int index = 0;
		double[] enemyFlux = new double[enemyInfo.length];
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			for(RobotInfo theInfo : enemyInfo)
			{
				enemyFlux[index] = theInfo.flux;
				index++;
			}
		}
		return enemyFlux;
	}
	
	//Given a list of damaged allies, returns their locations
	public MapLocation[] getDamagedAlliedLocations(RobotInfo[] damagedAllies)
	{
		MapLocation[] res = new MapLocation[damagedAllies.length];
		int index = 0;
		if(damagedAllies!=null && damagedAllies.length>0)
		{
			for(RobotInfo theInfo : damagedAllies)
			{
				res[index] = theInfo.location;
				index++;
			}
		}
		return res;
	}
	
	//returns a RobotInfo[] array of damaged allies
	public RobotInfo[] getDamagedAllies(RobotInfo[] alliedInfo)
	{
		int nad = numAlliesDamaged(alliedInfo);
		RobotInfo[] res = new RobotInfo[nad];
		int index = 0;
		for(RobotInfo theInfo : alliedInfo)
		{
			if(theInfo.energon<theInfo.type.maxEnergon)
			{
				res[index] = theInfo;
				index++;
			}
		}
		return res;
	}
	
	//count the number of damaged allies
	public int numAlliesDamaged(RobotInfo[] alliedInfo)
	{
		int counter = 0;
		for(RobotInfo theInfo : alliedInfo)
		{
			if(theInfo.energon<theInfo.type.maxEnergon)
			{
				counter++;
			}
		}
		return counter;
	}
	
	//count the number of damaged allies in range
	public int numAlliesDamagedInRange(RobotInfo[] alliedInfo)
	{
		int counter = 0;
		for(RobotInfo theInfo : alliedInfo)
		{
			if(theInfo.energon<theInfo.type.maxEnergon && myRC.getLocation().distanceSquaredTo(theInfo.location)<=myRC.getType().attackRadiusMaxSquared)
			{
				counter++;
			}
		}
		return counter;
	}
	
}