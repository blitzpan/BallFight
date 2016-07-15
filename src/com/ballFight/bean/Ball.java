package com.ballFight.bean;

public class Ball {
	private int type;
	private int x;
	private int y;
	private int radius;
	private int xS;
	private int yS;
	
	public static Ball initABall(){
		Ball b = new Ball();
		b.type = BallConstant.BALL_TYPE_FOOD;
		b.x = Area.RANDOM.nextInt(Area.WIDTH);
		b.y = Area.RANDOM.nextInt(Area.HEIGHT);
		b.radius = Area.RANDOM.nextInt(BallConstant.FOOD_MAX_RADIUS);
		return b;
	}
	public static Ball initPlayer(){
		Ball ball = new Ball();
		ball.setType(BallConstant.BALL_TYPE_PLAYER);
		ball.setX(Area.RANDOM.nextInt(Area.WIDTH));
		ball.setY(Area.RANDOM.nextInt(Area.HEIGHT));
		ball.setxS(10);
		ball.setyS(20);
		ball.setRadius(30);
		return ball;
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
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
	public int getxS() {
		return xS;
	}
	public void setxS(int xS) {
		this.xS = xS;
	}
	public int getyS() {
		return yS;
	}
	public void setyS(int yS) {
		this.yS = yS;
	}
	@Override
	public String toString() {
		return "Ball [type=" + type + ", x=" + x + ", y=" + y + ", radius=" + radius + ", xS=" + xS + ", yS=" + yS
				+ "]";
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
