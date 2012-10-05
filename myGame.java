import tester.*;
import javalib.funworld.*;
import javalib.worldcanvas.*;
import javalib.worldimages.*;
import javalib.colors.*;
import java.util.*;

class Fundies2Game extends World{
  
  
  int height = 600;
  int width = 300;
  Ship ship;
  ListofAliens LoA;
  Missile missile;
  
  public Fundies2Game(Ship ship, ListofAliens LoA, Missile missile){
    super();
    this.ship = ship;
    this.LoA = LoA;
    this.missile = missile;
  }
  
  public World onKeyEvent (String ke){
    return new Fundies2Game(this.ship.moveShip(ke), 
        this.LoA, 
        this.missile.dropMissile(ke, this.ship));
  }
  
 public World onTick(){
   
    return new Fundies2Game(this.ship.updateTimeSinceFired(), 
        this.LoA.spawn().collisions(this.missile).moveLoA(), 
        this.missile.moveMissile());
  }
 public WorldImage makeImage(){
    return new OverlayImages(this.ship.drawShip(), 
        new OverlayImages(this.LoA.drawLoA(), this.missile.drawMissile()));
  }
 

};

class Ship{
  Posn p;
  int lives;
  int f;
  
  Ship(Posn p, int lives, int f){
    this.p = p;
    this.lives = lives;
    this.f = f; // frames since last fired
  }
  Ship moveShip(String ke){
    if (ke.equals("left"))
      return new Ship(new Posn(this.p.x - 10, this.p.y), this.lives, this.f);
    if (ke.equals("right"))
      return new Ship(new Posn(this.p.x + 10, this.p.y), this.lives, this.f);
    if (ke.equals("up"))
      return new Ship(new Posn(this.p.x, this.p.y - 10), this.lives, this.f);
    if (ke.equals("down"))
      return new Ship(new Posn(this.p.x, this.p.y + 10), this.lives, this.f);
    if (ke.equals(" ") && (this.f > 3))
      return new Ship(new Posn(this.p.x, this.p.y), this.lives, 0);
    else 
      return this;
  }
  
  
  Ship updateTimeSinceFired(){
    return new Ship(this.p, this.lives, this.f+1);
  }
  WorldImage drawShip(){
    return new DiskImage(this.p, 15, new Green());
  }
}

class Alien{
  Posn p;
  
  Alien(Posn p){
    this.p = p;
  }
  
  Alien moveAlien(){
    return new Alien(new Posn(this.p.x, this.p.y + 10));
  }
  WorldImage drawAlien(){
    return new DiskImage(this.p, 5, new Red());
  }
  int distanceFromExplosion(Missile m){
    double dx2 = Math.pow((this.p.x - m.p.x),2);
    double dy2 = Math.pow((this.p.y - m.p.y),2);
    return (int)Math.round(Math.sqrt(dx2 + dy2));
  }
  boolean collides(Missile m){
    return this.distanceFromExplosion(m) <= m.radius;
  }
}

interface ListofAliens{
  ListofAliens moveLoA();
  WorldImage drawLoA();
  ListofAliens spawn();
  ListofAliens collisions(Missile m);
};

abstract class AListofAliens implements ListofAliens{
  public ListofAliens spawn(){
    Random rand = new Random();
    
    if (rand.nextInt(3) == 0)
        return new consAlien(new Alien(new Posn(rand.nextInt(300), 0)), this);
    else
      return this;
  }
}

class consAlien extends AListofAliens{
  Alien first;
  ListofAliens rest;
 
  consAlien(Alien first, ListofAliens rest){
    this.first = first;
    this.rest = rest;
  }
  public ListofAliens moveLoA(){
    if(this.first.p.y >= 600)
      return this.rest.moveLoA();
    else
      return new consAlien(this.first.moveAlien(), this.rest.moveLoA());
  }
  public WorldImage drawLoA(){
    return new OverlayImages(this.first.drawAlien(), this.rest.drawLoA());
  }
  public ListofAliens collisions(Missile m){
    if (m.status != "exploding")
      return this;
    if(this.first.collides(m))
      return this.rest.collisions(m);
    else 
      return new consAlien(this.first, this.rest.collisions(m));
  }
}

class mtAlien extends AListofAliens{
  public ListofAliens moveLoA(){
    return this;
  }
  public WorldImage drawLoA(){
    return new DiskImage(new Posn(0, 0), 0, new White());
  }
  public ListofAliens collisions(Missile m){
    return this;
  }
}


class Missile{
  Posn p;
  int t; //number of ticks since status change
  String status; //missile status
  int radius;
  
