package ewex.rapi;
import ewe.io.*;

//##################################################################
public class SystemInfo{
//##################################################################
public static final int PROCESSOR_ARCHITECTURE_INTEL =0;
public static final int PROCESSOR_ARCHITECTURE_MIPS  =1;
public static final int PROCESSOR_ARCHITECTURE_ALPHA =2;
public static final int PROCESSOR_ARCHITECTURE_PPC   =3;
public static final int PROCESSOR_ARCHITECTURE_SHx   =4;
public static final int PROCESSOR_ARCHITECTURE_ARM   =5;
public static final int PROCESSOR_ARCHITECTURE_IA64  =6;
public static final int PROCESSOR_ARCHITECTURE_ALPHA64 =7;
public static final int PROCESSOR_ARCHITECTURE_MSIL  =8;
public static final int PROCESSOR_ARCHITECTURE_UNKNOWN =0xFFFF;

public static final int PROCESSOR_INTEL_386     =386;
public static final int PROCESSOR_INTEL_486     =486;
public static final int PROCESSOR_INTEL_PENTIUM =586;
public static final int PROCESSOR_INTEL_860     =860;
public static final int PROCESSOR_MIPS_R2000    =2000;
public static final int PROCESSOR_MIPS_R3000    =3000;
public static final int PROCESSOR_MIPS_R4000    =4000;
public static final int PROCESSOR_HITACHI_SH3	=10003;
public static final int PROCESSOR_HITACHI_SH4	=10005;
public static final int PROCESSOR_ALPHA_21064   =21064;
public static final int PROCESSOR_PPC_403       =403;
public static final int PROCESSOR_PPC_601       =601;
public static final int PROCESSOR_PPC_603       =603;
public static final int PROCESSOR_PPC_604       =604;
public static final int PROCESSOR_PPC_620       =620;
public static final int PROCESSOR_PPC_821       =821;
public static final int PROCESSOR_SHx_SH3		=103;
public static final int PROCESSOR_SHx_SH4		=104;
public static final int PROCESSOR_STRONGARM		=2577;	// 0xA11
public static final int PROCESSOR_ARM720		=1824	;// 0x720
public static final int PROCESSOR_ARM820		=2080	;// 0x820
public static final int PROCESSOR_ARM920		=2336	;// 0x920
public static final int PROCESSOR_CEF			=0x494f;

public int processorArchitecture;
public int processorType;
public int processorLevel;
public int processorRevision;

public int osPlatform;
public int osMajorVersion;
public int osMinorVersion;

/**
* This returns either "Mips" or "SH3" or "SH4" or "Arm" depending on
* the discovered processor.
**/
//===================================================================
public String getGenericProcessor()
//===================================================================
{
	switch(processorType){
		case PROCESSOR_MIPS_R2000:
		case PROCESSOR_MIPS_R3000:
		case PROCESSOR_MIPS_R4000: return "Mips";
		case PROCESSOR_HITACHI_SH3:
		case PROCESSOR_SHx_SH3:		return "SH3";
		case PROCESSOR_HITACHI_SH4:
		case PROCESSOR_SHx_SH4:		return "SH4";
		case PROCESSOR_STRONGARM:
		case PROCESSOR_ARM720:
		case PROCESSOR_ARM820:
		case PROCESSOR_ARM920: return "Arm";
		default:
			return null;
	}
}
//##################################################################
}
//##################################################################

