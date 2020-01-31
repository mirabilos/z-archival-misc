all:
	@echo make run, edit, export or show
	@echo 'needs mgp=1.13a+upstream20090219-9 (Debian sid)'

run:
	(sleep 2; exec xclock -fg white -bg black -padding 1 -update 10 -brief \
	    -digital -twentyfour -face FixedMisc -geometry +0+0)& bgpid=$$! ;( \
	mgp -G -g 1024x768 -Q 100 -q -R foils/workshop.mgp; kill $$bgpid || :)

debug:
	mgp -G -g 1024x768 -Q 100 -q foils/workshop.mgp

edit:
	jupp -encoding latin1 foils/workshop.mgp

export:
	@rm -rf html*
	@mkdir html~
	mgp -G -g 1024x768 -Q 100 -D html~/ -E png foils/workshop.mgp
	@echo post-processing…
	@printf '%s\n' '$$-1r build/foil-stub.htm' w q | ed -s html~/index.html
	@set -e; for f in html~/*.txt; do \
		iconv -f iso-8859-1 -t iso-2022-jp-2 -o "$$f" "$$f"; \
		printf '%s\n' ',g/-A/s//(B/g' w q | ed -s "$$f"; \
		iconv -f iso-2022-jp-2 -t utf-8 -o "$$f" "$$f"; \
	done
	mv html~ html

show:
	$${BROWSER:-links2 -g} html/index.html

pdf:
	-${MAKE} -C tex clean
	${MAKE} -C tex all

linklist:
	-${MAKE} -C notes clean
	${MAKE} -C notes all

clean:
	-${MAKE} -C tex clean
	-${MAKE} -C notes clean
	-rm -rf public

pages:
	${MAKE} -C notes all
	${MAKE} -C tex all
	mkdir -p public/foils public/notes
	cp -r html public/
	cp foils/the_m_irabilos.png public/foils/
	cp notes/*.htm notes/*.png public/notes/
	cp tex/workshop.pdf public/
	sed 's!\.\./!!g' <build/pages-stub.htm >public/index.html

.PHONY: all run debug edit export show pdf linklist clean pages
