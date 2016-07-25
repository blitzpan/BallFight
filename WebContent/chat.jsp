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
	<script type="text/javascript" src="js/chat.js"></script>
	<script type="text/javascript" src="js/ws.js"></script>
	<script type="text/javascript" src="js/game.js"></script>
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
var chat,ws,game;
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
	game = new Game({});
	game.init();
	chat = new Chat({sessionId:'<%=sessionId %>'});
	ws = new Ws({socketPath:'<%=socketPath%>'});
	ws.init();
	
});
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
</script>
<body class="easyui-layout">
	<div data-options="region:'west'" style="width:200px;padding:5px;background:#eee;">
		<a id="mkRoom" href="javascript:void(0)" class="easyui-linkbutton" onclick="chat.mkRoom()">创建房间</a>
		<a href="javascript:void(0)" class="easyui-linkbutton" onclick="chat.refreshRoom()">刷新</a>
		<a href="javascript:void(0)" class="easyui-linkbutton" onclick="game.beginGame();">开始</a>
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