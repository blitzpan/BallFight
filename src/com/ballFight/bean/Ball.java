package com.ballFight.bean;

import java.util.UUID;

import net.sf.json.JSONObject;

public class Ball {
	private String id;
	private int type;
	private double x;
	private double y;
	private double radius;
	private double xS;
	private double yS;
	
	public static Ball initABall(){
		Ball b = new Ball();
		b.type = BallConstant.BALL_TYPE_FOOD;
		b.x = Area.RANDOM.nextInt(Area.WIDTH);
		b.y = Area.RANDOM.nextInt(Area.HEIGHT);
		b.radius = Area.RANDOM.nextInt(BallConstant.FOOD_MAX_RADIUS);
		return b;
	}
	public static Ball initAFood(){
		Ball b = new Ball();
		b.setId(UUID.randomUUID().toString());
		b.type = BallConstant.BALL_TYPE_FOOD;
		b.x = Area.getIntRandom(15, Area.WIDTH - 15);
		b.y = Area.getIntRandom(15, Area.HEIGHT - 15);
		b.radius = Area.getIntRandom(BallConstant.FOOD_MIN_RADIUS, BallConstant.FOOD_MAX_RADIUS);
		return b;
	}
	public static Ball initPlayer(JSONObject jo){
		Ball ball = new Ball();
		ball.setType(BallConstant.BALL_TYPE_PLAYER);
		ball.setX(jo.getDouble("x"));
		ball.setY(jo.getDouble("y"));
		ball.setxS(jo.getDouble("xS"));
		ball.setyS(jo.getDouble("yS"));
		ball.setRadius(jo.getDouble("radius"));
		return ball;
	}
	public String[] refresh(JSONObject jo){
		this.setX(jo.getDouble("x"));
		this.setY(jo.getDouble("y"));
		this.setxS(jo.getDouble("xS"));
		this.setyS(jo.getDouble("yS"));
		this.setRadius(jo.getDouble("radius"));
		return null;
	}
	public void dead(){
		this.type = BallConstant.BALL_TYPE_DEAD;
	}
	public boolean isPlayer(){
		return this.type == BallConstant.BALL_TYPE_PLAYER;
	}
	public boolean isDead(){
		return this.type==BallConstant.BALL_TYPE_DEAD;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getRadius() {
		return radius;
	}
	public void setRadius(double radius) {
		this.radius = radius;
	}
	public double getxS() {
		return xS;
	}
	public void setxS(double xS) {
		this.xS = xS;
	}
	public double getyS() {
		return yS;
	}
	public void setyS(double yS) {
		this.yS = yS;
	}
	
}
