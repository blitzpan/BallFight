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
var canvas=document.getElementById('area');
var ctx=canvas.getContext('2d');
//背景
ctx.fillStyle='#FFF';
ctx.fillRect(0,0,500,500);

var i=0;
while(i<10){
	i++;
	drawABall(Math.random()*500, Math.random()*500, Math.random()*20, 'black');
}



function drawABall(x,y,radius,color){
	ctx.fillStyle=color;
	ctx.beginPath();
	ctx.arc(x,y,radius,0,Math.PI*2,true);
	ctx.closePath();
	ctx.fill();
}
</script>

</body>
</html>