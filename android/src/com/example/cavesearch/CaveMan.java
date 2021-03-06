package com.example.cavesearch;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

public class CaveMan {
	private  Texture texture;
	private  TextureRegion textureRegion;
	private Sprite sprite;
	private PhysicsHandler phyHandler;
	private float healthbarX;
	private float healthbarY;
	private Scene scene;
	private String color;
	private int health;
	Rectangle healthBar;
	MainActivity boss;
	public float oldposx,oldposy;
	public boolean loadResource(MainActivity mAct,String ipath){
		boss = mAct;
		TextureRegionFactory.setAssetBasePath("gfx/");
		this.texture = new Texture(64,64,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.textureRegion = TextureRegionFactory.createFromAsset(texture, mAct.getApplicationContext(), ipath,0,0);
		return true;
	}

	public  Texture getTexture(){
		return this.texture;
		
	}
	public void drawHealthBar(){
		Line topLine =new Line(healthbarX,healthbarY,healthbarX+100,healthbarY,3);
		topLine.setColor(0,0,0);
		Line leftLine =new Line(healthbarX,healthbarY,healthbarX,healthbarY+10,3);
		leftLine.setColor(0,0,0);
		
		Line bottomLine =new Line(healthbarX,healthbarY+10,healthbarX+100,healthbarY+10,3);
		bottomLine.setColor(0, 0, 0);
		Line rightLine =new Line(healthbarX+100,healthbarY,healthbarX+100,healthbarY+10,3);
		rightLine.setColor(0, 0, 0);
		scene.getLastChild().attachChild(topLine);
		scene.getLastChild().attachChild(leftLine);
		scene.getLastChild().attachChild(rightLine);
		scene.getLastChild().attachChild(bottomLine);
		healthBar = new Rectangle(healthbarX, healthbarY, health,10);
		if(color.equals("red"))
			healthBar.setColor(0.6f, 0, 0);
		else if(color.equals("blue"))
			healthBar.setColor(0, 0, 0.6f);
		
		scene.getLastChild().attachChild(healthBar);
		scene.getLastChild().attachChild(topLine);
		scene.getLastChild().attachChild(leftLine);
		scene.getLastChild().attachChild(rightLine);
		scene.getLastChild().attachChild(bottomLine);
	}
	public void attachToScene(Scene exscene,float x,float y,String color,float hx,float hy){
		sprite = new Sprite(x,y,textureRegion);
		phyHandler = new PhysicsHandler(sprite);
		sprite.registerUpdateHandler(phyHandler);
	
		exscene.getLastChild().attachChild(sprite);

		this.color=color;
		healthbarX=hx;
		healthbarY=hy;
		this.scene = exscene;
		this.health=100;
		drawHealthBar();
	}
	public Sprite getSprite(){
		return this.sprite;
	}
	public void reduceHealth(){
		this.health-=5;
		healthBar.setWidth(health);
		
	}
	public int getHealth(){
		return this.health;
	}
	public PhysicsHandler getPhyHandler(){
		return this.phyHandler;
	}
	public int getWidth(){
		return textureRegion.getWidth();
	}
	public int getHeight(){
		return textureRegion.getHeight();
	}
	public void setVelocity(float x, float y){
		float valuex =x,valuey = y;
		if(sprite.getX()<=0 && x<0 )
			valuex =0;
		else if(sprite.getX()+ sprite.getWidth() >=boss.CAMERA_WIDTH && x>0)
			valuex = 0;
		if(sprite.getY()<=0 && y<0 )
			valuey =0;
		else if(sprite.getY()+ sprite.getHeight() >=boss.CAMERA_HEIGHT && y>0)
			valuey = 0;
		phyHandler.setVelocity(valuex * 300, valuey * 300);

	}
	public void adjust(float x0,float xfinal,float y0,float yfinal){
		float tempx=sprite.getX(),tempy=sprite.getY();
		if(sprite.getX()<x0)
			tempx=x0;
		if(sprite.getX()+sprite.getWidth()>xfinal)
			tempx=xfinal-sprite.getWidth();
		if(sprite.getY()<y0)
			tempy=y0;
		if(sprite.getY()+sprite.getHeight()>yfinal)
			tempy=yfinal-sprite.getHeight();
		sprite.setPosition(tempx, tempy);
	}
	public boolean isOutofBound(float x0,float xfinal,float y0,float yfinal){
		if(sprite.getX()<x0 || sprite.getX()+sprite.getWidth()>xfinal || sprite.getY()<0 || sprite.getY()+sprite.getHeight()>yfinal)
			return true;
		return false;
		
	}
	
	
}
