// $MirOS: contrib/hosted/ewe/examples/bottles/bottles.java,v 1.1.1.1 2007/08/30 20:57:27 tg Exp $

// java version of 99 bottles of beer on the wall
// 1995 Sean Russell (ser@cs.uoregon.edu)
// 2007 Thorsten Glaser (tg@mirbsd.de) for Ewe

class bottles {

public static void main(String args[])
{
	int beers;
	String s = "s";
	ewe.io.PrintWriter out = ewe.sys.Vm.out();

	ewe.sys.Vm.startEwe(args);
	for (beers = 99; beers > -1; /* nothing */) {
		out.print(beers + " bottle" + s + " of beer on the wall, ");
		out.println(beers + " bottle" + s + " of beer, ");
		if (beers == 0) {
			out.print("Go to the store, buy some more, ");
			out.println("99 bottles of beer on the wall.");
			ewe.sys.Vm.exit(0);
		} else
			out.print("Take one down, pass it around, ");
		s = (--beers == 1) ? "" : "s";
		out.println(beers + " bottle" + s + " of beer on the wall.\n");
	}
	ewe.sys.Vm.exit(0);
}

}
