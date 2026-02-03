Tathata
=======

> One day Soshi was walking on the bank of a river with a friend. 
> "How delightfully the fishes are enjoying themselves in the water!"
> exclaimed Soshi. His friend spake to him thus: "You are not a fish;
> how do you know that the fishes are enjoying themselves?" 
> "You are not myself," returned Soshi; "how do you know that I do 
> not know that the fishes are enjoying themselves?"
> 
> -- Okakura Kakuzo in "The Book of Tea"

Pods are a Clojure reference type used to store transient objects.  The implementation herein decomplects the transient/value dual from access policy and also from coordination.

API documentation is found at [doc/API.md](doc/API.md) and is ever-evolving.

**NOTE: This project is very much a moving target, so you should expect that the API will change from version to version until v1.0.0 is released.**

## Using as a gitdep

Currently, Tathata is available only as a deps.edn git dependency:

    ;; explicit dep
    me.fogus/tathata {:git/url "https://github.com/fogus/tathata.git"
                      :git/sha "4e840eaf66afb8938737d665f6428a376681e2be"}

or

    ;; dep via git tag
	io.github.fogus/tathata {:git/tag "v0.2.5"
                             :git/sha "4e840ea"}

## Examples

See the [tests](https://github.com/fogus/tathata/tree/master/test/tathata/test) and [examples](https://github.com/fogus/tathata/tree/master/examples/) for the kinds of problems this library is meant to solve.

## Dev

To run the tests:

    clj -X:dev:test

To generate the current API docs run the following:

    clj -Tquickdoc quickdoc '{:outfile "doc/API.md", :github/repo "https://github.com/fogus/tathata", :git/branch "master", :toc false}'

The above requires that you install quickdocs as a CLI tool first.

License
-------

Copyright (c) Rich Hickey and Michael Fogus. All rights reserved.
The use and distribution terms for this software are covered by the
[Eclipse Public License 1.0](http://www.eclipse.org/legal/epl-v10.html)
which can be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.
You must not remove this notice, or any other, from this software.

