import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import TUIO.*; 
import java.util.*; 
import controlP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class tuioClient extends PApplet {

/*

 A client to recieve data from tangibles using the TUIO protocol,
 read dataset from CSV files and enable tangible interaction with
 multivariate data.
 
 A project by Sagar Raut <sagarraut@gatech.edu> and Alex Godwin <alex.godwin@gatech.edu>
 
 Based on the TUIO processing demo - part of the reacTIVision project
 by Martin Kaltenbrunner
 http://reactivision.sourceforge.net/
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

// we need to import the TUIO library
// and declare a TuioProcessing client variable




TuioProcessing tuioClient;

// these are some helper variables which are used
// to create scalable graphical feedback
float cursor_size = 15;
float object_size = 60;
float table_size = 760;
float scale_factor = 1;
PFont font;
ReadCSV cereals;
int screenWidth = 1024, screenHeight = 768;
float[][] columns;
DataPoint[] datapoints;
Boolean showPtInfo = false;
int closestPoint;

ArrayList<Axis> axisList;
ArrayList<DataPoint[]> pointSets;
Boolean fiducialIn = false;
int fiducialId = 0;
float speed = 3;
pt origin; //Don't forget to get rid of this before turning in.
boolean drawing = false;


HashMap<Integer, String> idToAttr;  //Maps from fiducial id to the attribute it represents
Vector tuioObjectList;
ArrayList<String> availableAttr; //Contains a list of attributes which can be assigned to new fiducials 

menu fieldsMenu; //The object of the menu class, used to show the list of attributes
int menuFiducial = 12; //The id of the fiducial which brings up the menu
int pointyFiducial = 31; // The name says it all


public void setup()
{
  //size(screen.width,screen.height);
  size(screenWidth, screenHeight);
  noStroke();
  fill(0);
  smooth();

  //font = loadFont("DroidSerif-Italic-48.vlw");

  loop();
  frameRate(30);
  //noLoop();
  axisList = new ArrayList<Axis>();
  font = createFont("Arial", 18);
  scale_factor = height/table_size;

  // we create an instance of the TuioProcessing client
  // since we add "this" class as an argument the TuioProcessing class expects
  // an implementation of the TUIO callback methods (see below)
  tuioClient  = new TuioProcessing(this);


  availableAttr = new ArrayList<String>();
  availableAttr.add("calories");
  availableAttr.add("proteins");
  availableAttr.add("fats");
  availableAttr.add("sodium");
  availableAttr.add("fiber");
  availableAttr.add("carbs");
  availableAttr.add("sugars");
  availableAttr.add("potassium");
  availableAttr.add("vitamins");

  idToAttr = new HashMap<Integer, String>();

  idToAttr.put(111, "calories"); 
  idToAttr.put(112, "proteins"); 
  idToAttr.put(113, "fats");
  idToAttr.put(114, "sodium"); 
  idToAttr.put(115, "fiber"); 
  idToAttr.put(116, "carbs");
  idToAttr.put(117, "sugars"); 
  idToAttr.put(118, "potassium"); 
  idToAttr.put(119, "vitamins");



  cereals = new ReadCSV("data/cereals.csv"); //Read the cereals dataset csv
  columns = cereals.getTwoFields(4, 5); //Create a point for each entry in the dataset

  datapoints = cereals.getPoints();
  for (int i = 0; i < datapoints.length; i++) {
    datapoints[i].setloc("fats", "fiber", screenWidth/2);
    datapoints[i].fillNorm(cereals.min, cereals.range);
    //    datapoints[i].fillNorm(cereals.min, cereals.range);
  }

  closestPoint = 0;

  fieldsMenu = new menu(this, availableAttr); //pass the data-field names to the menu
  fieldsMenu.hide();                          //Keep the menu hidden initially
  pointSets = new ArrayList<DataPoint[]>();
  //  pointSets.add(datapoints);
}

// within the draw method we retrieve a Vector (List) of TuioObject and TuioCursor (polling)
// from the TuioProcessing client and then loop over both lists to draw the graphical feedback.
public void draw()
{

  background(220);
  textFont(font, 18*scale_factor);
  float obj_size = object_size*scale_factor; 
  float cur_size = cursor_size*scale_factor; 

  text("No of points:"+datapoints.length, 10, 30);

  //  pushStyle();
  //  fill(0, 150, 0);
  //  ellipse(origin.x, origin.y, 10, 10);  
  //  popStyle();

  pushStyle();
  tuioObjectList = tuioClient.getTuioObjects();
  //println(tuioObjectList.size());

  for (int i=0;i<tuioObjectList.size();i++) {
    TuioObject tobj = (TuioObject)tuioObjectList.elementAt(i);
    int id = tobj.getSymbolID();

    if (id == pointyFiducial) {

      stroke(0, 255, 0);
      strokeWeight(3);
      //fill(0,255,0,30);
      noFill();
      pushMatrix();
      translate(tobj.getScreenX(width), tobj.getScreenY(height));
      rotate(tobj.getAngle());
      rect(-obj_size/2, -obj_size/2, obj_size, obj_size);
      triangle(obj_size, 0, obj_size/2, -obj_size/2, obj_size/2, obj_size/2 );
      popMatrix();
      fill(100);
    }
    else if (id != menuFiducial) {
      stroke(0, 255, 0);
      strokeWeight(3);
      //fill(0,255,0,30);
      noFill();
      pushMatrix();
      translate(tobj.getScreenX(width), tobj.getScreenY(height));
      rotate(tobj.getAngle());
      rect(-obj_size/2, -obj_size/2, obj_size, obj_size);
      popMatrix();
      fill(100);
      text(""+idToAttr.get(tobj.getSymbolID()), tobj.getScreenX(width)-obj_size/2, tobj.getScreenY(height)-(obj_size/2 + 5));
    }
  }

  popStyle();

  pushStyle();
  Vector tuioCursorList = tuioClient.getTuioCursors();
  for (int i=0;i<tuioCursorList.size();i++) {

    TuioCursor tcur = (TuioCursor)tuioCursorList.elementAt(i);
    Vector pointList = tcur.getPath();

    if (pointList.size()>0) {
      stroke(0, 0, 255);
      TuioPoint start_point = (TuioPoint)pointList.firstElement();

      for (int j=0;j<pointList.size();j++) {
        TuioPoint end_point = (TuioPoint)pointList.elementAt(j);
        line(start_point.getScreenX(width), start_point.getScreenY(height), end_point.getScreenX(width), end_point.getScreenY(height));
        start_point = end_point;
      }

      stroke(192, 192, 192);
      fill(192, 192, 192);
      ellipse( tcur.getScreenX(width), tcur.getScreenY(height), cur_size, cur_size);
      fill(0);
      text(""+ tcur.getCursorID(), tcur.getScreenX(width)-5, tcur.getScreenY(height)+5);
    }
  }

  popStyle();

  //Loop to display each axis on screen
  //Because of the way the axis list is constructed, there
  //is a risk of a concurrent modification exception here.
  synchronized(this) {
    pushStyle();
    for (Axis a: axisList) {
      a.draw();
    }  
    popStyle();
  }

  text("No of points:"+datapoints.length, 10, 30);
  pushStyle();
  checkAssign();    //Check if it is possible to assign a value to the fiducial
  fill(0, 128, 255, 80);                     //Blue, with a slight transparency
  stroke(0, 200);

  synchronized(this) {
    for (DataPoint[] set : pointSets) {
      //Loop to display each datapoint on screen
      for (int i = 0; i < set.length; i++) {
        //  strokeWeight (10 - (i*10)/cereals.length);
        //  stroke(i*3,i*2,i);
        //  float x = (columns[i][0]*screenWidth)/200;
        //  float y = (columns[i][1]*screenHeight)/8;
        //    datapoints[i].move(speed);
        set[i].updateAndMove(10);      
        
        
        //
        if (i == closestPoint && showPtInfo) {
          
          if(set[i].line){
            stroke(0, 128, 255);
            strokeWeight(2);
          }
          else
            set[i].showInfo();           
          set[i].showpt();  
        }
        else{
          if(set[i].line){
            stroke(0,80);
           strokeWeight(1); 
          }         
          set[i].showpt();  
        }        

        //Show the scaled vector from each datapoint to the fiducial
        if (fiducialIn) {
          datapoints[i].showvec();
        }
      }
    }
  }
  popStyle();
}

// these callback methods are called whenever a TUIO event occurs

// called when an object is added to the scene
public void addTuioObject(TuioObject tobj) {
  int id = tobj.getSymbolID();
  println("add object "+ id +" ("+tobj.getSessionID()+") "+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle());
  pt fidPt = P(tobj.getX()*screenWidth, tobj.getY()*screenHeight);


  if (idToAttr.containsKey(id)) {

    if (id<9 && id>0) {
      //Calculate the vector from each datapoint to the fiducial
      if (pointSets.isEmpty())
        pointSets.add(datapoints);
      for (DataPoint[] set : pointSets) {
        for (int i = 0; i < set.length; i++) {
          set[i].setvec(fidPt, idToAttr.get(id));
        }
      }

      //Set a flag indicating that a fiducial is present
      fiducialIn = true;
      fiducialId = tobj.getSymbolID();
    }
    else if (id < 119 && id > 110) {
      createAxisList();
      generateAxisPositions();
    }
  } 
  else if (id == pointyFiducial) {
    showPtInfo = true;
  }
}

// called when an object is removed from the scene
// void removeTuioObject(TuioObject tobj) {
//   println("remove object "+tobj.getSymbolID()+" ("+tobj.getSessionID()+")");
//   fiducialIn = false;
//   fiducialId = 0;
//   int id = tobj.getSymbolID();
//   if (id < 119 && id > 110) {
//     createAxisList();
//     generateAxisPositions();
//   }
// }

public void removeTuioObject(TuioObject tobj) {
  println("remove object "+tobj.getSymbolID()+" ("+tobj.getSessionID()+")");
  fiducialIn = false;
  fiducialId = 0;

  int id = tobj.getSymbolID();
  if (id < 119 && id > 110) {
    createAxisList();
    generateAxisPositions();
  }
  else if (id == menuFiducial) {        //This is the id of the "Menu/Assignment fiducial" 
    fieldsMenu.hide();
  }
  else if (id == pointyFiducial) {
    showPtInfo = false;
  }
}

// called when an object is moved
// void updateTuioObject (TuioObject tobj) {
//   int id = tobj.getSymbolID();

//   //  println("update object "+id+" ("+tobj.getSessionID()+") "+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle()
//   //    +" "+tobj.getMotionSpeed()+" "+tobj.getRotationSpeed()+" "+tobj.getMotionAccel()+" "+tobj.getRotationAccel());
//   pt fidPt = P(tobj.getX()*screenWidth, tobj.getY()*screenHeight);
//   //
//   if (id<9 && id>0) {
//     //Calculate the vector from each datapoint to the fiducial and move it
//     //Will need to be updated to update all datapoints in all sets (even lines)
//     for (int i = 0; i < datapoints.length; i++) {
//       datapoints[i].setvec(fidPt, idToAttr.get(id));
//       datapoints[i].move(speed);
//     }
//   }
//   else if (id < 119 && id > 110) {
//     generateAxisPositions();
//   }
// }

public void updateTuioObject (TuioObject tobj) {
  int id  = tobj.getSymbolID();
  pt fidPt = P(tobj.getScreenX(width), tobj.getScreenY(height));

  if (idToAttr.containsKey(id)) {
    if (id<9 && id>=0) {
      //Calculate the vector from each datapoint to the fiducial and move it
      if (pointSets.isEmpty())
        pointSets.add(datapoints);
      for (DataPoint[] set : pointSets) {
        for (int i = 0; i < set.length; i++) {
          set[i].setvec(fidPt, idToAttr.get(id));
          set[i].move(speed);
        }
      }
    }
    else if (id < 119 && id > 110) {
      generateAxisPositions();
    }
  }
  else if (id == menuFiducial) {
    fieldsMenu.show(tobj.getScreenX(width)+ object_size/2, tobj.getScreenY(height));                      //Align the menu to the right of the menu fiducial
  }
  else if (id == pointyFiducial) {
    
    pt closestPt = new pt();
    pt fidLoc = P(tobj.getScreenX(width), tobj.getScreenY(height)); // The mid-right edge of fiducial
//    triangle(obj_size, 0, obj_size/2, -obj_size/2, obj_size/2, obj_size/2 );
    pt ray = P(0, -object_size);
    ray = ray.add(fidLoc);
    float angle = tobj.getAngle();
    ray = R(ray, angle + HALF_PI, fidLoc);
    
    
//    show(ray);
    for(DataPoint[] set: pointSets){
    for (int i = 0; i< set.length; i++) {
      if ( (d(ray, set[i].loc)) < (d(ray, closestPt)) ) {
        closestPt = set[i].loc;
        closestPoint = i;
      }
    }
    }
  }
}

//When an object is added or removed, we need to establish the current set of
//axes on the display. Synchronized with the axes draw method to avoid concurrent 
//modification issues
public synchronized void createAxisList() {
  axisList.clear();
  Vector tuioObjectList = tuioClient.getTuioObjects();
  for (int i=0;i<tuioObjectList.size();i++) {
    TuioObject tobj = (TuioObject)tuioObjectList.elementAt(i);
    int id = tobj.getSymbolID();
    if (id > 110 && id < 119) {
      TuioObject addedObject = null;
      for (Axis a: axisList) {
        addedObject = a.addTuioObject(tobj);
      }
      if (addedObject == null) {
        Axis axis = new Axis(tobj);        
        axisList.add(axis);
      }
    }
  }
}


public DataPoint[] copyDataPoints(DataPoint[] set) {
  DataPoint[] copiedSet = new DataPoint[set.length];

  for (int i = 0; i < copiedSet.length; i++) {
    copiedSet[i] = new DataPoint(set[i]);
  }

  return copiedSet;
}

//Generate the coordinate positions for all dust particles and set them
public synchronized void generateAxisPositions() {

  pointSets.clear();

  for (Axis a: axisList)
    a.paired = false;

  for (int i = 0; i < axisList.size(); i++) {
    Axis a = axisList.get(i); 

    if (a.isFull()) {    
      pt a0 = P(a.start.getX()*screenWidth, a.start.getY()* screenHeight);
      pt a1 = P(a.end.getX()*screenWidth, a.end.getY()* screenHeight);
      pt aM = P(a0, a1);

      for (int j = i+1; j < axisList.size(); j++) {
        Axis b = axisList.get(j);    
        if (b.isFull()) {
          pt b0 = P(b.start.getX()*screenWidth, b.start.getY()* screenHeight);
          pt b1 = P(b.end.getX()*screenWidth, b.end.getY()* screenHeight);
          println("a: " + a0.x + "," + a0.y + " : " + a1.x + "," + a1.y);
          println("b: " + b0.x + "," + b0.y + " : " + b1.x + "," + b1.y);
          pt bM = P(b0, b1);
          float d = d(aM, bM);
          println("Distance " + d);
          vec U = U(a0, a1);
          vec V = U(b0, b1);
          float dp = dot(U, V);
          //Determine if the two axes are close to a right angle. If so, they should create a scatterplot.
          if (d < 300 && abs(dp) < .2f) {
            a.paired = true;
            b.paired = true;

            println("right angle " + dp);      

            //find the origin of the coordinate system defined by the scatterplot axes (line-line intersection)        
            origin  = linelineintersection(a0, a1, b0, b1);

            //Determine that the new origin does not lay on within either of the two line segments
            boolean isBetween = isBetween(a0, a1, origin) || isBetween(b0, b1, origin);

            println(origin.x + " " + origin.y + " " + isBetween);            

            //find length of each axis (distance from endpoints to the new origin
            pt aNear, aFar;
            pt bNear, bFar;

            if (d(a0, origin) > d(a1, origin)) {
              aFar = a0;
              aNear = a1;
            }
            else {
              aFar = a1;
              aNear = a0;
            }

            if (d(b0, origin) > d(b1, origin)) {
              bFar = b0;
              bNear = b1;
            }
            else {
              bFar = b1;
              bNear = b0;
            }

            float aScale = d(origin, aFar);
            float bScale = d(origin, bFar);              

            //find angle between axes 
            float angle = angle(V(aFar, origin), V(bFar, origin));

            //Should be able to determine direction based on angle...

            //            float shear = 1/tan(angle);
            //
            //            float[][] sMatrix = {
            //              {
            //                1+shear*shear, shear
            //              }
            //              , {
            //                shear, 1
            //              }
            //            };

            //find angle from new axes to origin
            pt ave = P(aFar, bFar);
            vec aveV = V(origin, ave);
            vec identity = U(V(1.0f, 1.0f));
            float rotate = angle(U(aveV), identity);   

            DataPoint[] copiedSet = copyDataPoints(datapoints);           

            for (int k = 0; k < copiedSet.length; k++) {
              pt plot;
              if (angle > 0) {
                plot = P(copiedSet[k].getNormalizedValue(a.attribute), copiedSet[k].getNormalizedValue(b.attribute));
                plot.scale(aScale, bScale);
              }
              else {
                plot = P(copiedSet[k].getNormalizedValue(b.attribute), copiedSet[k].getNormalizedValue(a.attribute));
                plot.scale(bScale, aScale);
              }
              //              plot = multMatrix(sMatrix, plot);

              plot.rotate(rotate);
              plot.add(origin);                                
              copiedSet[k].setDest(plot);
              copiedSet[k].line = false;
            }
            pointSets.add(copiedSet);
          }
          else if (d < 300 && abs(dot(U, R(V))) < .2f) {
            //Otherwise, determine if the two axes are parallel. If so, they should create a parallel coordinate plot.
            println("parallel " + dp);
            a.paired = true;
            b.paired = true;
            DataPoint[] copiedSet = copyDataPoints(datapoints);

            for (int k = 0; k < copiedSet.length; k++) {
              copiedSet[k].line = true;
              copiedSet[k].loc = a.getDestinationAlongAxis(copiedSet[k]);
              copiedSet[k].setDest(b.getDestinationAlongAxis(copiedSet[k]));
            }

            pointSets.add(copiedSet);
          }
          //Need to compose bounding boxes for each plot and test for collisions?
        }
      }
      //Fill unpaired axes as number lines
      if (a.isFull() && !a.paired) {       
        DataPoint[] copiedSet = copyDataPoints(datapoints);       
        for (int j = 0; j < copiedSet.length; j++) {
          copiedSet[j].setDest(a.getDestinationAlongAxis(copiedSet[j]));
        }
        pointSets.add(copiedSet);
      }
    }
  }
}


//Returns the point of intersection between two lines. 
//Returns null if the lines are parallel.
public pt linelineintersection(pt a0, pt a1, pt b0, pt b1) {
  pt intersect = null;
  double x1 = a0.x, x2 = a1.x, x3 = b0.x, x4 = b1.x;
  double y1 = a0.y, y2 = a1.y, y3 = b0.y, y4 = b1.y;          
  double denom = det(x1-x2, y1-y2, x3-x4, y3-y4);

  //  println(xNum + " / " + denom + ", " + yNum + " / " + denom);              
  if (denom != 0) {
    double xNum = det(det(x1, y1, x2, y2), x1-x2, det(x3, y3, x4, y4), x3-x4);
    double yNum = det(det(x1, y1, x2, y2), y1-y2, det(x3, y3, x4, y4), y3-y4);
    double originX = xNum / denom;
    double originY = yNum / denom;

    intersect  = P((float)originX, (float)originY);
  }
  return intersect;
}

//multiply a point by a 2x2 matrix
public pt multMatrix(float[][] matrix, pt p) {
  pt result = P(matrix[0][0]*p.x + matrix [0][1]*p.y, matrix[1][0]*p.x+matrix[1][1]*p.y);
  return result;
}


//determine if a point exists on a line. the line is defined by two points.
public boolean pointOnLine(pt l1, pt l2, pt a) {
  float m = (l2.y - l1.y) / (l2.x - l1.x);
  boolean pointOnLine = false;
  if (a.y == m*(a.x - l1.x) + l1.y)
    pointOnLine = true;
  return pointOnLine;
}


//determinant of two points
public double det(pt a, pt b) {
  return det(a.x, b.x, a.y, b.y);
}

//determinant of a matrix [a,b,c,d]
public double det(double a, double b, double c, double d) {
  return a*d-b*c;
}

//determine if point C is aligned with and between points A & B
//doesn't seem to always work terribly well.
public boolean isBetween(pt A, pt B, pt C) {
  double epsilon = 1e-14f;
  boolean isBetween = true;
  double crossProduct = (C.y - A.y) * (B.x - A.x) - (C.x - A.x) * (B.y - A.y);
  //  println(crossProduct + " " + epsilon);  
  if (crossProduct > epsilon)
    return false;
  double dotProduct = (C.x - A.x) * (B.x - A.x) + (C.y - A.y)*(B.y - A.y);
  //  println(dotProduct + " " + epsilon);  
  if (dotProduct < 0 )
    return false;
  double squaredLengthBA = (B.x - A.x)*(B.x - A.x) + (B.y - A.y)*(B.y - A.y);
  //  println(squaredLengthBA + " " + epsilon);  
  if (dotProduct > squaredLengthBA)
    return false;
  return isBetween;
}

// called when a cursor is added to the scene
public void addTuioCursor(TuioCursor tcur) {
  println("add cursor "+tcur.getCursorID()+" ("+tcur.getSessionID()+ ") " +tcur.getX()+" "+tcur.getY());
}

// called when a cursor is moved
public void updateTuioCursor (TuioCursor tcur) {
  println("update cursor "+tcur.getCursorID()+" ("+tcur.getSessionID()+ ") " +tcur.getX()+" "+tcur.getY()
    +" "+tcur.getMotionSpeed()+" "+tcur.getMotionAccel());
}

// called when a cursor is removed from the scene
public void removeTuioCursor(TuioCursor tcur) {
  println("remove cursor "+tcur.getCursorID()+" ("+tcur.getSessionID()+")");
}

// called after each message bundle
// representing the end of an image frame
public void refresh(TuioTime bundleTime) { 
  //  redraw();
}


//public class Plot {
//  Axis a, b;
//  DataPoint[] set;
//  
//  public Plot (Axis x, Axis y, DataPoint[] data){
//   a = x;
//   b = y; 
//   set = new DataPoint[data.length];
//  }
//}



public class Axis {
  TuioObject start, end;

  int symbolID;
  String attribute; 
  boolean paired = false;

  public Axis (TuioObject tobj) {
    symbolID = tobj.getSymbolID();
    start=tobj; 
    end = null;
    attribute = idToAttr.get(symbolID);
  }

  public Axis (TuioObject tobj0, TuioObject tobj1) {
    symbolID = tobj0.getSymbolID();
    start=tobj0; 
    end = tobj1;
    attribute = idToAttr.get(symbolID);
  }

  public pt getDestinationAlongAxis(DataPoint point) {
    pt xy0 = P(start.getX()*screenWidth, start.getY()* screenHeight);
    pt xy1 = P(end.getX()*screenWidth, end.getY()* screenHeight);  
    return L(xy0, xy1, point.getNormalizedValue(attribute));
  }

  public String print() {
    return attribute;
  }

  public TuioObject addTuioObject(TuioObject tobj) {
    TuioObject addedObject = null;
    if (tobj.getSymbolID() == symbolID) {
      if (start == null && tobj.getAngle() - end.getAngle() < PI / 10) {
        start = tobj;
        addedObject = tobj;
      }
      else if (end == null && tobj.getAngle() - start.getAngle() < PI / 10) {
        end = tobj;
        addedObject = tobj;
      }
    }
    return addedObject;
  }

  public void removeTuioObject(TuioObject tobj) {
    if (tobj.getSymbolID() == symbolID) {
      if (start == tobj) {
        start = null;
      }
      else if (end == tobj) {
        end = null;
      }
    }
  }

  public void updateTuioObject(TuioObject tobj) {
  }

  public boolean isEmpty() {
    boolean isEmpty = false;
    if (start == null && end == null)
      isEmpty = true;
    return isEmpty;
  }

  public boolean isFull() {
    boolean isFull = true;
    if (start == null || end == null)
      isFull = false;
    return isFull;
  }

  public void draw() {
    if (isFull()) {
      pt xy0 = P(start.getX()*screenWidth, start.getY()* screenHeight);
      pt xy1 = P(end.getX()*screenWidth, end.getY()* screenHeight);  
      //      println("Drawing axis" + xy0.x + " " + xy0.y + " " + xy1.x + " " + xy1.y);
      pushStyle();
      fill(100);
      stroke(0);
      line(xy0.x, xy0.y, xy1.x, xy1.y);
      stroke(200);

      for (float i = 0; i <= 1; i+=.1f) {
        pt m0 = L(xy0, xy1, i);
        pt m1 = L(xy0, xy1, i+.05f);       
        pt r = P(m1);
        r.rotate(HALF_PI, m0); 
        line(m0.x, m0.y, r.x, r.y);
      }

      for (float i = 0; i <= 1; i+=.25f) {
        pt m0 = L(xy0, xy1, i);
        pt m1 = L(xy0, xy1, i+.1f);       
        pt r = P(m1);
        r.rotate(HALF_PI, m0); 
        line(m0.x, m0.y, r.x, r.y);
      }

      for (float i = 0; i <= 1; i+=.05f) {
        pt m0 = L(xy0, xy1, i);
        pt m1 = L(xy0, xy1, i+.025f);       
        pt r = P(m1);
        r.rotate(HALF_PI, m0); 
        line(m0.x, m0.y, r.x, r.y);
      }

      //Need text labels for the endpoints, perhaps the quarter increment labels.

      fill(255, 0, 0);
      popStyle();
    }
  }
}



//Method to handle events from the menu and act on it
public void controlEvent(ControlEvent theEvent) {
  // ListBox is if type ControlGroup.
  // 1 controlEvent will be executed, where the event
  // originates from a ControlGroup. therefore
  // you need to check the Event with
  // if (theEvent.isGroup())
  // to avoid an error message from controlP5.

  if (theEvent.isGroup()) {
    // an event from a group e.g. scrollList
    println(theEvent.group().value()+" from "+theEvent.group());
  }

  if (theEvent.isGroup() && theEvent.name().equals("myList")) {
    int test = (int)theEvent.group().value();
    println("test "+test);

    fieldsMenu.reDraw();
  }
}

public void keyPressed() {
  if (key=='0') {

    fieldsMenu.l.setValue(5); // will activate the listbox item with value 5
  }
  else if (key == 'h') {
    fieldsMenu.hide();
  }
  else if (key == 's') {
    fieldsMenu.show();
  }
}

//Checks if exactly two fiducials are present, if yes, checks whether one is the menu fiducial and the other is unassigned
public void checkAssign() {
  if (tuioObjectList.size() == 2 && availableAttr.size() > 0) {

    TuioObject tobj = (TuioObject)tuioObjectList.elementAt(0);
    int id1 = tobj.getSymbolID();
    tobj = (TuioObject)tuioObjectList.elementAt(1);
    int id2 = tobj.getSymbolID();
    if (id1 == menuFiducial && !idToAttr.containsKey(id2) && id2 != pointyFiducial) {
      idToAttr.put(id2, availableAttr.get(0));
      availableAttr.remove(0);
      fieldsMenu.reDraw(availableAttr);
    }
    else if (id2 == menuFiducial && !idToAttr.containsKey(id1) && id1 != pointyFiducial) {
      idToAttr.put(id1, availableAttr.get(0));
      availableAttr.remove(0);
      fieldsMenu.reDraw(availableAttr);
    }
  }
}

class DataPoint {
  pt loc;
  pt dest; 
  vec v;
  
  String name;  
  HashMap<String, Float> dataval;
  HashMap<String, Float> normdata;
  boolean line = false;

  // CREATE
  DataPoint() {
    loc = new pt();    
    dest = null;
    v = V(0,0);
  }


  DataPoint(String title,float cal, float pro, float fat, float sod, float fib, float car, float sug, float pot, float vit) {
    name = title;
    //normalizing values
    loc = new pt();     //initialize the location as the point

    dest = null;
    v = V(0, 0);
    dataval = new HashMap<String, Float>();
    dataval.put("calories", cal);
    dataval.put("proteins", pro);
    dataval.put("fats", fat); 
    dataval.put("sodium", sod); 
    dataval.put("fiber", fib); 
    dataval.put("carbs", car); 
    dataval.put("sugars", sug); 
    dataval.put("potassium", pot); 
    dataval.put("vitamins", vit);
  }   
  
  DataPoint (DataPoint otherPoint){    
    name = otherPoint.name;
    //normalizing values
    if(otherPoint.loc != null)
      loc = P(otherPoint.loc);
    if(otherPoint.dest != null)
      dest = P(otherPoint.dest);   
      
    v = V(otherPoint.v);
    
    normdata = new HashMap<String, Float>();
    normdata.put("calories", otherPoint.getNormalizedValue("calories"));
    normdata.put("proteins", otherPoint.getNormalizedValue("proteins"));
    normdata.put("fats", otherPoint.getNormalizedValue("fats")); 
    normdata.put("sodium", otherPoint.getNormalizedValue("sodium")); 
    normdata.put("fiber", otherPoint.getNormalizedValue("fiber")); 
    normdata.put("carbs", otherPoint.getNormalizedValue("carbs")); 
    normdata.put("sugars", otherPoint.getNormalizedValue("sugars")); 
    normdata.put("potassium", otherPoint.getNormalizedValue("potassium")); 
    normdata.put("vitamins", otherPoint.getNormalizedValue("vitamins"));   
    
  }


  //Normalize each data in a range from 0-1
  public void fillNorm(float[] min, float[] range) {
    //    println("Filling normalized values");

    //Switched these over to normalized in the range of 0-1, making vector manipulation much easier
    normdata = new HashMap<String, Float>();
    float normalizedMax = 1f;
    normdata.put("calories", ((dataval.get("calories")-min[0])*normalizedMax)/range[0]);
    normdata.put("proteins", ((dataval.get("proteins")-min[1])*normalizedMax)/range[1]);
    normdata.put("fats", ((dataval.get("fats")-min[2])*normalizedMax)/range[2]);
    normdata.put("sodium", ((dataval.get("sodium")-min[3])*normalizedMax)/range[3]);
    normdata.put("fiber", ((dataval.get("fiber")-min[4])*normalizedMax)/range[4]);
    normdata.put("carbs", ((dataval.get("carbs")-min[5])*normalizedMax)/range[5]);
    normdata.put("sugars", ((dataval.get("sugars")-min[6])*normalizedMax)/range[6]);
    normdata.put("potassium", ((dataval.get("potassium")-min[7])*normalizedMax)/range[7]);
    normdata.put("vitamins", ((dataval.get("vitamins")-min[8])*normalizedMax)/range[8]);

    //    for (String key : normdata.keySet()) {
    //      println(normdata.get(key));
    //    }
  }

  //Get normalized value for a DataPoint
  public float getNormalizedValue(String attr) {
    return normdata.get(attr);

  }

  //Specify which data valued to use as a co-ordinate
  public void setloc(String colX, String colY, float bias) {
    //println("Using " + colX + " & " + colY);
    loc = P(dataval.get(colX)+bias, dataval.get(colY)+bias);
  }


  //Explicitly set the location
  public void setloc(float X, float Y) {
//    println("Explicitly setting the location");

    loc = P(X, Y);
  } 

  //move the points by a certain value
  public void showpt() {
    if (!line){    
      stroke(0, 200);
      strokeWeight(1);
      ellipse(loc.x, loc.y, 6, 6);
    }
    else
      line(loc.x, loc.y, dest.x, dest.y);
  }
  
  public void showInfo() {
    text(name, loc.x + 5, loc.y);
    
  }

  //move the points by a certain value
  public void showpt(float bias) {
    if (!line)
      ellipse(loc.x, loc.y, 6, 6);
    else
      line(loc.x, loc.y, dest.x, dest.y);
  }

  //accepts the center of the fiducial, and sets a vector from  the current point to the fiducial
  public void setvec(pt fidPt, String attr) {

    //    println("calculating vector");
    v = U(V(loc, fidPt));
    v.scaleBy(normdata.get(attr));
  }

  public void setLine(pt start, pt end) {
    line = true;
    loc = P(start);
    dest = P(end);
    v = U(loc, dest);
  }


  public void setDest(pt destination) {
    dest = destination;
  }

  public void updateDest(pt destination, String attr) {
    if (dest == null)    
      dest = L(loc, destination, getNormalizedValue(attr));
    else
      dest = L(dest, destination, getNormalizedValue(attr));
  }

  public void showvec() {
    fill(0);
    show(loc, v);
  }

  //Update the vector based on the newest destination and move towards it
  public void updateAndMove(float speed) {
    if (!line) {
      if (dest != null) {
        if (d(loc, dest) > 5)
          v = U(loc, dest);
        else 
          v.zero();    
        loc = P(loc, mul(v, speed));
      }
      else {
        dest = loc;
        v = V(0, 0);
      }
    }
  }

  //Move the datapoint in the direction of the fiducial
  public void move(float speed) {
    if (!line) {
      loc = P(loc, mul(v, speed));
      dest = loc;
    }
  }
}


class menu {
  ControlP5 cp5;

  ListBox l;

  ArrayList<String> attributes;

  menu(PApplet theParent, ArrayList<String> idToAttr) {

    attributes = idToAttr;
    //ControlP5.printPublicMethodsFor(ListBox.class);

    cp5 = new ControlP5(theParent);
    l = cp5.addListBox("myList")
      .setPosition(100, 100)
        .setSize(120, 150)
          .setItemHeight(15)
            .setBarHeight(15)
              .setColorBackground(color(255, 128))
                .setColorActive(color(0))
                  .setColorForeground(color(255, 100, 0));

    l.captionLabel().toUpperCase(true);
    l.captionLabel().set("Assignable values");
    l.captionLabel().setColor(0xffff0000);
    l.captionLabel().style().marginTop = 3;
    l.valueLabel().style().marginTop = 3;

    for (int i=0;i<attributes.size();i++) {
      ListBoxItem lbi = l.addItem(attributes.get(i), i);

      if ( i == 0) { 
        lbi.setColorBackground(0xffff0000);
      }
      else {
        lbi.setColorBackground(0xffc1c1c1);
      }
    }
  }

  public void hide() {
    l.setSize(0, 0)
      .setPosition(0, 0);
  }

  public void show() {
    l.setSize(120, 150)
      .setPosition(20, 50);
  }
  
  public void show(float X, float Y) {
    l.setSize(120, 150)
      .setPosition(X, Y);
  }

  public void reDraw() {
    l.clear();
    for (int i=1;i<attributes.size();i++) {


      ListBoxItem lbi = l.addItem(attributes.get(i), i);
      if ( i == 0) { 
        lbi.setColorBackground(0xffff0000);
      }
      else {
        lbi.setColorBackground(color(0, 64));
      }
    }
  }

  //use the ArrayList passed by the calling method
  public void reDraw(ArrayList<String> newAttributes) {
    attributes = newAttributes;
    l.clear();
    for (int i=0;i < attributes.size();i++) {


      ListBoxItem lbi = l.addItem(attributes.get(i), i);
      if ( i == 0) { 
        lbi.setColorBackground(0xffff0000);
      }
      else {
        lbi.setColorBackground(color(0, 64));
      }
    }
  }
}


//*****************************************************************************
// TITLE:         GEOMETRY UTILITIES IN 2D  
// DESCRIPTION:   Classes and functions for manipulating points, vectors, edges, triangles, quads, frames, and circular arcs  
// AUTHOR:        Prof Jarek Rossignac
// DATE CREATED:  September 2009
// EDITS:         Revised July 2011
//*****************************************************************************
//************************************************************************
//**** POINT CLASS
//************************************************************************
class pt { 
  float x=0,y=0; 
  // CREATE
  pt () {}
  pt (float px, float py) {x = px; y = py;};

  // MODIFY
  public pt setTo(float px, float py) {x = px; y = py; return this;};  
  public pt setTo(pt P) {x = P.x; y = P.y; return this;}; 
  public pt setToMouse() { x = mouseX; y = mouseY;  return this;}; 
  public pt add(float u, float v) {x += u; y += v; return this;}                       // P.add(u,v): P+=<u,v>
  public pt add(pt P) {x += P.x; y += P.y; return this;};                              // incorrect notation, but useful for computing weighted averages
  public pt add(float s, pt P)   {x += s*P.x; y += s*P.y; return this;};               // adds s*P
  public pt add(vec V) {x += V.x; y += V.y; return this;}                              // P.add(V): P+=V
  public pt add(float s, vec V) {x += s*V.x; y += s*V.y; return this;}                 // P.add(s,V): P+=sV
  public pt translateTowards(float s, pt P) {x+=s*(P.x-x);  y+=s*(P.y-y);  return this;};  // transalte by ratio s towards P
  public pt scale(float u, float v) {x*=u; y*=v; return this;};
  public pt scale(float s) {x*=s; y*=s; return this;}                                  // P.scale(s): P*=s
  public pt scale(float s, pt C) {x*=C.x+s*(x-C.x); y*=C.y+s*(y-C.y); return this;}    // P.scale(s,C): scales wrt C: P=L(C,P,s);
  public pt rotate(float a) {float dx=x, dy=y, c=cos(a), s=sin(a); x=c*dx+s*dy; y=-s*dx+c*dy; return this;};     // P.rotate(a): rotate P around origin by angle a in radians
  public pt rotate(float a, pt G) {float dx=x-G.x, dy=y-G.y, c=cos(a), s=sin(a); x=G.x+c*dx+s*dy; y=G.y-s*dx+c*dy; return this;};   // P.rotate(a,G): rotate P around G by angle a in radians
  public pt rotate(float s, float t, pt G) {float dx=x-G.x, dy=y-G.y; dx-=dy*t; dy+=dx*s; dx-=dy*t; x=G.x+dx; y=G.y+dy;  return this;};   // fast rotate s=sin(a); t=tan(a/2); 
  public pt moveWithMouse() { x += mouseX-pmouseX; y += mouseY-pmouseY;  return this;}; 
     
  // DRAW , WRITE
  public pt write() {print("("+x+","+y+")"); return this;};  // writes point coordinates in text window
  public pt v() {vertex(x,y); return this;};  // used for drawing polygons between beginShape(); and endShape();
  public pt show(float r) {ellipse(x, y, 2*r, 2*r); return this;}; // shows point as disk of radius r
  public pt show() {show(3); return this;}; // shows point as small dot
  public pt label(String s, float u, float v) {fill(black); text(s, x+u, y+v); noFill(); return this; };
  public pt label(String s, vec V) {fill(black); text(s, x+V.x, y+V.y); noFill(); return this; };
  public pt label(String s) {label(s,5,4); return this; };
  public pt tag(String s) {fill(white); show(13); fill(black); text(s, x-5, y+4); return this;} 
    
  } // end of pt class

//************************************************************************
//**** VECTORS
//************************************************************************
class vec { float x=0,y=0; 
 // CREATE
  vec () {};
  vec (float px, float py) {x = px; y = py;};
 
 // MODIFY
  public vec setTo(float px, float py) {x = px; y = py; return this;}; 
  public vec setTo(vec V) {x = V.x; y = V.y; return this;}; 
  public vec zero() {x=0; y=0; return this;}
  public vec scaleBy(float u, float v) {x*=u; y*=v; return this;};
  public vec scaleBy(float f) {x*=f; y*=f; return this;};
  public vec reverse() {x=-x; y=-y; return this;};
  public vec divideBy(float f) {x/=f; y/=f; return this;};
  public vec normalize() {float n=sqrt(sq(x)+sq(y)); if (n>0.000001f) {x/=n; y/=n;}; return this;};
  public vec add(float u, float v) {x += u; y += v; return this;};
  public vec add(vec V) {x += V.x; y += V.y; return this;};   
  public vec add(float s, vec V) {x += s*V.x; y += s*V.y; return this;};   
  public vec rotateBy(float a) {float xx=x, yy=y; x=xx*cos(a)-yy*sin(a); y=xx*sin(a)+yy*cos(a); return this;};
  public vec left() {float m=x; x=-y; y=m; return this;};
 
  // OUTPUT VEC
  public vec clone() {return(new vec(x,y));}; 

  // OUTPUT TEST MEASURE
  public float norm() {return(sqrt(sq(x)+sq(y)));}
  public boolean isNull() {return((abs(x)+abs(y)<0.000001f));}
  public float angle() {return(atan2(y,x)); }

  // DRAW, PRINT
  public void write() {println("<"+x+","+y+">");};
  public void showAt (pt P) {line(P.x,P.y,P.x+x,P.y+y); }; 
  public void showArrowAt (pt P) {line(P.x,P.y,P.x+x,P.y+y); 
      float n=min(this.norm()/10.f,height/50.f); 
      pt Q=P(P,this); 
      vec U = S(-n,U(this));
      vec W = S(.3f,R(U)); 
      beginShape(); Q.add(U).add(W).v(); Q.v(); Q.add(U).add(M(W)).v(); endShape(CLOSE); }; 
  public void label(String s, pt P) {P(P).add(0.5f,this).add(3,R(U(this))).label(s); };
  } // end vec class

//************************************************************************
//**** POINTS
//************************************************************************
// create 
public pt P() {return P(0,0); };                                                                            // make point (0,0)
public pt P(float x, float y) {return new pt(x,y); };                                                       // make point (x,y)
public pt P(pt P) {return P(P.x,P.y); };                                                                    // make copy of point A
public pt Mouse() {return P(mouseX,mouseY);};                                                                 // returns point at current mouse location
public pt Pmouse() {return P(pmouseX,pmouseY);};                                                              // returns point at previous mouse location
public pt ScreenCenter() {return P(width/2,height/2);}                                                        //  point in center of  canvas

// display 
public void show(pt P, float r) {ellipse(P.x, P.y, 2*r, 2*r);};                                             // draws circle of center r around P
public void show(pt P) {ellipse(P.x, P.y, 6,6);};                                                           // draws small circle around point
public void edge(pt P, pt Q) {line(P.x,P.y,Q.x,Q.y); };                                                      // draws edge (P,Q)
public void arrow(pt P, pt Q) {arrow(P,V(P,Q)); }                                                            // draws arrow from P to Q
public void label(pt P, String S) {text(S, P.x-4,P.y+6.5f); }                                                 // writes string S next to P on the screen ( for example label(P[i],str(i));)
public void label(pt P, vec V, String S) {text(S, P.x-3.5f+V.x,P.y+7+V.y); }                                  // writes string S at P+V
public void v(pt P) {vertex(P.x,P.y);};                                                                      // vertex for drawing polygons between beginShape() and endShape()
public void v(pt P, float u, float v) {vertex(P.x,P.y,u,v);};// vertex for drawing polygons between beginShape() and endShape()
public void v(pt P, pt Q) {vertex(P.x,P.y,Q.x,Q.y);};
public void show(pt P, pt Q, pt R) {beginShape(); v(P); v(Q); v(R); endShape(CLOSE); };                      // draws triangle 

// transform 
public pt R(pt Q, float a) {float dx=Q.x, dy=Q.y, c=cos(a), s=sin(a); return new pt(c*dx+s*dy,-s*dx+c*dy); };  // Q rotated by angle a around the origin
public pt R(pt Q, float a, pt C) {float dx=Q.x-C.x, dy=Q.y-C.y, c=cos(a), s=sin(a); return P(C.x+c*dx-s*dy, C.y+s*dx+c*dy); };  // Q rotated by angle a around point P
public pt P(pt P, vec V) {return P(P.x + V.x, P.y + V.y); }                                                 //  P+V (P transalted by vector V)
public pt MoveByDistanceTowards(pt P, float d, pt Q) { return P(P,d,U(V(P,Q))); };                          //  P+dU(PQ) (transLAted P by *distance* s towards Q)!!!

// average 
public pt P(pt A, pt B) {return P((A.x+B.x)/2.0f,(A.y+B.y)/2.0f); };                                          // (A+B)/2 (average)
public pt P(pt A, pt B, pt C) {return P((A.x+B.x+C.x)/3.0f,(A.y+B.y+C.y)/3.0f); };                            // (A+B+C)/3 (average)
public pt P(pt A, pt B, pt C, pt D) {return P(P(A,B),P(C,D)); };                                            // (A+B+C+D)/4 (average)

// weighted average 
public pt P(float a, pt A) {return P(a*A.x,a*A.y);}                                                      // aA  
public pt P(float a, pt A, float b, pt B) {return P(a*A.x+b*B.x,a*A.y+b*B.y);}                              // aA+bB, (a+b=1) 
public pt P(float a, pt A, float b, pt B, float c, pt C) {return P(a*A.x+b*B.x+c*C.x,a*A.y+b*B.y+c*C.y);}   // aA+bB+cC 
public pt P(float a, pt A, float b, pt B, float c, pt C, float d, pt D){return P(a*A.x+b*B.x+c*C.x+d*D.x,a*A.y+b*B.y+c*C.y+d*D.y);} // aA+bB+cC+dD 

// frame maps
public pt P(pt O, float x, vec I) {return P(O.x+x*I.x,O.y+x*I.y);}                              // O+xI
public pt P(pt O, float x, vec I, float y, vec J) {return P(O.x+x*I.x+y*J.x,O.y+x*I.y+y*J.y);}   // O+xI+yJ
public float x(pt P, pt O, vec I, vec J) {return det(V(O,P),J)/det(I,J);}
public float y(pt P, pt O, vec I, vec J) {return det(V(O,P),I)/det(J,I);}

// barycentric coordinates and transformations
public float m(pt A, pt B, pt C) {return (B.x-A.x)*(C.y-A.y) - (B.y-A.y)*(C.x-A.x); }
public float a(pt P, pt A, pt B, pt C) {return m(P,B,C)/m(A,B,C); }
public float b(pt P, pt A, pt B, pt C) {return m(A,P,C)/m(A,B,C); }
public float c(pt P, pt A, pt B, pt C) {return m(A,B,P)/m(A,B,C); }

public float x(vec V, vec I, vec J) {return det(V,J)/det(I,J);}
public float y(vec V, vec I, vec J) {return det(V,I)/det(J,I);}

public float x(pt P, pt A, pt B) {return dot(V(A,B),V(A,P))/d2(A,B);}
public float y(pt P, pt A, pt B) {return det(V(A,B),V(A,P))/d2(A,B);}
     
// measure 
public boolean isSame(pt A, pt B) {return (A.x==B.x)&&(A.y==B.y) ;}                                         // A==B
public boolean isSame(pt A, pt B, float e) {return ((abs(A.x-B.x)<e)&&(abs(A.y-B.y)<e));}                   // ||A-B||<e

public float d(pt P, pt Q) {return sqrt(d2(P,Q));  };                                                       // ||AB|| ()

public float d2(pt P, pt Q) {return sq(Q.x-P.x)+sq(Q.y-P.y); };                                             // AB*AB (Distance squared)

public boolean projectsBetween(pt P, pt A, pt B) {return dot(V(A,P),V(A,B))>0 && dot(V(B,P),V(B,A))>0 ; };
public float disToLine(pt P, pt A, pt B) {return abs(det(U(A,B),V(A,P))); };
public pt projectionOnLine(pt P, pt A, pt B) {return P(A,dot(V(A,B),V(A,P))/dot(V(A,B),V(A,B)),V(A,B));}


//************************************************************************
//**** VECTORS
//************************************************************************
// create 
public vec V(vec V) {return new vec(V.x,V.y); };                                                             // make copy of vector V
public vec V(pt P) {return new vec(P.x,P.y); };                                                              // make vector from origin to P
public vec V(float x, float y) {return new vec(x,y); };                                                      // make vector (x,y)
public vec V(pt P, pt Q) {return new vec(Q.x-P.x,Q.y-P.y);};                                                 // PQ (make vector Q-P from P to Q
public vec U(vec V) {float n = n(V); if (n==0) return new vec(0,0); else return new vec(V.x/n,V.y/n);};      // V/||V|| (Unit vector : normalized version of V)
public vec U(pt P, pt Q) {return U(V(P,Q));};                                                                // PQ/||PQ| (Unit vector : from P towards Q)
public vec MouseDrag() {return new vec(mouseX-pmouseX,mouseY-pmouseY);};                                      // vector representing recent mouse displacement

// display 
public void show(pt P, vec V) {line(P.x,P.y,P.x+V.x,P.y+V.y); }                                              // show V as line-segment from P 
public void show(pt P, float s, vec V) {show(P,S(s,V));}                                                     // show sV as line-segment from P 
public void arrow(pt P, float s, vec V) {arrow(P,S(s,V));}                                                   // show sV as arrow from P 
public void arrow(pt P, vec V, String S) {arrow(P,V); P(P(P,0.70f,V),15,R(U(V))).label(S,V(-5,4));}       // show V as arrow from P and print string S on its side
public void arrow(pt P, vec V) {show(P,V);  float n=n(V); if(n<0.01f) return; float s=max(min(0.2f,20.f/n),6.f/n);       // show V as arrow from P 
     pt Q=P(P,V); vec U = S(-s,V); vec W = R(S(.3f,U)); beginShape(); v(P(P(Q,U),W)); v(Q); v(P(P(Q,U),-1,W)); endShape(CLOSE);}; 

// weighted sum 
public vec W(float s,vec V) {return V(s*V.x,s*V.y);}                                                      // sV
public vec W(vec U, vec V) {return V(U.x+V.x,U.y+V.y);}                                                   // U+V 
public vec W(vec U,float s,vec V) {return W(U,S(s,V));}                                                   // U+sV
public vec W(float u, vec U, float v, vec V) {return W(S(u,U),S(v,V));}                                   // uU+vV ( Linear combination)

// transformed 
public vec R(vec V) {return new vec(-V.y,V.x);};                                                             // V turned right 90 degrees (as seen on screen)
public vec R(vec V, float a) {float c=cos(a), s=sin(a); return(new vec(V.x*c-V.y*s,V.x*s+V.y*c)); };                                     // V rotated by a radians
public vec S(float s,vec V) {return new vec(s*V.x,s*V.y);};                                                  // sV
public vec Reflection(vec V, vec N) { return W(V,-2.f*dot(V,N),N);};                                          // reflection
public vec M(vec V) { return V(-V.x,-V.y); }                                                                  // -V


// measure 
public float dot(vec U, vec V) {return U.x*V.x+U.y*V.y; }                                                     // dot(U,V): U*V (dot product U*V)
public float det(vec U, vec V) {return dot(R(U),V); }                                                         // det | U V | = scalar cross UxV 
public float n(vec V) {return sqrt(dot(V,V));};                                                               // n(V): ||V|| (norm: length of V)
public float n2(vec V) {return sq(V.x)+sq(V.y);};                                                             // n2(V): V*V (norm squared)
public boolean parallel (vec U, vec V) {return dot(U,R(V))==0; }; 

public float angle (vec U, vec V) {return atan2(det(U,V),dot(U,V)); };                                   // angle <U,V> (between -PI and PI)
public float angle(vec V) {return(atan2(V.y,V.x)); };                                                       // angle between <1,0> and V (between -PI and PI)
public float angle(pt A, pt B, pt C) {return  angle(V(B,A),V(B,C)); }                                       // angle <BA,BC>
public float turnAngle(pt A, pt B, pt C) {return  angle(V(A,B),V(B,C)); }                                   // angle <AB,BC> (positive when right turn as seen on screen)
public int toDeg(float a) {return PApplet.parseInt(a*180/PI);}                                                           // convert radians to degrees
public float toRad(float a) {return(a*PI/180);}                                                             // convert degrees to radians 
public float positive(float a) { if(a<0) return a+TWO_PI; else return a;}                                   // adds 2PI to make angle positive

public vec mul(vec V,float f) {vec result = V(V); result.x*=f; result.y*=f; return result;};

//************************************************************************
//**** INTERPOLATION
//************************************************************************


// Interpolation of points LERP
public pt L(pt A, pt B, float t) {return P(A.x+t*(B.x-A.x),A.y+t*(B.y-A.y));}

// Interpolation of vectors 
public vec L(vec U, vec V, float s) {return new vec(U.x+s*(V.x-U.x),U.y+s*(V.y-U.y));};                      // (1-s)U+sV (Linear interpolation between vectors)
public vec S(vec U, vec V, float s) {float a = angle(U,V); vec W = R(U,s*a); float u = n(U); float v=n(V); W(pow(v/u,s),W); return W; } // steady interpolation from U to V
public vec slerp(vec U, float t, vec V) {float a = angle(U,V); float b=sin((1.f-t)*a),c=sin(t*a),d=sin(a); return W(b/d,U,c/d,V); }


/* Part of the TUIO client*/


class ReadCSV {
  String filePath;
  int length;
  String[] lines;
  float[] max = new float[9];
  float[] min = new float[9];
  float[] range = new float[9];
  ReadCSV(String path) {
    filePath = path;
    lines = loadStrings(filePath);
    length = lines.length-1;
    max = new float[9];
    min = new float[9];
  }

  public String getEntry(int line, int column) {

    String[] tokens = split(lines[line], ",");
    return tokens[column - 1];
  }

  public float[][] getTwoFields(int col1, int col2) {

    float[][] columns = new float[length][2];    
    for (int i = 0; i<length; i++) {
      String[] tokens = split(lines[i+1], ",");
      columns[i][0] = Float.parseFloat(tokens[col1-1]);
      columns[i][1] = Float.parseFloat(tokens[col2-1]);
    }

    return columns;
  }

  public DataPoint[] getPoints() {

    DataPoint[] points = new DataPoint[length];

    String[] tokens1 = split(lines[1], ",");
    for (int k = 0; k < 9; k++) {
      max[k] = 0;
      min[k] = Float.parseFloat(tokens1[k+3]);
    }

    println("Loading points..");

    for (int i = 0; i<length; i++) {

      String[] tokens = split(lines[i+1], ",");
      float[] values = new float[9];
      String name = tokens[0];
      for (int k = 0, j = 3; k < 9; k++, j++) { 
        values[k] = Float.parseFloat(tokens[j]);

        if (values[k] > max[k]) {
          max[k] = values[k];
        }
        if (values[k] < min[k]) {
          min[k] = values[k];
        }
      }



      points[i] = new DataPoint(name, values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8]);
    }

    for (int k = 0; k < 9; k++) {
      range[k] = max[k] - min[k];

//      print("max: "+ max[k]);
//      println("min: "+ min[k]);

    }

    println(length + " Points loaded");

    return points;
  }
}

