package com.ballFight;

import com.ballFight.bean.Area;
import com.ballFight.bean.Ball;
import com.ballFight.bean.BallConstant;
import com.ballFight.thread.InitThread;
import com.ballFight.thread.EatThread;

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
		Area.balls.add(initPlayer());
		Area.balls.add(initPlayer());
		Area.balls.add(initPlayer());
	}
	public static Ball initPlayer(){
		Ball ball = new Ball();
		ball.setType(BallConstant.BALL_TYPE_PLAYER);
		ball.setX(Area.RANDOM.nextInt(Area.WIDTH));
		ball.setY(Area.RANDOM.nextInt(Area.HEIGHT));
		ball.setxS(1);
		ball.setyS(2);
		ball.setRadius(30);
		return ball;
	}

}
