# Electric Starter App

Minimal setup for an Electric Clojure app. Plus instructions on how to integrate it into an existing app.
Be sure to check [Electric Tutorial](https://electric.hyperfiddle.net).

For real-world examples, check out [Electric Fiddle](https://github.com/hyperfiddle/electric-fiddle).

## Run it

```shell
clj -A:dev
```

At the REPL:
```clojure
(dev/-main)
```

Navigate to `http://localhost:8080`

## Customize it

With the app running:
1. Go to `src/electric_starter_app/main.cljc`
2. Edit and save
3. App will automatically update in your browser

## Release it for prod

```shell
clj -X:build:prod build-client # build it
clj -M:prod -m prod            # run it
```

### Uberjar

```shell
clj -X:build:prod uberjar :build/jar-name "target/app.jar"
java -cp target/app.jar clojure.main -m prod
```

There is also:
- an example `Dockerfile`
- `fly.io` deployment example (look at `.github/workflows/deploy.yml` and `fly.toml`)

### Integrate it in an existing clojure app

1. Look at `src-prod/prod.cljc`. It contains:
    - server entrypoint
    - client entrypoint
    - necessary configuration
2. Look at `src/electric_starter_app/server_jetty.clj`. It contains:
   - an example Jetty integration
   - required ring middlewares

You'll also need to account for the following requirements.

#### For a client to connect to the server, the two program versions must match.

Electric Clojure compiles a single program into two artifacts:
- a javascript program - runs in the browser,
- a JVM program - runs on the server,
- these two programs communicate via websocket.

If an outdated client tries to connect to the server, the server will:
- reject the connection,
- inform the client it needs to refresh.

How it works:
- `clj -X:build:prod build-client` will
  - generate js files with the version baked in
  - write the version in `resources/electric-manifest.edn`
- On server start, the server will
  - read `resources/electric-manifest.edn`
  - configure a ring middleware rejecting outdated client connections.
- On client -> server connection, the client will:
  - send it's baked in version
  - try to refresh the page if the server rejects the version.

Here are examples you might want to adapt to your existing app:
- `src/electric_starter_app/server_jetty.clj` esp. `reject-stale-client`
- `src-build/build.clj`


Notice that `src-prod/prod.clj` reads `electric-manifest.edn`.

#### Ensuring cache invalidation

If your production system doesn't already have a similar solution.

Complied js files are fingerprinted with their respective hash, to ensure a new release
properly invalidates asset caches. `index.html` is templated with the generated
js file name. The generated name comes from the `manifest.edn` file
(in `resources/public/electric_starter_app/js/manifest.edn`), produced by `clj -X:build:prod build-client`.

Notice that `src/electric_starter_app/server_jetty.clj` -> `wrap-index-page` reads `:manifest-path` from config.
The config comes from `src-prod/prod.clj`.



---
Notes

- This starter app uses Logback to log messages through `clojure.tools.logging`. Feel free to replace it by your favorite equivalent.
