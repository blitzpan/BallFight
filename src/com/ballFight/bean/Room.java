package com.ballFight.bean;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Room {
	private String id;
	private String roomName;
	private long lastOperTime;
	private List<User> users = Collections.synchronizedList(new LinkedList<User>());
	private List<Ball> foods = Collections.synchronizedList(new LinkedList<Ball>());
	
	
	public Room(){
		id = UUID.randomUUID().toString();
		roomName = UUID.randomUUID().toString();
		lastOperTime = new Date().getTime();
	}
	public void initFoods(int count){
		if(count == 0){
			count = BallConstant.MAX_FOOD - this.foods.size();
		}
		int i=0;
		while(i++ < count){
			foods.add(Ball.initAFood());
		}
	}
	
	public List<Ball> getFoods() {
		return foods;
	}
	public void setFoods(List<Ball> foods) {
		this.foods = foods;
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

	public long getLastOperTime() {
		return lastOperTime;
	}

	public void setLastOperTime(long lastOperTime) {
		this.lastOperTime = lastOperTime;
	}
	public void refreshLastOperTime() {
		this.lastOperTime = new Date().getTime();
	}

	@Override
	public String toString() {
		return "Room [id=" + id + ", roomName=" + roomName + ", lastOperTime=" + lastOperTime + ", users=" + users
				+ "]";
	}
	
}
