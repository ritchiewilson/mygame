import tester.*;
import javalib.funworld.*;
import javalib.worldcanvas.*;
import javalib.worldimages.*;
import javalib.colors.*;
import java.util.*;

// World class
class Fundies2Game extends World{
  
  
  int height = 600;
  int width = 300;
  Ship ship;
  ListofAliens LoA;
  Missile missile;
  int score; // keeps track of aliens destroyed
  
  public Fundies2Game(Ship ship, ListofAliens LoA, Missile missile, int score){
    super();
    this.ship = ship;
    this.LoA = LoA;
    this.missile = missile;
    this.score = score;
  }
  
  //produces a new world on a key event
  //World String -> World
  public World onKeyEvent (String ke){
    return new Fundies2Game(this.ship.moveShip(ke), 
        this.LoA, 
        this.missile.dropMissile(ke, this.ship),
        this.score);
  }
  
  //produces a new World every tick after running the move functions 
  //World -> World
 public World onTick(){

   int killed = this.LoA.aliensKilled(this.missile);
   if(this.LoA.hitShip(this.ship))
     return new Fundies2Game(new Ship(this.ship.p, 0, this.ship.f), 
         this.LoA, this.missile, this.score);
   else
    return new Fundies2Game(this.ship.updateTimeSinceFired(), 
        this.LoA.spawn().collisions(this.missile).moveLoA(), 
        this.missile.moveMissile(),
        this.updateScore());
  }
 //produces the text image that displays the current score
 //World -> WorldImage
 public WorldImage makeText(){
   return new TextImage(new Posn(this.width - 35, this.height - 10), 
       "score:"+this.score, 15, 1, new Yellow());
   }
 //produces the text image that displays on game over
 //World -> WorldImage
 public WorldImage makeGameOverText(){
   return new TextImage(new Posn(this.width/2, this.height/2),
       "Game Over", 50, 1, new Red());
 }
 //draws all the elements of the game
 //World -> WorldImage 
 public WorldImage makeImage(){
   WorldImage bg = new RectangleImage(new Posn(width/2,height/2), 
       this.width, this.height, new Black());
   WorldImage composite = new OverlayImages(bg, 
        new OverlayImages(this.ship.drawShip(), 
            new OverlayImages(this.LoA.drawLoA(),
                new OverlayImages(this.missile.drawMissile(), this.makeText()))));
   if(this.ship.lives == 0)
     return new OverlayImages(composite, this.makeGameOverText());
   else 
     return composite;
  }
 
 public int updateScore(){
   return this.score + this.LoA.aliensKilled(this.missile);
 }
 

};
//contains methods that deal with the player-controlled ship
class Ship{
  Posn p; //current position of the Ship
  int lives; // remaining lives (can only be 0 or 1)
  int f; // number of since fired
  
  Ship(Posn p, int lives, int f){
    this.p = p;
    this.lives = lives;
    this.f = f; 
  }
  // moves this ship based on given key events
  // Ship String -> Ship 
  Ship moveShip(String ke){
    if (this.lives == 0)
      return this;
    if (ke.equals("left") &&
        this.p.x >= 35)
      return new Ship(new Posn(this.p.x - 10, this.p.y), this.lives, this.f);
    if (ke.equals("right") &&
        this.p.x <= 265)
      return new Ship(new Posn(this.p.x + 10, this.p.y), this.lives, this.f);
    if (ke.equals("up") &&
        this.p.y >= 35)
      return new Ship(new Posn(this.p.x, this.p.y - 10), this.lives, this.f);
    if (ke.equals("down") &&
        this.p.y <= 565)
      return new Ship(new Posn(this.p.x, this.p.y + 10), this.lives, this.f);
    if (ke.equals(" ") && (this.f > 3))
      return new Ship(new Posn(this.p.x, this.p.y), this.lives, 0);
    else 
      return this;
  }
  //updates the f to keep track of how many ticks have passed since last fired
  //Ship -> Ship
  Ship updateTimeSinceFired(){
    return new Ship(this.p, this.lives, this.f+1);
  }
  //Produces the png file that represents the ship
  //Ship -> WorldImage
  WorldImage drawShip(){
    return new FromFileImage(this.p, "galagaship.png");
  }
}
// contains methods that deal with singular alien
class Alien{
  Posn p;//current position
  