// LecturesInGraphics: utilities
// Author: Jarek ROSSIGNAC, last edited on August 1, 2013

// ************************************************************************ COLORS 
int black=0xff000000, white=0xffFFFFFF, // set more colors using Menu >  Tools > Color Selector
   red=0xffFF0000, grey=0xff818181, green=0xff00FF01, blue=0xff0300FF, yellow=0xffFEFF00, cyan=0xff00FDFF, magenta=0xffFF00FB;

// ************************************************************************ GRAPHICS 
public void pen(int c, float w) {stroke(c); strokeWeight(w);}
public void showDisk(float x, float y, float r) {ellipse(x,y,r*2,r*2);}

// ************************************************************************ IMAGES & VIDEO 
int pictureCounter=0;
PImage myFace; // picture of author's face, should be: data/pic.jpg in sketch folder
public void snapPicture() {saveFrame("PICTURES/P"+nf(pictureCounter++,3)+".jpg"); }


// ************************************************************************ TEXT 
Boolean scribeText=true; // toggle for displaying of help text
public void scribe(String S, float x, float y) {fill(0); text(S,x,y); noFill();} // writes on screen at (x,y) with current fill color
public void scribeHeader(String S, int i) {fill(0); text(S,10,20+i*20); noFill();} // writes black at line i
public void scribeHeaderRight(String S) {fill(0); text(S,width-7.5f*S.length(),20); noFill();} // writes black on screen top, right-aligned
public void scribeFooter(String S, int i) {fill(0); text(S,10,height-10-i*20); noFill();} // writes black on screen at line i from bottom
public void scribeAtMouse(String S) {fill(0); text(S,mouseX,mouseY); noFill();} // writes on screen near mouse
public void scribeMouseCoordinates() {fill(black); text("("+mouseX+","+mouseY+")",mouseX+7,mouseY+25); noFill();}



// ************************************************************************ GENERIC TEXT FOR TITLE 
String subtitle = "for Jarek Rossignac's CS3451 class in the Fall 2013";
//************************ capturing frames for a movie ************************
boolean filming=false;  // when true frames are captured in FRAMES for a movie
int frameCounter=0;     // count of frames captured (used for naming the image files)
boolean change=false;   // true when the user has presed a key or moved the mouse
boolean animating=false; // must be set by application during animations to force frame capture

// ************************************************************************ IO FILES
String fileName="data/points";

public void fileSelected(File selection) {
  if (selection == null) println("Window was closed or the user hit cancel.");
  else {
    fileName = selection.getAbsolutePath();
    println("User selected " + fileName);
    }
  }



  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "tuioClient" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
