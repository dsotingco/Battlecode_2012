package team031;

import battlecode.common.*;
import java.math.*;
import java.util.*;

public class Pathfinder extends SensingControl
{
	//additional fields
	public MapLocation prevLoc;
	public Direction prevDir;
	public boolean isBugging;
	public BugDirection bugDir;
	public MapLocation bugStart;
	public int buggerCount;
	
	//constructor
	public Pathfinder(RobotController myRC, MapLocation pl, Direction pd, boolean ib, BugDirection bd, MapLocation bs, int bc)
	{
		super(myRC);
		prevLoc = pl;
		prevDir = pd;
		isBugging = ib;
		bugDir = bd;
		bugStart = bs;
		buggerCount = bc;
	}
	
	
	//move command
	public void move(MapLocation goal)
	{
		if(myRC.isMovementActive()==false)
		{
			Direction dtg = myRC.getLocation().directionTo(goal);
			if(isBugging==false)
			{
				if(dtg.equals(Direction.NONE) || dtg.equals(Direction.OMNI))
				{
					// do nothing
				}
				else if(myRC.getLocation().equals(goal) || (myRC.getLocation().add(myRC.getDirection()).equals(goal)))
				{
					//don't move; you've reached your goal
				}
				else if(clearPathToGoal(myRC.getLocation(),goal)==false)
				{
					//if there are no obstacles in the direction to the goal, then try to move in that direction
					if(noObstaclesInDirection(myRC.getLocation(),dtg) && myRC.canMove(dtg)) 
					{
						//if the way to the waypoint is clear as far as you can see, then try to move in that direction
						myRC.setIndicatorString(1,"1");
						turnOrMove(dtg);
					}
					//if there are no obstacles in the diagonal right to the goal, then try to move in that direction
					else if(noObstaclesInDirection(myRC.getLocation(),dtg.rotateRight()) && myRC.canMove(dtg.rotateRight()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateRight()))==false)
					{
						myRC.setIndicatorString(1,"2");
						turnOrMove(dtg.rotateRight());
					}
					//also check to see if there are no obstacles starting in the square to the right, in the direction to the goal; if so, also move diagonally right
					else if(noObstaclesInDirection(myRC.getLocation().add(dtg.rotateRight().rotateRight()),dtg) && myRC.canMove(dtg.rotateRight()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateRight()))==false)
					{
						myRC.setIndicatorString(1,"3");
						turnOrMove(dtg.rotateRight());
					}
					else if(noObstaclesInDirection(myRC.getLocation().add(dtg.rotateRight().rotateRight()),dtg) && myRC.canMove(dtg.rotateRight().rotateRight()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateRight().rotateRight()))==false)
					{
						myRC.setIndicatorString(1,"4");
						turnOrMove(dtg.rotateRight().rotateRight());
					}
					//check two squares to the right, in direction to goal
					else if(noObstaclesInDirection(myRC.getLocation().add(dtg.rotateRight().rotateRight(),2),dtg) && myRC.canMove(dtg.rotateRight()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateRight()))==false)
					{
						myRC.setIndicatorString(1,"5");
						turnOrMove(dtg.rotateRight());
					}
					else if(noObstaclesInDirection(myRC.getLocation().add(dtg.rotateRight().rotateRight(),2),dtg) && myRC.canMove(dtg.rotateRight().rotateRight()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateRight().rotateRight()))==false)
					{
						myRC.setIndicatorString(1,"6");
						turnOrMove(dtg.rotateRight().rotateRight());
					}
					//previous three blocks of logic, except for the left side
					else if(noObstaclesInDirection(myRC.getLocation(),dtg.rotateLeft()) && myRC.canMove(dtg.rotateLeft()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateLeft()))==false)
					{
						myRC.setIndicatorString(1,"7");
						turnOrMove(dtg.rotateLeft());
					}
					else if(noObstaclesInDirection(myRC.getLocation().add(dtg.rotateLeft().rotateLeft()),dtg) && myRC.canMove(dtg.rotateLeft()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateLeft()))==false)
					{
						myRC.setIndicatorString(1,"8");
						turnOrMove(dtg.rotateLeft());
					}
					else if(noObstaclesInDirection(myRC.getLocation().add(dtg.rotateLeft().rotateLeft()),dtg) && myRC.canMove(dtg.rotateLeft().rotateLeft()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateLeft().rotateLeft()))==false)
					{
						myRC.setIndicatorString(1,"9");
						turnOrMove(dtg.rotateLeft().rotateLeft());
					}
					else if(noObstaclesInDirection(myRC.getLocation().add(dtg.rotateLeft().rotateLeft(),2),dtg) && myRC.canMove(dtg.rotateLeft()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateLeft()))==false)
					{
						myRC.setIndicatorString(1,"10");
						turnOrMove(dtg.rotateLeft());
					}
					else if(noObstaclesInDirection(myRC.getLocation().add(dtg.rotateLeft().rotateLeft(),2),dtg) && myRC.canMove(dtg.rotateLeft().rotateLeft()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateLeft().rotateLeft()))==false)
					{
						myRC.setIndicatorString(1,"11");
						turnOrMove(dtg.rotateLeft().rotateLeft());
					}
					//if you see obstacles, try to move toward the goal anyway if you can
					else if(myRC.canMove(dtg))
					{
						myRC.setIndicatorString(1,"12");
						turnOrMove(dtg);
					}
					//if you've been waiting at that spot, try to move around the obstacle
					else if(myRC.canMove(dtg.rotateLeft()))
					{
						myRC.setIndicatorString(1,"13");
						turnOrMove(dtg.rotateLeft());
					}
					else if(myRC.canMove(dtg.rotateRight()))
					{
						myRC.setIndicatorString(1,"14");
						turnOrMove(dtg.rotateRight());
					}
					else
					{
						//start bugging
						myRC.setIndicatorString(1,"Decided to start bugging.");
						isBugging = true;
						bugStart = myRC.getLocation();
					}
				}
				else if(myRC.canMove(dtg) && prevLoc.equals(myRC.getLocation().add(dtg))==false)
				{
					//path is clear to the goal
					myRC.setIndicatorString(1,"15");
					turnOrMove(dtg);
				}
				//if you've been waiting at that spot, try to move around the obstacle
				else if(myRC.canMove(dtg.rotateLeft()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateLeft()))==false)
				{
					myRC.setIndicatorString(1,"16");
					turnOrMove(dtg.rotateLeft());
				}
				else if(myRC.canMove(dtg.rotateRight()) && prevLoc.equals(myRC.getLocation().add(dtg.rotateRight()))==false)
				{
					myRC.setIndicatorString(1,"17");
					turnOrMove(dtg.rotateRight());
				}
				else
				{
					//start bugging
					myRC.setIndicatorString(1,"Decided to start bugging.");
					isBugging = true;
					bugStart = myRC.getLocation();
					if(numClearSquaresInDirection(myRC.getLocation(),myRC.getDirection().rotateLeft().rotateLeft())<numClearSquaresInDirection(myRC.getLocation(),myRC.getDirection().rotateRight().rotateRight()))
					{
						bugDir = BugDirection.LEFTWALL;
					}
				}
			}
			else
			{
				//call bugging method here
				bugger(goal);
			}
			/*
			prevLoc = myRC.getLocation();
			prevDir = myRC.getDirection();
			*/
		}
		else if(isBugging && onMLineAndCloser(bugStart,goal,myRC.getLocation())) //if movement cooldown is taking place, still check to see if we crossed the m-line, if we are bugging
		{
			isBugging = false;
			buggerCount = 0;
		}
	}
	
	
	//main bugging method
	public void bugger(MapLocation goal)
	{
		Direction origDtg = bugStart.directionTo(goal);
		int buggerCountThreshold = 75;
		if(onMLineAndCloser(bugStart,goal,myRC.getLocation())) //condition 1 to STOP bugging
		{
			//if you reach the m-line closer to the goal, then stop bugging 
			myRC.setIndicatorString(1,"Stopped bugging because of m-line criterion.");
			isBugging = false;
			buggerCount = 0;
		}	
		else if(myRC.getLocation().distanceSquaredTo(goal)<=2)
		{
			//if you're within the offset to the goal, stop bugging 
			myRC.setIndicatorString(1,"Stopped bugging because I'm within the offset to the goal.");
			isBugging = false;
			buggerCount = 0;
		}
		else if(myRC.getLocation().equals(bugStart) && myRC.canMove(origDtg))
		{
			//also, if you're at the start and the way is no longer occupied, stop bugging
			myRC.setIndicatorString(1,"Stopped bugging because the way " + origDtg.toString() + " is no longer occupied.");
			isBugging = false;
			buggerCount = 0;
		}
		else if(clearPathToGoal(myRC.getLocation(),goal) && myRC.canMove(myRC.getLocation().directionTo(goal)))
		{
			//also, if you come within sight of the goal and notice that there is a clear path, stop bugging
			myRC.setIndicatorString(1,"Stopped bugging because I see a clear path to the goal.");
			isBugging = false;
			buggerCount = 0;
		}
		else if(noWallsAllAround())
		{
			//if there are no walls all around you, then units that were blocking your way probably moved out of the way, so stop bugging.
			myRC.setIndicatorString(1,"Stopped bugging because the space around me is clear.");
			isBugging = false;
			buggerCount = 0;
		}
		else if(buggerCount>buggerCountThreshold)
		{
			//if you've been bugging for very long, stop because it's probably and island
			myRC.setIndicatorString(1,"Stopped bugging because I reached buggerCountThreshold.");
			isBugging = false;
			buggerCount = 0;
		}
		else //now for the actual bugging
		{
			switch (bugDir)
			{
				case RIGHTWALL:
					//if there is no wall on your right, no wall in front of you, and no wall to your upper right, then you walked past a corner, so turn 90 degrees to the right
					if (myRC.canMove(myRC.getDirection().rotateRight().rotateRight()) && myRC.canMove(myRC.getDirection()) && myRC.canMove(myRC.getDirection().rotateRight()))
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
					//if there is no wall on your right and no wall in front of you, but a wall to your upper right, then move forward
					else if (myRC.canMove(myRC.getDirection().rotateRight().rotateRight()) && myRC.canMove(myRC.getDirection()) && myRC.canMove(myRC.getDirection().rotateRight())==false)
					{
						try
						{
							prevLoc = myRC.getLocation();
							myRC.moveForward();
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if you don't have a wall on your right, rotate 45 degrees to your right
					else if(myRC.canMove(myRC.getDirection().rotateRight().rotateRight()))
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
					//if you have a wall on your right, and no wall to your diagonal upper right, then turn 45 degrees to the right
					else if(myRC.canMove(myRC.getDirection().rotateRight()))
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
					//if you do have a wall on  your right and can move forward, then do so
					else if(myRC.canMove(myRC.getDirection()))
					{
						try
						{
							prevLoc = myRC.getLocation();
							myRC.moveForward();
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if there is a wall in front of you and on the right and the left, then rotate 135 degrees to the left
					else if(myRC.canMove(myRC.getDirection().rotateLeft().rotateLeft())==false)
					{
						try
						{
							prevDir = myRC.getDirection();
							myRC.setDirection(myRC.getDirection().rotateLeft().rotateLeft().rotateLeft());
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if you get here, then there is a wall on your right and also in front of you, but not the left, so rotate 45 degrees to the left 
					else
					{
						try
						{
							prevDir = myRC.getDirection();
							myRC.setDirection(myRC.getDirection().rotateLeft());
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					buggerCount++;
					break;
				case LEFTWALL:
					//if there is no wall on your left, no wall in front of you, and no wall to your upper left, then you walked past a corner, so turn 90 degrees to the left
					if (myRC.canMove(myRC.getDirection().rotateLeft().rotateLeft()) && myRC.canMove(myRC.getDirection()) && myRC.canMove(myRC.getDirection().rotateLeft()))
					{
						try
						{
							prevDir = myRC.getDirection();
							myRC.setDirection(myRC.getDirection().rotateLeft().rotateLeft());
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if there is no wall on your left and no wall in front of you, but a wall to your upper left, then move forward
					else if (myRC.canMove(myRC.getDirection().rotateLeft().rotateLeft()) && myRC.canMove(myRC.getDirection()) && myRC.canMove(myRC.getDirection().rotateLeft())==false)
					{
						try
						{
							prevLoc = myRC.getLocation();
							myRC.moveForward();
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if you don't have a wall on your left, rotate 45 degrees to your right
					else if(myRC.canMove(myRC.getDirection().rotateLeft().rotateLeft()))
					{
						try
						{
							prevDir = myRC.getDirection();
							myRC.setDirection(myRC.getDirection().rotateLeft());
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if you have a wall on your left, and no wall to your diagonal upper left, then turn 45 degrees to the left
					else if(myRC.canMove(myRC.getDirection().rotateLeft()))
					{
						try
						{
							prevDir = myRC.getDirection();
							myRC.setDirection(myRC.getDirection().rotateLeft());
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if you do have a wall on  your left and can move forward, then do so
					else if(myRC.canMove(myRC.getDirection()))
					{
						try
						{
							prevLoc = myRC.getLocation();
							myRC.moveForward();
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if there is a wall in front of you and on the right and the left, then rotate 135 degrees to the right
					else if(myRC.canMove(myRC.getDirection().rotateRight().rotateRight())==false)
					{
						try
						{
							prevDir = myRC.getDirection();
							myRC.setDirection(myRC.getDirection().rotateRight().rotateRight().rotateRight());
						}
						catch (Exception e)
						{
							System.out.println("Caught exception: ");
							e.printStackTrace();
							myRC.setIndicatorString(2,"exception: " + e.toString());
						}
					}
					//if you get here, then there is a wall on your left and also in front of you, but not the right, so rotate 45 degrees to the right 
					else
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
					buggerCount++;
					break;
			}
		}
	}
	
	//Returns true if the robot is free to move in all eight directions.
	public boolean noWallsAllAround()
	{
		if(myRC.canMove(myRC.getDirection()) && myRC.canMove(myRC.getDirection().rotateLeft()) && myRC.canMove(myRC.getDirection().rotateLeft().rotateLeft()) && myRC.canMove(myRC.getDirection().opposite().rotateRight()) && myRC.canMove(myRC.getDirection().opposite()) && myRC.canMove(myRC.getDirection().opposite().rotateLeft()) && myRC.canMove(myRC.getDirection().rotateRight()) && myRC.canMove(myRC.getDirection().rotateRight().rotateRight()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//turn or move in the given direction; if you're already facing the direction but cannot move there, do nothing
	public void turnOrMove(Direction dtg)
	{
		//check to see if you're facing that direction
		if (dtg.equals(myRC.getDirection()))
		{
			if(myRC.canMove(dtg))
			{
				//if yes you're facing direction dtg, then move in that direction if you can
				try
				{
					prevLoc = myRC.getLocation();
					myRC.moveForward();
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
				//then you're facing dtg, but there is something in the way.  
			}
		}
		else //you're not facing dtg
		{
			//so turn to face direction dtg
			try
			{
				prevDir = myRC.getDirection();
				myRC.setDirection(dtg);
			}
			catch (Exception e)
			{
				System.out.println("Caught exception: ");
				e.printStackTrace();
				myRC.setIndicatorString(2,"exception: " + e.toString());
			}
		}
	}
	
	//determines if a MapLocation is on the line (not necessarily the line segment) connecting two other MapLocations
	public boolean onMLine(MapLocation start, MapLocation end, MapLocation point)
	{
		if(start.equals(end))
		{
			//System.out.println("Error: Called onMLine() with the same start and end point.");
			return false;
		}
		else if(start.y==end.y)
		{
			//then you have a horizontal line
			if(point.y==end.y)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(start.x==end.x)
		{
			//then you have a vertical line
			if(point.x==end.x)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(Math.abs(end.y - start.y)==Math.abs(end.x - start.x))
		{
			//then you have a perfectly diagonal line (i.e. inclined at 45 degrees)
			//in which case no rounding is needed
			int m = (end.y - start.y) / (end.x - start.x); //m==1
			int y = m*(point.x - end.x) + end.y;
			if(y==point.y)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(Math.abs(end.x - start.x) > Math.abs(end.y - start.y))
		{
			//dx > dy -> sweep x
			double m = ((double)end.y - (double)start.y) / ((double)end.x - (double)start.x);
			double y = m*(point.x - end.x) + end.y;
			int yRounded = (int)Math.round(y);
			myRC.setIndicatorString(2,"bugStart: " + bugStart.toString() + " | Goal: " + end.toString() + " | Slope: " + Double.toString(m) + " | yRounded: " + Integer.toString(yRounded) +  " " + (point.y==yRounded));
			if(point.y == yRounded)
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
			//dy > dx -> sweep y
			double m = ((double)end.y - (double)start.y) / ((double)end.x - (double)start.x);
			double x = end.x + (point.y - end.y)/m;
			int xRounded = (int)Math.round(x);
			if(point.x == xRounded)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	//determines if a point is on the line between two other MapLocations, and also closer to the goal
	public boolean onMLineAndCloser(MapLocation start, MapLocation end, MapLocation point)
	{
		if(onMLine(start,end,point))
		{
			int dStartSquared = end.distanceSquaredTo(start);
			int dPointSquared = end.distanceSquaredTo(point);
			if(dPointSquared<dStartSquared)
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
	
	//
	
}