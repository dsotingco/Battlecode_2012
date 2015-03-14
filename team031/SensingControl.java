package team031;

import battlecode.common.*;
import java.math.*;
import java.util.*;

public class SensingControl
{
	//fields
	public RobotController myRC;
	public Team myTeam;
	public Team otherTeam;
	//sensing fields
	public Robot[] nearbyRobots;
	public Robot[] nearbyAllies;
	public RobotInfo[] nearbyAlliedInfo;
	public MapLocation[] nearbyAlliedLocations;
	public RobotInfo[] dangerousAlliedInfo;
	public Robot[] nearbyEnemies;
	public RobotInfo[] nearbyEnemyInfo;
	public MapLocation[] nearbyEnemyLocations;
	public RobotInfo[] dangerousEnemyInfo;
	public MapLocation[] cpn; //locations of capturable power nodes
	public MapLocation[] apn; //locations of allied power nodes
	public MapLocation[] epn; //known locations of enemy power nodes
	
	//constructor
	public SensingControl(RobotController RC)
	{
		myRC = RC;
		myTeam = myRC.getTeam();
		otherTeam = myTeam.opponent();
	}
	
	public void standardSense()
	{
		nearbyRobots = myRC.senseNearbyGameObjects(Robot.class);
		//allied stuff
		nearbyAllies = senseAllies(nearbyRobots);
		nearbyAlliedInfo = senseAlliedInfo(nearbyAllies);
		nearbyAlliedLocations = senseAlliedLocations(nearbyAlliedInfo);
		dangerousAlliedInfo = senseDangerousAllies(nearbyAlliedInfo);
		//enemy stuff
		nearbyEnemies = senseEnemies(nearbyRobots);
		nearbyEnemyInfo = senseEnemyInfo(nearbyEnemies);
		nearbyEnemyLocations = senseEnemyLocations(nearbyEnemyInfo);
		dangerousEnemyInfo = senseDangerousEnemies(nearbyEnemyInfo);
		//capturable power nodes
		cpn = myRC.senseCapturablePowerNodes();
		//allied power nodes
		apn = senseAlliedPowerNodeLocations();
		//enemy power nodes
		epn = senseEnemyLocationsOfType(nearbyEnemyInfo,RobotType.TOWER);
	}
	
	//sense nearby allies, given a list of nearby robots seen
	public Robot[] senseAllies(Robot[] nearbyRobotsSeen)
	{
		int numAllies = countAllies(nearbyRobotsSeen);
		Robot[] alliedUnits = new Robot[numAllies];
		if(nearbyRobotsSeen!=null && nearbyRobotsSeen.length>0)
		{
			//now fill in the alliedUnits array with the allies
			int index = 0;
			for(Robot bot : nearbyRobotsSeen)
			{
				if(bot.getTeam().equals(myTeam))
				{
					alliedUnits[index] = bot;
					index++;
				}
			}
		}
		return alliedUnits;
	}
	
	//count the number of allies in sight, given a list of robots seen
	public int countAllies(Robot[] nearbyRobotsSeen)
	{
		int numAllies = 0;
		if(nearbyRobotsSeen!=null && nearbyRobotsSeen.length>0)
		{
			for(Robot bot : nearbyRobotsSeen)
			{
				if (bot.getTeam().equals(myTeam))
				{
					numAllies++;
				}
			}
		}
		return numAllies;
	}
	
	//sense allied info, given a list of allies
	public RobotInfo[] senseAlliedInfo(Robot[] alliedUnits)
	{
		RobotInfo[] alliedInfo = new RobotInfo[alliedUnits.length];
		if(alliedUnits!=null && alliedUnits.length>0)
		{
			int index = 0;
			for(Robot bot : alliedUnits)
			{
				//need to check to see if robot is still in range?
				try
				{
					alliedInfo[index] = myRC.senseRobotInfo(bot);
				}
				catch (Exception e)
				{
					System.out.println("Caught exception: ");
					e.printStackTrace();
					myRC.setIndicatorString(2,"exception: " + e.toString());
				}
				index++;
			}
		}
		return alliedInfo;
	}
	
