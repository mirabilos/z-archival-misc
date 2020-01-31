__IDSTRING(rcsid_nmunix_serial, "$MirOS: contrib/hosted/ewe/vm/nmunix_serial.c,v 1.5 2008/04/14 11:31:47 tg Exp $");

#include <termios.h>

static void SerialPortDestroy(WObject sp)
{
	PipeDestroy(sp);
}
static Var SerialPortClose(Var stack[])
{
	Var v = PipeClose(stack);
	return v;
}

static Var SerialPortKill(Var stack[])
{
	Var v = PipeClose(stack);
	if (v.intValue == 1) v.obj = 0;
	else v.obj = createStringFromUtf(createUtfString("Could not close port."));
	return v;
}

static Var SerialPortRead(Var stack[])
{
	Var read = PipeReadWrite(stack,1);
	return read;
}
static Var SerialPortWrite(Var stack[])
{
	return PipeReadWrite(stack,0);
}

int openSerialPort(WObject name)
{
	char *nm, ttyBuffer[32];
	int fd = -1, len;
	struct stat sb;

	if (name == 0)
		return (-1);
	nm = stringToNativeText(name);
	len = strlen(nm);
	if (len >= 5 && strncmp(nm, "COM", 3) == 0 && nm[len - 1] == ':') {
		int port = 0;
		nm[len - 1] = 0;
		sscanf(nm + 3, "%d", &port);
		port--;
#if defined(__gnu_linux__) || defined(__linux__) || defined(linux)
		snprintf(ttyBuffer, sizeof (ttyBuffer), "ttyS%d", port);
#else
		snprintf(ttyBuffer, sizeof (ttyBuffer), "tty%02d", port);
#endif
	} else
		strlcpy(ttyBuffer, nm, sizeof (ttyBuffer));
	sprintf(sprintBuffer, "/dev/%s", ttyBuffer);
	if (stat(sprintBuffer, &sb)) {
		sprintf(sprintBuffer, "Warning: cannot open \"/dev/%s\","
		    " trying \"%s\"...\n", ttyBuffer, ttyBuffer);
		debugString(sprintBuffer);
		fd = open(ttyBuffer, O_RDWR | O_NOCTTY);
	} else
		fd = open(sprintBuffer, O_RDWR | O_NOCTTY);
	free(nm);
	return (fd);
}
static Var SerialPortCanOpen(Var stack[])
{
	int fd = openSerialPort(stack[0].obj);
	int ret = fd != -1;
	if (ret) close(fd);
	return returnVar(ret);
}
static Var SerialPortIsOpen(Var stack[])
{
	return returnVar(WOBJ_PipeNative(stack[0].obj) != -1);
}
static Var SerialPortCreate(Var stack[])
{
	WObject sp = stack[0].obj;
  struct termios oldtio,newtio;
	int fd = openSerialPort(stack[1].obj);
	WOBJ_PipeNative(sp) = fd;
	if (fd == -1) return returnVar(0);
	else{
		int BAUDRATE = B9600, parity = 0, stops = 0, data = CS8;
		switch(stack[2].intValue){
		case 0: BAUDRATE = B0; break;
		case 50: BAUDRATE = B50; break;
		case 75: BAUDRATE = B75; break;
		case 110: BAUDRATE = B110; break;
		case 134: BAUDRATE = B134; break;
		case 150: BAUDRATE = B150; break;
		case 200: BAUDRATE = B200; break;
		case 300: BAUDRATE = B300; break;
		case 600: BAUDRATE = B600; break;
		case 1200: BAUDRATE = B1200; break;
		case 1800: BAUDRATE = B1800; break;
		case 2400: BAUDRATE = B2400; break;
		case 4800: BAUDRATE = B4800; break;
		case 9600: BAUDRATE = B9600; break;
		case 19200: BAUDRATE = B19200; break;
		case 38400: BAUDRATE = B38400; break;
		case 57600: BAUDRATE = B57600; break;
		case 115200: BAUDRATE = B115200; break;
		case 230400: BAUDRATE = B230400; break;
		}
		switch(stack[3].intValue){ //Data bits
		case 5: data = CS5; break;
		case 6: data = CS6; break;
		case 7: data = CS7; break;
		case 8: data = CS8; break;
		}
		switch(stack[4].intValue){ //Parity
		case 0: parity = 0; break;
		case 1: parity = PARODD; break; //????
		case 2: parity = PARENB; break; //????
		}
		switch(stack[5].intValue){ //Stop bits
		case 1: stops = 0; break;
		default: stops = CSTOPB; //????
		}
 		tcgetattr(fd,&oldtio); /* save current port settings */

 		bzero(&newtio, sizeof(newtio));
 		newtio.c_cflag = BAUDRATE | CRTSCTS | data | parity | stops | CLOCAL | CREAD;
 		newtio.c_iflag = IGNPAR;
		newtio.c_oflag = 0;
 		/* set input mode (non-canonical, no echo,...) */
 		newtio.c_lflag = 0;

 		newtio.c_cc[VTIME]    = 0;   /* inter-character timer unused */
		newtio.c_cc[VMIN]     = 1;   /* blocking read until 5 chars received */

		/* serial speed for more modern unices */
		cfsetspeed(&newtio, stack[2].intValue);

 		tcflush(fd, TCIFLUSH);
 		tcsetattr(fd,TCSANOW,&newtio);
		setupPipe(sp,fd);
		//printf("Setup pipe: %d\n",fd);
		return returnVar(1);
	}
}
