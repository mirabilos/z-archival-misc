package ewe.graphics;
import ewe.fx.*;
import ewe.sys.TaskObject;
import ewe.sys.Handle;
import ewe.sys.Vm;

//##################################################################
public class ImageMover extends TaskObject{
//##################################################################

/** This is the length of time it should take to move the image. **/
public int timeToMove;
/** This is the image being moved. **/
public AniImage image;
/** This is the starting point.**/
public Point start;
/** This is the ending point.**/
public Point end;

/** This is the time to pause between steps, which defaults to 10 ms. */
public int pauseTime = 10;

//===================================================================
public ImageMover(AniImage image, Point start, Point end, int timeToMove)
//===================================================================
{
	this.image = image;
	this.timeToMove = timeToMove;
	this.start = new Point().set(start);
	this.end = new Point().set(end);
}
//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	int dx = end.x-start.x, dy = end.y-start.y;
	long end = Vm.getTimeStampLong()+timeToMove;
	while(!shouldStop){
		long left = end-Vm.getTimeStampLong();
		if (left < 0) left = 0;
		double f = ((double)(timeToMove-left))/timeToMove;
		image.move(start.x+(int)(dx*f), start.y+(int)(dy*f));
		image.refresh();
		if (left == 0) break;
		sleep(pauseTime);
	}
	if (shouldStop) handle.set(Handle.Aborted);
	else handle.set(Handle.Succeeded);
}

//##################################################################
}
//##################################################################