	//sense allied locations, given a RobotInfo[] list of allies
	public MapLocation[] senseAlliedLocations(RobotInfo[] alliedInfo)
	{
		MapLocation[] alliedLocs = new MapLocation[alliedInfo.length];
		if(alliedInfo!=null && alliedInfo.length>0)
		{
			int index = 0;
			for(RobotInfo theInfo : alliedInfo)
			{
				try
				{
					alliedLocs[index] = theInfo.location;
				}
				catch (Exception e)
				{
					System.out.println("Caught exception: ");
					e.printStackTrace();
					myRC.setIndicatorString(2,"exception: " + e.toString());
				}
				index++;
			}
		}
		return alliedLocs;
	}
	
	//returns locations of allies with a certain type within sight
	public MapLocation[] senseAlliedLocationsOfType(RobotInfo[] alliedInfo, RobotType rt)
	{
		RobotInfo[] infoList = senseAlliesOfType(alliedInfo,rt);
		MapLocation[] res = new MapLocation[infoList.length];
		if(alliedInfo!=null && alliedInfo.length>0)
		{
			int index = 0;
			for(RobotInfo theInfo : infoList)
			{
				res[index] = theInfo.location;
				index++;
			}
		}
		return res;
	}
	
	//gets locations of allied power nodes
	public MapLocation[] senseAlliedPowerNodeLocations()
	{
		PowerNode[] apn = myRC.senseAlliedPowerNodes();
		MapLocation[] res = new MapLocation[apn.length];
		int index = 0;
		for (PowerNode theNode : apn)
		{
			res[index] = theNode.getLocation();
			index++;
		}
		return res;
	}
	
	//get a RobotInfo[] list of allies with a certain type 
	public RobotInfo[] senseAlliesOfType(RobotInfo[] alliedInfo, RobotType rt)
	{
		int numAlliesOfType = countAlliesOfType(alliedInfo,rt);
		RobotInfo[] res = new RobotInfo[numAlliesOfType];
		if(alliedInfo!=null && alliedInfo.length>0)
		{
			int index = 0;
			for(RobotInfo theInfo : alliedInfo)
			{
				if(theInfo.type.equals(rt))
				{
					res[index] = theInfo;
					index++;
				}
			}
		}
		return res;
	}
	
	//count allied units of a certain type, given a list of allied RobotInfo
	public int countAlliesOfType(RobotInfo[] alliedInfo, RobotType rt)
	{
		int counter = 0;
		if(alliedInfo!=null && alliedInfo.length>0)
		{
			for(RobotInfo theInfo : alliedInfo)
			{
				if(theInfo.type.equals(rt))
				{
					counter++;
				}
			}
		}
		return counter;
	}
	
	//count allies in a given direction.  Does not include towers.
	public int countAlliesInDirection(RobotInfo[] allyInfo,Direction dir)
	{
		int counter = 0;
		if(allyInfo!=null && allyInfo.length>0)
		{
			for(RobotInfo theInfo : allyInfo)
			{
				if(theInfo.type.equals(RobotType.TOWER)==false)
				{
					if(dir.equals(myRC.getLocation().directionTo(theInfo.location)))
					{
						counter++;
					}
				}
			}
		}
		return counter;
	}
	
	//Returns a RobotInfo[] array of dangerous allies.  Dangerous allies are defined as: not towers, not scouts, have nonzero flux
	public RobotInfo[] senseDangerousAllies(RobotInfo[] allyInfo)
	{
		RobotInfo[] res = new RobotInfo[countDangerousAllies(allyInfo)];
		int index = 0;
		for(RobotInfo theInfo : allyInfo)
		{
			if(theInfo.type.equals(RobotType.TOWER)==false)
			{
				if(theInfo.type.equals(RobotType.SCOUT)==false)
				{
					if(theInfo.flux>0)
					{
						res[index] = theInfo;
						index++;
					}
				}
			}
		}
		return res;
	}
	
