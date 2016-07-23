function Ball(props){
	this.id = props.id||'';
	this.type = props.type||2;
	this.x = props.x;
	this.y = props.y;
	this.radius = props.radius||5;
	this.color = 'black';
	this.xS = props.xS || 0;
	this.yS = props.yS || 0;
	this.MAX_SPEED=1.8
}
Ball.prototype.move=function(){
	this.x = this.x+this.xS;
	this.y = this.y + this.yS;
	if(this.x - this.radius<0){
		this.xS = this.xS*-1;
		this.x=0 + this.radius;
	}else if(this.x + this.radius>game.WIDTH){
		this.xS = this.xS*-1;
		this.x=game.WIDTH - this.radius;
	}
	if(this.y-this.radius<0){
		this.yS = this.yS*-1;
		this.y=0+this.radius;
	}else if(this.y+this.radius>game.HEIGHT){
		this.yS = this.yS*-1;
		this.y=game.HEIGHT-this.radius;
	}
	//console.log(this.id+"-" + this.x+"-"+this.y);
}
Ball.prototype.setSpeed=function(x,y){
	var len3 = Math.sqrt(Math.pow(x-this.x,2) + Math.pow(y-this.y,2));
    this.xS = (x-this.x)/len3 * this.MAX_SPEED;
    this.yS = (y-this.y)/len3 * this.MAX_SPEED;
    //console.log("改变方向=" + x + "-" + y);
}
Ball.prototype.ifEat = function(ball){
	var res = (this.radius>ball.radius) && (Math.abs(this.radius-ball.radius) >= Math.sqrt(Math.pow(this.x-ball.x, 2) + Math.pow(this.y-ball.y, 2) ));
	if(res){//吃
		this.radius = Math.sqrt(this.radius*this.radius + ball.radius*ball.radius);
	}
	return res;
}