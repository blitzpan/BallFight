<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String socketPath = request.getServerName()+":"+request.getServerPort()+path+"/";
String sessionId = request.getSession().getId();
%>
<!DOCTYPE HTML>
<html>
<head>
	<title>WebSocket</title>
	<link rel="stylesheet" type="text/css" href="js/jquery-easyui-1.4.3/themes/bootstrap/easyui.css">
	<link rel="stylesheet" type="text/css" href="js/jquery-easyui-1.4.3/themes/icon.css">
	<script type="text/javascript" src="js/jquery-easyui-1.4.3/jquery.min.js"></script>
	<script type="text/javascript" src="js/jquery-easyui-1.4.3/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="js/jquery-easyui-1.4.3/locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript" src="js/ball.js"></script>
	<script type="text/javascript" src="js/json2.js"></script>
</head>
<style>
.chat_item{
	border-bottom:thin solid #D4D4D4;
	padding:5px;
}
.chat_item > .userName{
	font:bold 15px/15px black;
	padding-bottom:5px;
}
.chat_item_r{
	border-bottom:thin solid #D4D4D4;
	padding:5px;
}
.chat_item_r > .userName{
	font:bold 15px/15px black;
	padding-bottom:5px;
	text-align:right;
}
</style>
<script>
function test(){
	
}
function setNickName(){
	$.messager.prompt('提示', '输入昵称:', function(r){
		if (r){
			chat.nickName = r;
			$("#nickName").html(r);
			var msg = {};
			msg['type'] = 'setNickName';
			msg['nickName'] = r;
			ws.sendMsg(msg);
		}
	});
}
$(function(){
	ws.init();
	game.init();
});
var chat={
	sessionId:'<%=sessionId %>',
	nickName:null,
	roomId:null,
	login:function(){
		var msg = {};
		msg['type'] = 'login';
		msg['sessionId'] = chat.sessionId;
		ws.sendMsg(msg);
	},
	mkRoom:function(){
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
	},
	refreshRoom:function(){
		var msg = {};
		msg['type'] = 'refreshRoom';
		ws.sendMsg(msg);
	},
	inRoom:function(id){
		chat.roomId=id;
		var msg = {};
		msg['type'] = 'inRoom';
		msg['roomId'] = id;
		game.isBegin=false;
		game.myBall = null;
		ws.sendMsg(msg);
	},
	operMsg:function(msg){
		msg = JSON.parse(msg);
		//console.log("收到=");
		//console.log(msg);
		if(msg.type=='refreshUser'){
			chat.nickName = msg.obj.userName;
			if(msg.obj.roomId!=null && msg.obj.roomId!=''){
				chat.roomId = msg.obj.roomId;
			}
			if(msg.obj.userName!=null && msg.obj.userName !=''){
				$("#nickName").html(msg.obj.userName);
			}else{
				$("#nickName").html("昵称");
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
}
var ws={
	websocket:null,
	init:function(){
		//判断当前浏览器是否支持WebSocket
		if('WebSocket' in window){
			ws.websocket = new WebSocket("ws://<%=socketPath%>websocket1");
		} else {
			alert('您的浏览器版本太老，建议使用谷歌浏览器！');
			window.close();
		}
		//连接发生错误的回调方法
		ws.websocket.onerror = function(){
			showMsg("系统", "连接服务器出现异常！");
		};
		//连接成功建立的回调方法
		ws.websocket.onopen = function(event){
			showMsg("系统", "东风-41洲际导弹准备完毕，随时准备发射！");
			chat.login();
		};
		//接收到消息的回调方法
		ws.websocket.onmessage = function(event){
			chat.operMsg(event.data);
		};
		//连接关闭的回调方法
		ws.websocket.onclose = function(){
			showMsg("系统", "与服务器连接断开！");
		};
		//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
		window.onbeforeunload = function(){
			ws.websocket.close();
		};
	},
	sendMsg:function(msg){
		ws.websocket.send(JSON.stringify(msg));
	}
}
//将消息显示在网页上
function showMsg(name, msg, send){
	if(send==true){
		$("#msg_chat").append("<div class='chat_item_r'><div class='userName'>"+name+"</div><div>"+msg+"</div></div>");
	}else{
		$("#msg_chat").append("<div class='chat_item'><div class='userName'>"+name+"</div><div>"+msg+"</div></div>");
	}
	chatMsgScroll();
}
function chatMsgScroll(){
	var con = $("#msg_chat");
    var scrollTo = $("#msg_chat>div:last");
    var scrollHeight=0;
    if(scrollTo.offset().top < (con.offset().top + con.outerHeight(true))){
    	scrollHeight = 0;
    }else{
    	scrollHeight = scrollTo.offset().top - con.offset().top - con.outerHeight(true) + scrollTo.outerHeight(true) + con.scrollTop();
    }
    if(scrollHeight>0){
    	con.animate({scrollTop:scrollHeight},1000);
    }
}
function formatRoomOper(value,row,index){
	if(value=='1'){
		return '';
	}else{
		return "<a href='javascript:void(0)' class='easyui-linkbutton' onclick='chat.inRoom(\""+row.id+"\")'>进入</a>";
	}
}
function send(){
	var str = $.trim($("#sed_msg").textbox("getValue"));
	if(str==''){
		return;
	}
	if(chat.nickName==null || chat.nickName == ''){
		alert('请点击昵称新建昵称！');
		return ;
	}
	if(chat.roomId==null){
		alert("请先创建或者进入一个房间！");
		return;
	}
	showMsg(chat.nickName,str,true);
	
	var msg = {};
	msg['type'] = 'chat';
	msg['msg'] = str;
	ws.sendMsg(msg);
	$("#sed_msg").textbox("setValue","");
}
//游戏js
var game={
	isBegin:false,
	ifCanClick:true,
	WIDTH:500,
	HEIGHT:500,
	myBall:null,
	balls:[],
	canvas:null,
	ctx:null,
	canvasCache:null,
	ctxCache:null,
	repaintInterval:null,
	moveInterval:null,
	init:function(){
		game.canvas=document.getElementById('gameArea');
		game.ctx=game.canvas.getContext('2d');
		game.ctx.fillStyle='#FFF';
		game.ctx.fillRect(0,0,game.WIDTH,game.HEIGHT);
		game.canvas.onclick=function(e){//给canvas添加点击事件
			if(!game.ifCanClick){
				return;
			}
		    e=e||event;//获取事件对象
		    //获取事件在canvas中发生的位置
		    var offset = $("#gameArea").offset();
		    var cx=e.clientX-offset.left;
		    var cy=e.clientY-offset.top;
		    if(!game.isBegin){
		    	var props = {
		    		x:cx,
		    		y:cy
		    	};
		    	game.myBall = new Ball(props);
		    	game.repaint();
		    }else{
		    	game.myBall.setSpeed(cx,cy);
		        game.ifCanClick=false;
		        window.setTimeout("game.ifCanClick=true", 500);
		    }
		}
	},
	beginGame:function(){
		if(chat.roomId==null){
			alert("请先创建或者进入一个房间！");
			return;
		}
		if(game.myBall==null){
			alert("请先点击设置初始位置。");
			return;
		}
		game.isBegin=true;
		game.myBall.xS=1;
		game.ifCanClick = true;
		window.setInterval(game.sendIntervalGameMsg,1000);
		if(game.repaintInterval==null){
			game.repaintInterval=window.setInterval(game.repaint,40);
		}
		if(game.moveInterval==null){
			game.moveInterval=window.setInterval(game.move,40);
		}
	},
	repaintBack:function(){
		game.ctxCache.fillStyle='#FFF';
		game.ctxCache.fillRect(0,0,game.WIDTH,game.HEIGHT);
	},
	drawABall:function(ball){
		game.ctxCache.fillStyle=ball.color;
		game.ctxCache.beginPath();
		game.ctxCache.arc(ball.x,ball.y,ball.radius,0,Math.PI*2,true);
		game.ctxCache.closePath();
		game.ctxCache.fill();
	},
	repaint:function(){
		game.canvasCache = document.createElement('canvas');
		game.canvasCache.width=game.WIDTH;
		game.canvasCache.height=game.HEIGHT;
		game.ctxCache = game.canvasCache.getContext('2d');
		game.repaintBack();
		if(game.myBall!=null && game.myBall.type!=0){
			game.drawABall(game.myBall);
		}
		for(var tempBall of game.balls){
			if(tempBall.type!=0){
				game.drawABall(tempBall);
			}
		}
		game.ctx.drawImage(game.canvasCache, 0, 0);
	},
	move:function(){
		if(game.myBall!=null && game.myBall.type!=0){
			game.myBall.move();
		}
		var eatIndex=[];
		var eatBalls = [];
		//console.log(game.balls.length);
		for(var i=0;i<game.balls.length; i++){
			var tempBall = game.balls[i];
			if(tempBall.type!=0){
				tempBall.move();
				if(game.myBall!=null && game.myBall.type!=0 && game.myBall.ifEat(tempBall)){
					eatIndex.push(i);
				}
			}
		}
		for(var i=0;i<eatIndex.length;i++){
			eatBalls.push(game.balls[eatIndex[i]]);
			game.balls.splice(eatIndex[i],1);
		}
		//console.log(game.balls.length);
		//校验被吃的消息
		if(eatBalls.length>0){
			var msg = {};
			msg['type'] = 'game';
			msg['infoType'] = 'eat';
			msg['myBall'] = game.myBall;
			msg['balls'] = eatBalls;
			ws.sendMsg(msg);
		}
	},
	sendIntervalGameMsg:function(type){
		if(!game.isBegin){
			return;
		}
		var msg={
			type:'game',
			infoType:'interval',
			ball:game.myBall
		};
		ws.sendMsg(msg);
	},
	operMsgReceived:function(msg){
		//console.log(game.balls.length);
		if(msg.type=='game_refreshBall'){
			var ball;
			var recBall = msg.obj;
			var add=true;
			for(var i=0; i<game.balls.length; i++){
				ball = game.balls[i];
				if(ball.id==recBall.id){
					add = false;
					ball.x = recBall.x;
					ball.y = recBall.y;
					ball.radius = recBall.radius;
					ball.xS = recBall.xS;
					ball.yS = recBall.yS;
					ball.type = recBall.type;
				}
			}
			if(add){
				game.balls.push(new Ball(recBall));
			}
			if(game.repaintInterval==null){
				game.repaintInterval=window.setInterval(game.repaint,40);
			}
			if(game.moveInterval==null){
				game.moveInterval=window.setInterval(game.move,40);
			}
		}else if(msg.type=='game_add_foods'){
			var oldBalls = game.balls.filter(function(temp){
				if(temp.type==2){
					return true;
				}else{
					return false;
				}
			});
			game.balls=[];
			game.balls = oldBalls;
			console.log(game.balls.length);
			console.log(msg.obj);
			console.log("---------");
			for(var i=0; i<msg.obj.length; i++){
				game.balls.push(new Ball(msg.obj[i]));
			}
			if(game.repaintInterval==null){
				game.repaintInterval=window.setInterval(game.repaint,40);
			}
		}
	}
}
</script>
<body class="easyui-layout">
	<div data-options="region:'west'" style="width:200px;padding:5px;background:#eee;">
		<a id="mkRoom" href="javascript:void(0)" class="easyui-linkbutton" onclick="chat.mkRoom()">创建房间</a>
		<a href="javascript:void(0)" class="easyui-linkbutton" onclick="chat.refreshRoom()">刷新</a>
		<a href="javascript:void(0)" class="easyui-linkbutton" onclick="game.beginGame()">开始</a>
		<table id="roomDg" class="easyui-datagrid" data-options="singleSelect:true">
		<thead>
	        <tr>
	            <th data-options="field:'name',width:'70%'">房间名</th>
	            <th data-options="field:'ifin',width:'30%',align:'center',formatter:formatRoomOper ">操作</th>
	        </tr>
    	</thead>
		</table>
	</div>
	<div data-options="region:'center'," style="padding:5px;background:#eee;">
		<canvas id="gameArea" height="500" width="500"></canvas>
	</div>
	<div data-options="region:'east'" style="width:300px;">
		<div class="easyui-layout" data-options="fit:true">
		    <div data-options="region:'center'" style="padding:5px;background:#eee;">
		    	<table id="friendsDg" class="easyui-datagrid" data-options="singleSelect:true">
				<thead>
			        <tr>
			            <th data-options="field:'name',width:'100%'">好友</th>
			        </tr>
		    	</thead>
				</table>
		    </div>
		    <div data-options="region:'south',title:' ',collapsible:true" style="height:500px;">
		    	<div class="easyui-layout" data-options="fit:true">
		    		<div data-options="region:'north'" style="padding:5px;height:50px;background:#eee;">
				    	<span id="nickName" style="font:bold 25px/37px 黑体;" onclick='setNickName();'>昵称</span>
				    </div>
				    <div id="msg_chat" data-options="region:'center'" style="padding:5px;background:#eee;">
				    </div>
				    <div data-options="region:'south'" style="height:100px">
				    	<div class="easyui-panel" data-options="fit:'true',footer:'#ft'">
				    		<input id="sed_msg" class="easyui-textbox" type="text" data-options="fit:'true',multiline:true,required:true"/>
						</div>
						<div id="ft" style="padding:2px">
							<a id="sed_btn" style="float:right" href="javascript:void(0)" class="easyui-linkbutton" onclick="send()">发&ensp;&ensp;送</a>
						</div>
				    </div>
				</div>
		    </div>
		</div>
	</div>
</body>
</html>