package samples.natives.newthread;
import ewe.sys.Handle;
import ewe.ui.*;

//##################################################################
public class NativeThread implements Runnable{
//##################################################################
public double aValue = 123.4;
public static double theStatic;
static
{
	try{
		ewe.sys.Vm.loadDynamicLibrary("NativeThread");
	}catch(Exception e){}
}
//===================================================================
public static int average(int one,int two)
//===================================================================
{
	ewe.sys.Vm.debug("Asked: "+one+", "+two);
	return (one+two)/2;
}
//===================================================================
public native Handle backgroundTask();
//===================================================================
public void testBackground()
//===================================================================
{
	for (int i = 0; i<3; i++){
        ProgressBarForm pbf = new ProgressBarForm();
        pbf.showStop = true;
				Handle h = backgroundTask();
				ewe.sys.Vm.debug("Static: "+theStatic);
        pbf.setTask(h,"Background Task");
				pbf.show();
				try{
					h.waitUntilStopped();
					ewe.sys.Vm.debug("Stopped");
				}catch(Exception e){
					e.printStackTrace();
				}
	}
				/*
				try{
					ewe.sys.Vm.debug("Going to wait...");
					h.waitUntilStopped();
					ewe.sys.Vm.debug("Stopped");
				}catch(Exception e){
					e.printStackTrace();
				}
				*/
				//new ewe.ui.MessageBox("All!","Complete!",ewe.ui.Form.MBOK).execute();
}

//===================================================================
public void run()
//===================================================================
{
	testBackground();
	/*
	ewe.ui.Form f = new ewe.filechooser.FileChooser();
	f.show();
	*/
	TestNativeWindow tw = new TestNativeWindow();
	tw.show();
	tw.waitUntilClosed();
}

//##################################################################
}
//##################################################################
//##################################################################
class TestNativeWindow extends Editor{
//##################################################################
public Control drawOn;
public Control image;
ewe.fx.Image img, other;
//===================================================================
TestNativeWindow()
//===================================================================
{
	title = "Test Native Window Access";
	Panel one = new CellPanel();
	Panel p = new Panel();
	p.setText("Window panel");
	p.addLast(drawOn = new Panel().setPreferredSize(200,200));
	drawOn.backGround = ewe.fx.Color.White;
	one.addLast(p);
	one.addNext(addField(new mButton("Draw"),"draw")).setCell(HSTRETCH);
	mTabbedPanel mt = new mTabbedPanel();
	mt.addItem(one,"Native Window Access",null);
	addLast(mt);
	one = new CellPanel();
	img = new ewe.fx.PixelBuffer(new ewe.fx.Image("Animal-True.png")).toDrawableImage();
	other = new ewe.fx.Image("Family.jpg");
	final ewe.fx.mImage mi = new ewe.fx.mImage(img,ewe.fx.Graphics.DRAW_ALPHA,true);
	one.addLast(image = new Canvas(){
		public void doPaint(ewe.fx.Graphics g,ewe.fx.Rect area){
			super.doPaint(g,area);
			if (g != null) mi.draw(g,0,0,0);
		}
	}).setPreferredSize(200,200);
	one.addLast(addField(new mButton("Alter"),"alter")).setCell(HSTRETCH);
	mt.addItem(one,"Native Image Access",null);
}
int imageAccess = 0, otherAccess = 0;

public void action(String name,Editor ed)
{
	if (name.equals("draw")){
		Window w = getWindow();
		Object nv = w.getInfo(w.INFO_NATIVE_WINDOW,null,null,w.NATIVE_WINDOW_GET_DRAWING_SURFACE);
		ewe.fx.Point p = (ewe.fx.Point)w.getInfo(w.INFO_POSITION_IN_NATIVE_DRAWING_SURFACE,drawOn,null,0);
		if (nv instanceof ewe.sys.Long){
			ewe.fx.Dimension d = drawOn.getSize(null);
			int[] pars = new int[6];
			pars[0] = (int)((ewe.sys.Long)nv).value;
			pars[1] = p.x;
			pars[2] = p.y;
			pars[3] = d.width;
			pars[4] = d.height;
			pars[5] = imageAccess;
			doSingleDraw(pars);
		}
	}else if (name.equals("alter")){
		if (imageAccess == 0){
			Object got = img.getNativeResource();
			if (got instanceof ewe.sys.Long){
				imageAccess = (int)((ewe.sys.Long)got).value;
			}
			got = other.getNativeResource();
			if (got instanceof ewe.sys.Long){
				otherAccess = (int)((ewe.sys.Long)got).value;
			}
		}
		if (imageAccess != 0) {
			doModifyImage(imageAccess,otherAccess);
			image.repaintNow();
		}
	}
}
private native void doSingleDraw(int[] pars);
private native void doModifyImage(int imageAccess,int otherAccess);
//##################################################################
}
//##################################################################
