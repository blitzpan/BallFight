package com.ballFight.bean;

import java.util.UUID;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Ball {
	private static double MAX_SPEED = 9;//v = MAX_SPEED/radius
	private String id;
	private int type;
	private double x;
	private double y;
	private double radius;
	private double xS;
	private double yS;
	private double maxS;
	private String name;
	private String color;
	
	public static Ball initABall(){
		Ball b = new Ball();
		b.type = BallConstant.BALL_TYPE_FOOD;
		b.x = Area.RANDOM.nextInt(Area.WIDTH);
		b.y = Area.RANDOM.nextInt(Area.HEIGHT);
		b.radius = Area.RANDOM.nextInt(BallConstant.FOOD_MAX_RADIUS);
		return b;
	}
	public static Ball jsonToBall(JSONObject jo){
		Ball b = new Ball();
		b.id = jo.getString("id");
		b.type = jo.getInt("type");
		b.x = jo.getDouble("x");
		b.y = jo.getDouble("y");
		b.xS = jo.getDouble("xS");
		b.yS = jo.getDouble("yS");
		b.radius = jo.getDouble("radius");
		return b;
	}
	
	public static boolean ifLegal(Ball b1, Ball b2){
		if(b1==null || b2==null){
			return false;
		}
		if(Math.sqrt(Math.pow(b1.x-b2.x, 2) + Math.pow(b1.y-b2.y, 2) ) > 200){//两个球圆心相差10像素，非法
			return false;
		}
		if(Math.abs(b1.radius - b2.radius) > 5){//半径相差5像素，非法
			return false;
		}
		return true;
	}
	public static boolean ifEatLegal(Ball serv, Ball cli, JSONArray balls){
		if(serv==null || cli==null || balls==null){
			return false;
		}
		if(Math.sqrt(Math.pow(serv.x-cli.x, 2) + Math.pow(serv.y-cli.y, 2) ) > 200){//两个球圆心相差10像素，非法
			return false;
		}
		double area = 0;
		JSONObject tempJo;
		for(int i=0; i<balls.size(); i++){
			tempJo = balls.getJSONObject(i);
			area += Math.pow(tempJo.getDouble("radius"),2);
		}
		//服务端球的平方+被吃球的平方-客户端球的平方 理论应该=0，如果>100，那么非法
		if(Math.pow(serv.getRadius(), 2) + area - Math.pow(cli.getRadius(), 2) > 100){
			return false;
		}
		return true;
	}
	public static Ball initAFood(){
		Ball b = new Ball();
		b.setId(UUID.randomUUID().toString());
		b.type = BallConstant.BALL_TYPE_FOOD;
		b.x = Area.getIntRandom(15, Area.WIDTH - 15);
		b.y = Area.getIntRandom(15, Area.HEIGHT - 15);
		b.radius = Area.getIntRandom(BallConstant.FOOD_MIN_RADIUS, BallConstant.FOOD_MAX_RADIUS);
		b.setColor(BallConstant.BALL_COLORS[Area.RANDOM.nextInt(BallConstant.BALL_COLORS.length)]);
		return b;
	}
	public static Ball initPlayer(){
		Ball ball = new Ball();
		ball.setType(BallConstant.BALL_TYPE_PLAYER);
		ball.setX(Area.RANDOM.nextDouble()*20);
		ball.setY(Area.RANDOM.nextDouble()*Area.HEIGHT);
		ball.setRadius(5);
		ball.setColor(BallConstant.BALL_COLORS[Area.RANDOM.nextInt(BallConstant.BALL_COLORS.length)]);
		double v = Ball.MAX_SPEED / 5;
		ball.setMaxS(v);
		ball.setxS(v);
		ball.setyS(0);
		return ball;
	}
	public String[] refresh(JSONObject jo){
		this.setX(jo.getDouble("x"));
		this.setY(jo.getDouble("y"));
		this.setxS(jo.getDouble("xS"));
		this.setyS(jo.getDouble("yS"));
		this.setRadius(jo.getDouble("radius"));
		double v = Ball.MAX_SPEED / this.getRadius() + 0.2;
		double a = Math.abs(Math.sqrt( (v*v) / (this.xS * this.xS + this.yS * this.yS) ));
		this.xS = this.xS * a;
		this.yS = this.yS * a;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ball other = (Ball) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Ball [id=" + id + ", type=" + type + ", x=" + x + ", y=" + y + ", radius=" + radius + ", xS=" + xS
				+ ", yS=" + yS + ", maxS=" + maxS + ", name=" + name + "]";
	}
	public double getMaxS() {
		return maxS;
	}
	public void setMaxS(double maxS) {
		this.maxS = maxS;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	
}
