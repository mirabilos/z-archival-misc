package ewe.ui;
import ewe.data.PropertyList;
import ewe.net.HttpConnection;
import ewe.net.Socket;
import ewe.sys.Handle;
import ewe.util.Tag;
import ewe.util.Vector;

//##################################################################
public class WebBrowser extends HtmlViewer implements IWebBrowser{
//##################################################################

/**
* If you wish to change this, set it to a new value and then call <b>toControls("goTo")</b>
**/
public String goTo = "http://localhost/";

//##################################################################
protected class WebBrowserDisplay extends HtmlViewerDisplay{
//##################################################################


//##################################################################
}
//##################################################################

protected Vector history = new Vector();
protected mComboBox gotoBox;

protected static String clearHistoryText = "Clear History...";
protected static String HistoryKey = "Ewesoft\\WebBrowser\\History";

//-------------------------------------------------------------------
protected HtmlDisplay createDisplay()
//-------------------------------------------------------------------
{
	return new WebBrowserDisplay();
}

//===================================================================
public WebBrowser()
//===================================================================
{
	this(0);
}
//===================================================================
public WebBrowser(int displayOptions)
//===================================================================
{
	super(null,displayOptions);
	title = "Web Browser";
	//
	// Make sure there are tabs to add to.
	//
	if (tabs != null){
		CellPanel cp = new CellPanel();
		cp.addNext(new mLabel("URL:")).setCell(DONTSTRETCH);
		gotoBox = new mComboBox();
		gotoBox.actionOnChoiceSelect = true;
		gotoBox.choice.shortenItems = true;
		try{
			Vector v = ewe.io.IO.getStringList(HistoryKey);
			if (v != null) gotoBox.choice.items.addAll(v);
		}catch(Exception e){}
		mInput mi = gotoBox.input;
		mi.wantReturn = true;
		cp.addNext(addField(gotoBox,"goTo"));
		cp.addNext(addToolButton("go",ewe.fx.ImageCache.cache.get("ewe/rightarrowsmall.bmp",ewe.fx.Color.White),"Go to URL",false)).setCell(DONTSTRETCH);
		Card c = tabs.addCard(cp,"Web Document",null);
		c.iconize("ewe/websmall.png",null);
	}
}

//-------------------------------------------------------------------
protected void newDocumentLoaded(String url)
//-------------------------------------------------------------------
{
	super.newDocumentLoaded(url);
	goTo = url;
	toControls("goTo");
	addToHistory(url);
}

//-------------------------------------------------------------------
protected void addPropertiesTo(final ewe.data.PropertyList pl)
//-------------------------------------------------------------------
{
	super.addPropertiesTo(pl);
	pl.set("imageResolver",new ewe.ui.formatted.StreamImageResolver(){
		//-------------------------------------------------------------------
		protected ewe.util.Tag getStreamFor(String imageName) throws ewe.io.IOException
		//-------------------------------------------------------------------
		{
			try{
			String root = pl.getString("documentRoot","");
			String url = null;
			if (imageName.toLowerCase().startsWith("http://"))
				url = imageName;
			else if(root.toLowerCase().startsWith("http://"))
				url = toFullURL(root,imageName);
			if (url != null){
				//ewe.sys.Vm.debug("Fetching: "+url);
				HttpConnection ht = new HttpConnection(url);//root+"/"+imageName);
				while(true){
					ewe.net.Socket sock = ht.connect();
					HttpConnection r = ht.redirectTo();
					if (r != ht){
						ht = r;
						sock.close();
						continue;
					}
					Tag t = new Tag();
					t.value = ht.getInputStream();//sock;
					t.tag = ht.documentProperties.getInt("content-length",-1);
					return t;
				}
			}else{
				ewe.io.File f = ewe.io.File.getNewFile(root).getChild(imageName);
				if (f.canRead()) {
					Tag t = new Tag();
					t.value = f.toReadableStream();
					t.tag = (int)f.length();
					return t;
				}
			}
				throw new ewe.io.IOException();
			}catch(ewe.io.IOException e){
				//e.printStackTrace();
				throw e;
			}
		}
	});
}

//-------------------------------------------------------------------
protected String toFullURL(String root,String document)
//-------------------------------------------------------------------
{
	String ret = super.toFullURL(root,document);
	if (ret != null) return ret;
	if (document.startsWith("/")){
		HttpConnection hr = new HttpConnection(root);
		hr.document = document;
		return hr.toURLString();
	}
	try{
		HttpConnection hr = new HttpConnection(document.indexOf(':') == -1 ? root+"/"+document : document);
		return hr.toURLString();
	}catch(Exception e){
		return null;
	}
}

//===================================================================
public ewe.sys.Handle openWebDocument(final String url,final PropertyList pl)
//===================================================================
{
	try{
		return new ewe.sys.TaskObject(){
			protected void doRun(){
				try{
					String curUrl = url;
					HttpConnection hr = new HttpConnection(curUrl);
					while(true){
						String d = hr.document;
						int where = d.lastIndexOf('/');
						if (where != -1) d = d.substring(0,where);
						pl.set("documentRoot","http://"+hr.host+":"+hr.port+d);
						pl.set("document",curUrl);
						Handle h = hr.connectAsync();
						if (!waitOnSuccess(h,true)) return;
						Socket s = (Socket)h.returnValue;
						HttpConnection redir = hr.redirectTo();
						if (redir != hr){
							s.close();
							hr = redir;
							curUrl = redir.toURLString();
							continue;
						}
						h = hr.readInData(s);
						//h = ewe.io.StreamUtils.readAllBytes(hr.getInputStream(),null,hr.contentLength,0);
						if (!waitOnSuccess(h,true)) return;
						handle.returnValue = h.returnValue;
						handle.set(Handle.Succeeded);
						return;
					}
				}catch(Exception e){
					handle.failed(e);
				}
			}
		}.startTask();
	}catch(Exception e){
		return new Handle(Handle.Failed,null);
	}
}

//-------------------------------------------------------------------
protected Handle openDocument(String url,PropertyList pl)
//-------------------------------------------------------------------
{
	Handle h = super.openDocument(url,pl);
	if ((h.check() & h.Failure) == 0) return h;
	return openWebDocument(url,pl);
}

/**
* The maximum number of URLs to save in the history. By default it is 10.
**/
public static int MaxHistory = 10;

//===================================================================
public void addToHistory(String url)
//===================================================================
{
	Vector v = gotoBox.choice.items;
	if (MaxHistory != 0){
		for (int i = 0; i<v.size(); i++)
			if (v.get(i).toString().equalsIgnoreCase(url))
				return;
		if (v.size() == 0) v.add(clearHistoryText);
		v.add(1,url);
		while(v.size() > MaxHistory+1){
			String was = v.get(v.size()-1).toString();
			removeCached(was);
			v.del(v.size()-1);
		}
	}
	try{
		ewe.io.IO.saveStringList(v,HistoryKey,0);
	}catch(Exception e){}
}

//===================================================================
public void clearHistory()
//===================================================================
{
	Vector v = gotoBox.choice.items;
	v.clear();
	try{
		ewe.io.IO.saveStringList(v,"Ewesoft\\WebBrowser\\History",0);
	}catch(Exception e){}
}
//===================================================================
public void action(String fieldName, Editor ed)
//===================================================================
{
	if (fieldName.equals("goTo") || fieldName.equals("go")){
		if (goTo.equals(clearHistoryText)){
			if (new MessageBox("Clear History","Clear your history?",MBYESNO).execute() == IDYES)
				clearHistory();
			goTo = getCurrentURL();
			if (goTo == null) goTo = "";
			ed.toControls("goTo");
			return;
		}
		doOpen(goTo);
	}else super.action(fieldName,ed);
}
//===================================================================
public static HtmlViewer openAndDisplay(String url,boolean execute)
//===================================================================
{
	HtmlViewer np = new WebBrowser();
	if (url != null) np.doOpen(url);
	if (execute) np.exec();
	else np.show();
	return np;
}
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	String toOpen = null;
	if (args.length != 0) toOpen = args[0];
	HtmlViewer np = openAndDisplay(toOpen,false);
	np.exitSystemOnClose = true;
}
//===================================================================
public boolean showFor(String url, boolean execModal)
//===================================================================
{
	if (url != null) doOpen(url);
	if (execModal) exec();
	else show();
	return true;
}
//===================================================================
public static HtmlViewer openAndDisplay(String url,int displayOptions,boolean execute)
//===================================================================
{
	WebBrowser np = new WebBrowser(displayOptions);
	np.showFor(url,execute);
	return np;
}

//##################################################################
}
//##################################################################

