package com.company;
import java.sql.Timestamp;
public class Request
{
	public Request(Timestamp t,int f,Direction d)
	{
		this.time = t;
		this.floor = f;
		this.director = d;
	}

	public Timestamp time;
	public int floor;
	public Direction director;
}