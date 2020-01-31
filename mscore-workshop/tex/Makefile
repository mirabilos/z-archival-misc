CLEANFILES+=	workshop.aux workshop.log workshop.out workshop.toc
GENERATED+=	workshop.pdf

# -recorder
LATEX=		pdflatex -file-line-error -halt-on-error -no-shell-escape
ORDROP=		|| { rm -f $@; exit 1; }
TEXDEPS+=	mypubmat.cls mystyle.sty the_m_irabilos.pdf
TEXDEPS+=	unicodedomino.sty unicodedomino_compat.def \
		unicodedomino_kernel_better_decode.def \
		unicodedomino_kernel_cosmetics.def \
		unicodedomino_kernel_fixup_f4_and_checkseq.def
TEXDEPS+=	unicodepoints.sty

TEXDEPS+=	../notes/linklist.tex

all: workshop.pdf
	-rm -f ${CLEANFILES}

workshop.pdf: workshop.tex ${TEXDEPS}
	${LATEX} workshop.tex ${ORDROP}
	# last latex run for this file
	${LATEX} workshop.tex ${ORDROP}

../notes/linklist.tex:
	${MAKE} -C ../notes all

clean:
	rm -f ${CLEANFILES} ${GENERATED}

show:
	mupdf workshop.pdf

.PHONY: all clean show
