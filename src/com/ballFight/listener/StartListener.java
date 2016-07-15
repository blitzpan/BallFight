package com.ballFight.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.ballFight.bean.Area;
import com.ballFight.bean.Ball;
import com.ballFight.thread.EatThread;
import com.ballFight.thread.InitThread;
import com.ballFight.thread.MoveThread;

/**
 * Application Lifecycle Listener implementation class StartListener
 *
 */
@WebListener
public class StartListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public StartListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0)  { 
    	//初始化线程
		InitThread it = new InitThread();
//		System.out.println(Area.balls);
		it.start();
		try {
			it.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		System.out.println(Area.balls.size());
//		System.out.println(Area.balls);
		//移动线程
		MoveThread mt = new MoveThread();
		mt.start();
		//吃线程
		EatThread et = new EatThread();
		et.start();
		//添加player
		Area.balls.add(Ball.initPlayer());
		Area.balls.add(Ball.initPlayer());
		Area.balls.add(Ball.initPlayer());
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    }
	
}
