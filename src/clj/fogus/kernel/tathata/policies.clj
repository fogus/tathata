; (c) Rich Hickey and Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns fogus.kernel.tathata.policies
  "Policies are meant to control the access to pods and optionally the
   creation, comparison, and coordination of one or more objects with.
   `Suchness`.  Objects provide this behavior via the mixed extension of
   the `Sentry`, `Coordinator`, and `Axiomatic` protocols."
  (:require [fogus.kernel.tathata.protocols :as tathata.protocols]
            [fogus.kernel.tathata.impl.general-pod :as gp]))

(def ^:private current-thread?
  #(identical? (Thread/currentThread) %))

;;
;; The `SingleThreadedRWAccess` record is a policy that subsumes
;; the behavior of Clojure's built-in transient change policy.
;; That is, when using this policy a pod can only be modified
;; in the same thread as the one contained in the policy.
;; Therefore, to emulate the change policy for transients you
;; would access a pod only in the same thread as the one that
;; was given to the `SingleThreadedRWAccess` policy at
;; construction time.  Further, you can create the policy with
;; any thread, but you'll need to ensure that any access operations
;; happen on that same thread.
;; 
(defrecord SingleThreadedRWAccess [thread]
  tathata.protocols/Sentry
  (make-pod [this val]
    (gp/->GeneralPod this val :tathata.core/無 {}))

  ;; All access must occur only within the same thread in
  ;; which the policy itself was created.
  tathata.protocols/Axiomatic
  (precept-get [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (current-thread? thread)
            "You cannot access this pod across disparate threads."))
  (precept-set [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (current-thread? thread)
            "You cannot access this pod across disparate threads."))
  (precept-render [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (current-thread? thread)
            "You cannot access this pod across disparate threads.")))

;;
;; The `ConstructOnly` record is a policy that allows a *value* to
;; be set on construction, but no further access to the ephemeral is
;; allowed.  However, this policy allows the rendering of its value.
;;
(defrecord ConstructOnly []
  tathata.protocols/Sentry
  (make-pod [this val]
    (gp/->GeneralPod this val :tathata.core/無 {}))

  tathata.protocols/Axiomatic
  (precept-get [_ _]
    (assert false "You cannot access this pod after construction."))
  (precept-set [_ _]
    (assert false "You cannot access this pod after construction."))
  (precept-render [_ _] true))


(def ^:dynamic *in-pods* nil)

;;
;; The `ThreadLockPolicy` record is a more complicated version of the
;; `SingleThreadedRWAccess` policy.  Whereas the `SingleThreadedRWAccess`
;; limits access to the same thread that the hosting pod was created.
;; However, a `ThreadLockPolicy` will enable access on any thread, as
;; long as the thread holds the policy's lock.  The details of obtaining
;; the lock is external to the policy.  
;;
(defrecord ThreadLockPolicy [lock]
  tathata.protocols/Axiomatic
  (precept-get [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (.isHeldByCurrentThread lock)
            "This pods requires guarding before access."))

  (precept-set [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same."))

  (precept-render [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (.isHeldByCurrentThread lock)
            "This pod requires guarding before rendering."))

  ;;
  ;; The `Sentry implementation is fairly straight forward in that
  ;; the pod creation is trivial and the comparison is performed
  ;; in terms of the locks themselves.  This is done to ensure that
  ;; locks are not share amongst different pods.
  ;; 
  tathata.protocols/Sentry
  (make-pod [this val]
    (gp/->GeneralPod this val :tathata.core/無 {}))
  (compare-pod [this lhs rhs]
    (assert (identical? (.policy lhs) (.policy rhs))
            "This policy does not match the LHS pod's policy.")
    (let [rlock (.lock (.policy rhs))]
      (cond (identical? lock rlock) 0
            (< (hash lock) (hash rlock)) -1
            (> (hash lock) (hash rlock)) 1
            :else (throw (IllegalStateException. (str "Duplicate lock hashes for distinct locks: "
                                                      lhs
                                                      " vs. "
                                                      rhs))))))
  tathata.protocols/Coordinator
  (guard [_ fun pod]
    (assert lock)
    (assert (nil? *in-pods*))
    (binding [*in-pods* true]
      (.lock lock)
      (try
        (fun pod)
        (finally
         (.unlock lock)))))
  (coordinate [_ fun pods]
    (assert (nil? *in-pods*))
    (let [s (java.util.TreeSet. #^java.util.Collection pods)
          unlock-all #(doseq [pod %]
                        (let [lock #^java.util.concurrent.locks.ReentrantLock (.lock (.policy pod))]
                          (when (.isHeldByCurrentThread lock) (.unlock lock))))]
      (binding [*in-pods* true]
        (try
          (doseq [pod s]
            (assert (.lock (.policy pod)))
            (.lock #^java.util.concurrent.locks.ReentrantLock (.lock (.policy pod))))
          (apply fun pods)
          (finally
           (unlock-all s)))))))

