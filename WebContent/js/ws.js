function Ws(props){
	this.websocket=null;
	this.socketPath=props.socketPath||'';
}
Ws.prototype.init = function(){
	//判断当前浏览器是否支持WebSocket
	if('WebSocket' in window){
		this.websocket = new WebSocket("ws://"+this.socketPath+"websocket1");
	} else {
		alert('您的浏览器版本太老，建议使用谷歌浏览器！');
		window.close();
	}
	//连接发生错误的回调方法
	this.websocket.onerror = function(){
		showMsg("系统", "连接服务器出现异常！");
	};
	//连接成功建立的回调方法
	this.websocket.onopen = function(event){
		showMsg("系统", "东风-41洲际导弹准备完毕，随时准备发射！");
		chat.login();
	};
	//接收到消息的回调方法
	this.websocket.onmessage = function(event){
		chat.operMsg(event.data);
	};
	//连接关闭的回调方法
	this.websocket.onclose = function(){
		showMsg("系统", "与服务器连接断开！");
		alert("与服务器连接断开，页面将刷新！");
		window.location.href = window.location.href;
	};
	//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
	window.onbeforeunload = function(){
		this.websocket.close();
	};
}
Ws.prototype.sendMsg=function(msg){
	this.websocket.send(JSON.stringify(msg));
}