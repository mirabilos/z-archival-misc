% Lokale Dokumentklasse für Workshopmaterial
%
% Basiert auf:
% Dokumentklasse „Unterrichtsmaterial 2.0“ für den Teckids e.V.
%
% Copyright © 2015, 2017, 2018, 2019
%	Thorsten Glaser <thorsten.glaser@teckids.org>
% Copyright © 2013
%	Dominik George <nik@naturalnet.de>
%
% Provided that these terms and disclaimer and all copyright notices
% are retained or reproduced in an accompanying document, permission
% is granted to deal in this work without restriction, including un‐
% limited rights to use, publicly perform, distribute, sell, modify,
% merge, give away, or sublicence.
%
% This work is provided “AS IS” and WITHOUT WARRANTY of any kind, to
% the utmost extent permitted by applicable law, neither express nor
% implied; without malicious intent or gross negligence. In no event
% may a licensor, author or contributor be held liable for indirect,
% direct, other damage, loss, or other issues arising in any way out
% of dealing in the work, even if advised of the possibility of such
% damage or existence of a defect, except proven that it results out
% of said person’s immediate fault when using the work as intended.

\NeedsTeXFormat{LaTeX2e}%
\ProvidesClass{mypubmat}[2019/05/15 1.0 Workshopmaterial Dokumentklasse]%

\PassOptionsToPackage{paper=a4}{typearea}%
\RequirePackage{fancyhdr}%
\LoadClass[bibliography=totocnumbered,bibliography=leveldown,toc=sectionentrywithdots]{scrartcl}%
\RequirePackage{mystyle}%

% How to use:
% \documentclass{mypubmat}%
% \dokumentLizenzMirOS% oder \dokumentLizenzCCBYSAfour% oder \dokumentLizenzCCBYSAthreeDE%
% \myhdr[2017]{Dokumenttitel}% für allgemeine Dokumente, geht auch ohne Jahr
% \myhdr[2017 Name, Projekt]{Dokumenttitel}% besser dies verwenden, Autoren aufzählen
% % Mehrere Autorennamen durch Komma+Leerzeichen trennen, schließendes Komma + Projekt
