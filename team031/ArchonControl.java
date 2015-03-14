package team031;

import battlecode.common.*;
import java.math.*;
import java.util.*;

public class ArchonControl extends InteractionControl
{
	//additional fields
	
	//constructor
	public ArchonControl(RobotController myRC, MapLocation pl, Direction pd, boolean ib, BugDirection bd, MapLocation bs, int bc)
	{
		super(myRC,pl,pd,ib,bd,bs,bc);
	}
	
	//main method
	public void Go()
	{
		standardSense();
		//set flux thresholds
		selfFluxThreshold = RobotType.SCOUT.maxFlux;
		receiveFluxThreshold = RobotType.SCOUT.maxFlux/5; //should be below creationFluxThreshold
		creationFluxThreshold = 25; //flux to give to a newly created unit
		sleepFluxThreshold = -1; //we don't ever want the archons to sleep
		
		if(dangerousEnemyInfo.length>0)
		{
			//if you see dangerous enemies, run away
				//move(myRC.getLocation().add(dirMostEnemies(nearbyEnemyInfo).opposite(),5));
			
			//message sending
			sendMessage(getNearest(nearbyEnemyLocations),"ATTACKERS","ATTACKMOVE");
			
			move(myRC.getLocation().add(fleeDirection(),5));
		}
		else if(nearbyEnemies.length>0)
		{
			//message sending
			sendMessage(getNearest(nearbyEnemyLocations),"ATTACKERS","ATTACKMOVE");
			
			//if you see enemies but they aren't dangerous, build soldiers
			int soldierThreshold = 6;
			if(countAlliesOfType(nearbyAlliedInfo,RobotType.SOLDIER)<soldierThreshold)
			{
				if(containsCPN(myRC.getLocation().add(myRC.getDirection()))==false) //don't build soldiers on top of capturable tower nodes
				{
					buildIfAble(RobotType.SOLDIER);
				}
			}
			int scoutThreshold = 3;
			if(countAlliesOfType(nearbyAlliedInfo,RobotType.SOLDIER)>=soldierThreshold && countAlliesOfType(nearbyAlliedInfo,RobotType.SCOUT)<scoutThreshold)
			{
				buildIfAble(RobotType.SCOUT);
			}
			if(amArchonWithLowestID())
			{
				fluxMove(getNearest(cpn),1,SurroundDirection.INWARD);
			}
			else 
			{
				fluxMove(getNearest(cpn),2,SurroundDirection.OUTWARD);
			}
		}
		else if(containsCPN(myRC.getLocation()))
		{
			//if you're on top of a capturable power node, go away
			fluxMove(getFarthest(cpn),2,SurroundDirection.OUTWARD);
		}
		else
		{
			//if you see no enemies, build towers if you can and then build soldiers
			buildIfAble(RobotType.TOWER);
			int soldierThreshold = 6;
			if(countAlliesOfType(nearbyAlliedInfo,RobotType.SOLDIER)<soldierThreshold)
			{
				if(containsCPN(myRC.getLocation().add(myRC.getDirection()))==false) //don't build soldiers on top of capturable tower nodes
				{
					buildIfAble(RobotType.SOLDIER);
				}
			}
			int scoutThreshold = 3;
			if(countAlliesOfType(nearbyAlliedInfo,RobotType.SOLDIER)>=soldierThreshold && countAlliesOfType(nearbyAlliedInfo,RobotType.SCOUT)<scoutThreshold)
			{
				buildIfAble(RobotType.SCOUT);
			}
			if(amArchonWithLowestID())
			{
				//fluxMove(getNearest(cpn),1,SurroundDirection.INWARD);
				giveFluxIfAble3(nearbyAlliedInfo);
				move(getNearest(cpn));
			}
			else 
			{
				fluxMove(getNearest(cpn),2,SurroundDirection.OUTWARD);
			}
		}
	}
	