  Alien(Posn p){
    this.p = p;
  }
  // moves this alien by a set amount
  // Alien -> Alien
  Alien moveAlien(){
    return new Alien(new Posn(this.p.x, this.p.y + 10));
  }
  //represents this Alien on the game screen
  //Alien -> WorldImage
  WorldImage drawAlien(){
    return new DiskImage(this.p, 5, new Red());
  }
  //calculates the distance of this alien from the given missile
  //Alien Missile -> int
  int distanceFromExplosion(Missile m){
    double dx2 = Math.pow((this.p.x - m.p.x),2);
    double dy2 = Math.pow((this.p.y - m.p.y),2);
    return (int)Math.round(Math.sqrt(dx2 + dy2));
  }
  //returns true if this alien is within the radius of the given missile
  //Alien Missile -> boolean
  boolean collides(Missile m){
    return this.distanceFromExplosion(m) <= m.radius;
  }
  //calculates distance of this alien from the given ship
  //Alien Ship -> int
  int distanceToShip(Ship s){
    double dx = Math.pow((this.p.x - s.p.x), 2);
    double dy = Math.pow((this.p.y - s.p.y), 2);
    return (int)Math.round(Math.sqrt(dx + dy));
  }
}
//This interface adapts functions from the Alien class for use with a List of Aliens
interface ListofAliens{
  //Moves this ListofAliens using the moveAlien method
  //ListofAliens -> ListofAliens
  ListofAliens moveLoA();
  //draws this ListofAliens using the drawAlien method
  //ListofAlien -> WorldImage
  WorldImage drawLoA();
  //produces a ListofAliens that contains elements that spawned at a given y
  //with a randomly generated x
  //ListofAliens -> ListofAliens
  ListofAliens spawn();
  //checks this ListofAliens against the given Missile to check for collisions
  //ListofAliens Missile -> ListofAliens
  ListofAliens collisions(Missile m);
  //calculates the number of aliens that collided with the given missile
  //ListofAliens Missile -> int
  int aliensKilled(Missile m);
  //checks if any elements of this list came into contact with the given ship 
  //ListofAliens Ship -> boolean
  boolean hitShip(Ship s);
};
//contains methods that are consistent in both consAlien and mtAlien
abstract class AListofAliens implements ListofAliens{
  public ListofAliens spawn(){
    Random rand = new Random();
    
    if (rand.nextInt(3) == 0)
        return new consAlien(new Alien(new Posn(rand.nextInt(300), 0)), this);
    else
      return this;
  }
}
//called on if there are Aliens in the list of Aliens
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
  public int aliensKilled(Missile m){
    if (m.status == "exploding")
          if(this.first.collides(m))
            return 1 + this.rest.aliensKilled(m);
          else 
            return this.rest.aliensKilled(m);
    else 
      return 0;
  }
  public boolean hitShip(Ship s){
    if(this.first.distanceToShip(s) < 40)
      return true;
    else 
      return this.rest.hitShip(s);
  }
}
//refers to an empty list of Aliens
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
  public int aliensKilled(Missile m){
    return 0;
  }
  public boolean hitShip(Ship s){
    return false;
  }
}


class Missile{
  Posn p; //current position
  int t; //number of ticks since status change
  String status; //missile status
  int radius;//radius of the missile
  
  Missile(Posn p, int t, String status, int radius){
    this.p = p;
    this.t= t;
    this.status = status;
    this.radius = radius;
  }
  //moves this missile by a set distance 
  //Missile -> Missile
  Missile moveMissile(){
    int radius = 0;
    if(this.status == "dropping")
      radius = 5;
    else if(this.status == "exploding")
      radius = (30 * this.t) + 5 - (4 * this.t * this.t);
    if(this.status == "dropping" && this.t >= 10)
      return new Missile(this.p, 0, "exploding", radius);
    if(this.status == "dropping")
      return new Missile(new Posn(this.p.x, this.p.y - 10), this.t + 1,"dropping", radius);
    if(this.status == "exploding" && t > 6)
      return new Missile(new Posn(0, 0), 0, "onBoard", 0);
    if(this.status == "exploding")
      return new Missile(this.p, this.t + 1, "exploding", radius);
    else 
      return this;
    
  }
  //Draws this Missile based on what state it is in
  //Missile -> Missile
  WorldImage drawMissile(){
    if (this.status == "dropping")
      return new DiskImage(this.p, this.radius, new Blue());
    if (this.status == "exploding"){
      return new DiskImage(this.p, this.radius, new Blue());
    }
    else
      return new DiskImage(this.p, this.radius, new Black());
    
  } 
  //Creates a new missile if both the space bar is pressed and the ship has reloaded
  //Missile String Ship -> Missile 
  Missile dropMissile(String ke, Ship ship){
    if(ke.equals(" ") && this.status == "onBoard")
      return new Missile(new Posn(ship.p.x, ship.p.y), 0, "dropping", 5);
    else return this;
  }
};




