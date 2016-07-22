package com.test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.ballFight.bean.Ball;
import com.ballFight.bean.Msg;
import com.ballFight.bean.Room;
import com.ballFight.bean.User;
import com.ballFight.bean.WebSocketConstant;

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
        
        WebSocketConstant.DELAY_DEL_SESSIONS.add(this.session);
        System.out.println("延迟删除queue.size=" + WebSocketConstant.DELAY_DEL_SESSIONS.size());
    }
     
    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
    	System.out.println("收到消息=" + message);
    	JSONObject jo = JSONObject.fromObject(message);
    	String type = jo.getString("type");
    	if(type.equals("login")){
        	String sessionId = jo.getString("sessionId");
        	User user = WebSocketConstant.SESSIONID_USER_MAP.get(sessionId);
        	if(user!=null){//用户已经登陆
        		WebSocketConstant.SESSION_USER_MAP.remove(user.getSession());
        		user.setSession(session);
        		WebSocketConstant.SESSION_USER_MAP.put(session, user);
        		//返回用户信息
        		returnUserInfo(session, user);
				//返回房间信息
        		returnRooms(session, user);
        		returnCurUserFriends(user);
        	}else{//用户没有登陆
        		user = new User();
        		user.setId(sessionId);
            	user.setSessionId(sessionId);
            	user.setSession(session);
            	WebSocketConstant.SESSION_USER_MAP.put(session, user);
            	WebSocketConstant.SESSIONID_USER_MAP.put(sessionId, user);
            	Msg msg = new Msg();
            	msg.success("setNickName","请设置昵称！",null);
				sendMessage(session, JSONObject.fromObject(msg).toString());
				//返回房间信息
        		returnRooms(session, user);
        	}
        }else if(type.equals("setNickName")){//
        	User user = WebSocketConstant.SESSION_USER_MAP.get(session);
        	user.setName(jo.getString("nickName"));
        	//返回用户信息
    		returnUserInfo(session, user);
    		refreshRoomUsers(user.getRoomId());
        }else if(type.equals("inRoom")){//进入一个房间
        	String roomId = jo.getString("roomId");
        	Room room = WebSocketConstant.ROOMID_ROOM_MAP.get(roomId);
        	if(room!=null){
        		User user = WebSocketConstant.SESSION_USER_MAP.get(session);
        		String oldRoomId = user.getRoomId();
        		removeUserFromRoom(user);
            	user.setRoomId(roomId);
            	room.getUsers().add(user);
            	room.refreshLastOperTime();
            	//返回房间信息
        		returnRooms(session, user);
        		//刷新老房间和新房间的所有用户的好友列表
        		refreshRoomUsers(oldRoomId);
        		refreshRoomUsers(roomId);
        	}else{
        		Msg msg = new Msg();
            	msg.fail("error", "该房间不存在！");
    			sendMessage(session, JSONObject.fromObject(msg).toString());
        	}
        }else if(type.equals("mkRoom")){//创建一个房间
        	User user = WebSocketConstant.SESSION_USER_MAP.get(session);
        	removeUserFromRoom(user);
        	Room room = new Room();
        	room.getUsers().add(user);
        	room.setRoomName(jo.getString("name"));
        	WebSocketConstant.ROOMID_ROOM_MAP.put(room.getId(), room);
        	user.setRoomId(room.getId());
        	this.returnRooms(session, user);
        	this.returnUserInfo(session, user);
        	this.returnCurUserFriends(user);
        }else if(type.equals("chat")){//聊天
        	User user = WebSocketConstant.SESSION_USER_MAP.get(session);
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
        		List<User> users = WebSocketConstant.ROOMID_ROOM_MAP.get(roomId).getUsers();
        		for(User tempUser : users){
        			if(!tempUser.equals(user)){
        				sendMessage(tempUser.getSession(), msgStr);
        			}
        		}
        	}
        }else if(type.equals("outRoom")){//
        	User user = WebSocketConstant.SESSION_USER_MAP.get(session);
        	this.removeUserFromRoom(user);
        	user.setRoomId(null);
        }else if(type.equals("logout")){//
        	User user = WebSocketConstant.SESSION_USER_MAP.get(session);
        	this.removeUserFromRoom(user);
        	WebSocketConstant.SESSIONID_USER_MAP.remove(user.getSessionId());
        	WebSocketConstant.SESSION_USER_MAP.remove(session);
        }else if(type.equals("refreshRoom")){
        	User user = WebSocketConstant.SESSION_USER_MAP.get(session);
        	returnRooms(session, user);
        }else if(type.equals("game")){
//        	log.debug("game-info=" + jo);
//        	{"type":"game","infoType":"interval","ball":{"x":141.7345582610817,"y":468.4278086152108,"radius":10,"color":"black","xs":-1.7562031457790073,"ys":-0.3946523923098893,"MAX_SPEED":1.8}}
        	String infoType = jo.getString("infoType");
        	if(infoType.equals("interval")){//定时上传消息
        		JSONObject jSBall = jo.getJSONObject("ball");
        		User user = WebSocketConstant.SESSION_USER_MAP.get(session);
        		if(user.getBall()==null){
        			Ball ball = Ball.initPlayer(jSBall);
        			ball.setId(user.getId());
        			user.setBall(ball);
        		}else{
        			user.getBall().refresh(jSBall);
        		}
        		//通知其他玩家
        		this.tellOtherPlayerLoc(user);
        	}
        	
        }
    	
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
    		Room tempRoom = WebSocketConstant.ROOMID_ROOM_MAP.get(tempRoomId);
    		if(tempRoom!=null){
    			tempRoom.getUsers().remove(user);
    		}
    	}
    }
    private List getAllRooms(User user){
    	List res = new ArrayList();
    	Map oneRoomMap;
    	for(Room oneR: WebSocketConstant.ROOMID_ROOM_MAP.values()){
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
    /**
     * @Description:返回当前用户信息 
     * @param @param session
     * @param @param user   
     * @return void  
     * @throws
     * @author Panyk
     * @date 2016年7月19日
     */
    private void returnUserInfo(Session session, User user){
    	Msg msg = new Msg();
		Map userMap = new HashMap();
		userMap.put("userName", user.getName());
		userMap.put("roomId", user.getRoomId());
		msg.success("refreshUser","", userMap);
		sendMessage(session, JSONObject.fromObject(msg).toString());
    }
    /**
     * @Description:刷新当前用户的好友列表 
     * @param @param user   
     * @return void  
     * @throws
     * @author Panyk
     * @date 2016年7月19日
     */
    private void returnCurUserFriends(User user){
    	String roomId = user.getRoomId();
    	if(roomId==null){
    		return;
    	}
    	Room room = WebSocketConstant.ROOMID_ROOM_MAP.get(roomId);
		if(room == null){
			return;
		}
    	Msg msg = new Msg();
		msg.success("refreshFriends","", getARoomUsers(room));
		sendMessage(user.getSession(), JSONObject.fromObject(msg).toString());
    }
    /**
     * @Description:返回所有房间信息 
     * @param @param session
     * @param @param user   
     * @return void  
     * @throws
     * @author Panyk
     * @date 2016年7月19日
     */
    private void returnRooms(Session session, User user){
    	Msg msg = new Msg();
    	msg.success("rooms", "", getAllRooms(user));
		sendMessage(session, JSONObject.fromObject(msg).toString());
    }
    /**
     * @Description:刷新这个房间的所有用户的好友列表 
     * @param @param roomId   
     * @return void  
     * @throws
     * @author Panyk
     * @date 2016年7月19日
     */
    private void refreshRoomUsers(String roomId){
    	if(roomId==null || roomId.length()<1){
    		return;
    	}
    	Room room = WebSocketConstant.ROOMID_ROOM_MAP.get(roomId);
    	if(room==null){
    		return;
    	}
    	Msg msg = new Msg();
    	msg.success("refreshFriends", "", getARoomUsers(room));
    	String refreshFriends = JSONObject.fromObject(msg).toString();
		List<User> users = room.getUsers();
		for(User tempUser : users){
			sendMessage(tempUser.getSession(), refreshFriends);
		}
    }
    /**
     * @Description: 将当前玩家位置信息通知其他玩家
     * @param @param user   
     * @return void  
     * @throws
     * @author Panyk
     * @date 2016年7月22日
     */
    private void tellOtherPlayerLoc(User user){
    	Ball ball = user.getBall();
    	String roomId = user.getRoomId();
    	if(roomId == null){
    		return;
    	}
    	Room room = WebSocketConstant.ROOMID_ROOM_MAP.get(roomId);
    	Msg msg = new Msg();
    	msg.success("game_refreshBall", "", ball);
    	String refreshBall = JSONObject.fromObject(msg).toString();
    	for(User tempUser : room.getUsers()){
    		if(!user.equals(tempUser)){
    			sendMessage(tempUser.getSession(), refreshBall);
    		}
    	}
    }
}