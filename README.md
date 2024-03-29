# funkyweb

NOTE: funkyweb is WIP and is not production ready

funkyweb is a clojure web framework with route inference. It uses the
name of the controller and actions to generate routes and url helpers.

## Controller

Controllers are defined with the defcontroller macro and takes a name
followed by any number of actions.

    ; generates the base route /dashboard
    (defcontroller dashboard
      ... actions ...)

Controllers can also be nested using ->

    ; generates the base route /blog/posts
    (defcontroller blog->posts
      ... actions ...)

And they can contain dynamic parts

    ; generates the base route /blog/:id/posts
    (defcontroller blog->:id->posts
      ... actions ...)

### Actions

Actions are defined using the GET PUT POST or DELETE macros and
normally reside within a controller

    (defcontroller dashboard

      ; generates the route /dashboard/index
      (GET index []
        "Welcome to your dashboard))

but can be standalone as well

    ; generates the route /index
    (GET index []
      "This is the index")


#### GET

Actions defined with the GET macro responds to HTTP GET requests

    ; curl http://localhost:8080/index 
    ;  => this is the index action
    (GET index [] "this is the index action")

    ; curl http://localhost:8080/say-hello/foo 
    ;  => Hello, foo!
    (GET say-hello [name] (str "Hello, " name "!"))

#### POST

Actions defined with the POST macro responds to HTTP POST requests

    ; curl -d "" http://localhost:8080/index 
    ;  => this is the index action
    (POST index [] "this is the index action")

    ; curl -d "name=foo" http://localhost:8080/say-hello
    ;  => Hello, foo!
    (POST say-hello [name] (str "Hello, " name "!"))     

#### PUT

Actions defined with the PUT macro responds to HTTP PUT requests,
either real PUT's or fake ones with the _method hack.

    ; curl -d "_method=put" http://localhost:8080/index
    ;  => this is the index action
    (PUT index [] "this is the index action")

    ; curl -d "name=foo" -d "_method=put" http://localhost:8080/say-hello
    ;  => Hello, foo!
    (PUT say-hello [name] (str "Hello, " name "!"))

#### DELETE

Actions defined with the DELETE macro responds to HTTP DELETE requests,
either real DELETE's or fake ones with the _method hack.

    ; curl -d "_method=delete" http://localhost:8080/index 
    ;  => this is the index action
    (DELETE index [] "this is the index action")

    ; curl -d "name=foo" -d "_method=delete" http://localhost:8080/say-hello 
    ;  => Hello, foo!
    (DELETE say-hello [name] (str "Hello, " name "!"))

#### Variadic arguments

    ; curl http://localhost:8080/numbers/10
    ;  => first number: 10 more: 
    ; curl http://localhost:8080/numbers/10/20/30
    ;  => first number: 10 more: (20 30)
    (GET numbers [first-number & more]
      (str "first number: " first-number " more: " more))

#### Type hints 

By using type hints funkyweb converts the parameters for you before your action is called and will return a 404 if the parameters don't match their specified types

    (defcontroller dashboard
      
      ; curl http://localhost:8080/add/foo/2.3/4.6
      ;  => 404 - not found
      ; curl http://localhost:8080/add/2.0/2.3/4.6
      ;  => 404 - not found
      ; curl http://localhost:8080/add/2/2.3/4.6
      ;  => 8.900000190734863
      ; curl http://localhost:8080/add/2/2/4
      ;  => 8.0
      (GET add [:int a :float b :double c]
        (str (+ a b c))

#### URL Helpers

When you define an action a function of the same name and arguments is
also generated which when called returns the url for the action

    (GET show [id]
      (str "id: " id))

    (show 10) ;=> /show/10

These functions also take an optional map of query-string parameters

    (GET show [id] (str "id: " id))
    
    (show 10 {:foo "bar"}) ;=> /show/10?foo=bar


#### Rendering

What you return from an action controls what the response will look
like.

##### String

Returning a string will render a response with the status set to 200
and the content-type set to text/html

    ; {:status 200 :headers {:content-type "text/html} :body "foo"}
    (GET with-string []
       "foo")

##### Integer

Returning an integer will render a response with the status set to the
integer you returned and the content-type set to text/html

    ; {:status 404 :headers {:content-type "text/html} :body "404 - not found"}
    (GET with-int []
      404)

##### Vector

Returning a vector of status-code, content-type and body will build a
response composed of those values

    ;  {:status 200 :headers {:content-type "text/xml} :body "foo"}
    (GET with-vector []
       [200 "text/xml" "foo"]

##### Map

Returning a map gives you full control over the response

    ; {:status 200 :headers {:content-type "text/html} :body "foo"}
    (GET with-map []
      {:status 200 :headers {:content-type "text/html} :body "foo"})

### Custom error handlers

The error function let's you create custom error handlers, it takes a
status code and a body

    (error 404
      "404 - not found")

    (error 500
      "500 - internal server error")

### Helpers

The helpers namespace contains aliases to the request, cookies and
session namespaces and some convenience functions

respond-with let's your actions return different content based on the url

    ; GET /index, GET /index.html
    ;  => <h1>HTML</h1>
    ; GET /index.xml
    ;  => <response>xml</response>
    ; GET /index.json
    ;  => {"response" : "json"}
    (GET index []
      (respond-with :html "<h1>HTML</h1>"
                    :xml  "<response>xml</response>"
                    :json "{\"response\" : \"json\"}"

redirect-to takes either a string or a url generating function

    (GET say-hello [name] (str "Hello, " name "!"))

    ; Redirects the user to /say-hello/foo
    (redirect-to say-hello "foo")
    
    ; Redirects the user to http://google.se
    (redirect-to "http://google.se")

#### Request

request-get

    ; GET /index.json
    ;  => content-type is: application/json
    (GET index [] 
      (str "content-type is: " (request-get :content-type)))

query-string, qs

    ; GET /index?foo=bar
    ;  => foo = bar
    (GET index []
      (str "foo = " (query-string :foo)))

#### Flash

flash-set

    (flash-set :notice "this is a notice")

flash-get

    (flash-get :notice)

#### Cookies

cookies-set
 - takes a key and a value and any number of configuration
options and sets a cookie for the path / that expires at the end of
the session

    (cookies-set :foo "bar")
    (cookies-set :foo "bar" :expires "Fri, 31-Dec-2010 23:59:59 GMT")

cookies-get

    (cookies-get :foo)

#### Session

session-set
 - takes any number of key value pairs and associates them with the session

    (session-set :foo "bar" :baz "quux")

session-get

    (session-get :foo)

## Server

server
 - takes an adapter function, eg run-jetty, and an optional options
 hash and starts a server on port 8080 with :join? set to false

    (server run-jetty)
    
    (server run-jetty {:port 9090 :join? true})
 

## A simple example

    (ns myapp
      (:use funkyweb.controller
            ring.adapter.jetty)
      (:require [funkyweb.server  :as server ]
                [funkyweb.helpers :as helpers]))

    (defcontroller hello-world
      
      (GET say-hello [name]
        (str "Hello, " name "!"))

      (GET hello-world []
        (helpers/redirect-to say-hello "world")))


    (server/server run-jetty)

## Installation

If you are using leiningen add the following to your project.clj:

    :dependencies [[org.clojure/clojure "1.2.0"]
                   [org.clojure/clojure-contrib "1.2.0"]
                   [funkyweb "0.1.0"]]

## License

Copyright (C) 2010 Patrik Hedman

Distributed under the Eclipse Public License, the same as Clojure.