  Missile(Posn p, int t, String status, int radius){
    this.p = p;
    this.t= t;
    this.status = status;
    this.radius = radius;
  }
  Missile moveMissile(){
    int radius = 0;
    if(this.status == "dropping")
      radius = 5;
    else if (this.status == "dropping")
      radius = (30 * this.t) + 5 - (4 * this.t * this.t);
    if(this.status == "dropping" && this.t >= 10)
      return new Missile(this.p, 0, "exploding", radius);
    if(this.status == "dropping")
      return new Missile(new Posn(this.p.x, this.p.y - 10), this.t + 1,"dropping", radius);
    if(this.status == "exploding" && t > 6)
      return new Missile(new Posn(0, 0), 0, "onBoard", radius);
    if(this.status == "exploding")
      return new Missile(this.p, this.t + 1, "exploding", radius);
    else 
      return this;
    
  }
  WorldImage drawMissile(){
    if (this.status == "dropping")
      return new DiskImage(this.p, this.radius, new Blue());
    if (this.status == "exploding"){
      return new DiskImage(this.p, this.radius, new Blue());
    }
    else
      return new DiskImage(this.p, this.radius, new White());
    
  } 
  Missile dropMissile(String ke, Ship ship){
    if(ke.equals(" ") && this.status == "onBoard")
      return new Missile(new Posn(ship.p.x, ship.p.y), 0, "dropping", 5);
    else return this;
  }
};





class ExamplesFundies2Game{
  
  Ship s1 = new Ship(new Posn(150, 550), 3, 0);
  Ship s2 = new Ship(new Posn(10, 550), 3, 0);
  Alien a1 = new Alien(new Posn(150, 0));
  Alien a2 = new Alien(new Posn(150, 10));
  Alien a3 = new Alien(new Posn(150, 20));
  Alien a4 = new Alien(new Posn(150, 30));
  Alien a5 = new Alien(new Posn(150, 600));
  Alien a6 = new Alien(new Posn(250, 600));
  ListofAliens mt1 = new mtAlien();
  ListofAliens LoA1 = new consAlien(a1, 
      new consAlien(a2,
      new consAlien(a3,
          new consAlien(a5, mt1))));
  Missile m1 = new Missile(new Posn(150, 600), 0, "onBoard", 0);
  Missile m2 = new Missile(new Posn(150, 590), 3, "dropping", 5);
  Missile m3 = new Missile(new Posn(150, 580), 0, "onBoard", 0);
  Missile m4 = new Missile(new Posn(150, 570), 0, "onBoard", 0);
  Missile m5 = new Missile(new Posn(150, 0), 7, "exploding", 229);
  Missile m6 = new Missile(new Posn(150, 590), 10, "dropping", 5);
  Missile m7 = new Missile(new Posn(150, 570), 0, "onBoard", 0);
  
  
  
  
  /*
   * Initial game screen with a ship, a list of Aliens and a list of missiles.
   * 
   */
  Fundies2Game game = new Fundies2Game(s1, LoA1, m1);
  
  
  boolean testGame(Tester t){
    return t.checkExpect(s1.moveShip("up"), new Ship(new Posn(150, 540), 3, 0)) &&
        t.checkExpect(a1.moveAlien(), new Alien(new Posn(150, 10))) &&
        t.checkExpect(LoA1.moveLoA(), new consAlien(a2, 
            new consAlien(a3,
                new consAlien(a4, mt1)))) &&

        t.checkExpect(a1.distanceFromExplosion(m5), 0) &&
        t.checkExpect(a1.distanceFromExplosion(m1), 600) &&
        t.checkExpect(a6.distanceFromExplosion(m3), 102) &&
        
        
        /*
         *  Tests for Missiles
         */
        // Testing missile.moveMissile()
        t.checkExpect(m1.moveMissile(), m1) &&
        t.checkExpect(m2.moveMissile(), 
            new Missile(new Posn(m2.p.x, m2.p.y-10), m2.t+1, "dropping", 5)) &&
        t.checkExpect(m2.moveMissile(), 
            new Missile(new Posn(m2.p.x, m2.p.y-10), m2.t+1, "dropping", 5)) &&
        t.checkExpect(m5.moveMissile(), 
            new Missile(new Posn(0,0), 0, "onBoard", 0)) &&
        t.checkExpect(m6.moveMissile(), 
            new Missile(m6.p, 0, "exploding", 5)) &&
        
        // Testing dropMissile
        t.checkExpect(m6.dropMissile(" ", s1), m6) &&
        t.checkExpect(m3.dropMissile("t", s1), m3) &&
        t.checkExpect(m3.dropMissile(" ", s2), 
            new Missile(s2.p, 0, "dropping", 5)) &&
      
                game.bigBang(300, 600, 0.3);
    
  }
  
}  
    












