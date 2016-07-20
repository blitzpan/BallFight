package com.ballFight.thread;

import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import org.apache.log4j.Logger;

import com.ballFight.bean.Room;
import com.ballFight.bean.User;
import com.ballFight.bean.WebSocketConstant;

public class DelayDelThread extends Thread{
	private Logger log = Logger.getLogger(getClass());
	private static long nullSleep = 300;//5分钟
	private static long delSleep = 2*60;//2分钟
	@Override
	public void run() {
		Session session;
		User user;
		String roomId;
		String sessionId;
		Room room;
		while(true){
			try{
				log.info("汇总数据：\nSESSION_USER_MAP.size=" + WebSocketConstant.SESSION_USER_MAP.size()
					+"\nSESSIONID_USER_MAP.size=" + WebSocketConstant.SESSIONID_USER_MAP.size()
					+"\nROOMID_ROOM_MAP.size=" + WebSocketConstant.ROOMID_ROOM_MAP.size()
					+"\nDELAY_DEL_SESSIONS.size=" + WebSocketConstant.DELAY_DEL_SESSIONS.size());
				session = WebSocketConstant.DELAY_DEL_SESSIONS.poll();
				if(session == null){
					TimeUnit.SECONDS.sleep(nullSleep);
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
				TimeUnit.SECONDS.sleep(delSleep);
			}catch(Exception e){
				log.error("", e);
			}
		}
	}
}