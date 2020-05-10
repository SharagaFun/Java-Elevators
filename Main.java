package com.company;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
public class Main
{
	public static void main(String[] args) throws IOException, InterruptedException {
		ElevatorController e = new ElevatorController(2);
		for (int i=0; i<100; i++)
		{
			int from = ThreadLocalRandom.current().nextInt(0, 100);
			int to = ThreadLocalRandom.current().nextInt(0, 100);
			try {
				e.call(from, from<to?Direction.UP:Direction.DOWN, to);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
			System.out.println("from "+from+" to "+to);
			Thread.sleep(5000);
		}
	}

}