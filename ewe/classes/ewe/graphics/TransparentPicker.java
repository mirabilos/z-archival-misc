package ewe.graphics;
import ewe.ui.*;
import ewe.fx.*;
//##################################################################
public class TransparentPicker extends Canvas{
//##################################################################
{
	backGround = Color.Sand;
	isFullScrollClient = false;
	//objectClass = ewe.reflect.Reflect.getForName("ewe.fx.mImage");
}
mImage image;
Color transparent;
PixelBuffer pb;
boolean hadMask;
//===================================================================
public void setData(Object data)
//===================================================================
{
	if (data instanceof mImage){
		image = (mImage)data;
		transparent = image.transparentColor;
		hadMask = image.transparentColor == null && (image.mask != null || image.usesAlpha());
	}else if (data == null){
		image = null;
		transparent = null;
		hadMask = false;
	}
	if (pb != null) pb.free();
	pb = null;
	repaintNow();
}
//===================================================================
public void getData(Object data)
//===================================================================
{
	if (data instanceof mImage){
		((mImage)data).transparentColor = transparent;
		((mImage)data).mask = null;
	}
}
//===================================================================
public void doPaint(Graphics g,Rect area)
//===================================================================
{
	doBackground(g);
	if (image != null){
		if (pb == null){
			double xscale = (double)width/image.getWidth();
			double yscale = (double)height/image.getHeight();
			double s = xscale < yscale ? xscale : yscale;
			int w = (int)(image.getWidth()*s), h = (int)(image.getHeight()*s);
			pb = new PixelBuffer(image).scale(w,h);
		}
		pb.draw(g,(width-pb.getWidth())/2,(height-pb.getHeight())/2,0);
	}
}
//===================================================================
public void penClicked(Point where)
//===================================================================
{
	if (pb == null) return;
	int x = where.x-(width-pb.getWidth())/2;
	int y = where.y-(height-pb.getHeight())/2;
	if (x < 0 || x >= pb.getWidth() || y < 0 || y >= pb.getHeight()) return;
	if (hadMask){
		new MessageBox("Warning",
		"This image already has transparent areas defined.\n"+
		"Click the color again if you are sure you want \n"+
		"to redefine the transparent area.",MessageBox.MBOK).execute();
		hadMask = false;
		return;
	}
	int color = pb.getBuffer()[y*pb.getWidth()+x];
	transparent = new Color(color >> 16 & 0xff, color >> 8 & 0xff, color & 0xff);
	pb.free();
	pb = null;
	notifyDataChange();
	repaintNow();
}
//==================================================================
public void resizeTo(int width,int height)
//==================================================================
{
	if (pb != null) pb.free();
	pb = null;
	super.resizeTo(width,height);
}
/*
public static void main(String args[])
{
	ewe.sys.Vm.startEwe(args);
	Form f = new Form();
	TransparentPicker tp = new TransparentPicker();
	f.addLast(tp).setPreferredSize(100,100);
	tp.setData(new mImage("ewe/editsmall.bmp",Color.White));
	f.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

