package com.ballFight.thread;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.ballFight.bean.Area;
import com.ballFight.bean.Ball;

public class EatThread extends Thread{
	@Override
	public void run() {
		List<Ball> copyList = null;
		Ball curBall = null;
		Ball tempBall = null;
		double distance = 0;
		while(true){
			synchronized(Area.balls){
				copyList = new LinkedList(Area.balls);
			}
			for(int i=copyList.size()-1; i>0; i--){
				curBall = copyList.get(i);
				if(curBall.isDead()){
					continue;
				}
				for(int j= i-1; j>-1; j--){
					tempBall = copyList.get(j);
					if(tempBall.isDead()){
						continue;
					}
					distance = Math.sqrt( Math.pow(curBall.getX()-tempBall.getX(), 2) + Math.pow(curBall.getY()-tempBall.getY(), 2) );
					if(distance < Math.abs(curBall.getRadius() - tempBall.getRadius())){
						if(curBall.getRadius()<tempBall.getRadius()){//小的被吃
							curBall.dead();
							break;
						}else{
							tempBall.dead();
							continue;
						}
					}
				}
			}
			synchronized(Area.balls){
				Iterator<Ball> ite = Area.balls.iterator();
				Ball ball = null;
				while(ite.hasNext()){
					ball = ite.next();
					if(ball.isDead()){
						ite.remove();
					}
				}
			}
//			synchronized (Area.BALLCOUNT) {
//				Area.BALLCOUNT.notifyAll();
//			}
//			System.out.println(Area.balls.size());
//			System.out.println(Area.balls);
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