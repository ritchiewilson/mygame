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
  ListofMissiles LoM;
  
  public Fundies2Game(Ship ship, ListofAliens LoA, ListofMissiles LoM){
    super();
    this.ship = ship;
    this.LoA = LoA;
    this.LoM = LoM;
  }
  
  public World onKeyEvent (String ke){
    return new Fundies2Game(this.ship.moveShip(ke), 
        this.LoA, 
        this.LoM.addMissile(ke, this.ship));
  }
  
 public World onTick(){
   
    return new Fundies2Game(this.ship.updateTimeSinceFired(), 
        this.LoA.spawn().moveLoA(), 
        this.LoM.moveLoM());
  }
 public WorldImage makeImage(){
    return new OverlayImages(this.ship.drawShip(), 
        new OverlayImages(this.LoA.drawLoA(), this.LoM.drawLoM()));
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
//  public boolean collide(ListofMissiles LoM){
 //   if(this.p.x < LoM.first.p.x + 5 &&
   //     this.p.x > LoM.first.p.x - 5 &&
     //   this.p.y < LoM.first.p.y + 5 &&
   //     this.p.y > LoM.first.p.y - 5)
   // return true;
   // else 
    //  return this.collide(LoM.rest);
 // }
}

interface ListofAliens{
  ListofAliens moveLoA();
  WorldImage drawLoA();
  ListofAliens spawn();
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
  
//  public ListofAliens collision(ListofMissiles LoM){
//    if(this.first.collides(LoM))
//      return this.rest.collision(LoM.rest);
//    else return new consAlien(this.first, this.rest.collision(LoM))  ;
//  }
}

class mtAlien extends AListofAliens{
  public ListofAliens moveLoA(){
    return this;
  }
  public WorldImage drawLoA(){
    return new DiskImage(new Posn(0, 0), 0, new White());
  }
  
  public ListofAliens collision(ListofMissiles LoM){
    return this;
  }
}


class Missile{
  Posn p;
  
  Missile(Posn p){
    this.p = p;
  }
  Missile moveMissle(){
    return new Missile(new Posn(this.p.x, this.p.y - 10));
  }
  WorldImage drawMissile(){
    return new DiskImage(this.p, 5, new Blue());
  }
 
};

interface ListofMissiles{
  ListofMissiles moveLoM();
  WorldImage drawLoM();
  ListofMissiles addMissile(String ke, Ship s);
}

abstract class AListofMissiles implements ListofMissiles{
  
  public ListofMissiles addMissile(String ke, Ship s){
    if (s.f <= 3)
      return this;
    if (ke.equals(" "))
      return new consMissile(new Missile(new Posn(s.p.x, s.p.y)), this);
    else 
      return this;
  }
}

class consMissile extends AListofMissiles{
  Missile first;
  ListofMissiles rest;
  
  consMissile(Missile first, ListofMissiles rest){
    this.first = first;
    this.rest = rest;
  }
 public ListofMissiles moveLoM(){
    if(this.first.p.y <= 0)
      return this.rest.moveLoM();
    else
      return new consMissile(this.first.moveMissle(), this.rest.moveLoM());
  }
 public WorldImage drawLoM(){
   return new OverlayImages(this.first.drawMissile(), this.rest.drawLoM());
 }
 
 
}

class mtMissile extends AListofMissiles{
  
  public ListofMissiles moveLoM(){
    return this;
  }
  public WorldImage drawLoM(){
    return new DiskImage(new Posn(0, 0), 0, new White());
  }
};



class ExamplesFundies2Game{
  
  Ship s1 = new Ship(new Posn(150, 550), 3, 0);
  Alien a1 = new Alien(new Posn(150, 0));
  Alien a2 = new Alien(new Posn(150, 10));
  Alien a3 = new Alien(new Posn(150, 20));
  Alien a4 = new Alien(new Posn(150, 30));
  Alien a5 = new Alien(new Posn(150, 600));
  ListofAliens mt1 = new mtAlien();
  ListofAliens LoA1 = new consAlien(a1, 
      new consAlien(a2,
      new consAlien(a3,
          new consAlien(a5, mt1))));
  Missile m1 = new Missile(new Posn(150, 600));
  Missile m2 = new Missile(new Posn(150, 590));
  Missile m3 = new Missile(new Posn(150, 580));
  Missile m4 = new Missile(new Posn(150, 570));
  Missile m5 = new Missile(new Posn(150, 0));
  ListofMissiles mt2 = new mtMissile();
  ListofMissiles LoM1 = new consMissile(m1, 
      new consMissile(m2,
          new consMissile(m3,
              new consMissile(m5, mt2))));
  
  
  
  /*
   * Initial game screen with a ship, a list of Aliens and a list of missiles.
   * 
   */
  Fundies2Game game = new Fundies2Game(s1, LoA1, new mtMissile());
  
  
  boolean testGame(Tester t){
    return t.checkExpect(s1.moveShip("up"), new Ship(new Posn(150, 540), 3, 0)) &&
        t.checkExpect(a1.moveAlien(), new Alien(new Posn(150, 10))) &&
        t.checkExpect(LoA1.moveLoA(), new consAlien(a2, 
            new consAlien(a3,
                new consAlien(a4, mt1)))) &&
        t.checkExpect(LoM1.moveLoM(), new consMissile(m2, 
            new consMissile(m3,
                new consMissile(m4, mt2)))) &&
                
                game.bigBang(300, 600, 0.3);
    
  }
    
    
}











