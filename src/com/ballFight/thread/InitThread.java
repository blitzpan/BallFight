package com.ballFight.thread;

import java.util.ArrayList;
import java.util.List;

import com.ballFight.bean.Area;
import com.ballFight.bean.Ball;

public class InitThread extends Thread{
	@Override
	public void run() {
		int ballC = 0;
		while(true){
			ballC = Area.balls.size();
			List ballList = new ArrayList();
			while(ballC++ < Area.BALLCOUNT){
				ballList.add(Ball.initABall());
			}
			synchronized(Area.balls){
				Area.balls.addAll(ballList);
			}
			try {
				Area.BALLCOUNT.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}