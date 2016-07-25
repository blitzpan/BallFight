function Chat(props){
	this.sessionId=props.sessionId || '';
	this.nickName=props.sessionId || null;
	this.roomId=props.roomId || null;
}
Chat.prototype.login=function(){
	var msg = {};
	msg['type'] = 'login';
	msg['sessionId'] = this.sessionId;
	ws.sendMsg(msg);
};
Chat.prototype.mkRoom=function(){
	$.messager.prompt('提示', '输入房间名称:', function(r){
		if (r){
			var msg = {};
			msg['type'] = 'mkRoom';
			msg['name'] = r;
			game.isBegin=false;
			game.myBall = null;
			game.balls=[];
			ws.sendMsg(msg);
		}
	});
}
Chat.prototype.refreshRoom=function(){
	var msg = {};
	msg['type'] = 'refreshRoom';
	ws.sendMsg(msg);
}
Chat.prototype.inRoom=function(id){
	this.roomId=id;
	var msg = {};
	msg['type'] = 'inRoom';
	msg['roomId'] = id;
	game.isBegin=false;
	game.myBall = null;
	ws.sendMsg(msg);
}
Chat.prototype.operMsg=function(msg){
	msg = JSON.parse(msg);
	if(msg.type=='refreshUser'){
		this.nickName = msg.obj.userName;
		if(msg.obj.roomId!=null && msg.obj.roomId!=''){
			this.roomId = msg.obj.roomId;
		}
		if(msg.obj.userName!=null && msg.obj.userName !=''){
			$("#nickName").html(msg.obj.userName);
		}else{
			$("#nickName").html("昵称");
		}
		if(msg.obj.ball!=null){
			game.myBall = new Ball(msg.obj.ball);
			if(game.repaintInterval==null){
				game.repaintInterval=window.setInterval(function(){game.repaint();},40);
			}
			if(game.moveInterval==null){
				game.moveInterval=window.setInterval(function(){game.move();},40);
			}
			if(game.sendInterval==null){
				game.sendInterval = window.setInterval(function(){game.sendIntervalGameMsg();},1000);
			}
			game.isBegin = true;
		}
	}else if(msg.type=='setNickName'){
		setNickName();
	}else if(msg.type=='rooms'){
		$('#roomDg').datagrid('loadData',msg.obj);
	}else if(msg.type=='refreshFriends'){
		$('#friendsDg').datagrid('loadData',msg.obj);
	}else if(msg.type=='chat'){
		showMsg(msg.obj.from, msg.obj.msg);
	}else if(msg.type=='error'){
		showMsg(msg.obj.from, msg.obj.msg);
	}else if(msg.type.indexOf("game")>=0){
		game.operMsgReceived(msg);
	}
}