package com.ballFight.bean;

public class Msg {
	private int status = 0;//失败
	private String msg = "";//简单消息
	private Object obj;//复杂对象
	
	public void success(String msg){
		this.msg = msg;
	}
	public void success(String msg, Object obj){
		this.msg = msg;
		this.obj = obj;
	}
	public void fail(String msg){
		this.msg = msg;
	}
}
