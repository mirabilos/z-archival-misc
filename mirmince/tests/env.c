#include <string.h>
#include <unistd.h>

extern char **environ;

const char nul[] = "";

int
main(void)
{
	char *cp, **wp = environ;

	if (wp)
		while ((cp = *wp++))
			write(1, cp, strlen(cp) + 1);
	write(1, nul, 1);
	return (environ ? 0 : 1);
}
