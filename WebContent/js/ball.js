function Ball(props){
	this.id = props.id;
	this.x = props.x;
	this.y = props.y;
	this.radius = 10;
	this.color = 'black';
	this.xS = props.xS || 0;
	this.yS = props.yS || 0;
	this.MAX_SPEED=1.8
}
Ball.prototype.move=function(){
	this.x = this.x+this.xS;
	this.y = this.y + this.yS;
	if(this.x<0){
		this.xS = this.xS*-1;
		this.x=0;
	}else if(this.x>game.WIDTH){
		this.xS = this.xS*-1;
		this.x=game.WIDTH;
	}
	if(this.y<0){
		this.yS = this.yS*-1;
		this.y=0
	}else if(this.y>game.HEIGHT){
		this.yS = this.yS*-1;
		this.y=game.HEIGHT;
	}
	//console.log(this.id+"-" + this.x+"-"+this.y);
}
Ball.prototype.setSpeed=function(x,y){
	var len3 = Math.sqrt(Math.pow(x-this.x,2) + Math.pow(y-this.y,2));
    this.xS = (x-this.x)/len3 * this.MAX_SPEED;
    this.yS = (y-this.y)/len3 * this.MAX_SPEED;
    //console.log("改变方向=" + x + "-" + y);
}