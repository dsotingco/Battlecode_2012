package team031;

import battlecode.common.*;
import java.math.*;
import java.util.*;

public class RadioControl extends Pathfinder
{
	//additional fields
	String extraSecurityString;
	
	//constructor
	public RadioControl(RobotController myRC, MapLocation pl, Direction pd, boolean ib, BugDirection bd, MapLocation bs, int bc)
	{
		super(myRC,pl,pd,ib,bd,bs,bc);
		extraSecurityString = "bvguiowc4gnlisdjk";
	}
	
	//send a message
	public void sendMessage(MapLocation loc, String receiverType, String command)
	{
		if(myRC.hasBroadcasted()==false)
		{
			Message m = new Message();
			//initialize arrays
			int[] ints = new int[2];
			MapLocation[] locations = new MapLocation[1];
			String[] strings = new String[3];
			//fill in the data to fill in
			locations[0] = loc;
			strings[0] = receiverType;
			strings[1] = command;
			strings[2] = myRC.getTeam().toString();
			ints[0] = Clock.getRoundNum();
			//hashing
			String hashString = strings[0] + Integer.toString(ints[0]) + strings[1] + locations[0].toString() + strings[2] + extraSecurityString;
			ints[1] = hashString.hashCode();
			//put the arrays into the message
			m.ints = ints;
			m.locations = locations;
			m.strings = strings;
			if(myRC.getFlux()>m.getFluxCost())
			{
				try
				{
					myRC.broadcast(m);
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
	
	//finds our team's message in a Message[] queue.  Returns null if we don't have a message.
	public Message findMessage(Message[] msgs)
	{
		int index = 0;
		while(Clock.getBytecodesLeft()>1000 && index<msgs.length-1 && checkMessage(msgs[index])==false)
		{
			index++;
		}
		if(index<msgs.length-1)
		{
			return msgs[index];
		}
		else if(checkMessage(msgs[index])==true)
		{
			return msgs[index];
		}
		else
		{
			return null;
		}
	}
	
	//checks message; returns true if it matches our team's format and validates the hash, and matches our team (i.e. A or B)
	public boolean checkMessage(Message msg)
	{
		//first, check that the message itself is not null, and that the arrays themselves are not null
		if(msg!=null && msg.ints!=null && msg.locations!=null && msg.strings!=null)
		{
			//now check array lengths
			if(msg.ints.length==2 && msg.locations.length==1 && msg.strings.length==3)
			{
				//now check array contents
				if(msg.locations[0]!=null)
				{
					if(msg.strings[0]!=null && msg.strings[1]!=null && msg.strings[2]!=null)
					{
						//now validate the hash, replacing msg.strings[2] with our team string
						String hashString = msg.strings[0] + Integer.toString(msg.ints[0]) + msg.strings[1] + msg.locations[0].toString() + myRC.getTeam().toString() + extraSecurityString;
						int hc = hashString.hashCode();
						if(hc==msg.ints[1])
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
	
	//send noise 1
	public void sendNoise1()
	{
		if(myRC.hasBroadcasted()==false)
		{
			Message m = new Message();
			int[] ints = new int[3];
			MapLocation[] locations = new MapLocation[5];
			String[] strings = new String[1];
			if(myRC.getFlux()>m.getFluxCost())
			{
				try
				{
					myRC.broadcast(m);
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