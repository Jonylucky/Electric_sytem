;; =====================================================
;;  SPLIT_MODEL_FRAMES_GERMAN.lsp (Chuan cho CAD Duc)
;; =====================================================
(vl-load-com)

(defun c:SPLITFLOORS (/ outFolder ss i ent minPt maxPt fullPath oldFiledia filename ssIn)
  (vl-load-com)
  
  ;; Thu muc luu file cua ban
  (setq outFolder "D:\\code\\Electric_sytem\\file_ makesse_ai\\claude_autocad\\")
  
  (setq oldFiledia (getvar "FILEDIA"))
  (setvar "FILEDIA" 0)

  (princ "\n=== BAT DAU QUET KHUNG TRONG MODEL ===")
  (princ "\nHay quet chon tat ca cac khung trang bao ngoai...")
  
  ;; Su dung bo loc lay Polyline (khung hinh chu nhat cua ban)
  (setq ss (ssget '((0 . "LWPOLYLINE"))))

  (if ss
    (progn
      (setq i 0)
      (while (setq ent (ssname ss i))
        (vla-getboundingbox (vlax-ename->vla-object ent) 'minPt 'maxPt)
        (setq minPt (vlax-safearray->list minPt))
        (setq maxPt (vlax-safearray->list maxPt))
        
        (setq filename (strcat "MatBang_Tang_" (itoa (+ i 1))))
        (setq fullPath (strcat outFolder filename ".dwg"))
        
        (if (vl-file-size fullPath) (vl-file-delete fullPath))
        
        ;; Them dau "_C" de CAD Duc hieu la che do quet Crossing window
        (setq ssIn (ssget "_C" minPt maxPt))
        
        (if ssIn
          (progn
            ;; Them dau _ truoc cac lenh WBLOCK va thiet lap goc 0,0
            (command "_.-WBLOCK" fullPath "" "0,0" ssIn "")
            (princ (strcat "\n -> Da xuat file: " filename ".dwg"))
          )
        )
        (setq i (1+ i))
      )
    )
    (princ "\nKhong tim thay duong Polyline nao duoc chon!")
  )

  (setvar "FILEDIA" oldFiledia)
  (princ "\n=== HOAN TAT XUAT FILE! ===")
  (princ)
)