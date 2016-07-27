package com.ballFight.bean;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Area {
	public static Random RANDOM = new Random();
	public static int WIDTH = 600;
	public static int HEIGHT = 600;
	public static Integer BALLCOUNT = 50;
	public static List<Ball> balls = Collections.synchronizedList(new LinkedList());
	public static int getIntRandom(int min, int max){
		int i=-1000;
		while(i<min){
			i = RANDOM.nextInt(max);
		}
		return i;
	}
}
