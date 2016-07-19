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
			$("#nickName").html(msg.obj.userName);
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
			setMessageInnerHTML("error");
		};
		//连接成功建立的回调方法
		ws.websocket.onopen = function(event){
			setMessageInnerHTML("open");
			chat.login();
		}
		//接收到消息的回调方法
		ws.websocket.onmessage = function(event){
			setMessageInnerHTML(event.data);
			chat.operMsg(event.data);
		}
		//连接关闭的回调方法
		ws.websocket.onclose = function(){
			setMessageInnerHTML("close");
		}
		//监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
		window.onbeforeunload = function(){
			ws.websocket.close();
		}
	},
	sendMsg:function(msg){
		console.log(msg);
		ws.websocket.send(JSON.stringify(msg));
	}
}
//将消息显示在网页上
function setMessageInnerHTML(msg){
	$("#msg_chat").append(msg+"<br/><hr/>");
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
	var msg = {};
	msg['type'] = 'chat';
	msg['msg'] = str;
	ws.sendMsg(msg);
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
		    <div data-options="region:'south',title:' ',collapsible:true" style="height:600px;">
		    	<div class="easyui-layout" data-options="fit:true">
		    		<div data-options="region:'north'" style="padding:5px;height:50px;background:#eee;">
				    	<span id="nickName" style="font:bold 25px/37px 黑体;" onclick='setNickName();'></span>
				    </div>
				    <div id="msg_chat" data-options="region:'center'" style="padding:5px;background:#eee;">
				    	123
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