/*-
 * Copyright © 2013
 *	Thorsten Glaser <tg@mirbsd.org>
 *
 * Provided that these terms and disclaimer and all copyright notices
 * are retained or reproduced in an accompanying document, permission
 * is granted to deal in this work without restriction, including un‐
 * limited rights to use, publicly perform, distribute, sell, modify,
 * merge, give away, or sublicence.
 *
 * This work is provided “AS IS” and WITHOUT WARRANTY of any kind, to
 * the utmost extent permitted by applicable law, neither express nor
 * implied; without malicious intent or gross negligence. In no event
 * may a licensor, author or contributor be held liable for indirect,
 * direct, other damage, loss, or other issues arising in any way out
 * of dealing in the work, even if advised of the possibility of such
 * damage or existence of a defect, except proven that it results out
 * of said person’s immediate fault when using the work as intended.
 */

#include <stdlib.h>

void _mince_csu_run(int argc, char *argv[], char *envp[])
	/* actually more, machdep, not yet used */
	mirmince_dead
	mirmince_cc_cdecl;
extern int main(int argc, char *argv[], char *envp[]);

extern char **environ;

mirmince_dead mirmince_cc_cdecl void
_mince_csu_run(int argc, char *argv[], char *envp[])
{
	/* we will want __progname later */
	environ = envp;

	exit(main(argc, argv, envp));
}
