/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.fx.print;
import ewe.sys.Convert;
import ewe.util.Copyable;
import ewe.util.Range;
import ewe.util.RangeList;
import ewe.util.mString;
/**
This class holds options for the pages that are to be printed - but do not relate to the layout
of the pages.
**/
//##################################################################
public class PrintOptions implements Copyable{
//##################################################################
/**
This indicates that printing should be from the highest page to the lowest.
**/
public boolean printBackwards;
/**
This defaults to ODD_AND_EVEN_PAGES, but can also be set to ODD_PAGES_ONLY and EVEN_PAGES_ONLY.
**/
public int whichPages = ODD_AND_EVEN_PAGES;
/**
If this is null this indicates that all pages should be printed. To set this
to non-null call addRange().
**/
private RangeList requestedRanges;
private RangeList printRanges;


//===================================================================
public Object getCopy()
//===================================================================
{
	PrintOptions po = new PrintOptions();
	po.printBackwards = printBackwards;
	po.whichPages = whichPages;
	po.requestedRanges = (requestedRanges == null) ? null : (RangeList)requestedRanges.getFullCopy();
	return po;
}
/** The default value for whichPages **/
public static final int ODD_AND_EVEN_PAGES = 0;
/** A possible value for whichPages **/
public static final int ODD_PAGES_ONLY = 1;
/** A possible value for whichPages **/
public static final int EVEN_PAGES_ONLY = 2;
/**
This can be a lastPage value for addRange().
**/
public static final int TO_END_OF_DOCUMENT = Integer.MAX_VALUE;
/**
 * Convert the ranges of this PrintOptions to a String.
 * @param showAll if this is true and all the pages are selected
 * (i.e. no explicit range has been set) then this will return "All"
 * otherwise it will return a blank String.
 * @return the ranges of this PrintOptions converted to a String.
 */
public String rangesToString(boolean showAll)
{
	StringBuffer sb = new StringBuffer();
	if (requestedRanges == null || requestedRanges.countRanges() == 0) return showAll ? "All" : "";
	int num = requestedRanges.countRanges();
	Range r = new Range(0,0);
	for (int i = 0; i<num; i++){
		requestedRanges.rangeAt(i,r);
		if (i != 0) sb.append(',');
		sb.append(r.first);
		if (r.last != r.first){
			sb.append('-');
			if (r.last != TO_END_OF_DOCUMENT)
				sb.append(r.last);
		}
	}
	return sb.toString();
}
public void clearRanges()
{
	requestedRanges = null;
}
public void rangesFromString(String ranges)
{
	ranges = ranges.trim().toUpperCase();
	if (ranges.equals("ALL")) clearRanges();
	else{
		String[] all = mString.split(ranges,',');
		for (int i = 0; i<all.length; i++){
			String got = all[i].trim();
			String left = null, right = null;
			if (got.indexOf('-') != -1){
				left = mString.leftOf(got,'-').trim();
				right = mString.rightOf(got,'-').trim();
			}else left = got;
			int first = Convert.toInt(left);
			if (first <= 0) continue;
			int last = Convert.toInt(right);
			if (right == null) last = first;
			else if (right.length() == 0) last = TO_END_OF_DOCUMENT;
			else if (last <= 0) continue;
			addRange(first,last);
		}
	}
}
/**
Add a range of pages to print. The firstPage and lastPage values are inclusive and should start from 1.
Use TO_END_OF_DOCUMENT as a lastPage value to specifiy printing to the end of the document.
**/
//===================================================================
public void addRange(int firstPage, int lastPage)
//===================================================================
{
	if (requestedRanges == null) requestedRanges = new RangeList();
	requestedRanges.addRange(firstPage, lastPage);
}
/**
Return the RangeList of pages to print. If this returns null it indicates
that no ranges were set - indicating to print all pages.
**/
//===================================================================
public RangeList getRanges()
//===================================================================
{
	return requestedRanges;
}

/**
A possible return value from getFirstPage() or getNextPage().
**/
public static final int NO_MORE_PAGES = 0;

private int curRange, curPage;
private int numPages;
private boolean finished = false;

//-------------------------------------------------------------------
private boolean pageIsAcceptable(int pageNumber)
//-------------------------------------------------------------------
{
	if (whichPages == ODD_AND_EVEN_PAGES) return true;
	else if (whichPages == ODD_PAGES_ONLY) return (pageNumber%2 == 1);
	else return (pageNumber%2 == 0);
}
/**
Return the first page that should be printed.
* @param reportedNumberOfPages the number of pages the Printable object reported for the document.
* @return the first page to be printed (where 1 is the lowest page) or NO_MORE_PAGES if there are none to print.
* @exception IllegalStateException if printBackwards is true but the reportedNumberOfPages is unknown.
*/
//===================================================================
public int getFirstPage(int reportedNumberOfPages) throws IllegalStateException
//===================================================================
{
	finished = false;
	boolean unknown = reportedNumberOfPages == Printable.UNKNOWN_NUMBER_OF_PAGES;
	if (printBackwards && unknown)
		throw new IllegalStateException();
	//
	numPages = reportedNumberOfPages;
	if (requestedRanges != null) printRanges = (RangeList)requestedRanges.getFullCopy();
	else{
		printRanges = new RangeList();
		printRanges.addRange(0,unknown ? TO_END_OF_DOCUMENT : reportedNumberOfPages);
	}
	//
	if (printBackwards){
		curRange = printRanges.countRanges()-1;
		curPage = reportedNumberOfPages+1;
	}else{
		curRange = 0;
		curPage = 0;
	}
	return getNextPage();
}
/**
Return the next page that should be printed.
* @return the next page to be printed (where 1 is the lowest page) or NO_MORE_PAGES if there are none to print.
*/
//===================================================================
public int getNextPage()
//===================================================================
{
	while(true){
		if (finished) return NO_MORE_PAGES;
		Range r = (Range)printRanges.rangeAt(curRange,null);
		if (printBackwards){
			curPage--;
			if (curPage > numPages) curPage = numPages;
			if (curPage > r.last) curPage = r.last;
			if (curPage < r.first){
				if (curRange == 0){
					finished = true;
					continue;
				}else{
					curRange--;
					curPage++;
					continue;
				}
			}else{
				if (pageIsAcceptable(curPage)) return curPage;
				else continue;
			}
		}else{
			if (curPage < r.first) curPage = r.first;
			else curPage++;
			if (curPage > r.last){
				if (curRange >= printRanges.countRanges()-1) {
					finished = true;
					continue;
				}else{
					curRange++;
					curPage = 0;
					continue;
				}
			}else{
				if (pageIsAcceptable(curPage)) return curPage;
				else continue;
			}
		}
	}
}
/**
Count the number of pages that will print if reportedNumberOfPages is not UNKNOWN_NUMBER_OF_PAGES.
**/
//===================================================================
public int countPagesWillPrint(int reportedNumberOfPages)
//===================================================================
{
	boolean unknown = reportedNumberOfPages == Printable.UNKNOWN_NUMBER_OF_PAGES;
	if (unknown) return reportedNumberOfPages;
	boolean wasNull = requestedRanges == null || requestedRanges.size() == 0;
	if (wasNull) addRange(1,reportedNumberOfPages);
	int total = 0;
	for (int i = 0; i<requestedRanges.countRanges(); i++){
		Range r = (Range)requestedRanges.rangeAt(i,null);
		if (r.last == TO_END_OF_DOCUMENT) r.last = reportedNumberOfPages;
		int t = 1+r.last-r.first;
		if (whichPages == ODD_AND_EVEN_PAGES) total += t;
		else{
			total += t/2;
			if (t%2 == 0) continue;
			boolean lastIsOdd = (r.last%2 == 1);
			if (whichPages == ODD_PAGES_ONLY){
				if (lastIsOdd) total++;
			}else{
				if (!lastIsOdd) total++;
			}
		}
	}
	if (wasNull) requestedRanges = null;
	return total;
}
/**
 * Returns the list of page numbers, starting from 1, that will print using this
 * PrintOptions - given the total number of pages.
 * @param reportedNumberOfPages the number of pages in total. This should not be UNKNOWN_NUMBER_OF_PAGES.
 * @return an int[] of all the page numbers that will print.
 */
public int[] getPrintedPages(int reportedNumberOfPages)
{
	if (reportedNumberOfPages == Printable.UNKNOWN_NUMBER_OF_PAGES) return null;
	if (reportedNumberOfPages <= 0) return new int[0];
	int total = countPagesWillPrint(reportedNumberOfPages);
	int[] dest = new int[total];
	for (int i = 0; i<dest.length; i++)
		dest[i] = (i == 0) ? getFirstPage(reportedNumberOfPages) : getNextPage();
	return dest;
}
//##################################################################
}
//##################################################################

