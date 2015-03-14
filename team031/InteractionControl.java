package team031;

import battlecode.common.*;
import java.math.*;
import java.util.*;

public class InteractionControl extends RadioControl
{
	//additional fields
	public double selfFluxThreshold;
	public double receiveFluxThreshold;
	public double creationFluxThreshold;
	public double sleepFluxThreshold;
	
	//constructor
	public InteractionControl(RobotController myRC, MapLocation pl, Direction pd, boolean ib, BugDirection bd, MapLocation bs, int bc)
	{
		super(myRC,pl,pd,ib,bd,bs,bc);
	}
	
	//the basic "move or turn right" code provided
	public void basicMove()
	{
		try 
		{
			while (myRC.isMovementActive()) 
			{
				myRC.yield();
			}
			if (myRC.canMove(myRC.getDirection())) 
			{
				myRC.moveForward();
			} 
			else 
			{
				myRC.setDirection(myRC.getDirection().rotateRight());
			}
		} 
		catch (Exception e) 
		{
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}
	
	//surround a target MapLocation with a given offset
	public void surroundLocation(MapLocation goal, int offset, SurroundDirection sd)
	{
		if(myRC.isMovementActive()==false)
		{
			Integer a = offset*offset;
			Integer b = 2*a;
			if(myRC.getLocation().distanceSquaredTo(goal)>b)
			{
				//move towards goal
				//might want to change to attack-move, flux-move, etc. later on
				move(goal);
			}
			else
			{
				//if you're a soldier, disrupter, or scorcher and you've reached the dead zone, and you don't see enemies, then spin around
				if(myRC.getType().equals(RobotType.SOLDIER) || myRC.getType().equals(RobotType.DISRUPTER) || myRC.getType().equals(RobotType.SCORCHER))
				{
					if(nearbyEnemies.length==0)
					{
						try
						{
							prevDir = myRC.getDirection();
							myRC.setDirection(myRC.getDirection().rotateRight().rotateRight());
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
				}
				else if(myRC.getType().equals(RobotType.ARCHON))
				{
					//if you're an archon, you're not facing a capturable power node, and the space in front is not free to build, then turn so you can build
					if(containsCPN(myRC.getLocation().add(myRC.getDirection()))==false && myRC.canMove(myRC.getDirection())==false)
					{
						try
						{
							prevDir = myRC.getDirection();
							myRC.setDirection(myRC.getDirection().rotateRight());
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
				}
				else
				{
					switch(sd)
					{
						case INWARD:
							if(myRC.getDirection().equals(myRC.getLocation().directionTo(goal).opposite()))
							{
								//do nothing
							}
							else if(myRC.getLocation().directionTo(goal)!=Direction.NONE && myRC.getLocation().directionTo(goal)!=Direction.OMNI)
							{
								try
								{
									prevDir = myRC.getDirection();
									myRC.setDirection(myRC.getLocation().directionTo(goal).opposite());
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
								// do nothing
							}
							break;
						case OUTWARD:
							if(myRC.getDirection().equals(myRC.getLocation().directionTo(goal)))
							{
								//do nothing
							}
							else if(myRC.getLocation().directionTo(goal)!=Direction.NONE && myRC.getLocation().directionTo(goal)!=Direction.OMNI)
							{
								try
								{
									prevDir = myRC.getDirection();
									myRC.setDirection(myRC.getLocation().directionTo(goal));
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
							break;
					}
				}
			}
		}
	}
	
	//flee direction score -- more negative is worse
	public int fleeDirectionScore(Direction dir)
	{
		int counter = 0;
		int nde = countEnemiesInDirection(nearbyEnemyInfo,dir);
		int ndeLeft = countEnemiesInDirection(nearbyEnemyInfo,dir.rotateLeft());
		int ndeRight = countEnemiesInDirection(nearbyEnemyInfo,dir.rotateRight());
		counter = counter - 4*nde;	//penalize the score 4x for every enemy in that direction
		counter = counter - 2*ndeLeft;
		counter = counter - 2*ndeRight;
		if(noObstaclesInDirection(myRC.getLocation(),dir))
		{
			counter++;	//if there are no obstacles in that direction, give +1 to the score
		}
		if(myRC.canMove(dir))
		{
			counter++;	//if the space is free to move currently, also give +1 to the score
		}
		if(countAlliesInDirection(dangerousAlliedInfo,dir)>0)
		{
			counter++;	//if there are allies in that direction, also give +1 to the score
		}
		return counter;
	}
	
	//returns RobotLevel given what type of robot it is
	public RobotLevel getRobotLevel(RobotType rt)
	{
		if(rt.isAirborne())
		{
			return RobotLevel.IN_AIR;
		}
		else if(rt.equals(RobotType.TOWER))
		{
			return RobotLevel.POWER_NODE;
		}
		else
		{
			return RobotLevel.ON_GROUND;
		}
	}
}