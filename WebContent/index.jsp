<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>你好</title>
	<script type="text/javascript" src='./js/jquery-3.1.0.min.js'></script>
<style>
body{
	background:gray;
}
</style>
</head>
<body>
<canvas id="area" height="500" width="500"></canvas>

<script type="text/javascript">
var isBegin=false;
var WIDTH=500;
var HEIGHT=500;
var MAX_SPEED = 1.8;
var x=10,y=10;
var xs = 0, ys=0;
var canvas=document.getElementById('area');
var ctx=canvas.getContext('2d');
var canvasCache,ctxCache;//双缓冲

//绑定点击事件
canvas.onclick=function(e){//给canvas添加点击事件
    e=e||event;//获取事件对象
    //获取事件在canvas中发生的位置
    var cx=e.clientX-canvas.offsetLeft;
    var cy=e.clientY-canvas.offsetTop;
    //如果事件位置在矩形区域中
    console.log(cx +" - "+ cy);
    var len3 = Math.sqrt(Math.pow(cx-x,2) + Math.pow(cy-y,2));
    xs = (cx-x)/len3*MAX_SPEED;
    ys = (cy-y)/len3*MAX_SPEED;
}

//背景
function repaintBack(){
	ctxCache.fillStyle='#FFF';
	ctxCache.fillRect(0,0,WIDTH,HEIGHT);
}
function drawABall(x,y,radius,color){
	ctxCache.fillStyle=color;
	ctxCache.beginPath();
	ctxCache.arc(x,y,radius,0,Math.PI*2,true);
	ctxCache.closePath();
	ctxCache.fill();
}
function repaint(){
	canvasCache = document.createElement('canvas');
	canvasCache.width=WIDTH;
	canvasCache.height=HEIGHT;
	ctxCache = canvasCache.getContext('2d');
	ctxCache.clearRect(0,0,WIDTH,HEIGHT);
	repaintBack();
	drawABall(x,y,10,'black');
	ctx.drawImage(canvasCache, 0, 0);
}
function move(){
	x = x+xs;
	y = y + ys;
	if(x<0){
		xs = xs*-1;
		x=0;
	}else if(x>WIDTH){
		xs = xs*-1;
		x=WIDTH;
	}
	if(y<0){
		ys = ys*-1;
		y=0
	}else if(y>HEIGHT){
		ys = ys*-1;
		y=HEIGHT;
	}
}
window.setInterval(repaint,40);
window.setInterval(move,40);
</script>
</body>
</html>