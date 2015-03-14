package team031;

import battlecode.common.*;
import java.math.*;
import java.util.*;

public class DisrupterControl extends InteractionControl
{
	//additional fields
	
	//constructor
	public DisrupterControl(RobotController myRC, MapLocation pl, Direction pd, boolean ib, BugDirection bd, MapLocation bs, int bc)
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
		sleepFluxThreshold = RobotType.DISRUPTER.moveCost; 
		
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
	
	//attack-move command
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
	
	//attack command
	public void attack()
	{
		if(myRC.isAttackActive()==false)
		{
			RobotInfo[] ae = getAttackableEnemies(nearbyEnemyInfo);
			if(ae.length>0)
			{
				//first, look for enemy scorchers
				int index = 0;
				while(index<ae.length-1 && ae[index].type.equals(RobotType.SCORCHER)==false)
				{
					index++;
				}
				if(index<ae.length-1)
				{
					//then attack the enemy scorcher
					MapLocation theLocation = ae[index].location;
					RobotType theType = ae[index].type;
					RobotLevel theLevel = RobotLevel.ON_GROUND; 
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
				else if(ae[index].type.equals(RobotType.SCORCHER)==true)
				{
					//then attack the enemy scorcher
					MapLocation theLocation = ae[index].location;
					RobotType theType = ae[index].type;
					RobotLevel theLevel = RobotLevel.ON_GROUND; 
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
				else
				{
					//if you found no scorchers, look for soldiers
					index = 0;
					while(index<ae.length-1 && ae[index].type.equals(RobotType.SOLDIER)==false)
					{
						index++;
					}
					if(index<ae.length-1)
					{
						//then attack the enemy soldier
						MapLocation theLocation = ae[index].location;
						RobotType theType = ae[index].type;
						RobotLevel theLevel = RobotLevel.ON_GROUND; 
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
					else if(ae[index].type.equals(RobotType.SOLDIER)==true)
					{
						//then attack the enemy scorcher
						MapLocation theLocation = ae[index].location;
						RobotType theType = ae[index].type;
						RobotLevel theLevel = RobotLevel.ON_GROUND; 
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
					else
					{
						//if you found no archons and no scorchers, just attack the thing with the lowest hp
						double[] enemyHP = getEnemyHP(ae);
						int theIndex = indexOfSmallest(enemyHP);
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
		}
	}
	
	//Given a RobotInfo[] array of enemy info, returns a double[] array of enemy HP
	public double[] getEnemyHP(RobotInfo[] enemyInfo)
	{
		int index = 0;
		double[] enemyHP = new double[enemyInfo.length];
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			for(RobotInfo theInfo : enemyInfo)
			{
				enemyHP[index] = theInfo.energon;
				index++;
			}
		}
		return enemyHP;
	}
}