//Examples class
class ExamplesFundies2Game{
  
  Ship s1 = new Ship(new Posn(150, 550), 3, 0);
  Ship s2 = new Ship(new Posn(10, 550), 3, 0);
  Ship initShip = new Ship(new Posn(150, 550), 1, 0);
  Alien a1 = new Alien(new Posn(150, 0));
  Alien a2 = new Alien(new Posn(150, 10));
  Alien a3 = new Alien(new Posn(150, 20));
  Alien a4 = new Alien(new Posn(150, 30));
  Alien a5 = new Alien(new Posn(150, 600));
  Alien a6 = new Alien(new Posn(50, 600));
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
  Missile m8 = new Missile(new Posn(150, 40), 7, "exploding", 229);
  Missile m9 = new Missile(new Posn(150, 40), 7, "onBoard", 229);
  Missile m10 = new Missile(new Posn(150, 40), 7, "dropping", 229);
  int score = 0;
  
  
  
  /*
   * Initial game screen with a ship, a list of Aliens and a list of missiles.
   * 
   */
  Fundies2Game game = new Fundies2Game(initShip, mt1, m1, score);
  Fundies2Game game2 = new Fundies2Game(s1, LoA1, m8, 5);
  
  
  boolean testGame(Tester t){
    return 
        /*
         * Tests for Fundies2Game methods
         */
        t.checkExpect(game.updateScore(), 0) &&
        t.checkExpect(game2.updateScore(), 8) &&
        
        /*
         * Tests for Ship methods
         */
        t.checkExpect(s1.moveShip("up"), new Ship(new Posn(150, 540), 3, 0)) &&
        t.checkExpect(s1.moveShip("down"), new Ship(new Posn(150, 560), 3, 0)) &&
        t.checkExpect(s1.moveShip("left"), new Ship(new Posn(140, 550), 3, 0)) &&
        t.checkExpect(s1.moveShip("right"), new Ship(new Posn(160, 550), 3, 0)) &&
        
        t.checkExpect(s1.updateTimeSinceFired(), new Ship(s1.p, s1.lives, s1.f+1)) &&
        
        
        /*
         * Check methods on Alien
         */
        t.checkExpect(a1.moveAlien(), new Alien(new Posn(150, 10))) &&
        t.checkExpect(a2.moveAlien(), new Alien(new Posn(150, 20))) &&
        
        t.checkExpect(a1.distanceFromExplosion(m5), 0) &&
        t.checkExpect(a1.distanceFromExplosion(m1), 600) &&
        t.checkExpect(a6.distanceFromExplosion(m3), 102) &&
        
        t.checkExpect(a1.collides(m3), false) &&
        t.checkExpect(a6.collides(m2), false) &&
        t.checkExpect(a1.collides(m8), true) &&
        
        
        /*
         * Tests for ListAliens Methods
         */
        t.checkExpect(LoA1.moveLoA(), new consAlien(a2, 
            new consAlien(a3,
                new consAlien(a4, mt1)))) &&
        t.checkExpect(mt1.moveLoA(), mt1) &&
        
        t.checkExpect(LoA1.collisions(m1), LoA1) &&
        t.checkExpect(LoA1.collisions(m8), new consAlien(a5, mt1)) &&
        t.checkExpect(LoA1.collisions(m9),LoA1) &&
        t.checkExpect(LoA1.collisions(m10),LoA1) &&
        
        t.checkExpect(LoA1.aliensKilled(m1), 0) &&
        t.checkExpect(LoA1.aliensKilled(m8), 3) &&
        t.checkExpect(mt1.aliensKilled(m8), 0) &&
        t.checkExpect(LoA1.aliensKilled(m9), 0) &&
        t.checkExpect(LoA1.aliensKilled(m10), 0) &&
        
        
        
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
    












