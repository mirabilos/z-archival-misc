#include <sys/types.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>

static const char hw[] = {
	'H', 'e', 'l', 'l', 'o', ',', ' ',
	'W', 'o', 'r', 'l', 'd', '!', '\n',
};

static const char fn[] = "hw-unix.out";

int
main(int argc, char *argv[])
{
	int fd;

	if ((fd = open(fn, O_WRONLY | O_CREAT | O_TRUNC, 0666)) < 0)
		return (1);
	if ((size_t)write(fd, hw, sizeof(hw)) != sizeof(hw)) {
		close(fd);
		return (2);
	}
	close(fd);
	if ((fd = open(fn, O_WRONLY | O_APPEND)) < 0)
		return (1);
	while (argc--) {
		write(fd, *argv, strlen(*argv) + 1);
		++argv;
	}
	close(fd);
	write(STDOUT_FILENO, hw, sizeof(hw));
	return (0);
}
