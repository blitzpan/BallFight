function Game(props){
	this.isBegin=false;
	this.ifCanClick=true;
	this.WIDTH=600;
	this.HEIGHT=600;
	this.myBall=null;
	this.balls=[];
	this.canvas=null;
	this.ctx=null;
	this.canvasCache=null;
	this.ctxCache=null;
	this.repaintInterval=null;
	this.moveInterval=null;
	this.sendInterval=null;
}
Game.prototype.init=function(){
	this.canvas=document.getElementById('gameArea');
	this.ctx=this.canvas.getContext('2d');
	this.ctx.fillStyle='#383838';
	this.ctx.fillRect(0,0,this.WIDTH,this.HEIGHT);
	var curThis = this;
	this.canvas.onclick=function(e){//给canvas添加点击事件
		if(!curThis.ifCanClick){
			return;
		}
		if(!curThis.isBegin){
			return;
		}
	    e=e||event;//获取事件对象
	    //获取事件在canvas中发生的位置
	    var offset = $("#gameArea").offset();
	    var cx=e.clientX-offset.left;
	    var cy=e.clientY-offset.top;
	    curThis.myBall.setSpeed(cx,cy);
	    curThis.sendIntervalGameMsg();
	    curThis.ifCanClick=false;
        window.setTimeout(function(){curThis.ifCanClick=true;}, 500);
	};
}
Game.prototype.beginGame=function(){
	if(this.myBall!=null){
		return;
	}
	if(chat.roomId==null){
		alert("请先创建或者进入一个房间！");
		return;
	}
	this.replay();
}
Game.prototype.replay=function(){
	if(this.myBall==null){
		this.initMyBall();
	}
}
Game.prototype.initMyBall=function(){
	var msg = {};
	msg['type'] = 'game';
	msg['infoType'] = 'initPlayer';
	ws.sendMsg(msg);
}
Game.prototype.move=function(){
	if(this.myBall!=null && this.myBall.type!=0){
		this.myBall.move();
	}
	var eatIndex=[];
	var eatBalls = [];
	for(var i=0;this.balls!=null && i<this.balls.length; i++){
		var tempBall = this.balls[i];
		if(tempBall.type!=0){
			if(tempBall.type==2){
				tempBall.move();
			}
			if(this.myBall!=null && this.myBall.type!=0 && this.myBall.ifEat(tempBall)){
				eatIndex.push(i);
			}
		}
	}
	for(var i=eatIndex.length-1;i>-1;i--){
		eatBalls.push(this.balls[eatIndex[i]]);
		this.balls.splice(eatIndex[i],1);
	}
	//校验被吃的消息
	if(eatBalls.length>0){
		var msg = {};
		msg['type'] = 'game';
		msg['infoType'] = 'eat';
		msg['myBall'] = this.myBall;
		msg['balls'] = eatBalls;
		ws.sendMsg(msg);
	}
}
Game.prototype.repaint=function(){
	this.canvasCache = document.createElement('canvas');
	this.canvasCache.width=this.WIDTH;
	this.canvasCache.height=this.HEIGHT;
	this.ctxCache = this.canvasCache.getContext('2d');
	this.repaintBack();
	for(var tempBall of this.balls){
		if(tempBall.type!=0){
			this.drawABall(tempBall);
		}
	}
	if(this.myBall!=null && this.myBall.type!=0){
		this.drawABall(this.myBall);
	}
	this.ctx.drawImage(this.canvasCache, 0, 0);
}
Game.prototype.drawABall=function(ball){
	this.ctxCache.fillStyle=ball.color;
	this.ctxCache.beginPath();
	this.ctxCache.arc(ball.x,ball.y,ball.radius,0,Math.PI*2,true);
	this.ctxCache.closePath();
	this.ctxCache.fill();
	if(ball.type==2){
		this.ctxCache.fillText(ball.name,ball.x-this.ctxCache.measureText(ball.name).width/2,ball.y-ball.radius-4);
	}
}
Game.prototype.repaintBack=function(){
	this.ctxCache.fillStyle='#383838';
	this.ctxCache.fillRect(0,0,this.WIDTH,this.HEIGHT);
}
Game.prototype.sendIntervalGameMsg=function(type){
	if(!this.isBegin){
		return;
	}
	var msg={
		type:'game',
		infoType:'interval',
		ball:this.myBall
	};
	ws.sendMsg(msg);
}
Game.prototype.operMsgReceived=function(msg){
	if(msg.type=='game_refreshBall'){
		var ball;
		var recBall = msg.obj;
		if(game.myBall!=null && recBall.id==game.myBall.id){
			this.myBall.xS = recBall.xS;
			this.myBall.yS = recBall.yS;
			this.myBall.maxS = recBall.maxS;
			return;
		}
		var add=true;
		for(var i=0; this.balls!=null && i<this.balls.length; i++){
			ball = this.balls[i];
			if(ball.id==recBall.id){
				add = false;
				ball.x = recBall.x;
				ball.y = recBall.y;
				ball.radius = recBall.radius;
				ball.xS = recBall.xS;
				ball.yS = recBall.yS;
				ball.type = recBall.type;
				ball.maxS = recBall.maxS;
			}
		}
		if(add){
			if(recBall.type==2){//玩家
				this.balls.push(new Ball(recBall));
			}else{
				this.balls.splice(0,0,new Ball(recBall));
			}
			
		}
		var curThis=this;
		if(this.repaintInterval==null){
			this.repaintInterval=window.setInterval(function(){curThis.repaint();},40);
		}
		if(this.moveInterval==null){
			this.moveInterval=window.setInterval(function(){curThis.move();},40);
		}
	}else if(msg.type=='game_add_foods'){
		var oldBalls = this.balls.filter(function(temp){
			if(temp.type==2){
				return true;
			}else{
				return false;
			}
		});
		this.balls=[];
		for(var i=0; i<msg.obj.length; i++){
			this.balls.push(new Ball(msg.obj[i]));
		}
		this.balls = this.balls.concat(oldBalls);
		this.repaint();
	}else if(msg.type=='game_mydead'){//game_mydead
		this.myBall=null;
		this.isBegin=false;
		var curThis = this;
		$.messager.confirm('提示', '你被吃辣~~~~(>_<)~~~~<br/>3秒后复活？', function(r){
            if (r){
            	window.clearInterval(curThis.sendInterval);
            	curThis.sendInterval = null;
    			window.setTimeout(function(){curThis.initMyBall();curThis.replay();}, 3000);
            }
        });
	}else if(msg.type=='game_otherDead'){//game_otherDead
		var oldBalls = this.balls.filter(function(temp){
			if(temp.type!=2){
				return true;
			}
			if($.inArray(temp.id, msg.obj)>-1){
				return false;
			}else{
				return true;
			}
		});
		this.balls = oldBalls;
	}else if(msg.type=='game_initMyBall'){
		this.myBall = new Ball(msg.obj);
		this.isBegin=true;
		this.ifCanClick = true;
		var curThis = this;
		if(this.sendInterval==null){
			this.sendInterval = window.setInterval(function(){curThis.sendIntervalGameMsg();},1000);
		}
		if(this.repaintInterval==null){
			this.repaintInterval=window.setInterval(function(){curThis.repaint();},40);
		}
		if(this.moveInterval==null){
			this.moveInterval=window.setInterval(function(){curThis.move();},40);
		}
	}else if(msg.type=='game_delABall'){
		var delId = msg.obj;
		console.log(msg);
		this.balls = this.balls.filter(function(ball){
			if(ball.id==delId){
				return false;
			}else{
				return true;
			}
		});
	}
}