#!/bin/mksh
# -*- mode: sh -*-

LC_ALL=C; LANGUAGE=C
export LC_ALL; unset LANGUAGE

set -e
set -o pipefail
cd "$(dirname "$0")"
x=$(git status --porcelain)
if [[ -n $x ]]; then
	print -ru2 -- "N: git status info follows"
	print -r -- "$x" | sed 's/^/N:  /' >&2
	print -ru2 -- "E: source tree not clean"
	exit 1
fi
set -x

x=$(git show --no-notes -s --pretty=tformat:%cd --date=format:%Y%m%d%H%M.%S)
git ls-tree -r --name-only -z HEAD | xargs -0 touch -h -t "$x" --

git ls-tree -r --name-only -z HEAD | sort -z | pax -w -0 -P \
    -x ustar -o write_opt=nodir -b 512 -M dist -s "!^!mgp-$1/!" | \
    gzip -n9 >"mgp-$1.tgz~"
mv "mgp-$1.tgz~" "mgp-$1.tgz"
