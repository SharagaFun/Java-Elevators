package com.company;

import java.util.*;
import java.sql.Timestamp;
public class Elevator
{
	private ArrayList<Request> upQ = new ArrayList<Request>();
	private ArrayList<Request> downQ = new ArrayList<Request>();
	private ArrayList<Request> currentQ = new ArrayList<Request>();

	private Direction direction = Direction.UP;
	private int floor = 0;
	private int number;
	private LState state = LState.STOPPED;
	private DOOR door = DOOR.CLOSED;

	private Thread processThread = new Thread();
	private boolean run = false;
	private ElevatorController controller;

	private static Comparator<Request> ComparatorUP = new Comparator<Request>() {
		@Override
		public int compare(Request a, Request b)
		{
			return a.floor > b.floor ? 1 : a.floor == b.floor ? 0 : -1;
		}
	};
	private static Comparator<Request> ComparatorDown = new Comparator<Request>() {
		@Override
		public int compare(Request a, Request b)
		{
			return a.floor < b.floor ? 1 : a.floor == b.floor ? 0 : -1;
		}
	};

	public Elevator(ElevatorController c, int num)
	{
		controller = c;
		number = num;
	}

	public final void emergencyStop()
	{
		System.out.println("Lift "+number+": "+"emergencyStop");
		run = false;
		upQ.clear();
		downQ.clear();
		currentQ.clear();
	}

		public final void call(int flr,Direction d) // from floor
	{
		if (direction == Direction.UP)
		{
			Timestamp time = new Timestamp(System.currentTimeMillis());
			if (flr >= floor)
			{
				for (Request r: currentQ)
				{
					if (r.floor == flr)
						return;
				}
				currentQ.add(new Request(time, flr, d));
				Collections.sort(currentQ, this.ComparatorUP);
			}
			else
			{
				for (Request r: upQ)
				{
					if (r.floor == flr)
						return;
				}
				upQ.add(new Request(time, flr, d));
				Collections.sort(upQ, this.ComparatorUP);
			}
		}
		else
		{
			Timestamp time = new Timestamp(System.currentTimeMillis());
			if (flr <= floor)
			{
				for (Request r: currentQ)
				{
					if (r.floor == flr)
						return;
				}
				currentQ.add(new Request(time, flr, d));
				Collections.sort(currentQ, this.ComparatorDown);
			}
			else
			{
				for (Request r: downQ)
				{
					if (r.floor == flr)
						return;
				}
				downQ.add(new Request(time, flr, d));
				Collections.sort(downQ, this.ComparatorDown);
			}
		}
	}

	public final void goTo(int flr)
	{
		call(flr, direction);
	}

	private final Timestamp getLowestTimeUpQ()
	{
		Timestamp min = new Timestamp(0);
		for (Request request : upQ) {
			if (request.time.before(min)) {
				min = request.time;
			}
		}
		return min;
	}

	private final Timestamp getLowestTimeDownQ()
	{
		Timestamp min = new Timestamp(0);
		for (int i = 0;i < downQ.size();i++)
		{
			if (downQ.get(i).time.before(min))
			{
				min = downQ.get(i).time;
			}
		}
		return min;
	}

	private final void goToFloor(int flr) throws InterruptedException // elevator moving logic here
		{
			System.out.println("Lift "+number+": "+"GOING TO " + flr);

			System.out.println("Lift "+number+": "+"START FLOOR "+floor);
			System.out.println("Lift "+number+": "+"DOORS ARE CLOSING");
			door = DOOR.CLOSED;
			state = LState.MOVING;
			System.out.println("Lift "+number+": "+"MOVING");
			Thread.sleep(500);
			floor = flr;
			System.out.println("Lift "+number+": "+"REACHED "+floor);
			System.out.println("Lift "+number+": "+"STOPPED");
			state = LState.STOPPED;
			door = DOOR.OPEN;
			System.out.println("Lift "+number+": "+"DOORS ARE OPEN");
			controller.callback(floor, direction, this);
			Thread.sleep(200);
			System.out.println("Lift "+number+": "+"DOORS ARE CLOSING");
			door = DOOR.CLOSED;
		}


	private final void processNextQueue()
	{
		if (getLowestTimeDownQ().before(getLowestTimeUpQ()))
		{
			currentQ = upQ;
		}
		else
		{
			currentQ = downQ;
		}
	}

	public final Direction getDirection()
	{
		return this.direction;
	}

	public final int getFloor()
	{
		return this.floor;
	}

	public final int getLastFloor()
	{
		if (!currentQ.isEmpty())
			return currentQ.get(currentQ.size()-1).floor;
		else
			return this.floor;
	}

	public final boolean isEmptyQueue()
	{
		return currentQ.isEmpty();
	}

	public final boolean isWorking()
	{
		return run;
	}

	public final LState getState()
	{
		return this.state;
	}

	public void run() {
		Thread thread = new Thread(new Runnable()
		{
			public void run()

			{
				run = true;
				while (run)
				{
					if (currentQ.isEmpty() && !upQ.isEmpty() && !downQ.isEmpty())
						processNextQueue();
					else if (currentQ.isEmpty() && !upQ.isEmpty())
					{
						currentQ = upQ;
					}
					else if (currentQ.isEmpty()) {
						currentQ = downQ;
					}
					if (!currentQ.isEmpty())
					{
						Request r = currentQ.get(0);
						currentQ.remove(0);
						try {
							goToFloor(r.floor);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				currentQ.clear();
				upQ.clear();
				downQ.clear();
			}

		});
		thread.start();
	}
}
