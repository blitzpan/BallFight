package com.ballFight.bean;

public class Msg {
	private int status = 0;//失败
	private String msg = "";//简单消息
	private Object obj;//复杂对象
	private String type="";
	
	public void success(String msg){
		status = 1;
		this.msg = msg;
	}
	public void success(String msg, Object obj){
		status = 1;
		this.msg = msg;
		this.obj = obj;
	}
	public void success(String type, String msg, Object obj){
		status = 1;
		this.type = type;
		this.msg = msg;
		this.obj = obj;
	}
	public void fail(String msg){
		this.msg = msg;
	}
	public void fail(String type, String msg){
		this.type = type;
		this.msg = msg;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
