all: kalender.pdf

CLEANFILES+=	jahresplan.def
CLEANFILES+=	kalender.aux
CLEANFILES+=	kalender.log
CLEANFILES+=	kalender.out
CLEANFILES+=	kalender.pdf
CLEANFILES+=	kalender.xwm

clean:
	-rm -f ${CLEANFILES}

kalender.pdf: kalender.tex jahresplan.def teckidscal.sty pgflibrarycalendar.easter.def teckids1m.sty teckids_logo_aufkleber.pdf
	pdflatex kalender.tex || { rm -f $@; exit 1; }
	pdflatex kalender.tex || { rm -f $@; exit 1; }

jahresplan.def: jahresplan.sh jahresplan.txt
	mksh jahresplan.sh

.PHONY: all clean
.NOTPARALLEL: yes
