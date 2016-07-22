package com.ballFight;

import com.ballFight.bean.Area;
import com.ballFight.bean.Ball;
import com.ballFight.thread.EatThread;
import com.ballFight.thread.InitThread;

public class Main {
	public static void main(String[] args) {
		//初始化线程
		InitThread it = new InitThread();
		System.out.println(Area.balls);
		it.start();
		try {
			it.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Area.balls.size());
		System.out.println(Area.balls);
		//移动线程
		EatThread mt = new EatThread();
		mt.start();
		//添加player
//		Area.balls.add(Ball.initPlayer());
//		Area.balls.add(Ball.initPlayer());
//		Area.balls.add(Ball.initPlayer());
	}
}
