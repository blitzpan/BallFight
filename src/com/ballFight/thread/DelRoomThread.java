package com.ballFight.thread;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ballFight.bean.Room;
import com.ballFight.bean.WebSocketConstant;
/**
 * ClassName: DelHomeThread 
 * @Description: 删除长时间无人的房间
 * @author Panyk
 * @date 2016年7月20日
 */
public class DelRoomThread extends Thread{
	private Logger log = Logger.getLogger(WebSocketConstant.class);
	private long sleepTime = 5*60;//5分钟
	@Override
	public void run() {
		Room room;
		int c = 0;
		long curDate;
		while(true){
			c = 0;
			curDate = new Date().getTime();
			try{
				for(String roomId : WebSocketConstant.ROOMID_ROOM_MAP.keySet()){
					room = WebSocketConstant.ROOMID_ROOM_MAP.get(roomId);
					if(room==null){
						continue;
					}
					if(room.getUserCount()==0 && (curDate-room.getLastOperTime()>WebSocketConstant.delRoomTime) ){
						WebSocketConstant.ROOMID_ROOM_MAP.remove(roomId);
						c++;
					}
				}
				log.info("删除" +c + "个房间。");
				TimeUnit.SECONDS.sleep(sleepTime);
			}catch(Exception e){
				log.error("", e);
			}
		}
	}
}