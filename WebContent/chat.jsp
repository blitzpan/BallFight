<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
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
function setNickName(){
	$.messager.prompt('提示', '输入昵称:', function(r){
		if (r){
			chat.nickName = r;
			$("#nickName").html(r);
			var msg = {};
			msg['type'] = 'setNickName';
			msg['nickName'] = r;
			ws.sendMsg(msg);
		}else{
			setNickName();
		}
	});
}
$(function(){
	ws.init();
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
				ws.sendMsg(msg);
			}
		});
	},
	inRoom:function(id){
		chat.roomId=id;
		var msg = {};
		msg['type'] = 'inRoom';
		msg['roomId'] = id;
		ws.sendMsg(msg);
	},
	operMsg:function(msg){
		msg = JSON.parse(msg);
		console.log(msg.type);
		if(msg.type=='refreshUser'){
			chat.nickName = msg.obj.userName;
			if(msg.obj.roomId!=null && msg.obj.roomId!=''){
				$("#nickName").html(msg.obj.userName + " in " + msg.obj.roomId.substring(0,4));
				chat.roomId = msg.obj.roomId;
			}else{
				$("#nickName").html(msg.obj.userName);
			}
		}else if(msg.type=='setNickName'){
			setNickName();
		}else if(msg.type=='rooms'){
			$('#roomDg').datagrid('loadData',msg.obj);
		}else if(msg.type=='refreshFriends'){
			$('#friendsDg').datagrid('loadData',msg.obj);
		}
	}
}
var ws={
	websocket:null,
	init:function(){
		//判断当前浏览器是否支持WebSocket
		if('WebSocket' in window){
			ws.websocket = new WebSocket("ws://127.0.0.1:8080/ball/websocket1");
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
			showMsg("系统", "数据加载完成！");
			chat.login();
		}
		//接收到消息的回调方法
		ws.websocket.onmessage = function(event){
			chat.operMsg(event.data);
		}
		//连接关闭的回调方法
		ws.websocket.onclose = function(){
			showMsg("系统", "与服务器连接断开！");
		}
		//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
		window.onbeforeunload = function(){
			ws.websocket.close();
		}
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
		<table id="roomDg" class="easyui-datagrid" data-options="singleSelect:true">
		<thead>
	        <tr>
	            <th data-options="field:'name',width:'70%'">房间名</th>
	            <th data-options="field:'ifin',width:'30%',align:'center',formatter:formatRoomOper ">操作</th>
	        </tr>
    	</thead>
		<tbody>
			<tr>
				<td>测试aaaa</td>
				<td>
					<a href="javascript:void(0)" class="easyui-linkbutton" onclick="chat.mkRoom()">进入</a>
				</td>
			</tr>
		</tbody>
		</table>
	</div>
	<div data-options="region:'center'," style="padding:5px;background:#eee;"></div>
	<div data-options="region:'east'" style="width:300px;">
		<div class="easyui-layout" data-options="fit:true">
		    <div data-options="region:'center'" style="padding:5px;background:#eee;">
		    	<table id="friendsDg" class="easyui-datagrid" data-options="singleSelect:true">
				<thead>
			        <tr>
			            <th data-options="field:'name',width:'100%'">名称</th>
			        </tr>
		    	</thead>
				</table>
		    </div>
		    <div data-options="region:'south',title:' ',collapsible:true" style="height:500px;">
		    	<div class="easyui-layout" data-options="fit:true">
		    		<div data-options="region:'north'" style="padding:5px;height:50px;background:#eee;">
				    	<span id="nickName" style="font:bold 25px/37px 黑体;" onclick='setNickName();'></span>
				    </div>
				    <div id="msg_chat" data-options="region:'center'" style="padding:5px;background:#eee;">
				    	<div class="chat_item">
				    		<div class="userName">张三</div>
				    		<div>来自火星的问候</div>
				    	</div>
				    	<div class="chat_item_r">
				    		<div class="userName">李四</div>
				    		<div>吃屎吧火星</div>
				    	</div>
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