;; mgp-mode.el
;;     --- A mode for editing MagicPoint files
;;
;;  Tested on Mule 2.3, based on GNU Emacs 19.28

;; Copyright (C) 1999  Hirotsugu Kakugawa

;; Maintainer: Hirotsugu Kakugawa (h.kakugawa@computer.org)
;;   1 Apr 1999 1.00  First Implementation
;;   2 Apr 1999 1.01  Added mgp-direc-emph. Changed key bindings.
;;   2 Apr 1999 1.10  Added jumping to error line in MagicPoint file
;;   4 Apr 1999 1.20  Added directive completion input feature
;;   5 Apr 1999 1.21  Added starting MagicPoint at the current page
;;   9 Apr 1999 1.22  Changed mistakes in software license.
;;   8 Dec 1999 1.30  Added syntax hilighting. Carlos Puchol (cpg@puchol.com)

;; This software is free software; you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation; either version 2, or (at your option)
;; any later version.

;; This software is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.

;; You should have received a copy of the GNU General Public License
;; along with GNU Emacs; see the file COPYING.  If not, write to
;; the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.

;; In your ~/.emacs (for example)
;;
;;    (setq auto-mode-alist
;;          (cons '("\\.mgp?\\'" . mgp-mode)
;; 	       auto-mode-alist))
;;    (autoload 'mgp-mode "mgp-mode")
;;    (setq mgp-options "-g 800x600")
;;    (setq mgp-window-height 6)
;;    (cond
;;     ((= emacs-major-version 19)     ;; Emacs 19, Mule 2.3
;;      (setq mgp-mode-hook
;;    	(function (lambda ()
;;    		    (set-file-coding-system '*iso-2022-jp*unix)))))
;;     ((= emacs-major-version 20)     ;; Emacs 20
;;      (setq mgp-mode-hook
;;    	(function (lambda ()
;;    		    (set-file-coding-system-for-read 'iso-2022-jp-unix))))))

;; Editting Commands:
;;
;;   KEY SEQUENCE    DESCRIPTION
;;   ------------    ----------------------------------------------------------
;;   M-x mgp-mode    Change the mode of current buffer to Magic-Point mode
;;   C-c C-v         Run MagicPoint for the current buffer.
;;                   If prefix argument with explicit numbers are given, the
;;                   specified page number is displayed as an inital page.
;;                   If prefix argument without numbe (only 'C-u') is given,
;;                   the page where the cursor is displayed.
;;                       Example 1: C-c C-v  ==> the first page
;;                       Example 2: C-u C-c C-v  ==> the current page
;;                       Example 3: C-u 7 C-c C-v  ==> the seventh page
;;   C-x ` or C-c `  Jump the cursor to the error line in the MagicPoint file.
;;   C-c C-c         Kill the running MagicPoint (if any).
;;   M-TAB           Completing input. If the character at the beginning of
;;                   line is %, MagicPoint directives are completed. Othewise,
;;                   ISPELL is invoked to complete a word.
;;   C-c C-f         Insert the "fore" directive.
;;   C-c C-b         Insert the "back" directive.
;;   C-c C-p         Insert the "page" directive.
;;   C-c C-l         Insert the "lcutin" directive.
;;   C-c C-r         Insert the "rcutin" directive.
;;   C-c C-i         Insert the "image" directive.
;;   C-c C-e         Insert a sequence of directives to emphasizing text.
;;   C-c c           Insert the "center" directive.
;;   C-c l           Insert the "leftfill" directive.
;;   C-c L           Insert the "left-line" directive.
;;   C-c r           Insert the "right-line" directive.
;;   C-c f           Insert the "font" directive.
;;   C-c b           Insert the "bimage" directive.
;;   C-c i           Insert the "icon" directive.
;;   C-c p           Insert the "pause" directive.
;;   C-c a           Insert the "again" directive.
;;   C-c m           Insert the "mark" directive.
;;   C-c n           Insert the "nodefault" directive.
;;   C-c s           Insert the "size" directive.
;;   C-c B           Insert the "bar" directive.
;;   C-c C           Insert the "cont" directive.
;;   C-c G           Insert the "bgrad" directive.
;;   C-c P           Insert the "prefix" directive.
;;   C-c S           Insert the "system" directive.
;;   C-c F           Insert the "filter, endfilter" directive sequence.

;;  Variables for customization
;;   NAME                     DISCRIPTION (buffer local var. is marked *)
;;   --------------------     ---------------------------------------------
;;   mgp-program               MagicPoint program name
;;   mgp-options              *Command line option for MagicPoint
;;   mgp-directives-optional   List of directives for input completion
;;   mgp-window-height         Height of window for output of MagicPoint
;;   mgp-page-separator       *String inserted before %page directive
;;   mgp-emph-color           *Color name for emphasized text, C-c C-e
;;   mgp-emph-color-normal    *Color name for unemphasizing text, C-c C-e
;;

(require 'compile)

;; Program name of MagicPoint
(defvar  mgp-program "mgp"
  "*MagicPoint program name")

;; Command line option for MagicPoint
(defvar  mgp-options nil
  "*MagicPoint optional arguments. nil or  string. Buffer local.")

;; Height of MagicPoint message window
(defvar mgp-window-height nil
  "*Number of lines of a MagicPoint window.  If nil, use Emacs default.")

;; MagicPoint directive alist for completing input
(defvar mgp-directives
  '("size" "fore" "back" "bgrad" "ccolor" "left" "leftfill" "center" "right"
    "shrink" "lcutin" "rcutin" "cont" "nodefault" "xfont"
    "bar" "image" "prefix" "icon" "bimage" "default"
    "tab" "tabprefix" "page" "vgap" "hgap" "pause" "mark" "again"
    "system" "xsystem" "filter" "endfilter" "deffont"
    "font" "noop" "linestart" "lineend" "quality")
  "*List of MagicPoint directives for completing input.")
(defvar mgp-directives-optional nil
  "*List of optional MagicPoint directives for completing input. For customization.")

;; Style
(defvar mgp-page-separator "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
  "Page separator string.")
(defvar mgp-emph-color "red"
  "The text color name for emphasized words, for mgp-direc-emph function.")
(defvar mgp-emph-color-normal "black"
  "The text color name for non-emphasized words, for mgp-direc-emph function.")

(cond ((= emacs-major-version 19)
       ;; Emacs 19 settings
       (cond ((fboundp 'copy-face)
	      (require 'hilit19)
	      (hilit-set-mode-patterns
	       '(mgp-mode)
	       '(
		 ("^%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*" nil Yellow)
		 ("^#.*" nil comment)
		 ("\"" "[^\\]\"" string)
		 ("\\<\\(size\\|fore\\|back\\|bgrad\\|ccolor\\|left\\|leftfill\\|center\\|right\\|shrink\\|lcutin\\|rcutin\\|cont\\|nodefault\\|xfont\\|bar\\|image\\|prefix\\|icon\\|bimage\\|default\\|tab\\|tabprefix\\|page\\|vgap\\|hgap\\|pause\\|mark\\|again\\|system\\|xsystem\\|filter\\|endfilter\\|deffont\\|font\\|noop\\|linestart\\|lineend\\|quality\\)\\>" nil keyword)
		 ("^%.*" nil defun)
		 ))))))

;; History
(defvar mgp-history nil
  "History of mgp commands.")

;; Abbrev Table
(defvar mgp-mode-abbrev-table nil
  "Abbrev table used while in mgp mode.")
(define-abbrev-table 'mgp-mode-abbrev-table ())

;; Directive Completion
(defvar mgp-directive-alist nil
  "Alist of MagicPoint directives.")
(if mgp-directive-alist
    ()
  (setq mgp-directive-alist
	(mapcar (function (lambda (direc) (list direc)))
		(sort (append mgp-directives mgp-directives-optional)
		      'string<))))

;; Syntax Table
(defvar mgp-mode-syntax-table nil
  "Syntax table used while in mgp mode.")
(if mgp-mode-syntax-table
    ()
  (setq mgp-mode-syntax-table (make-syntax-table))
  (modify-syntax-entry ?% ".   " mgp-mode-syntax-table)
  (modify-syntax-entry ?\" ".   " mgp-mode-syntax-table)
  (modify-syntax-entry ?\\ ".   " mgp-mode-syntax-table)
  (modify-syntax-entry ?' "w   " mgp-mode-syntax-table))

;; Mode Map
(defvar mgp-mode-map nil
  "Keymap for Mgp (MagicPoint) mode.
Many other modes, such as Mail mode, Outline mode and Indented Mgp mode,
inherit all the commands defined in this map.")
(if mgp-mode-map
    ()
  (setq mgp-mode-map (make-sparse-keymap))
  (define-key mgp-mode-map "\t" 'tab-to-tab-stop)
  (define-key mgp-mode-map "\e\t" 'mgp-complete-word)
  (define-key mgp-mode-map "\C-c\C-v" 'mgp-run-mgp)
  (define-key mgp-mode-map "\C-c\C-c" 'kill-compilation)
  (define-key mgp-mode-map "\C-c`"    'next-error)
  (define-key mgp-mode-map "\C-x`"    'next-error)
  (define-key mgp-mode-map "\C-c\C-f" 'mgp-direc-fore)
  (define-key mgp-mode-map "\C-c\C-b" 'mgp-direc-back)
  (define-key mgp-mode-map "\C-c\C-p" 'mgp-direc-page)
  (define-key mgp-mode-map "\C-c\C-l" 'mgp-direc-lcutin)
  (define-key mgp-mode-map "\C-c\C-r" 'mgp-direc-rcutin)
  (define-key mgp-mode-map "\C-c\C-i" 'mgp-direc-image)
  (define-key mgp-mode-map "\C-c\C-e" 'mgp-direc-emph)
  (define-key mgp-mode-map "\C-cc" 'mgp-direc-center-line)
  (define-key mgp-mode-map "\C-cl" 'mgp-direc-leftfill)
  (define-key mgp-mode-map "\C-cL" 'mgp-direc-left-line)
  (define-key mgp-mode-map "\C-cr" 'mgp-direc-right-line)
  (define-key mgp-mode-map "\C-cf" 'mgp-direc-font)
  (define-key mgp-mode-map "\C-cb" 'mgp-direc-bimage)
  (define-key mgp-mode-map "\C-ci" 'mgp-direc-icon)
  (define-key mgp-mode-map "\C-cp" 'mgp-direc-pause)
  (define-key mgp-mode-map "\C-ca" 'mgp-direc-again)
  (define-key mgp-mode-map "\C-cm" 'mgp-direc-mark)
  (define-key mgp-mode-map "\C-cn" 'mgp-direc-nodefault)
  (define-key mgp-mode-map "\C-cs" 'mgp-direc-size)
  (define-key mgp-mode-map "\C-cB" 'mgp-direc-bar)
  (define-key mgp-mode-map "\C-cC" 'mgp-direc-cont)
  (define-key mgp-mode-map "\C-cG" 'mgp-direc-bgrad)
  (define-key mgp-mode-map "\C-cP" 'mgp-direc-prefix)
  (define-key mgp-mode-map "\C-cS" 'mgp-direc-system)
  (define-key mgp-mode-map "\C-cF" 'mgp-direc-filter))

;; MagicPoint mode
(defun mgp-mode ()
  "Major mode for editing MagicPoint files.

Special commands:
\\{mgp-mode-map}
Turning on Mgp mode calls the value of the variable `mgp-mode-hook',
if that value is non-nil.

Variables for customization
  NAME                     DISCRIPTION (buffer local var. is marked *)
  --------------------     ---------------------------------------------
   mgp-program               MagicPoint program name
   mgp-options              *Command line option for MagicPoint
   mgp-directives-optional   List of directives for input completion
   mgp-window-height         Height of window for output of MagicPoint
   mgp-page-separator       *String inserted before %page directive
   mgp-emph-color           *Color name for emphasized text, C-c C-e
   mgp-emph-color-normal    *Color name for unemphasizing text, C-c C-e
"
  (interactive)
  (kill-all-local-variables)
  (use-local-map mgp-mode-map)
  (setq mode-name "MagicPoint")
  (setq major-mode 'mgp-mode)
  (setq local-abbrev-table mgp-mode-abbrev-table)
  (set-syntax-table mgp-mode-syntax-table)
  (make-local-variable 'mgp-options)
  (make-local-variable 'mgp-page-separator)
  (make-local-variable 'mgp-emph-color)
  (make-local-variable 'mgp-emph-color-normal)
  (run-hooks 'mgp-mode-hook))

;; Inserting Directives

(defun mgp-direc-center-line ()
  (interactive)
  (mgp-insert-directive "center"))
(defun mgp-direc-left-line ()
  (interactive)
  (mgp-insert-directive "left"))
(defun mgp-direc-leftfill ()
  (interactive)
  (mgp-insert-directive "leftfill"))
(defun mgp-direc-right-line ()
  (interactive)
  (mgp-insert-directive "right"))
(defun mgp-direc-image ()
  (interactive)
  (mgp-insert-directive "image" ""))
(defun mgp-direc-fore ()
  (interactive)
  (mgp-insert-directive "fore" ""))
(defun mgp-direc-back ()
  (interactive)
  (mgp-insert-directive "back" ""))
(defun mgp-direc-lcutin ()
  (interactive)
  (mgp-insert-directive "lcutin"))
(defun mgp-direc-rcutin ()
  (interactive)
  (mgp-insert-directive "rcutin"))
(defun mgp-direc-cont ()
  (interactive)
  (mgp-insert-directive "cont"))
(defun mgp-direc-nodefault ()
  (interactive)
  (mgp-insert-directive "nodefault"))
(defun mgp-direc-bar ()
  (interactive)
  (mgp-insert-directive "bar" t))
(defun mgp-direc-pause ()
  (interactive)
  (mgp-insert-directive "pause"))
(defun mgp-direc-font ()
  (interactive)
  (mgp-insert-directive "font" ""))
(defun mgp-direc-noop ()
  (interactive)
  (mgp-insert-directive "noop"))
(defun mgp-direc-system ()
  (interactive)
  (mgp-insert-directive "system" ""))
(defun mgp-direc-bgrad ()
  (interactive)
  (mgp-insert-directive "bgrad" t))
(defun mgp-direc-size ()
  (interactive)
  (mgp-insert-directive "size" t))
(defun mgp-direc-prefix ()
  (interactive)
  (mgp-insert-directive "prefix" ""))
(defun mgp-direc-icon ()
  (interactive)
  (mgp-insert-directive "icon" t))
(defun mgp-direc-bimage ()
  (interactive)
  (mgp-insert-directive "bimage" ""))
(defun mgp-direc-mark ()
  (interactive)
  (mgp-insert-directive "mark"))
(defun mgp-direc-again ()
  (interactive)
  (mgp-insert-directive "again"))

(defun mgp-insert-directive (dirc &optional arg)
  (let ((pos-eol (save-excursion
		   (end-of-line)
		   (point))))
    (beginning-of-line)
    (if (looking-at "%")
	(if (re-search-forward "[a-zA-Z]" pos-eol t)
	    (progn
	      (end-of-line)
	      (insert ", "))
	  (beginning-of-line)
	  (forward-char 1))
      (insert "%")))
  (insert dirc)
  (if arg
      (if (stringp arg)
	  (insert (concat " " "\"" arg "\""))
	(insert " ")
	(backward-char 1))))

(defun mgp-direc-page ()
  (interactive)
  (insert mgp-page-separator)
  (insert "\n%page\n"))

(defun mgp-direc-filter ()
  (interactive)
  (mgp-insert-directive2 "filter"))
(defun mgp-insert-directive2 (direc)
  (insert (concat "%" direc "\n%end" direc "\n")))

(defun mgp-direc-emph (n)
  (interactive "P")
  (let ((cn
	 (concat "%" (if n "cont, " "") "fore "))
	(c1 (if mgp-emph-color
		mgp-emph-color ""))
	(c2 (if mgp-emph-color-normal
		mgp-emph-color-normal  "")))
    (insert (concat cn "\"" c1 "\"\n\n"))
    (insert (concat cn "\"" c2 "\"\n"))
    (previous-line 2)))

;; Running MagicPoint

(defun mgp-run-mgp (command-args)
  (interactive
   (list (let ((page-opt ""))
	   (if current-prefix-arg
	       (setq page-opt
		     (concat " -p "
			     (mgp-current-page-position current-prefix-arg)
			     " ")))
	   (read-from-minibuffer
	    "Run MagicPoint: "
	    (concat mgp-program " " mgp-options page-opt)
	    nil nil 'mgp-history))))
  (if (buffer-modified-p)
      (if (y-or-n-p "MagicPoint document is modified. Save it? ")
	  (save-buffer)))
  (if (buffer-file-name (current-buffer))
      (let ((compilation-window-height mgp-window-height))
	(compile-internal
	 (concat command-args " "
		 (file-name-nondirectory (buffer-file-name (current-buffer))))
	 "No more errors" "MagicPoint"))))

(defun mgp-current-page-position (arg)
  (if (numberp arg)
      (if (<= arg 0)
	  1
	(let ((total (mgp-total-pages)))
	  (if (< arg total)
	      arg
	    total)))
    (save-excursion
      (end-of-line)
      (let ((bound (point))
	    (page 0))
	(goto-char (point-min))
	(while (search-forward "\n%page" bound t)
	  (setq page (+ page 1)))
	(number-to-string (if (= page 0) 1 page))))))

(defun mgp-total-pages ()
  (save-excursion
    (let ((pages 0))
      (goto-char (point-min))
      (while (search-forward "\n%page" (point-max) t)
	(setq pages (+ pages 1)))
      pages)))

;; Completion

(defun mgp-complete-word ()
  "Perform completion on a MagicPoint directive or a word preceding point."
  (interactive)
  (if (not (save-excursion
	     (beginning-of-line)
	     (looking-at "%")))
      (ispell-complete-word)
    (if (save-excursion
	  (goto-char (max (point-min) (- (point) 1)))
	  (= (char-syntax (following-char)) ?\w))
	(let* ((end (point))
	       (beg (save-excursion
		      (backward-word 1)
		      (while (= (char-syntax (following-char)) ?\')
			(forward-char 1))
		      (point)))
	       (pattern (buffer-substring beg end)))
	  (mgp-complete-directive pattern))
      (mgp-complete-directive ""))))

(defun mgp-complete-directive (pattern)
  (let ((completion (try-completion pattern mgp-directive-alist)))
    (cond ((eq completion t)
	   (message "Sole completion"))
	  ((null completion)
	   (message "Can't find completion for \"%s\"" pattern)
	   (ding))
	  ((not (string= pattern completion))
	   (delete-region beg end)
	   (insert completion))
	  (t
	   (message "Making completion list...")
	   (let* ((lizt (all-completions pattern mgp-directive-alist))
		  (new))
	     (while lizt
	       (setq new (cons (car lizt) new))
	       (setq lizt (cdr lizt)))
	     (setq lizt (nreverse new))
	     (with-output-to-temp-buffer "*Completions*"
	       (display-completion-list lizt)))
	   (message "Making completion list...%s" "done")))))

;;; mgp-mode.el ends here
