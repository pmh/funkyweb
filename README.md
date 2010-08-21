# funkyweb

funkyweb is a route inferring clojure web framework. It uses the 
name of the namespace and macros to generate routes and url helpers.


NOTE: funkyweb is still in a highly experimental phase and is not 
yet production ready

## Usage

Anything you put after 'controllers' in a ns declaration that uses 
funkyweb.controller will be used as the base of the route, 
eg. 
    (ns myapp.controllers.dashboard
        (:use funkyweb.controller))

gives /dashboard as the base and

    (ns myapp.controllers.foo.bar
        (:use funkyweb.controller))

gives /foo/bar as the base.

Actions are defined by using the GET, PUT, POST or DELETE macros 
(though only GET has been implemented yet),
eg.

    (ns myapp.controllers.dashboard
        (:use funkyweb.controller))

    (GET foo [bar] "foo")

generates the route /dashboard/foo/:bar and a url helper function  
with the same name and arguments, so in the previous example it 
generated a function called foo of one argument so calling (foo "baz")
would return the url /dashboard/foo/baz.

## Example: Hello World

    (ns myapp.controllers.dashboard
        (:use [funkyweb.controller]
              [ring.adapter.jetty]))
    
    (GET say-hello [name]
         (str "Hello, " name "!"))
    
    (GET hello-foo []
         (str "<a href='" (say-hello "foo") "'>Say hello to foo</a>"))
    
    
    (future (server run-jetty {:port 8080}))
    
    
    ; http://localhost:8080/dashboard/say-hello/pmh 
      ;=> Hello, pmh!
    
    ; http://localhost:8080/dashboard/hello-foo     
      ;=> <a href='/dashboard/say-hello/foo'>Say hello to foo</a>

## Installation

FIXME: write me

## License

Copyright (C) 2010 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
