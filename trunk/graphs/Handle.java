package graphs;

/**
 * Class to create an interactive box to modify force-directed layout forces
 * @deprecated
 * @author Roberto Therón
 *
 */
public class Handle
{
  int x, y;
  int boxx, boxy;
  int length;
  int size;
  boolean over = false;
  boolean press = false;
  boolean locked = false;
  boolean otherslocked = false;
  Handle[] others;
  String label;
  double minValue;
  double maxValue;
  double level;
  double currentValue = minValue; 
  private BiclusVis applet = null;
  
  public Handle(int ix, int iy, int il, int is, Handle[] o, BiclusVis bv)
  {
    x = ix;
    y = iy;
    length = il;
    size = is;
    boxx = x+length - size/2;
    boxy = y - size/2;
    others = o;
    label ="";
    applet=bv;
  }
  
  public void update() 
  {
    boxx = x+length;
    boxy = y - size/2;
    
    
    for(int i=0; i<others.length; i++) {
      if(others[i].locked == true) {
        otherslocked = true;
        break;
      } else {
        otherslocked = false;
      }  
    }
    
    if(otherslocked == false) {
      over();
      press();
    }
    
    if(press) {
      length = applet.lock(applet.mouseX-x-size/2, 0, applet.getHandleLength());
      level = (maxValue - minValue) / applet.getHandleLength();
      currentValue = ((boxx-x) * level) + minValue;
    }
  }
  
  public void over()
  {
    if(applet.overRect(boxx, boxy, size, size)) {
      over = true;
    } else {
      over = false;
    }
  }
  
  public void press()
  {
    if(over && applet.mousePressed || locked) {
      press = true;
      locked = true;
    } else {
      press = false;
    }
  }
  
  public void release()
  {
    locked = false;
  }
  
  public void display() 
  {
	applet.textAlign(BiclusVis.LEFT);
	  
	//valores min y max
	applet.textSize(8);
	applet.fill(200,100,0,255);
	applet.text(""+minValue,x-size/2,y+size+2);
	applet.text(""+maxValue,x+applet.getHandleLength()+size,y+size+2);
    
	applet.textSize(11);  
	applet.fill(0);
	applet.stroke(0);
	applet.text(">",x-size/2,y+size/2);
	applet.line(x, y, x+length, y);
    //stroke(220);
    //line(x+length+1, y, x+handleLength, y);
	applet.text("<",x+applet.getHandleLength()+size,y+size/2);
	applet.stroke(0);
	applet.fill(255);
	applet.rect(boxx, boxy, size, size);
    // valor actual en porcentaje de intervalo min y max
	applet.textSize(8);
	applet.fill(200,100,0,255);
	applet.text(""+(int)((currentValue - minValue)/(maxValue - minValue) * 99),boxx+1, boxy +(3*size/4));
	
	applet.textSize(11);
	applet.fill(0);
	applet.text(this.label, x+applet.getHandleLength()+size+10, boxy+size);
    
    if(over || press) {
      //line(boxx, boxy, boxx+size, boxy+size);
      //line(boxx, boxy+size, boxx+size, boxy);
    	applet.textSize(8);
    	applet.fill(200,0,0,255);
    	applet.text(""+(int)((currentValue - minValue)/(maxValue - minValue) * 99),boxx+1, boxy +(3*size/4));
    	applet.textSize(11);
    	applet.fill(255);
    	applet.text(this.label, x+applet.getHandleLength() +size+ 10, boxy+size);
  	  
    }
    applet.textAlign(BiclusVis.CENTER);

  }
  
  public void setLabel(String s){
	  label = s;
  }
  
  public void setMinValue(double min){
	  minValue = min;
  }
  
  public void setMaxValue(double max){
	  maxValue = max;
  }
  
  public void setCurrentValue(double current){
	  currentValue = current;
  }
  
  public void moveHandle(double value, boolean draw)
  	{
	  if (value > maxValue)
		  maxValue = value;
	  
	  if (value < minValue)
		  minValue = value;
	  
	  level = (maxValue - minValue) / applet.getHandleLength();
      currentValue = value;
	  length = (int)((currentValue - minValue)/level);
	  if(draw)	applet.drawHandles();
  }
  
  public void moveHandle(double value)
  	{
	  if (value > maxValue)
		  maxValue = value;
	  
	  if (value < minValue)
		  minValue = value;
	  
	  level = (maxValue - minValue) / applet.getHandleLength();
    currentValue = value;
	  length = (int)((currentValue - minValue)/level);
	 // if(draw)	applet.drawHandles();
  	}
  
}

