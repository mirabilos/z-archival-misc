package samples.database.example2;
import ewe.database.*;

//##################################################################
class DataRandomizer{
//##################################################################

static String allLastNames[] =
{
"Brereton","Mahabir","Charles","Wallace","Monsegue","Raymond","Granger",
"Rousea","Rampersad","Joseph","Khan","Hay","DeLima","Farfan","Che Ting",
"Bush","Clinton","Reagan","Gorbachev","Ali","Ghandi","Ramnarine","Chirac",
"Beck","Thomas","Daniels","Goldberg","Willis","Foster","Roberts","Jolie",
};

public static String allFirstNames[] =
{
"Michael","Asha","Peter","Jean-Anne","Valerie","Norma","Pat","Ken",
"Jill","Margaret","Abraham","Karen","Krystal","Jennifer","George","Rita",
"James","Louis","Lia","Raymon","Antonio",
"Vincent","Damian","Anne","Mary","Sylvia",
"Scott","Deborah","Samuel","Che","Andre",
"Jeff","Angelina","Jeff","Whoopie","Bruce","Jodie","Julia"
};

//-------------------------------------------------------------------
static int rand()
//-------------------------------------------------------------------
{
	return (int)(java.lang.Math.random()*0xffff);
}
//-------------------------------------------------------------------
static String getRandomString(String[] strings)
//-------------------------------------------------------------------
{
	return strings[rand()%strings.length];
}

//===================================================================
public static TestData getRandomData(int numberOfFirstNames)
//===================================================================
{
	if (numberOfFirstNames < 1) numberOfFirstNames = 1;
	TestData td = new TestData();
	td.lastName = getRandomString(allLastNames);
	td.firstNames = "";
	for (int i = 0; i<numberOfFirstNames; i++){
		if (i != 0) td.firstNames += " ";
		td.firstNames += getRandomString(allFirstNames);
	}
	td.gender = ((rand() % 2) == 0) ? "M" : "F";
	td.salary.setDouble((rand() % 100)*1000);
	td.retirementAge = (rand() % 10)+60;
	return td;
}
//##################################################################
}
//##################################################################
