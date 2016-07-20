package com.ballFight.bean;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.Session;

public class WebSocketConstant {
    //websocket.session和user对应关系
	public static ConcurrentHashMap<Session, User> SESSION_USER_MAP = new ConcurrentHashMap<Session, User>();
    //httpsession.id和user对应关系
    public static ConcurrentHashMap<String, User> SESSIONID_USER_MAP = new ConcurrentHashMap<String, User>();
    //roomid和room的对应关系
    public static ConcurrentHashMap<String, Room> ROOMID_ROOM_MAP = new ConcurrentHashMap<String, Room>();
    //延迟删除的一个list
    public static ConcurrentLinkedQueue<Session> DELAY_DEL_SESSIONS = new ConcurrentLinkedQueue<Session>();
    //超过该房间无访问则删除
    public static long delRoomTime = 5*60*1000;//5分钟
}