	//build the specified thing if you can
	public void buildIfAble(RobotType rt)
	{
		if(myRC.isMovementActive()==false)
		{
			switch(rt)
			{
				case ARCHON:
					//do nothing; you can't build an archon
					break;
				case SCOUT:
					//for scouts, need to check if the air is already occupied
					if(frontSquareHasScout()==false)
					{
						if(myRC.getFlux()>=(rt.spawnCost+creationFluxThreshold) && myRC.canMove(myRC.getDirection()))
						{
							try
							{
								myRC.spawn(rt);
							}
							catch (Exception e)
							{
								System.out.println("Caught exception: ");
								e.printStackTrace();
								myRC.setIndicatorString(2,"exception: " + e.toString());
							}
						}
					}
					break;
				case TOWER:
					//first, check to see that you have enough flux, and that the space is unoccupied
					if(myRC.getFlux()>=rt.spawnCost && myRC.canMove(myRC.getDirection()))
					{
						//then, check to see if you can build a tower
						MapLocation[] oknodes = myRC.senseCapturablePowerNodes();
						for(MapLocation node:oknodes)
						{
							//if the MapLocation you're facing matches one among the list of capturable nodes, then build a tower
							if (node.equals(myRC.getLocation().add(myRC.getDirection())) && myRC.canMove(myRC.getDirection()))
							{
								try
								{
									myRC.spawn(rt);
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
					break;
				default:
					//if the space is free and you have enough flux, then build the thing
					if(myRC.getFlux()>=(rt.spawnCost+creationFluxThreshold) && myRC.canMove(myRC.getDirection()))
					{
						try
						{
							myRC.spawn(rt);
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					break;
			}
		}
	}
	
	//flux-move command
	public void fluxMove(MapLocation goal, int offset, SurroundDirection sd)
	{
		RobotInfo[] nfa = needFluxAllies(nearbyAlliedInfo);
		RobotInfo[] nfair = needFluxAlliesInRange(nfa);
		if(nfair.length>0)
		{
			giveFluxIfAble3(nfair);
		}
		else if(nfa.length>0)
		{
			move(getNearest(RIarray2MLarray(nfa)));
		}
		else
		{
			surroundLocation(goal,offset,sd);
		}
	}
	
	//gives flux to robots that need it, if able to, trial 3
	public void giveFluxIfAble3(RobotInfo[] alliedInfo)
	{
		RobotInfo[] nfa = needFluxAllies(alliedInfo);
		RobotInfo[] nfair = needFluxAlliesInRange(nfa);
		for (RobotInfo theInfo : nfair)
		{
			if(myRC.getFlux()>selfFluxThreshold)
			{
				double theAmount = Math.min(myRC.getFlux(),creationFluxThreshold-theInfo.flux);
				MapLocation theLoc = theInfo.location;
				RobotLevel theLevel = getRobotLevel(theInfo.type);
				try
				{
					myRC.setIndicatorString(2,"Tried to transfer flux to: " + locToString(theLoc));
					myRC.transferFlux(theLoc,theLevel,theAmount);
				}
				catch (Exception e)
				{
					System.out.println("Caught exception: ");
					e.printStackTrace();
					myRC.setIndicatorString(2,"exception: " + e.toString() + "Tried to transfer flux to: " + locToString(theLoc));
				}
			}
		}
	}
	
	/*
	//gives flux to robots that need it, if able to, REDO
	public void giveFluxIfAble2(RobotInfo[] alliedInfo)
	{
		if(myRC.getFlux()>selfFluxThreshold)
		{
			int numNeedFluxAllies = countNeedFluxAllies(alliedInfo);
			if(numNeedFluxAllies>0)
			{
				RobotInfo[] nfa = needFluxAllies(alliedInfo);
				RobotInfo theInfo = nfa[0];
				if(canReceiveFlux(theInfo))
				{
					if(myRC.getFlux()>selfFluxThreshold)
					{
						double theAmount = Math.min(myRC.getFlux(),creationFluxThreshold-theInfo.flux);
						if (theAmount>0)
						{
							MapLocation theLoc = theInfo.location;
							if(theLoc.equals(myRC.getLocation()) || theLoc.isAdjacentTo(myRC.getLocation()))
							{
								RobotLevel theLevel = getRobotLevel(theInfo.type);
								try
								{
									myRC.setIndicatorString(2,"Tried to transfer flux to: " + locToString(theLoc));
									myRC.transferFlux(theLoc,theLevel,theAmount);
								}
								catch (Exception e)
								{
									System.out.println("Caught exception: ");
									e.printStackTrace();
									myRC.setIndicatorString(2,"exception: " + e.toString() + "Tried to transfer flux to: " + locToString(theLoc));
								}
							}
						}
					}
				}
			}
		}
	}
	*/
	
	//Returns a RobotInfo array of allies that need flux and are also within range
	public RobotInfo[] needFluxAlliesInRange(RobotInfo[] nfa)
	{
		RobotInfo[] nfair = new RobotInfo[countNeedFluxAlliesInRange(nfa)];
		int index = 0;
		for(RobotInfo theInfo : nfa)
		{
			if(canReceiveFlux(theInfo))
			{
				nfair[index] = theInfo;
				index++;
			}
		}
		return nfair;
	}
	
	//Returns a RobotInfo array of allies that need flux
	public RobotInfo[] needFluxAllies(RobotInfo[] alliedInfo)
	{
		RobotInfo[] nfa = new RobotInfo[countNeedFluxAllies(alliedInfo)];
		int index = 0;
		for (RobotInfo theInfo : alliedInfo)
		{
			if (theInfo.flux<receiveFluxThreshold)
			{
				if(theInfo.type.equals(RobotType.TOWER)==false)
				{
					if(theInfo.type.equals(RobotType.ARCHON)==false)
					{
						nfa[index] = theInfo;
						index++;
					}
				}
			}
		}
		return nfa;
	}
	
	//Given RobotInfo, returns true if the robot is eligible to receive flux from the calling robot.  Does not take into account whether the calling robot has enough flux to give.
	public boolean canReceiveFlux(RobotInfo ri)
	{
		//To be eligible to receive flux, the receiving robot must be adjacent to or at the same location as the giving robot, and also not an archon or a tower
		if(myRC.getLocation().equals(ri.location) || myRC.getLocation().isAdjacentTo(ri.location))
		{
			if(ri.type.equals(RobotType.TOWER)==false)
			{
				if(ri.type.equals(RobotType.ARCHON)==false)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	//counts the number of allied robots that need flux and are also within range
	public int countNeedFluxAlliesInRange(RobotInfo[] nfa)
	{
		int counter = 0;
		if(nfa.length>0)
		{
			for (RobotInfo theInfo : nfa)
			{
				if(canReceiveFlux(theInfo))
				{
					counter++;
				}
			}
		}
		return counter;
	}
	
	//counts the number of allied robots with flux below the receiveFluxThreshold.  Does not include towers or archons.
	public int countNeedFluxAllies(RobotInfo[] alliedInfo)
	{
		int counter = 0;
		if(alliedInfo.length>0)
		{
			for (RobotInfo theInfo : alliedInfo)
			{
				if(theInfo.flux<receiveFluxThreshold)
				{
					if(theInfo.type.equals(RobotType.TOWER)==false)
					{
						if(theInfo.type.equals(RobotType.ARCHON)==false)
						{
							counter++;
						}
					}
				}
			}
		}
		return counter;
	}
	
	//determines if I am the archon with the lowest game ID
	public boolean amArchonWithLowestID()
	{
		RobotInfo[] theArchons = senseAlliesOfType(nearbyAlliedInfo, RobotType.ARCHON);
		int counter = 0;
		if(theArchons!=null && theArchons.length>0)
		{
			for(RobotInfo arch : theArchons)
			{
				if(arch.robot.getID()<myRC.getRobot().getID())
				{
					counter++;
				}
			}
		}
		if(counter==0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//determines if the square in front has a scout already
	public boolean frontSquareHasScout()
	{
		GameObject theObject = null;
		if(myRC.canSenseSquare(myRC.getLocation().add(myRC.getDirection())))
		{
			try
			{
				theObject = myRC.senseObjectAtLocation(myRC.getLocation().add(myRC.getDirection()),RobotLevel.IN_AIR);
			}
			catch (Exception e)
			{
				System.out.println("Caught exception: ");
				e.printStackTrace();
				myRC.setIndicatorString(2,"exception: " + e.toString());
			}
			if(theObject!=null)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	//determines flee direction
	public Direction fleeDirection()
	{
		double[] fleeScores = new double[8];
		fleeScores[0] = (double)fleeDirectionScore(Direction.NORTH);
		fleeScores[1] = (double)fleeDirectionScore(Direction.NORTH_EAST);
		fleeScores[2] = (double)fleeDirectionScore(Direction.EAST);
		fleeScores[3] = (double)fleeDirectionScore(Direction.SOUTH_EAST);
		fleeScores[4] = (double)fleeDirectionScore(Direction.SOUTH);
		fleeScores[5] = (double)fleeDirectionScore(Direction.SOUTH_WEST);
		fleeScores[6] = (double)fleeDirectionScore(Direction.WEST);
		fleeScores[7] = (double)fleeDirectionScore(Direction.NORTH_WEST);
		int indexBestDirection = indexOfBiggest(fleeScores);
		if(indexBestDirection==0)
		{
			return Direction.NORTH;
		}
		else if(indexBestDirection==1)
		{
			return Direction.NORTH_EAST;
		}
		else if(indexBestDirection==2)
		{
			return Direction.EAST;
		}
		else if(indexBestDirection==3)
		{
			return Direction.SOUTH_EAST;
		}
		else if(indexBestDirection==4)
		{
			return Direction.SOUTH;
		}
		else if(indexBestDirection==5)
		{
			return Direction.SOUTH_WEST;
		}
		else if(indexBestDirection==6)
		{
			return Direction.WEST;
		}
		else if(indexBestDirection==7)
		{
			return Direction.NORTH_WEST;
		}
		else
		{
			//if all else fails, go home
			return myRC.getLocation().directionTo(myRC.sensePowerCore().getLocation());
		}
	}
	
	//determines which direction has the most enemies
	public Direction dirMostEnemies(RobotInfo[] enemyInfo)
	{
		double[] enemyDirections = new double[8];
		enemyDirections[0] = (double)countEnemiesInDirection(enemyInfo,Direction.NORTH);
		enemyDirections[1] = (double)countEnemiesInDirection(enemyInfo,Direction.NORTH_EAST);
		enemyDirections[2] = (double)countEnemiesInDirection(enemyInfo,Direction.EAST);
		enemyDirections[3] = (double)countEnemiesInDirection(enemyInfo,Direction.SOUTH_EAST);
		enemyDirections[4] = (double)countEnemiesInDirection(enemyInfo,Direction.SOUTH);
		enemyDirections[5] = (double)countEnemiesInDirection(enemyInfo,Direction.SOUTH_WEST);
		enemyDirections[6] = (double)countEnemiesInDirection(enemyInfo,Direction.WEST);
		enemyDirections[7] = (double)countEnemiesInDirection(enemyInfo,Direction.NORTH_WEST);
		int indexMaxEnemies = indexOfSmallestNonzero(enemyDirections);
		if(indexMaxEnemies==0)
		{
			return Direction.NORTH;
		}
		else if(indexMaxEnemies==1)
		{
			return Direction.NORTH_EAST;
		}
		else if(indexMaxEnemies==2)
		{
			return Direction.EAST;
		}
		else if(indexMaxEnemies==3)
		{
			return Direction.SOUTH_EAST;
		}
		else if(indexMaxEnemies==4)
		{
			return Direction.SOUTH;
		}
		else if(indexMaxEnemies==5)
		{
			return Direction.SOUTH_WEST;
		}
		else if(indexMaxEnemies==6)
		{
			return Direction.WEST;
		}
		else if(indexMaxEnemies==7)
		{
			return Direction.NORTH_WEST;
		}
		else
		{
			return Direction.NONE;
		}
	}
}