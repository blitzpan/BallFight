package com.test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.ballFight.bean.Msg;
import com.ballFight.bean.Room;
import com.ballFight.bean.User;

import net.sf.json.JSONObject;
 
//该注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置。
@ServerEndpoint("/websocket1")
public class MyWebSocket1 {
	private static Logger log = Logger.getLogger(MyWebSocket1.class);
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    private static CopyOnWriteArraySet<MyWebSocket1> webSocketSet = new CopyOnWriteArraySet<MyWebSocket1>();
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    //websocket.session和user对应关系
    private static ConcurrentHashMap<Session, User> SESSION_USER_MAP = new ConcurrentHashMap<Session, User>();
    //httpsession.id和user对应关系
    private static ConcurrentHashMap<String, User> SESSIONID_USER_MAP = new ConcurrentHashMap<String, User>();
    //roomid和room的对应关系
    private static ConcurrentHashMap<String, Room> ROOMID_ROOM_MAP = new ConcurrentHashMap<String, Room>();
    //延迟删除的一个list
    private static ConcurrentLinkedQueue<Session> delayDelSessions = new ConcurrentLinkedQueue<Session>();

    /**
     * 连接建立成功调用的方法
     * @param session  可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }
     
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(){
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1    
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
        
        delayDelSessions.add(this.session);
        System.out.println("延迟删除queue.size=" + delayDelSessions.size());
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
    	System.out.println("收到消息=" + message);
    	System.out.println("session"+session);
    	JSONObject jo = JSONObject.fromObject(message);
    	String type = jo.getString("type");
    	if(type.equals("login")){
        	String sessionId = jo.getString("sessionId");
        	User user = SESSIONID_USER_MAP.get(sessionId);
        	if(user!=null){//用户已经登陆
        		SESSION_USER_MAP.remove(user.getSession());
        		user.setSession(session);
        		SESSION_USER_MAP.put(session, user);
        		Msg msg = new Msg();
        		Map userMap = new HashMap();
        		userMap.put("userName", user.getName());
        		msg.success("refreshUser","", userMap);
				sendMessage(session, JSONObject.fromObject(msg).toString());
				msg = new Msg();
            	msg.success("rooms", "", getAllRooms(user));
				sendMessage(session, JSONObject.fromObject(msg).toString());
        	}else{//用户没有登陆
        		user = new User();
            	user.setSessionId(sessionId);
            	user.setSession(session);
            	SESSION_USER_MAP.put(session, user);
            	SESSIONID_USER_MAP.put(sessionId, user);
            	Msg msg = new Msg();
            	msg.success("setNickName","请设置昵称！",null);
				sendMessage(session, JSONObject.fromObject(msg).toString());
				msg = new Msg();
            	msg.success("rooms", "", getAllRooms(user));
				sendMessage(session, JSONObject.fromObject(msg).toString());
        	}
        }else if(type.equals("setNickName")){//
        	User user = SESSION_USER_MAP.get(session);
        	user.setName(jo.getString("nickName"));
    		Msg msg = new Msg();
    		Map userMap = new HashMap();
    		userMap.put("userName", user.getName());
    		msg.success("refreshUser","", userMap);
			sendMessage(session, JSONObject.fromObject(msg).toString());
        }else if(type.equals("inRoom")){//进入一个房间
        	String roomId = jo.getString("roomId");
        	Room room = ROOMID_ROOM_MAP.get(roomId);
        	if(room!=null){
        		User user = SESSION_USER_MAP.get(session);
        		removeUserFromRoom(user);
            	user.setRoomId(roomId);
            	room.getUsers().add(user);
            	Msg msg = new Msg();
            	msg.success("refreshFriends", "", getARoomUsers(room));
            	String refreshFriends = JSONObject.fromObject(msg).toString();
    			sendMessage(session, refreshFriends);
    			
    			msg = new Msg();
            	msg.success("rooms", "", getAllRooms(user));
				sendMessage(session, JSONObject.fromObject(msg).toString());
				
				List<User> users = room.getUsers();
        		for(User tempUser : users){
        			if(!tempUser.equals(users)){
        				sendMessage(session, refreshFriends);
        			}
        		}
        	}else{
        		Msg msg = new Msg();
            	msg.fail("error", "该房间不存在！");
    			sendMessage(session, JSONObject.fromObject(msg).toString());
        	}
        }else if(type.equals("mkRoom")){//创建一个房间
        	User user = SESSION_USER_MAP.get(session);
        	removeUserFromRoom(user);
        	Room room = new Room();
        	room.getUsers().add(user);
        	room.setRoomName(jo.getString("name"));
        	ROOMID_ROOM_MAP.put(room.getId(), room);
        	user.setRoomId(room.getId());
        	Msg msg = new Msg();
        	msg.success("rooms", "", getAllRooms(user));
			sendMessage(session, JSONObject.fromObject(msg).toString());
        }else if(type.equals("chat")){//聊天
        	User user = SESSION_USER_MAP.get(session);
        	String roomId = user.getRoomId();
        	if(roomId==null){//
        		Msg msg = new Msg();
            	msg.fail("error", "该房间不存在！");
    			sendMessage(session, JSONObject.fromObject(msg).toString());
        	}else{
        		String msgStr = jo.getString("msg");
        		Map resMap = new HashMap();
        		resMap.put("from", user.getName());
        		resMap.put("msg", msgStr);
        		Msg msg = new Msg();
            	msg.success("chat", "", resMap);
            	msgStr = JSONObject.fromObject(msg).toString();
        		List<User> users = ROOMID_ROOM_MAP.get(roomId).getUsers();
        		for(User tempUser : users){
//        			if(tempUser.equals(users)){
//        				sendMessage(session, msgStr);
//        			}
        			sendMessage(session, msgStr);
        		}
        	}
        }else if(type.equals("outRoom")){//
        	User user = SESSION_USER_MAP.get(session);
        	this.removeUserFromRoom(user);
        	user.setRoomId(null);
        }else if(type.equals("logout")){//
        	User user = SESSION_USER_MAP.get(session);
        	this.removeUserFromRoom(user);
        	SESSIONID_USER_MAP.remove(user.getSessionId());
        	SESSION_USER_MAP.remove(session);
        }
        /*
        //群发消息
        for(MyWebSocket1 item: webSocketSet){             
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
        */
    }
     
    /**
     * 发生错误时调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误");
        error.printStackTrace();
    }
     
    /**
     * 这个方法与上面几个方法不一样。没有用注解，是根据自己需要添加的方法。
     * @param message
     * @throws IOException
     */
    public void sendMessage(Session session, String message){
        try {
        	if(session.isOpen()){
        		System.out.println("返回消息=" + message);
        		session.getBasicRemote().sendText(message);
        	}
		} catch (IOException e) {
			this.log.error("sendMessage",e);
		}
    }
 
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }
 
    public static synchronized void addOnlineCount() {
        MyWebSocket1.onlineCount++;
    }
     
    public static synchronized void subOnlineCount() {
        MyWebSocket1.onlineCount--;
    }
    //将用户从原来房间移除
    private void removeUserFromRoom(User user) {
    	String tempRoomId = user.getRoomId();
    	if(tempRoomId!=null && tempRoomId.length()>0){
    		Room tempRoom = ROOMID_ROOM_MAP.get(tempRoomId);
    		if(tempRoom!=null){
    			tempRoom.getUsers().remove(user);
    		}
    	}
    }
    private List getAllRooms(User user){
    	List res = new ArrayList();
    	Map oneRoomMap;
    	for(Room oneR: ROOMID_ROOM_MAP.values()){
    		oneRoomMap = new HashMap();
    		oneRoomMap.put("id", oneR.getId());
    		oneRoomMap.put("name", oneR.getRoomName());
    		if(user.getRoomId()!=null && user.getRoomId().equals(oneR.getId())){//用户在该房间是1
    			oneRoomMap.put("ifin", 1);
    		}else{
    			oneRoomMap.put("ifin", 0);
    		}
    		res.add(oneRoomMap);
    	}
    	return res;
    }
    private List getARoomUsers(Room room){
    	List res = new ArrayList();
    	Map oneUser;
    	for(User user: room.getUsers()){
    		oneUser = new HashMap();
    		oneUser.put("name", user.getName());
    		res.add(oneUser);
    	}
    	return res;
    }
}