# Reacl Tutorial

This is the skeleton project for the
[Reacl](https://github.com/active-group/reacl)
[tutorial](https://icfp18.sigplan.org/event/icfp-2018-tutorials-purely-functional-uis-with-reacl)
at [ICFP 2018](https://icfp18.sigplan.org/home).

## Tutorial preparations

You should clone this repository, install Java JDK,
[install Leiningen](http://leiningen.org/#install), and run

```
lein build-dev
```

in this directory.  Your browser should display a web page that says
"Reacl Tutorial".  If your browser does not come up, point it to
[`http://localhost:9500/`](http://localhost:9500/).

To run the tests, go to
[`http://localhost:9500/figwheel-extra-main/tests'](http://localhost:9500/figwheel-extra-main/tests)
and open the JavaScript console in your browser.

If any of the above doesn't work as described, don't hesitate to contact me
directly.

If it works, get an editor or IDE that works with Clojure and that you
feel comfortable with
- [here](http://dev.clojure.org/display/doc/IDEs+and+Editors) is a
list.

See if you can edit `project.clj`, for example.  Note that we will
only need basic editor functionality for the tutorial, and won't spend
much time on editor support for Clojure at the tutorial.

## License

Copyright © 2018 Mike Sperber

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