	//count dangerous allies.  Dangerous allies are defined as: not towers, not scouts, have nonzero flux
	public int countDangerousAllies(RobotInfo[] allyInfo)
	{
		int counter = 0;
		for(RobotInfo theInfo : allyInfo)
		{
			if(theInfo.type.equals(RobotType.TOWER)==false)
			{
				if(theInfo.type.equals(RobotType.SCOUT)==false)
				{
					if(theInfo.flux>0)
					{
						counter++;
					}
				}
			}
		}
		myRC.setIndicatorString(1,"I see " + Integer.toString(counter) + " dangerous allies.");
		return counter;
	}
	
	//sense nearby enemies, given a list of nearby robots sen
	public Robot[] senseEnemies(Robot[] nearbyRobotsSeen)
	{
		int numEnemies = countEnemies(nearbyRobotsSeen);
		Robot[] enemyUnits = new Robot[numEnemies];
		if(nearbyRobotsSeen!=null && nearbyRobotsSeen.length>0)
		{
			//now fill in the enemyUnits array with the enemies
			int index = 0;
			for(Robot bot : nearbyRobotsSeen)
			{
				if (bot.getTeam().equals(myTeam)==false)
				{
					enemyUnits[index] = bot;
					index++;
				}
			}
		}
		return enemyUnits;
	}
	
	//count the number of enemies in sight, given a list of robots seen
	public int countEnemies(Robot[] nearbyRobotsSeen)
	{
		int numEnemies = 0;
		if(nearbyRobotsSeen!=null && nearbyRobotsSeen.length>0)
		{
			for(Robot bot : nearbyRobotsSeen)
			{
				if (bot.getTeam().equals(myTeam)==false)
				{
					numEnemies++;
				}
			}
		}
		return numEnemies;
	}
	
	//sense enemy info, given a list of enemies
	public RobotInfo[] senseEnemyInfo(Robot[] enemyUnits)
	{
		RobotInfo[] enemyInfo = new RobotInfo[enemyUnits.length];
		if(enemyUnits!=null && enemyUnits.length>0)
		{
			int index = 0;
			for(Robot bot : enemyUnits)
			{
				//need to check to see if robot is still in range?
				try
				{
					enemyInfo[index] = myRC.senseRobotInfo(bot);
				}
				catch (Exception e)
				{
					System.out.println("Caught exception: ");
					e.printStackTrace();
					myRC.setIndicatorString(2,"exception: " + e.toString());
				}
				index++;
			}
		}
		return enemyInfo;
	}
	
