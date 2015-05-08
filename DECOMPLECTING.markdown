# On Decomplecting

A technique that I've found useful in my programming career is the application of lenses.  To explain what this means allow me to use [Clojure](http://www.clojure.org) as an example.

![clojure-logo](https://farm9.staticflickr.com/8638/16548618428_8a14452fc2_o_d.png)

That is, while I've found Clojure a highly useful tool in building work and hobby projects, I think more than anything else I've found its principles more important.  In other words, while Clojure is a wonderful tool in its own right, it's a much better "lens" for viewing computing:[^others]

![clj2comp](https://farm9.staticflickr.com/8566/16734968101_172cf52eef_d.jpg)

Or for viewiing systems architecture:

![arch](https://farm9.staticflickr.com/8636/16710288236_1f4eb6d949_o_d.png)

Or maybe even databases, a very good example being [Datomic](http://www.datomic.com):

![d](https://farm9.staticflickr.com/8590/16116224773_7328432de5_o_d.png)

But what happens when that lens is truned onto Clojure itself?

![cljlenscljs](https://farm9.staticflickr.com/8639/16736176715_4ce2116a32_o_d.png)

One recent example of this self-reflection was the development of Clojure's [Transducers](http://blog.cognitect.com/blog/2014/8/6/transducers-are-coming) capability.  In this post I'll talk a little about a "[code painting](http://blog.fogus.me/2015/02/16/code-painting/)" that I've been working on that implements a variant of something called [Pods](http://www.infoq.com/interviews/hickey-clojure-protocols) that for a time were being considered for inclusion into the core language.  But first, I want to spend a little time talking about [transients](http://clojure.org/transients), a feature of Clojure that motivated the [original development of Pods](https://gist.github.com/richhickey/306174).  I'll also talk a little about "decomplecting" which I'd like to think is a fancy word describing a sub-category of a larger topic that we might call "functional refactoring."  I'll not spend a lot of time on that topic, but I'll hit relevant points along the way.

[^others]: Clojure is not the only useful viewing lens of course as languages and systems like Haskell, Racket, CLIPS, UNIX, and many others have served me well too.

## Focusing the Clojure lens onto Clojure transients

A few years ago I wrote [a post about Clojure's support for mutation](http://blog.fogus.me/2011/07/12/no-stinking-mutants/).  However, left out of that post was Clojure's [transients](http://clojure.org/transients) which when used according to spec do not look like mutation as it typically manifests.  

Clojure provides a feature called [transients](http://clojure.org/transients) that provide a way to perform (potentially) faster data structure operations by using underlying mutable strategies that assume that the mutation occurs in a single thread only. The interface for transient manipulation looks very similar to that of the normal structure functions, as seen in the `zencat` function below:

<pre class="prettyprint lang-clj">
(defn zencat [x y] 
  (loop [src y, ret (transient x)]
    (if (seq src) 
      (recur (next src) 
             (conj! ret (first src)))
      (persistent! ret))))
    
(zencat [1 2 3] [4 5 6])
;;=> [1 2 3 4 5 6]
</pre>

That is, the `zencat` function builds a transient return vector that is the concatenation of two vectors `x` and `y` provided as arguments.  This implementation is pretty close to what you might create using Clojure's standard fare, except that the use of transients requires explicit wrap, special operators, and unwrap steps via `transient`, `conj!`, and `persistent!` respectively.  The use of transients can uncover nice performance gains because of an inherent limitation in their implementation.  That is, transients utilize mutation to great effect and guarantee safety by isolating all access of the transient to the thread in which the transient was created.

While extremely useful, Clojure's transients weave together three separate concerns:

 - Mutation
 - Access policy
 - Coordination

In the case of Clojure transients, mutation is effectively an implementation detail.  By design, transients have the single-threaded access policy baked in.  Indeed, the very definition of a transient is such that the policy is part of the definition.  Likewise, the matter of coordinated access to transients is dictated by its single-threaded nature.

