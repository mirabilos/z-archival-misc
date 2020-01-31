;;; mgp.el --- Magic Point tool
;;; Copyright (C) 1999 Electrotechnical Lab., JAPAN

;; Author: K.Handa <handa@etl.go.jp>
;; Created: 1999/04/28
;; Keywords: mgp, Magic Point

;; Copyright (c) 1999 K.Handa <handa@etl.go.jp>
;;
;; This program is free software; you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation; either version 2 of the License, or
;; (at your option) any later version.
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License along
;; with this program; if not, write to the Free Software Foundation, Inc.,
;; 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

;;; Code:

(eval-and-compile

(or mule-version
    (error "This program requires Mule feature"))

;; For compatibility between Mule 2.3 and Emacs 20.

(defun mgp-buffer-file-coding-system ()
  (if (boundp 'buffer-file-coding-system)
      (symbol-value 'buffer-file-coding-system)
    (symbol-value 'file-coding-system)))

(or (fboundp 'set-buffer-file-coding-system)
    (defalias 'set-buffer-file-coding-system 'set-file-coding-system))

(or (fboundp 'coding-system-base)
    (defun coding-system-base (coding)
      (cond ((memq coding '(*ctext* *iso-8859-1*)) 'latin-1)
	    ((eq coding '*iso-8859-2*) 'latin-2)
	    ((eq coding '*iso-8859-3*) 'latin-3)
	    ((eq coding '*iso-8859-4*) 'latin-4)
	    ((eq coding '*iso-8859-5*) 'latin-5))))

(or (fboundp 'coding-system-equal)
    (defalias 'coding-system-equal 'eq))

(or (fboundp 'line-beginning-position)
    (defun line-beginning-position ()
      (save-excursion
	(beginning-of-line)
	(point))))

(or (fboundp 'line-end-position)
    (defun line-end-position ()
      (save-excursion
	(end-of-line)
	(point))))

)

(defvar mgp-version "0.90")

(defvar mgp-paper-size "a4")
(defvar mgp-image-scale-factor 0.95)
(defvar mgp-landscape t)
(defvar mgp-lib-directory "/usr/X11R6/lib/X11/mgp")
(defvar mgp-disable-color t)

(defconst mgp-paper-size-list '("a5" "a4" "b5" "letter" "legal" "executive"))
(defconst mgp-predefined-color-list
  '("black" "white" "red" "green" "blue" "yellow" "cyan" "magenta" "gray"))
(defvar mgp-color-list nil)

(defvar mgp-entex-inbuf nil)
(defvar mgp-entex-outbuf nil)

(defvar mgp-entex-source nil)

(defun mgp-entex-file (file)
  (interactive "fMGP source file: ")
  (setq mgp-entex-source file)
  (mgp-entex-setup-color-data)
  (mgp-entex-setup-buffer)
  (mgp-entex-process-buffer)
  (switch-to-buffer mgp-entex-outbuf)
  (goto-char (point-min))
  (save-buffer))

(defun mgp-entex-buffer ()
  (interactive)
  (setq mgp-entex-source (current-buffer))
  (mgp-entex-setup-color-data)
  (mgp-entex-setup-buffer)
  (mgp-entex-process-buffer)
  (switch-to-buffer mgp-entex-outbuf)
  (goto-char (point-min))
  (save-buffer))

(defvar mgp-max-color-value nil)

(defun mgp-entex-setup-color-data ()
  (if (eq window-system 'x)
      (setq mgp-max-color-value (car (x-color-values "white")))))

(defmacro mgp-with-output-buffer (&rest body)
  `(let ((buf (current-buffer)))
     (or (eq buf mgp-entex-outbuf)
	 (set-buffer mgp-entex-outbuf))
     (unwind-protect
	 (progn ,@body)
       (or (eq buf mgp-entex-outbuf)
	   (set-buffer buf)))))

(put 'mgp-with-output-buffer 'lisp-indent-function 0)

(defun mgp-entex-setup-buffer ()
  (let (filename dir coding)
    (save-excursion
      (setq mgp-entex-inbuf (get-buffer-create "*mgp*"))
      (set-buffer mgp-entex-inbuf)
      (erase-buffer)
      (insert "%%\n")
      (if (bufferp mgp-entex-source)
	  (progn
	    (save-excursion
	      (set-buffer mgp-entex-source)
	      (setq filename (concat (file-name-sans-extension
				      (or buffer-file-name (buffer-name)))
				     ".tex")
		    dir default-directory
		    coding (mgp-buffer-file-coding-system)))
	    (set-buffer-file-coding-system coding)
	    (setq default-directory dir)
	    (insert-buffer mgp-entex-source))
	(setq filename
	      (concat (file-name-sans-extension mgp-entex-source) ".tex")
	      dir
	      (file-name-directory (expand-file-name mgp-entex-source)))
	(insert-file-contents (expand-file-name mgp-entex-source))
	(setq coding (mgp-buffer-file-coding-system))
	(setq default-directory dir))

      (goto-char (point-min))
      (while (and (search-forward "\n%include" nil t)
		  (looking-at " +\\([^ ]+\\)"))
	(let ((incfile (mgp-entex-get-string-arg))
	      (pos (line-beginning-position))
	      len)
	  (forward-line 1)
	  (delete-region pos (point))
	  (if (or (file-readable-p incfile)
		  (progn
		    (setq incfile (expand-file-name incfile mgp-lib-directory))
		    (file-readable-p incfile)))
	      (progn
		(message "Inserting file %s..." incfile)
		(insert-file-contents incfile)))
	  (goto-char (point-min))))
      (goto-char (point-min))
      (while (search-forward "\\\n" nil t)
	(delete-char -2))
      (goto-char (point-min))
      (setq mgp-entex-outbuf
	    (get-buffer-create (file-name-nondirectory filename)))
      (set-buffer mgp-entex-outbuf)
      (erase-buffer)
      (setq default-directory dir
	    buffer-file-name filename)
      (set-buffer-file-coding-system coding))))

(defvar mgp-entex-continue nil)
(defvar mgp-entex-linehead nil)
(defvar mgp-entex-prefix "")

(defun mgp-entex-process-buffer ()
  (message "Converting MGP to LaTeX...")
  (mgp-entex-init-global-setting)
  (save-excursion
    (set-buffer mgp-entex-inbuf)
    (while (not (or (eobp) (looking-at "^%page")))
      (mgp-entex-process-preamble)
      (forward-line 1))
    (mgp-entex-start-body)
    (forward-line 1)
    (let ((page 0)
	  line)
      (while (not (eobp))
	(mgp-entex-init-local-setting)
	(setq page (1+ page))
	(message "Page %d" page)
	(setq line 1)
	(mgp-entex-start-page)
	(setq mgp-entex-linehead t
	      mgp-entex-prefix "")
	(while (not (or (eobp) (looking-at "^%page")))
	  (setq line (mgp-entex-process-page line))
	  (forward-line 1))
	(mgp-entex-finish-page line)
	(forward-line 1))
      (mgp-entex-finish-body))))

(defconst mgp-setting-num 128)
(defvar mgp-global-setting nil)
(defvar mgp-local-setting nil)

(defun mgp-entex-init-global-setting ()
  (let ((i 2)
	(len (* mgp-setting-num 2)))
    (setq mgp-global-setting (make-vector len nil))
    (aset mgp-global-setting 1 (list 'font nil))
    (while (< i len)
      (aset mgp-global-setting i (list 'font nil))
      (setq i (1+ i)))))

(defun mgp-entex-init-local-setting ()
  (let ((len (length mgp-global-setting))
	(i 0))
    (if (< (length mgp-local-setting) len)
	(setq mgp-local-setting (make-vector len nil)))
    (while (< i len)
      (aset mgp-local-setting i (copy-sequence (aref mgp-global-setting i)))
      (setq i (1+ i)))))

(defun mgp-entex-parse-cmd (globalp defaultp index)
  (save-restriction
    (narrow-to-region (point) (line-end-position))
    (while (re-search-forward "[a-z]+" nil t)
      (let ((func (intern (concat "mgp-entex-cmd-"
				  (downcase (match-string 0))))))
	(if (fboundp func)
	    (funcall func globalp defaultp index)))
      (skip-chars-forward ",")
      (or (eobp)
	  (forward-char 1)))))

(defun mgp-entex-process-preamble ()
  (if (looking-at "^%\\(default\\|tab\\) *")
      (progn
	(forward-char 1)
	(or (eolp)
	    (mgp-entex-parse-cmd 'global nil nil)))))

(defun mgp-entex-start-body ()
  (mgp-with-output-buffer
    (insert "%%% This file was generated by mgp.el ")
    (insert (format "(Ver.%s).\n"  mgp-version))
    (insert "%%%   Document source: ")
    (if (bufferp mgp-entex-source)
	(insert (format "buffer \"%s\"\n" (buffer-name mgp-entex-source)))
      (insert (format "file \"%s\"\n" mgp-entex-source)))
    (insert (format "\\documentclass[%spaper%s]{article}\n\\input{mgp.sty}\n"
		    (if (member mgp-paper-size mgp-paper-size-list)
			mgp-paper-size
		      "a4")
		    (if mgp-landscape ",landscape" "")))
    (or mgp-disable-color
	(progn
	  (insert "\\usepackage{color}\n\\definecolor{gray}{gray}{0.001}")
	  (setq mgp-color-list (cons "gray" mgp-predefined-color-list))))
    (let ((coding (mgp-buffer-file-coding-system)))
      (if coding
	  (let ((base (coding-system-base (mgp-buffer-file-coding-system))))
	    (cond ((coding-system-equal base 'latin-1)
		   (insert "\\usepackage[latin1]{inputenc}\n"))
		  ((coding-system-equal base 'latin-2)
		   (insert "\\usepackage[latin2]{inputenc}\n"))
		  ((coding-system-equal base 'latin-3)
		   (insert "\\usepackage[latin3]{inputenc}\n"))
		  ((coding-system-equal base 'latin-5)
		   (insert "\\usepackage[latin5]{inputenc}\n"))))))
    (insert "\\begin{document}\n")))

(defun mgp-entex-finish-body ()
  (mgp-with-output-buffer
    (insert "\n\\end{document}\n")))

(defun mgp-entex-set-setting (globalp defaultp index prop val)
  (let ((table (if globalp mgp-global-setting mgp-local-setting)))
    (or defaultp
	(setq index (+ mgp-setting-num index)))
    (plist-put (aref table index) prop val)))

(defun mgp-entex-get-setting (globalp defaultp index prop this-line-only)
  (let ((table (if globalp mgp-global-setting mgp-local-setting))
	(limit 0)
	val)
    (or defaultp
	(setq index (+ mgp-setting-num index)
	      limit mgp-setting-num))
    (if this-line-only
	(setq limit index))
    (while (and (>= index limit) (not val))
      (setq val (plist-get (aref table index) prop))
      (setq index (1- index)))
    val))

(defun mgp-entex-get-string-arg ()
  (skip-chars-forward " \t")
  (if (= (following-char) ?\")
      (read (current-buffer))
    (if (looking-at "[^, \n]+")
	(progn
	  (goto-char (match-end 0))
	  (match-string 0)))))

(defun mgp-entex-get-number-arg ()
  (if (looking-at " *[0-9.]+")
      (let ((num (string-to-int (match-string 0))))
	(goto-char (match-end 0))
	num)))

(defun mgp-entex-cmd-default (globalp &rest ignore)
  (let ((index (mgp-entex-get-number-arg)))
    (goto-char (match-end 0))
    (mgp-entex-parse-cmd globalp 'default index)))

(put 'mgp-entex-cmd-default 'mgp-entex-special t)

(defun mgp-entex-cmd-tab (globalp &rest ignore)
  (let ((index (mgp-entex-get-number-arg)))
    (goto-char (match-end 0))
    (mgp-entex-parse-cmd globalp nil index)))

(put 'mgp-entex-cmd-tab 'mgp-entex-special t)

(defvar mgp-mark-position nil)
(defvar mgp-current-line nil)

(defun mgp-entex-cmd-mark (globalp defaultp index)
  (setq mgp-mark-position
	(cons mgp-current-line
	      (mgp-with-output-buffer (line-beginning-position)))))

(defun mgp-entex-cmd-again (globalp defaultp index)
  (mgp-entex-set-setting globalp defaultp index 'again t))

(defun mgp-entex-cmd-fore (globalp defaultp index)
  (let ((arg (mgp-entex-get-string-arg)))
    (if arg
	(mgp-entex-set-setting globalp defaultp index 'fore arg))))

(defun mgp-entex-cmd-back (globalp defaultp index)
  (let ((arg (mgp-entex-get-string-arg)))
    (if arg
	(mgp-entex-set-setting globalp defaultp index 'back arg))))

(defun mgp-entex-cmd-size (globalp defaultp index)
  (let ((arg (mgp-entex-get-number-arg)))
    (if arg
	(mgp-entex-set-setting globalp defaultp index 'size arg))))

(defun mgp-entex-cmd-center (globalp defaultp index)
  (mgp-entex-set-setting globalp defaultp index 'align 'center))

(defun mgp-entex-cmd-left (globalp defaultp index)
  (mgp-entex-set-setting globalp defaultp index 'align 'left))

(defun mgp-entex-cmd-leftfill (globalp defaultp index)
  (mgp-entex-set-setting globalp defaultp index 'align 'left))

(defun mgp-entex-cmd-right (globalp defaultp index)
  (mgp-entex-set-setting globalp defaultp index 'align 'right))

(defun mgp-entex-cmd-vgap (globalp defaultp index)
  (let ((arg (mgp-entex-get-number-arg)))
    (if arg
	(mgp-entex-set-setting globalp defaultp index 'vgap arg))))

(defun mgp-entex-cmd-font (globalp defaultp index)
  (let ((arg (mgp-entex-get-string-arg)))
    (if arg
	(mgp-entex-set-setting globalp defaultp index 'font arg))))

(defun mgp-entex-cmd-xfont (globalp defaultp index)
  (let ((arg (mgp-entex-get-string-arg)))
    (if arg
	(mgp-entex-set-setting globalp defaultp index 'font arg))))

(defun mgp-entex-cmd-cont (globalp defaultp index)
  (mgp-entex-set-setting globalp defaultp index 'cont t))

(defun mgp-entex-cmd-nodefault (globalp defaultp index)
  (if globalp
      (error "%nodefault can't be used in preamble"))
  (let ((len (length mgp-local-setting))
	(i 2))
    (aset mgp-local-setting 1 (list 'font nil))
    (while (< i len)
      (aset mgp-local-setting i (list 'font nil))
      (setq i (1+ i)))))

(defun mgp-entex-cmd-bar (globalp defaultp index)
  (let ((color (mgp-entex-get-string-arg))
	(width (or (mgp-entex-get-number-arg) 10))
	(start (or (mgp-entex-get-number-arg) 0))
	(length (or (mgp-entex-get-number-arg) 100)))
    (mgp-entex-set-setting globalp defaultp index 'bar
			   (list color width start length))))

(defun mgp-entex-cmd-image (globalp defaultp index)
  (let ((arg1 (mgp-entex-get-string-arg)))
    (if arg1
	(let ((arg2 (mgp-entex-get-string-arg))
	      (arg3 (mgp-entex-get-string-arg))
	      (arg4 (mgp-entex-get-string-arg))
	      (arg5 (mgp-entex-get-string-arg)))
;;; SYNTAX for %image is documented as below:
;;; %image "imagefile" <numcolor> <xzoomrate> <yzoomrate> <zoomflag>
;;; %image "imagefile" [<numcolor>] <screensize>
;;; but it seems the second form should actually be:
;;; %image "imagefile" [ [<numcolor>] <screensize> ]
	  (if arg4
	      (setq arg2 (and arg2 (string-to-int arg2))
		    arg3 (and arg3 (string-to-int arg3))
		    arg4 (and arg4 (string-to-int arg4))
		    arg5 (and arg5 (string-to-int arg5)))
	    (if arg3
		(setq arg2 (string-to-int arg2))
	      (setq arg3 arg2 arg2 nil)))
	  (mgp-entex-set-setting globalp defaultp index 'image
				 (list arg1 arg2 arg3 arg4 arg5))))))

(defun mgp-entex-cmd-prefix (globalp defaultp index)
  (let ((arg (mgp-entex-get-string-arg)))
    (if arg
	(mgp-entex-set-setting globalp defaultp index 'prefix arg))))

(defun mgp-entex-cmd-icon (globalp defaultp index)
  (let* ((type (mgp-entex-get-string-arg))
	 ;; Currently color and size are just ignored.
	 (color (mgp-entex-get-string-arg))
	 (size (mgp-entex-get-number-arg))
	 (str (cond ((string= type "arc") "$\\bullet$")
		    ((string= type "box") "$\\Box$")
		    ((string= type "dia") "$\\Diamond$")
		    ((string= type "delta1") "$\\bigtriangleup$")
		    ((string= type "delta2") "$\\bigtriangledown$")
		    ((string= type "delta3") "$\\triangleright$")
		    ((string= type "delta4") "$\\triangleleft$")
		    (t "$\\diamond$"))))
    (mgp-entex-set-setting globalp defaultp index 'icon str)))

(defconst mgp-entex-special-characters
  '((?# . "\\#")
    (?$ . "\\$")
    (?% . "\\%")
    (?& . "\\&")
    (?_ . "\\_")
    (?@ . "{\\makeatletter@\\makeatother}")
    (?< . "$<$")
    (?> . "$>$")
    (?~ . "\\~{ }")))

(defconst mgp-entex-space-width 4)

(defun mgp-entex-handle-string (str prefix &optional size force-indent)
  (mgp-with-output-buffer
    (let ((len (length str))
	  (indent 0)
	  i j)
      (if prefix
	  (progn
	    (if (string-match "^ +" prefix)
		(setq indent (match-end 0)
		      prefix (substring prefix indent)))))
      (if (and (not force-indent)
	       (= indent 0) (= (length prefix) 0) (= len 0))
	  (if (eq mgp-entex-linehead t)
	      (setq mgp-entex-linehead 'empty))
	(if (or prefix force-indent)
	    (insert "{"))
	(if size
	    (mgp-entex-handle-size size))
	(if (or (> indent 0) (> (length prefix) 0))
	    (insert (format "\\mgpi{%d}{%s}" indent prefix)))
	(if (string-match "^ +" str)
	    (if (< (match-end 0) len)
		(progn
		  (setq i (match-end 0) j i)
		  (insert (format "\\mgps{%d}" i)))
	      (setq i len j i))
	  (setq i 0 j 0))
	(while (< i len)
	  (let ((slot (assq (aref str i) mgp-entex-special-characters)))
	    (if slot
		(progn
		  (if (< j i)
		      (insert (substring str j i)))
		  (insert (cdr slot))
		  (setq j (1+ i)))))
	  (setq i (1+ i)))
	(if (< j len)
	    (insert (substring str j)))
	(setq mgp-entex-linehead nil)))))

(defun mgp-entex-process-page (line)
  (setq mgp-current-line line)
  (if (memq (following-char) '(?% ?#))
      (progn
	(forward-char 1)
	(if (or (eolp) (= (preceding-char) ?#) (= (following-char) ?%))
	    nil
	  (mgp-entex-parse-cmd nil 'default line)))
    (let* ((str (buffer-substring (point) (line-end-position)))
	   (plist (aref mgp-local-setting line))
	   (continued (plist-get plist 'cont)))
      (if continued
	  (plist-put plist 'cont nil)
	(mgp-with-output-buffer
	  (cond ((eq mgp-entex-linehead 'empty)
		 (insert "\\nl\n")
		 (setq mgp-entex-linehead t))
		((not mgp-entex-linehead)
		 (insert "\\nl}\n")
		 (setq mgp-entex-linehead t)))
	  (setq line (1+ line))))
      (let (func)
	(while plist
	  (setq func (intern (concat "mgp-entex-handle-"
				     (symbol-name (car plist)))))
	  (if (fboundp func)
	      (funcall func (nth 1 plist)))
	  (setq plist (nthcdr 2 plist))))
      (let ((indent 0)
	    (len (length str)))
	(or continued
	    (while (and (< indent len) (= (aref str indent) ?\t))
	      (setq indent (1+ indent))))
	(if (> indent 0)
	    (mgp-entex-handle-indent indent str)
	  (mgp-entex-handle-string str
				   (and (not continued) mgp-entex-prefix))))))
  line)

(defun mgp-entex-start-page ()
  (mgp-with-output-buffer
    (insert "\\parbox{\\hsize}{\n")
    ))

(defun mgp-entex-finish-page (line)
  (mgp-with-output-buffer
    (let ((plist (aref mgp-local-setting line))
	  func)
      (while plist
	(setq func (intern (concat "mgp-entex-handle-"
				   (symbol-name (car plist)))))
	(if (fboundp func)
	    (funcall func (nth 1 plist)))
	(setq plist (nthcdr 2 plist))))
    (cond ((eq mgp-entex-linehead 'empty)
	   (insert "\\nl\n")
	   (setq mgp-entex-linehead t))
	  ((not mgp-entex-linehead)
	   (insert "\\nl}\n")
	   (setq mgp-entex-linehead t)))
    (insert "}\n\\newpage\n")
    (setq mgp-entex-linehead t)))

(defun mgp-entex-handle-again (&rest arg)
  (mgp-with-output-buffer
    (if mgp-mark-position
	(progn
	  ;;(setq line (car mgp-mark-position))
	  (goto-char (cdr mgp-mark-position))
	  (delete-region (point) (point-max))))))

(defun mgp-entex-handle-font (arg)
  (if arg
      (mgp-with-output-buffer
	(insert "\\normalfont")
	(cond ((string-match "bold-r" arg) (insert "\\bfseries"))
	      ((string-match "bold-[oi]" arg) (insert "\\bfseries\\itshape"))
	      ((string-match "medium-[oi]" arg) (insert "\\itshape")))
	(insert " "))))

(defun mgp-entex-define-color (arg)
  (or (member arg mgp-color-list)
      (and (eq window-system 'x)
	   (let ((color-values (x-color-values arg)))
	     (insert (format "\\definecolor{%s}{rgb}{" arg))
	     (while color-values
	       (insert (format "%.3f" (/ (float (car color-values))
					 mgp-max-color-value)))
	       (setq color-values (cdr color-values))
	       (if color-values
		   (insert ",")))
	     (insert "}")
	     t))))

(defun mgp-entex-handle-back (arg)
  (or mgp-disable-color
      (mgp-with-output-buffer
	(if (string= arg "black")
	    (setq arg "gray"))
	(if (mgp-entex-define-color arg)
	    (insert (format "\\pagecolor{%s}" arg))))))

(defun mgp-entex-handle-fore (arg)
  (or mgp-disable-color
      (mgp-with-output-buffer
	(if (mgp-entex-define-color arg)
	    (insert (format "\\color{%s}" arg))))))

(defun mgp-entex-handle-size (arg)
  (mgp-with-output-buffer
    (cond ((>= arg 10) (insert "\\Huge "))
	  ((>= arg 7) (insert "\\huge "))
	  ((>= arg 6) (insert "\\LARGE "))
	  ((>= arg 5) (insert "\\Large "))
	  ((>= arg 4) (insert "\\large "))
	  ((>= arg 3) (insert "\\normalsize "))
	  (t (insert "\\tiny ")))))

(defun mgp-entex-handle-icon (arg)
  (mgp-with-output-buffer
    (insert arg)))

(defun mgp-entex-handle-indent (indent str)
  (mgp-with-output-buffer
    (let ((prefix (or (mgp-entex-get-setting nil nil indent 'prefix t) " "))
	  (size (mgp-entex-get-setting nil nil indent 'size t))
	  (icon (mgp-entex-get-setting nil nil indent 'icon t)))
      (if icon
	  (setq prefix (concat prefix icon)))
      (mgp-entex-handle-string (substring str indent) prefix size t))))

(defun mgp-entex-handle-prefix (arg)
  (setq mgp-entex-prefix arg))

(defun mgp-entex-handle-align (arg)
  (mgp-with-output-buffer
    (insert
     (cond ((eq arg 'center) "\\centering ")
	   ((eq arg 'right) "\\raggedleft ")
	   (t "\\raggedright ")))))

(defun mgp-entex-handle-vgap (arg)
  ;; Currently we don't support it.
  )

(defun mgp-entex-handle-bar (arg)
  (mgp-with-output-buffer
    (cond ((eq mgp-entex-linehead 'empty)
	   (insert "\\nl\n")
	   (setq mgp-entex-linehead t))
	  ((not mgp-entex-linehead)
	   (insert "\\nl}\n")
	   (setq mgp-entex-linehead t)))
    (insert (format "\\mgpb{%d}{%d}{%d}\n"
		    (floor (nth 1 arg))
		    (floor (nth 2 arg))
		    (floor (nth 3 arg))))))

(defun mgp-entex-handle-image (arg)
  (let ((file (car arg))
	;;(numcolor (nth 1 arg)) ; not supported
	(xzoom (nth 2 arg))
	(yzoom (nth 3 arg))
	(zoomflag (nth 4 arg))
	(screensize))
    (setq file (concat (file-name-sans-extension file) ".ps"))
    (or (file-readable-p file)
	(setq file (concat (file-name-sans-extension file) ".eps")))
    (if (file-readable-p file)
	(mgp-with-output-buffer
	  (if zoomflag
	      (if (and (= yzoom 0) (= xzoom 0))
		  (insert (format "\\includegraphics{%s}" file))
		(insert
		 (format "\\scalebox{%f}[%f]{\\includegraphics{%s}}"
			 (* (/ (float xzoom) 100) mgp-image-scale-factor)
			 (* (/ (float yzoom) 100) mgp-image-scale-factor)
			 file)))
	    (insert (format "\\resizebox{\\hsize}{!}{\\includegraphics{%s}}"
			    file)))))))

(provide 'mgp)

;; mgp.el ends here
