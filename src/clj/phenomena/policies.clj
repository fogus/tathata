;   Copyright (c) Rich Hickey and Michael Fogus. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns phenomena.policies
  "Policies are meant to control the access to pods and optionally the
   creation, comparison, and coordination of one or more pods. Pods
   provide this behavior via the mixed extension of the `Sentry`,
   `Coordinator`, and `Axiomatic` protocols."
  (:require phenomena.protocols
            [phenomena.impl.thread-pod :as tc]
            [phenomena.impl.lock-pod   :as lp]))

(def ^:private current-thread?
  #(identical? (Thread/currentThread) %))

;;
;; The `SingleThreadedRWAccess` record is a policy that subsumes
;; the behavior of Clojure's built-in Transient change policy.
;; That is, when using this policy a pod can only be modified
;; in the same thread as the one contained in the policy.
;; Therefore, to emulate the change policy for Transients you
;; would access a pod only in the same thread as the one that
;; was given to the `SingleThreadedRWAccess` policy at
;; construction time.  Further, you can create the policy with
;; any thread, but you'll need to ensure that any access operations
;; happen on that same thread.
;; 
(defrecord SingleThreadedRWAccess [thread]
  phenomena.protocols/Sentry
  (make-pod [this val]
    (tc/->ThreadPod this val :phenomena.core/nothing {}))

  ;; All access must occur only within the same thread in
  ;; which the policy itself was created.
  phenomena.protocols/Axiomatic
  (precept-get [_ _]
    (assert (current-thread? thread)
            "You cannot access this pod across disparate threads."))
  (precept-set [_ _]
    (assert (current-thread? thread)
            "You cannot access this pod across disparate threads."))
  (precept-render [_ _]
    (assert (current-thread? thread)
            "You cannot access this pod across disparate threads.")))

;;
;; The `ConstructOnly` record is a policy that allows a *value* to
;; be set on construction, but no further set or get access is
;; allowed.  However, this policy allows the rendering of the
;; contained object.
;;
(defrecord ConstructOnly []
  phenomena.protocols/Sentry
  (make-pod [this val]
    (tc/->ThreadPod this val :phenomena.core/nothing {}))

  phenomena.protocols/Axiomatic
  (precept-get [_ _]
    (assert false "You cannot access this pod after construction."))
  (precept-set [_ _]
    (assert false "You cannot access this pod after construction."))
  (precept-render [_ _] true))


(def ^:dynamic *in-pods* nil)

(defrecord ThreadLockPolicy [lock]
  phenomena.protocols/Axiomatic
  (precept-get [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (.isHeldByCurrentThread lock)
            "This pods requires guarding before access."))
  (precept-set [_ _] true)
  (precept-render [this pod]
    (assert (identical? this (.policy pod))
            "Policies are not the same.")
    (assert (.isHeldByCurrentThread lock)
            "This pod requires guarding before rendering."))

  phenomena.protocols/Sentry
  (make-pod [this val]
    (lp/->LockPod this val :phenomena.core/nothing {}))
  (compare-pod [this lhs rhs]
    (assert (identical? this (:policy lhs))
            "This policy does not match the LHS pod's policy.")
    (let [rlock (.lock (:policy rhs))]
      (cond (identical? lock rlock) 0
            (< (hash lock) (hash rlock)) -1
            (> (hash lock) (hash rlock)) 1
            :else (throw (IllegalStateException. (str "Duplicate lock hashes for distinct locks: "
                                                      lhs
                                                      " vs. "
                                                      rhs))))))

  phenomena.protocols/Coordinator
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
                        (let [lock #^java.util.concurrent.locks.ReentrantLock (:lock pod)]
                          (when (.isHeldByCurrentThread lock) (.unlock lock))))]
      (binding [*in-pods* true]
        (try
          (doseq [pod s]
            (assert (:lock pod))
            (.lock #^java.util.concurrent.locks.ReentrantLock (:lock pod)))
          (apply fun pods)
          (finally
           (unlock-all s)))))))

