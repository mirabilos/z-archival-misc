package ewe.util;
/**
* This is an ObjectFinder that will use a comparer to check if the lookingFor()
* method should return true. If the comparer's compare(searchData, lookingForData) method returns 0, then
* lookingFor() will return true.
**/
//##################################################################
public class ComparerObjectFinder implements ObjectFinder {
//##################################################################

Object searchData;
Comparer comparer;

//===================================================================
public ComparerObjectFinder(Comparer comparer,Object searchData)
//===================================================================
{
	this.comparer = comparer;
	this.searchData = searchData;
}
/**
* This will return true if the Comparer's compare() method returns 0.
**/
//===================================================================
public boolean lookingFor(Object data)
//===================================================================
{
	return comparer.compare(searchData,data) == 0;
}
//##################################################################
}
//##################################################################

