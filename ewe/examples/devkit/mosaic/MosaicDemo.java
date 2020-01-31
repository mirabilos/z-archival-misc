/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.mosaic;
import ewe.ui.*;
import ewe.graphics.*;
import ewe.fx.*;
import ewe.util.*;

//##################################################################
public class MosaicDemo extends Form{
//##################################################################

//===================================================================
public MosaicDemo()
//===================================================================
{
	title = "Interactive Panel Demo";
	addLast(new TestInteractivePanel(null));
	setPreferredSize(200,300);
}
//##################################################################
}
//##################################################################


//##################################################################
class TestInteractivePanel extends InteractivePanel{
//##################################################################

mImage open = new mImage("ewe/OpenFolder.bmp",Color.White);
mImage closed = new mImage("ewe/ClosedFolder.bmp",Color.White);
mImage page = new mImage("ewe/Page.bmp","ewe/PageMask.bmp");
AniImage folder, apage;
Vector pages = new Vector();

//int hotSize = 6;
//-------------------------------------------------------------------
void addPages()
//-------------------------------------------------------------------
{
	for (int i = 0; i<5; i++){
		AniImage ai = new AniImage(page);
		ai.location.x = 50;
		ai.location.y = 50+(20*i);
		ai.properties |= ai.IsMoveable;
		//int w = ai.getWidth();
		//int h = ai.getHeight();
		//ai.setHotAreaInImage(new Rect((w-hotSize)/2,(h-hotSize)/2,hotSize,hotSize));
		addImage(ai);
		pages.add(ai);
		apage = ai;
	}
}
Canvas canvas;
int grabCursor;
//===================================================================
public TestInteractivePanel(Canvas c)
//===================================================================
{
	grabCursor = ewe.sys.Vm.createCursor(new mImage("samples/mosaic/Grab.bmp","samples/mosaic/GrabMask.bmp").toCursor(new Point(8,3)));
	canvas = c;
	borderWidth = 2; borderStyle = c.EDGE_SUNKEN;
//......................................................
// Add the folder image.
//......................................................
	folder = new AniImage(closed);
	folder.properties |= folder.IsMoveable;
	//int w = folder.getWidth();
	//int h = folder.getHeight();
	//folder.setHotAreaInImage(new Rect((w-hotSize)/2,(h-hotSize)/2,hotSize,hotSize));
	folder.move(25,25);
	addImage(folder);
//......................................................
// We want to detect when an image is dragged over the "folder" image,
// therefore we have to add it to the "touching" ImageList.
//......................................................
	touching = new ImageList();
	touching.add(folder);
//......................................................
// Add the page images.
//......................................................
	addPages();
}
/**
* This will change the folder image when a page image is dragged over it.
**/
//===================================================================
public void draggedOver(ImageDragContext dc)
//===================================================================
{
	if (dc.draggingOver == folder){
		if (canvas != null){
			Graphics c = canvas.getGraphics();
			Rect cr = canvas.getRect(null);
			cr.x = cr.y = canvas.borderWidth;
			c.drawImage(apage.image,null/*apage.mask*/,apage.transparentColor,
			new Rect().set(2,2,folder.getWidth()-4,folder.getHeight()-4),cr,0);
		}
		folder.change(open);
		folder.refresh();
	}
}
/**
* This will change the folder image when a page image is dragged off of it.
**/
//===================================================================
public void draggedOff(ImageDragContext dc)
//===================================================================
{
	if (dc.draggingOver == folder){
		folder.change(closed);
		folder.refresh();
	}
}
/**
* Indicates the pen/mouse is over an image without the pen/mouse being down (yet).
*/
//===================================================================
public boolean imageMovedOn(AniImage which)
//===================================================================
{
	setCursor(grabCursor);
	return true;
}
/**
* Indicates the pen/mouse is no longer over an image.
*/
//===================================================================
public boolean imageMovedOff(AniImage which)
//===================================================================
{
	setCursor(0);
	if (which != folder) {
		return true;
	}
	folder.change(closed);
	folder.refresh();
	return true;
}

/**
* This is called when a page is dropped on top of the folder.
**/
//===================================================================
public void droppedOn(ImageDragContext dc)
//===================================================================
{
	if (dc.draggingOver == folder){
		pages.remove(dc.image);
		removeImage(dc.image);
		folder.change(closed);
		folder.updated();
		refresh();
	}
	if (pages.size() == 0){
		new MessageBox("Done","All pages have been placed\nin the folder",0).execute();
	}
}
//===================================================================
//These are also overridable.
//==================================================================
//public void draggingStopped(ImageDragContext dc){}
//public void draggingImage(ImageDragContext dc){}
//public void imageClicked(AniImage which,Point pos){}
//===================================================================
//##################################################################
}
//##################################################################
