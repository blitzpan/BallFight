package com.ballFight.thread;

import java.util.concurrent.TimeUnit;

import com.ballFight.bean.Area;
import com.ballFight.bean.Ball;

public class MoveThread extends Thread{
	@Override
	public void run() {
		Ball ball = null;
		while(true){
			for(int i=0; i<Area.balls.size(); i++){
				ball = Area.balls.get(i);
				if(!ball.isPlayer()){
					continue;
				}
				if(ball.getX()> ball.getRadius() && ball.getX() + ball.getRadius() < Area.WIDTH ){
				}else{
					ball.setxS(ball.getxS() * -1);
				}
				ball.setX(ball.getX() + ball.getxS());
				if(ball.getY()> ball.getRadius() && ball.getY() + ball.getRadius() < Area.WIDTH ){
				}else{
					ball.setyS(ball.getyS() * -1);
				}
				ball.setY(ball.getY() + ball.getyS());
			}
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
	}
}