package com.ballFight.bean;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Room {
	private String id;
	private String roomName;
	private List<User> users = Collections.synchronizedList(new LinkedList<User>());
	
	public Room(){
		id = UUID.randomUUID().toString();
		roomName = UUID.randomUUID().toString();
	}
	
	public int getUserCount(){
		return users.size();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	
}
