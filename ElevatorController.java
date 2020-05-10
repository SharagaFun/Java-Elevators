package com.company;
import java.util.*;
public class ElevatorController
{

    private Elevator[] elevators;
    private List<List<Integer>> passengersUP = new ArrayList<List<Integer>>(100);
    private List<List<Integer>> passengersDOWN = new ArrayList<List<Integer>>(100);
    public ElevatorController(int num)
    {
        for(int i = 0; i < 100; i++)
        {
            passengersUP.add(new ArrayList<Integer>());
        }
        for(int i = 0; i < 100; i++)
        {
            passengersDOWN.add(new ArrayList<Integer>());
        }
        elevators = new Elevator[num];
        for (int i=0; i<num; i++)
        {
            elevators[i] = new Elevator(this, i+1);
            elevators[i].run();
        }
    }

    private final Elevator getNearestElevator(int floor, Direction d) throws Exception {
        int min=Integer.MAX_VALUE;
        Elevator ret = null;
        for (Elevator elev: elevators)
        {
            if(elev.getDirection()==d||elev.getState()==LState.STOPPED)
            {
                if (Math.abs(floor-elev.getFloor())<min && elev.isWorking())
                {
                    min = Math.abs(floor - elev.getFloor());
                    ret = elev;
                }
            }
        }
        if (ret==null)
        {
            for (Elevator elev: elevators)
            {
                if (Math.abs(floor-elev.getLastFloor())<min && elev.isWorking())
                {
                    min = Math.abs(floor - elev.getFloor());
                    ret = elev;
                }
            }
        }
        if (ret==null)
            throw new Exception("No elevators");
        return ret;
    }

    public final void call(int floor, Direction d, int nfloor) throws Exception {
        getNearestElevator(floor, d).call(floor,d);
        if (d==Direction.DOWN)
            passengersDOWN.get(floor).add(nfloor);
        else
            passengersUP.get(floor).add(nfloor);
    }

    public final void callback(int floor, Direction d, Elevator e)
    {
        if (e.isEmptyQueue())
        {
            for (int p:passengersDOWN.get(floor))
            {
                e.goTo(p);
            }
            passengersDOWN.get(floor).clear();
            for (int p:passengersUP.get(floor))
            {
                e.goTo(p);
            }
            passengersUP.get(floor).clear();
            return;
        }
        if (d==Direction.DOWN) {
            for (int p : passengersDOWN.get(floor)) {
                e.goTo(p);
            }
            passengersDOWN.get(floor).clear();
        }
        else {
            for (int p : passengersUP.get(floor)) {
                e.goTo(p);
            }
            passengersUP.get(floor).clear();
        }
    }
}