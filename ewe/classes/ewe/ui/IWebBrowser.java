package ewe.ui;
import ewe.sys.Handle;

//##################################################################
public interface IWebBrowser{
//##################################################################

//===================================================================
public Handle setHtml(Object htmlText,String urlToDisplay,String bookmark);
//===================================================================
public boolean showFor(String url, boolean execModal);
//===================================================================

//##################################################################
}
//##################################################################

