package com.ballFight.thread;

import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.ballFight.bean.Room;
import com.ballFight.bean.User;
import com.ballFight.bean.WebSocketConstant;

public class DelayDelThread extends Thread{
	private Logger log = Logger.getLogger(getClass());
	@Override
	public void run() {
		Session session;
		User user;
		String roomId;
		String sessionId;
		Room room;
		while(true){
			try{
				session = WebSocketConstant.DELAY_DEL_SESSIONS.poll();
				log.info("延迟删除队列.size="+WebSocketConstant.DELAY_DEL_SESSIONS.size());
				if(session == null){
					TimeUnit.SECONDS.sleep(300);
					continue;
				}
				user = WebSocketConstant.SESSION_USER_MAP.get(session);
				if(user==null){
					continue;
				}
				roomId = user.getRoomId();
				sessionId = user.getSessionId();
				if(sessionId!=null){
					WebSocketConstant.SESSIONID_USER_MAP.remove(sessionId);
				}
				if(roomId==null){
					continue;
				}
				room = WebSocketConstant.ROOMID_ROOM_MAP.get(roomId);
				if(room!=null){
					room.getUsers().remove(user);
				}
				WebSocketConstant.SESSION_USER_MAP.remove(session);
				TimeUnit.SECONDS.sleep(60);
			}catch(Exception e){
				log.error("", e);
			}
		}
	}
}