package ewe.fx;

/**
This class provides a set of static toolkit methods that work on Font and/or FontMetrics
**/

//##################################################################
public class FontTools{
//##################################################################

/** A Metric value for getFontForMetric(). **/
public final static int METRIC_HEIGHT = 1;
/** A Metric value for getFontForMetric(). **/
public final static int METRIC_ASCENT = 2;
/** A Metric value for getFontForMetric(). **/
public final static int METRIC_DESCENT = 3;
/** A Metric value for getFontForMetric(). **/
public final static int METRIC_WIDTH_OF_TEXT = 4;
/** A Metric value for getFontForMetric(). **/
public final static int METRIC_HEIGHT_OF_TEXT = 5;

private static Dimension dim;
private static String[] strings;
/**
 * Get the value of a particular Metric for a FontMetrics for a set of data.
 * @param whichMetric one of the METRIC_XXX values
 * @param data This is only needed for METRIC_WIDTH_OF_TEXT or METRIC_HEIGHT_OF_TEXT.
 * The data must be a String, an array of Strings (which is assumed will be placed one above the other)
 * or a Character (for a single character).
 * @param fm The FontMetrics to use.
 * @return the value of the specified Metric.
 */
//===================================================================
public static int getMetricValue(int whichMetric, Object data, FontMetrics fm)
//===================================================================
{
	switch(whichMetric){
		case METRIC_HEIGHT: return fm.getHeight();
		case METRIC_ASCENT: return fm.getAscent();
		case METRIC_DESCENT: return fm.getDescent();
		case METRIC_WIDTH_OF_TEXT:
		case METRIC_HEIGHT_OF_TEXT:
			{
				if (dim == null){
					dim = new Dimension();
					strings = new String[1];
				}
				String[] check = null;
				if (data instanceof String[]) check = (String[])data;
				else {
					check = strings;
					check[0] = data == null ? "" : data.toString();
				}
				Graphics.getSize(fm,check,0,check.length,dim);
				return whichMetric == METRIC_WIDTH_OF_TEXT ? dim.width : dim.height;
			}
		default:
			throw new IllegalArgumentException();
	}
}
/**
 * Retrieve the Font which is of the correct size, such that its metric is equal to,
 * or just smaller than the specified metric in pixels, or for which the metrics of
 * the supplied data is equal to or just smaller than the specified metric in pixels.
 * @param valueOfMetricInPixels The value of the metrics in Pixels.
 * @param whichMetric one of the METRIC_XXX values
 * @param data This is only needed for METRIC_WIDTH_OF_TEXT or METRIC_HEIGHT_OF_TEXT.
 * The data must be a String, an array of Strings (which is assumed will be placed one above the other)
 * or a Character (for a single character).
 * @param baseFont The FontMetrics representing the base font to use. The returned Font will have the
 * same name and style as the baseFont.
 * @return the Font of the correct size so that it fits within the specified metric.
 */
//===================================================================
public static Font getFontForMetric(int valueOfMetricInPixels,int whichMetric,Object data,FontMetrics baseFont)
//===================================================================
{
	int num = valueOfMetricInPixels;
	int start = 20;
	Font f = baseFont.getFont().changeNameAndSize(null,start);
	FontMetrics fm = baseFont.getNewFor(f);
	int sz = getMetricValue(whichMetric, data, fm);
	start = (start*num)/sz;
	f = f.changeNameAndSize(null,start);
	fm = fm.getNewFor(f);
	sz = getMetricValue(whichMetric, data, fm);
	if (sz == num) return f;
	else if (sz > num){
		for (int fs = start-1; fs > 1; fs--){
			f = f.changeNameAndSize(null,fs);
			fm = fm.getNewFor(f);
			sz = getMetricValue(whichMetric, data, fm);
			if (sz <= num) return f;
		}
		return f;
	}else{ //sz < num
		for (int fs = start+1;; fs++){
			f = f.changeNameAndSize(null,fs);
			fm = fm.getNewFor(f);
			sz = getMetricValue(whichMetric, data, fm);
			if (sz > num) return f.changeNameAndSize(null,fs-1);
			else if (sz == num) return f;
		}
	}
}


/**
 * Find the biggest Font such that the data provided fits within the size provided.
 * @param size The size in pixels.
 * @param data This can be a Character or a String or an Array of Strings.
 * @param baseFont The FontMetrics representing the base font to use. The returned Font will have the
 * same name and style as the baseFont.
 * @return the biggest Font such that the data provided fits within the size provided.
 */
//===================================================================
public static Font fitInto(Dimension size, Object data, FontMetrics baseFont)
//===================================================================
{
	Font fw = getFontForMetric(size.width,METRIC_WIDTH_OF_TEXT,data,baseFont);
	Font fh = getFontForMetric(size.height,METRIC_HEIGHT_OF_TEXT,data,baseFont);
	return fw.getSize() < fh.getSize() ? fw : fh;
}
/**
 * Find the biggest Font such that its height is less than or equal to the requiredHeight.
 * @param requiredHeight The required height for the Font.
 * @param baseFont The FontMetrics representing the base font to use. The returned Font will have the
 * same name and style as the baseFont.
 * @return the biggest Font such that its height is less than or equal to the requiredHeight.
 */
//===================================================================
public static Font getFontForHeight(int requiredHeight, FontMetrics baseFont)
//===================================================================
{
	return getFontForMetric(requiredHeight,METRIC_HEIGHT,null,baseFont);
}
//##################################################################
}
//##################################################################

