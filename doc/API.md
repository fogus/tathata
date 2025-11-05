
-----
# <a name="fogus.kernel.tathata">fogus.kernel.tathata</a>


This is the public API for Tathata.  The API is designed to
   present the smallest possible set of functions for dealing with
   pods including:

   - An operation to mutate an ephemeral object contained in a pod.
   - An operation to fetch an ephemeral value from an object contained in a pod.
   - An operation to create a pod based on a policy.
   - An operation to coordinate fetches and mutations based on a policy.

   There's one other operation on pods not directly offered in this
   namespace.  That is, pods provide a mutable object -> value operation
   that may or may not be exposed via the `deref` protocol.  This
   exposure is left to the discretion of pod designers.  See the
   [`fogus.kernel.tathata.protocols`](#fogus.kernel.tathata.protocols) namespace for more details on
   crafting new pods.




## <a name="fogus.kernel.tathata/fetch">`fetch`</a><a name="fogus.kernel.tathata/fetch"></a>
``` clojure

(fetch op pod & args)
```
Function.

Calls a method or function `op` through the given [`pod`](#fogus.kernel.tathata/pod) with
   the supplied arguments.  This macro is intended to be used
   for operations that read the value of the object held in the
   pod and guarded / coordinated by the pod's policy.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata.clj#L37-L44">Source</a></sub></p>

## <a name="fogus.kernel.tathata/guarded">`guarded`</a><a name="fogus.kernel.tathata/guarded"></a>
``` clojure

(guarded pods & body)
```
Function.

Creates a guarded block used to coordinate one or more pods based
   on the dictates of the policy contained therein.  It's expected
   that the policy used for coordination extends the `Coordinator`
   protocol, otherwise a `AbstractMethodError` exception will be
   thrown.

   *note: This macro might be extended to throw a different error
   other than the stock `AbstractMethodError` one, which is a bit
   clunky.*
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata.clj#L57-L78">Source</a></sub></p>

## <a name="fogus.kernel.tathata/pod">`pod`</a><a name="fogus.kernel.tathata/pod"></a>
``` clojure

(pod obj policy)
```

Takes an object and a policy and returns an object having `Suchness`,
   managed by the given policy.  This functions delegates the object's
   creation out to the policy, but will perform some checks to
   ensure the veracity of the incoming object and the resulting
   instance.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata.clj#L46-L55">Source</a></sub></p>

## <a name="fogus.kernel.tathata/via">`via`</a><a name="fogus.kernel.tathata/via"></a>
``` clojure

(via op pod & args)
```
Function.

Calls a method or function `op` through the given [`pod`](#fogus.kernel.tathata/pod) with
   the supplied arguments.  This macro is intended to be used
   for operations that might mutate the object held in the
   pod and guarded / coordinated by the pod's policy.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata.clj#L26-L35">Source</a></sub></p>

-----
# <a name="fogus.kernel.tathata.impl.general-pod">fogus.kernel.tathata.impl.general-pod</a>


This namespace defines the particulars of a specific kind of
   pod that is meant to provide a capability similar to that of
   Clojure's transients.  That is, a [[`GeneralPod`](#fogus.kernel.tathata.impl.general-pod/GeneralPod)](#fogus.kernel.tathata.impl.general-pod/GeneralPod) provides the
   substrate for which to build a transient-like capability on.

   The `^:unsynchronized-mutable` is, for lack of a better term
   a pattern for creating pods.  That is, since pods are meant to
   hold an ephemeral object there should be some modecum of protection.
   That modecum is of course just to make the object and its
   representational value private properties of the pod itself. Now,
   how these 'assignables'  are set is entirely up to the disgression
   of the pod creator.
  
   The [[`GeneralPod`](#fogus.kernel.tathata.impl.general-pod/GeneralPod)](#fogus.kernel.tathata.impl.general-pod/GeneralPod) type should be considered as:
  
   1. A base-level pod capability provider
   2. A template for more complex pod implementations

   A more complex pod is implemented as `LockPod`, though much
   of what makes a pod interesting is delegated out to policies.




## <a name="fogus.kernel.tathata.impl.general-pod/->GeneralPod">`->GeneralPod`</a><a name="fogus.kernel.tathata.impl.general-pod/->GeneralPod"></a>
``` clojure

(->GeneralPod policy val ephemeron _meta)
```
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/impl/general_pod.clj#L31-L84">Source</a></sub></p>

## <a name="fogus.kernel.tathata.impl.general-pod/GeneralPod">`GeneralPod`</a><a name="fogus.kernel.tathata.impl.general-pod/GeneralPod"></a>



<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/impl/general_pod.clj#L31-L84">Source</a></sub></p>

-----
# <a name="fogus.kernel.tathata.policies">fogus.kernel.tathata.policies</a>


Policies are meant to control the access to pods and optionally the
   creation, comparison, and coordination of one or more objects with.
   `Suchness`.  Objects provide this behavior via the mixed extension of
   the `Sentry`, `Coordinator`, and `Axiomatic` protocols.




## <a name="fogus.kernel.tathata.policies/*in-pods*">`*in-pods*`</a><a name="fogus.kernel.tathata.policies/*in-pods*"></a>



<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/policies.clj#L73-L73">Source</a></sub></p>

-----
# <a name="fogus.kernel.tathata.protocols">fogus.kernel.tathata.protocols</a>


This namespace contains only the protocols used to create and
   extend pods.  All pod and policy operations will occur in
   relation to these protocols, including:

   - Turning a value into a mutable reprsentation and back again.
   - Performing checks to ensure that pod operations can be executed.
   - Sentry operations for pod creation and comparison.
   - Coordination.
   - Pod mutation, fetch, and rendering operations.




## <a name="fogus.kernel.tathata.protocols/Axiomatic">`Axiomatic`</a><a name="fogus.kernel.tathata.protocols/Axiomatic"></a>




This protocol is used to provide the minimum required behavior for a
   *policy*: the determination whether a given action is allowed on a
   pod or not.  It is meant to be called at three different stages of
   a pod's use: retrieval, setting, and rendering.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L42-L52">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/Coordinator">`Coordinator`</a><a name="fogus.kernel.tathata.protocols/Coordinator"></a>




If a pod requires special coordination to access (read or write)
   then this protocol is expected to come into play.  Additionally,
   aside from singleton access, it's conceivable that more than one
   pod will need to be coordinated as well.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L68-L87">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/Sentry">`Sentry`</a><a name="fogus.kernel.tathata.protocols/Sentry"></a>




Some pods will require guarded access or careful instantiation. In these
   cases it's expected that this protocol will be responsible for the
   careful logic around certain guarded tasks.  Very often the policies
   will take on the role of the [`Sentry`](#fogus.kernel.tathata.protocols/Sentry) but that is not a requirement.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L54-L66">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/Suchness">`Suchness`</a><a name="fogus.kernel.tathata.protocols/Suchness"></a>




The [`Suchness`](#fogus.kernel.tathata.protocols/Suchness) protocol represents the fine-grained access logic
   along the get, set, and rendering logics.  These functions are
   meant to operate orthogonally, but are expected to leave the
   pod in a stable state upon completion.  The *ephemeron* is the
   object that is contained in the pod and that should not be
   accessed except through the pod itself or the supporting macros.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L89-L116">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/ToMutable">`ToMutable`</a><a name="fogus.kernel.tathata.protocols/ToMutable"></a>




This protocol used to extend a value type such that by calling the
   function [`value->mutable`](#fogus.kernel.tathata.protocols/value->mutable) a mutable version of the
   object is returned.  A good example for this protocol is to extend
   Java's `String` type to [`ToMutable`](#fogus.kernel.tathata.protocols/ToMutable) whereby a `StringBuffer` or
   perhaps a `StringBuilder` instance is returned.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L21-L27">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/ToValue">`ToValue`</a><a name="fogus.kernel.tathata.protocols/ToValue"></a>




This protocol is the dual of the [`ToMutable`](#fogus.kernel.tathata.protocols/ToMutable) protocol.  It's meant to
   extend a mutable type (including Clojure's transients) such that by calling the
   [`mutable->value`](#fogus.kernel.tathata.protocols/mutable->value) function a value type is returned.  A good example for
   this protocol is to extend the `StringBuffer` type to [`ToValue`](#fogus.kernel.tathata.protocols/ToValue)
   whereby a `String` instance is returned.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L29-L40">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/compare-pod">`compare-pod`</a><a name="fogus.kernel.tathata.protocols/compare-pod"></a>
``` clojure

(compare-pod sentry lpod rpod)
```

Tasked with comparing two pods for equality. The `sentry` type is
    responsible for the entire equality semantics including, but not
    limited to the pod types, value types, and policy types.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L63-L66">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/coordinate">`coordinate`</a><a name="fogus.kernel.tathata.protocols/coordinate"></a>
``` clojure

(coordinate sentry fun pods)
```

This function is meant to coordinate the access of more than
    one pod. The [[`coordinate`](#fogus.kernel.tathata.protocols/coordinate)](#fogus.kernel.tathata.protocols/coordinate) implementation will receive a function
    `fun` and a sequence of `pods`.  The pods in the sequence should
    be given as arguments to the given function. It's left to the
    specific implementations of [[`coordinate`](#fogus.kernel.tathata.protocols/coordinate)](#fogus.kernel.tathata.protocols/coordinate) to define if the `pods`
    given are mutually compatible.  Likewise, the implementation may
    choose to build on the [`guard`](#fogus.kernel.tathata.protocols/guard) functionality, but that is not a
    requirement.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L79-L87">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/get-ephemeron">`get-ephemeron`</a><a name="fogus.kernel.tathata.protocols/get-ephemeron"></a>
``` clojure

(get-ephemeron pod)
```

Given a `pod`, this function is expected to return the mutable
    representation of its stored object, the ephemeron. The argument
    to this function is expected to be valid according to the
    instance's get precept as defined by the pod's policy, where
    appropriate.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L96-L101">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/guard">`guard`</a><a name="fogus.kernel.tathata.protocols/guard"></a>
``` clojure

(guard sentry fun pod)
```

This function is meant to encapsulate the access logic for a
    single pod. It's conceivable that the [[`guard`](#fogus.kernel.tathata.protocols/guard)](#fogus.kernel.tathata.protocols/guard) logic might be
    constituent to the coordination logic around multiple pods,
    but this is not a requirement. [[`guard`](#fogus.kernel.tathata.protocols/guard)](#fogus.kernel.tathata.protocols/guard) will receive a function
    `fun` that is meant to receive the `pod` as its only argument.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L73-L78">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/make-pod">`make-pod`</a><a name="fogus.kernel.tathata.protocols/make-pod"></a>
``` clojure

(make-pod sentry val)
(make-pod sentry val mutable)
```

This function is tasked with building a pod based on the sentry
    type and the value given. The type of the pod returned is dependent
    on the dictates of the `sentry` type.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L59-L62">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/mutable->value">`mutable->value`</a><a name="fogus.kernel.tathata.protocols/mutable->value"></a>
``` clojure

(mutable->value mutable)
(mutable->value mutable sentry)
```

The `[mutable]` form of this function is expected to take a
   mutable object (including Clojure's transients) and
   return a representational value of it.  The function taking
   a second argument is expected to receive a [`Sentry`](#fogus.kernel.tathata.protocols/Sentry) instance that
   can safely guide the conversion of the mutable into a value.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L35-L40">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/mutant?">`mutant?`</a><a name="fogus.kernel.tathata.protocols/mutant?"></a>
``` clojure

(mutant? pod)
```

Returns `true` or `false` depending if the object in the
    pod has been mutated through the pod.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L114-L116">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/precept-get">`precept-get`</a><a name="fogus.kernel.tathata.protocols/precept-get"></a>
``` clojure

(precept-get this pod)
```

Given a pod, determine if a value retrieval is allowed.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L47-L48">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/precept-render">`precept-render`</a><a name="fogus.kernel.tathata.protocols/precept-render"></a>
``` clojure

(precept-render this pod)
```

Given a pod, determine if a snapshot can be built.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L51-L52">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/precept-set">`precept-set`</a><a name="fogus.kernel.tathata.protocols/precept-set"></a>
``` clojure

(precept-set this pod)
```

Given a pod, determine if assignment is allowed
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L49-L50">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/render">`render`</a><a name="fogus.kernel.tathata.protocols/render"></a>
``` clojure

(render pod)
```

Given a `pod`, the [`render`](#fogus.kernel.tathata.protocols/render) function is expected to produce a
    representational value of the contained ephemeron. The rendering
    is subject to the restrictions dictated by the render precept, where
    appropriate.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L109-L113">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/set-ephemeron">`set-ephemeron`</a><a name="fogus.kernel.tathata.protocols/set-ephemeron"></a>
``` clojure

(set-ephemeron pod mutable)
```

Given a `pod` and an object, this function is expected to set the
    mutable version of its stored object, the ephemeron. Though the object
    given is likely to be an actual mutable object, that is not required.
    Indeed, the object given could be another pod.  In any case, the argument
    to this function are expected to be valid according to the instance's
    put precept as defined by the pod's policy, where appropriate.
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L102-L108">Source</a></sub></p>

## <a name="fogus.kernel.tathata.protocols/value->mutable">`value->mutable`</a><a name="fogus.kernel.tathata.protocols/value->mutable"></a>
``` clojure

(value->mutable value)
(value->mutable value this)
```
<p><sub><a href="https://github.com/fogus/tathata/blob/master/src/clj/fogus/kernel/tathata/protocols.clj#L27-L27">Source</a></sub></p>
