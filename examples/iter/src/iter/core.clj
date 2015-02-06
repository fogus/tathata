(ns iter.core
  (:require [phenomena.protocols :as pods]
            [phenomena.core :refer (via fetch pod)]))


(defprotocol Iter
  (has-item [iter])
  (item [iter])
  (move! [iter]))

(definterface IterSeqImpl
  (mkseq []))