	//get a RobotInfo[] list of attackable enemies
	public RobotInfo[] getAttackableEnemies(RobotInfo[] enemyInfo)
	{
		int numAttackableEnemies = countAttackableEnemies(enemyInfo);
		RobotInfo[] res = new RobotInfo[numAttackableEnemies];
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			int index = 0;
			for(RobotInfo theInfo : enemyInfo)
			{
				if(myRC.canAttackSquare(theInfo.location))
				{
					if(myRC.getType().canAttack(theInfo.type.level))
					{
						res[index] = theInfo;
						index++;
					}
				}
			}
		}
		return res;
	}
	
	//count attackable enemies
	public int countAttackableEnemies(RobotInfo[] enemyInfo)
	{
		int counter = 0;
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			for (RobotInfo theInfo : enemyInfo)
			{
				if(myRC.canAttackSquare(theInfo.location))
				{
					if(myRC.getType().canAttack(theInfo.type.level))
					{
						counter++;
					}
				}
			}
		}
		return counter;
	}
	
	//sense enemy locations, given a RobotInfo[] list of enemies
	public MapLocation[] senseEnemyLocations(RobotInfo[] enemyInfo)
	{
		MapLocation[] enemyLocs = new MapLocation[enemyInfo.length];
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			int index = 0;
			for(RobotInfo theInfo : enemyInfo)
			{
				try
				{
					enemyLocs[index] = theInfo.location;
				}
				catch (Exception e)
				{
					System.out.println("Caught exception: ");
					e.printStackTrace();
					myRC.setIndicatorString(2,"exception: " + e.toString());
				}
				index++;
			}
		}
		return enemyLocs;
	}
	
	//returns locations of enemies with a certain type within sight
	public MapLocation[] senseEnemyLocationsOfType(RobotInfo[] enemyInfo, RobotType rt)
	{
		RobotInfo[] infoList = senseEnemiesOfType(enemyInfo,rt);
		MapLocation[] res = new MapLocation[infoList.length];
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			int index = 0;
			for(RobotInfo theInfo : infoList)
			{
				res[index] = theInfo.location;
				index++;
			}
		}
		return res;
	}
	
	//get a RobotInfo[] list of enemies with a certain type 
	public RobotInfo[] senseEnemiesOfType(RobotInfo[] enemyInfo, RobotType rt)
	{
		int numEnemiesOfType = countEnemiesOfType(enemyInfo,rt);
		RobotInfo[] res = new RobotInfo[numEnemiesOfType];
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			int index = 0;
			for(RobotInfo theInfo : enemyInfo)
			{
				if(theInfo.type.equals(rt))
				{
					res[index] = theInfo;
					index++;
				}
			}
		}
		return res;
	}
	
	//count enemy units of a certain type, given a list of enemy RobotInfo
	public int countEnemiesOfType(RobotInfo[] enemyInfo, RobotType rt)
	{
		int counter = 0;
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			for(RobotInfo theInfo : enemyInfo)
			{
				if(theInfo.type.equals(rt))
				{
					counter++;
				}
			}
		}
		return counter;
	}
	
	//count enemies in a given direction.  Does not include towers.
	public int countEnemiesInDirection(RobotInfo[] enemyInfo,Direction dir)
	{
		int counter = 0;
		if(enemyInfo!=null && enemyInfo.length>0)
		{
			for(RobotInfo theInfo : enemyInfo)
			{
				if(theInfo.type.equals(RobotType.TOWER)==false)
				{
					if(dir.equals(myRC.getLocation().directionTo(theInfo.location)))
					{
						counter++;
					}
				}
			}
		}
		return counter;
	}
	
	//Returns a RobotInfo[] array of dangerous enemies.  Dangerous enemies are defined as: not towers, not scouts, have nonzero flux, and have the calling unit within their attacking range+2
	public RobotInfo[] senseDangerousEnemies(RobotInfo[] enemyInfo)
	{
		RobotInfo[] res = new RobotInfo[countDangerousEnemies(enemyInfo)];
		int index = 0;
		for(RobotInfo theInfo : enemyInfo)
		{
			if(theInfo.type.equals(RobotType.TOWER)==false)
			{
				if(theInfo.type.equals(RobotType.SCOUT)==false)
				{
					if(theInfo.flux>0)
					{
						int critDistance = theInfo.type.attackRadiusMaxSquared+2;
						if(myRC.getLocation().distanceSquaredTo(theInfo.location)<=critDistance)
						{
							res[index] = theInfo;
							index++;
						}
					}
				}
			}
		}
		return res;
	}
	
	//count dangerous enemies.  Dangerous enemies are defined as: not towers, not scouts, have nonzero flux, and have the calling unit within their attacking range+18 (three squares diagonally)
	public int countDangerousEnemies(RobotInfo[] enemyInfo)
	{
		int counter = 0;
		for(RobotInfo theInfo : enemyInfo)
		{
			if(theInfo.type.equals(RobotType.TOWER)==false)
			{
				if(theInfo.type.equals(RobotType.SCOUT)==false)
				{
					if(theInfo.flux>0)
					{
						int critDistance = theInfo.type.attackRadiusMaxSquared+18;
						if(myRC.getLocation().distanceSquaredTo(theInfo.location)<=critDistance)
						{
							counter++;
						}
					}
				}
			}
		}
		//myRC.setIndicatorString(1,"I see " + Integer.toString(counter) + " dangerous enemies.");
		return counter;
	}
	
	//given a location and a direction, looks for the farthest square with no obstacle in that direction. 
	//Note that the location does not need to be the robot's current location.
	//Note that if you haven't sensed the tile before, this method will assume that there is an obstacle there.
	public MapLocation farthestClearSquareInDirection(MapLocation loc, Direction dir)
	{
		MapLocation res = loc;
		while((myRC.senseTerrainTile(res.add(dir))!=null) && myRC.senseTerrainTile(res.add(dir)).equals(TerrainTile.LAND))
		{
			res = res.add(dir);
		}
		return res;
	}
	
	public int numClearSquaresInDirection(MapLocation loc, Direction dir)
	{
		MapLocation testSquare = loc;
		int counter = 0;
		while((myRC.senseTerrainTile(testSquare.add(dir))!=null) && myRC.senseTerrainTile(testSquare.add(dir)).equals(TerrainTile.LAND))
		{
			counter++;
			testSquare = testSquare.add(dir);
		}
		return counter;
	}
	
	//given a location and a direction, looks to see if there are obstacles in that direction.
	//Note that the location does not need to be the robot's current location
	public boolean noObstaclesInDirection(MapLocation loc, Direction dir)
	{
		MapLocation fcs = farthestClearSquareInDirection(loc,dir);
		if(((myRC.senseTerrainTile(fcs.add(dir))==null) || myRC.senseTerrainTile(fcs.add(dir)).equals(TerrainTile.LAND)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//like numClearSquaresInDirection, but counts obstacle blocks instead
	public int numObstaclesInDirection(MapLocation loc, Direction dir)
	{
		MapLocation testSquare = loc;
		int counter = 0;
		while((myRC.senseTerrainTile(testSquare.add(dir))!=null) && myRC.senseTerrainTile(testSquare.add(dir)).equals(TerrainTile.LAND)==false)
		{
			counter++;
		}
		return counter;
	}
	
	//determines if a square contains an allied tower
	public boolean containsAlliedTower(MapLocation loc)
	{
		int index = 0;
			while(index<apn.length-1 && loc.equals(apn[index])==false)
			{
				index++;
			}
			if(index!=apn.length-1)
			{
				return true; //i.e. yes there is a tower
			}
			else //if you get to the last one, check it
			{
				if(loc.equals(apn[index]))
				{
					return true;
				}
				else
				{
					return false; //i.e. checked everything and the location didn't match any of the allied tower locations
				}
			}
	}
	
	//determines if a square contains an enemy tower
	public boolean containsEnemyTower(MapLocation loc)
	{
		int index = 0;
		if (epn.length==0)
		{
			return false;
		}
		else
		{
			while(index<epn.length-1 && loc.equals(epn[index])==false)
			{
				index++;
			}
			if(index!=epn.length-1)
			{
				return true; //i.e. yes there is an enemy tower
			}
			else //if you get to the last one, check it
			{
				if(loc.equals(epn[index]))
				{
					return true;
				}
				else
				{
					return false; //i.e. checked everything and the location didn't match any of the enemy tower locations
				}
			}
		}
	}
	
	//determines if a square contains a tower
	public boolean containsTower(MapLocation loc)
	{
		if(containsAlliedTower(loc) || containsEnemyTower(loc))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	
	//given a MapLocation array, returns the nearest MapLocation
	public MapLocation getNearest(MapLocation[] locList)
	{
		double[] squaredistances = new double[locList.length];
		for(int index = 0; index<squaredistances.length; index++)
		{
			squaredistances[index] = myRC.getLocation().distanceSquaredTo(locList[index]);
		}
		return locList[indexOfSmallest(squaredistances)];
	}
	
	//given a MapLocation array, returns the farthest MapLocation
	public MapLocation getFarthest(MapLocation[] locList)
	{
		double[] squaredistances = new double[locList.length];
		for(int index = 0; index<squaredistances.length; index++)
		{
			squaredistances[index] = myRC.getLocation().distanceSquaredTo(locList[index]);
		}
		return locList[indexOfBiggest(squaredistances)];
	}
	
	//converts MapLocation to String
	public String locToString(MapLocation loc)
	{
		String a = Integer.toString(loc.x);
		String b = Integer.toString(loc.y);
		return("(" + a + "," + b + ")");
	}
	
	//given a double[] array, returns the index of the smallest element
	public int indexOfSmallest(double[] arr)
	{
		int j = 0;
		for (int i = 0; i<arr.length; i++)
		{
			if(arr[i] < arr[j])
			{
				j = i;
			}
		}
		return j;
	}
	
	//given a double[] array, returns the index of the smallest nonzero element
	public int indexOfSmallestNonzero(double[] arr)
	{
		int j = 0;
		for (int i = 0; i<arr.length; i++)
		{
			if(arr[i] < arr[j] && arr[i]!=0)
			{
				j = i;
			}
		}
		return j;
	}
	
	//given a double[] array, returns the index of the biggest element
	public int indexOfBiggest(double[] arr)
	{
		int j = 0;
		for (int i = 0; i<arr.length; i++)
		{
			if(arr[i] > arr[j])
			{
				j = i;
			}
		}
		return j;
	}
	
	//determines if there is a clear path to the goal. Returns false immediately if you cannot see the goal
	public boolean clearPathToGoal(MapLocation start, MapLocation goal)
	{
		Direction dtg = start.directionTo(goal);
		if(myRC.senseTerrainTile(goal)==null)
		{
			return false;
		}
		else if( (start.y==goal.y) || (start.x==goal.x) || (Math.abs(goal.y - start.y) == Math.abs(goal.x - start.x)) )
		{
			//horizontal line, vertical line, or diagonal line at 45 degrees
			MapLocation loc = start;
			while(loc.equals(goal)==false && myRC.senseTerrainTile(loc.add(dtg))!=null && myRC.senseTerrainTile(loc.add(dtg)).equals(TerrainTile.LAND))
			{
				loc = loc.add(dtg);
			}
			if(loc.equals(goal))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(Math.abs(goal.x - start.x) > Math.abs(goal.y - start.y))
		{
			//dx > dy -> sweep x
			double m = ((double)goal.y - (double)start.y) / ((double)goal.x - (double)start.x);
			int xSquare = start.x+1;
			double y = m*(xSquare - goal.x) + goal.y;
			int ySquare = (int)Math.round(y);
			MapLocation loc = start;
			MapLocation nextSquare = new MapLocation(xSquare,ySquare);
			while(loc.equals(goal)==false && myRC.senseTerrainTile(nextSquare)!=null && myRC.senseTerrainTile(nextSquare).equals(TerrainTile.LAND))
			{
				loc = nextSquare;
				xSquare++;
				y = m*(xSquare - goal.x) + goal.y;
				ySquare = (int)Math.round(y);
				nextSquare = new MapLocation(xSquare,ySquare);
			}
			if(loc.equals(goal))
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
			double m = ((double)goal.y - (double)start.y) / ((double)goal.x - (double)start.x);
			int ySquare = start.y+1;
			double x = goal.x + (ySquare - goal.y)/m;
			int xSquare = (int)Math.round(x);
			MapLocation loc = start;
			MapLocation nextSquare = new MapLocation(xSquare,ySquare);
			while(loc.equals(goal)==false && myRC.senseTerrainTile(nextSquare)!=null && myRC.senseTerrainTile(nextSquare).equals(TerrainTile.LAND))
			{
				loc = nextSquare;
				ySquare++;
				x = goal.x + (ySquare - goal.y)/m;
				xSquare = (int)Math.round(x);
				nextSquare = new MapLocation(xSquare,ySquare);
			}
			if(loc.equals(goal))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	//determines if the target MapLocation is the location of a capturable power node.  
	public boolean containsCPN(MapLocation loc)
	{
		int counter = 0;
		if(cpn!=null && cpn.length>0)
		{
			for(MapLocation nodeLoc : cpn)
			{
				if(loc.equals(nodeLoc))
				{
					counter++;
				}
			}
		}
		if(counter>0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//converts RobotInfo array to MapLocation array
	public MapLocation[] RIarray2MLarray(RobotInfo[] ria)
	{
		MapLocation[] res = new MapLocation[ria.length];
		int index = 0;
		for(RobotInfo theInfo : ria)
		{
			res[index] = theInfo.location;
			index++;
		}
		return res;
	}
	
	/*
	//Returns the "secondary direction" from startLoc to endLoc.  
	public Direction getSecondaryDirection(MapLocation startLoc, MapLocation endLoc)
	{
	}
	*/
}