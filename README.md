# Vaadin Quarkus extension on Quarkus 4

Reproducer to check if the Vaadin Quarkus extension works with Quarkus 4.

- Vaadin 25.4.0-beta3 (Flow / `vaadin-quarkus` 25.4.0-beta2)
- Quarkus 4.0.1
- Based on `vaadin/base-starter-flow-quarkus` (branch `v25`)

Changes from the starter:

- `pom.xml`: Vaadin 25.4.0-beta3, Quarkus 4.0.1.
- `AppConfig`: `@Push` (WebSocket push).
- `PushView` (`/push`): a background thread updates a counter every second,
  to check server push over the Undertow WebSocket integration.
- `MainView` (`/`): unchanged; it uses an injected CDI bean (`GreetService`).

## What to check

On every mode:

1. Open http://localhost:8080/, type a name, click "Say hello".
   A "Hello <name>" paragraph appears.
2. Open http://localhost:8080/push. The counter goes up every second and the
   transport is `WEBSOCKET_XHR`. In the browser dev tools, the Network tab
   shows an open WebSocket to `/VAADIN/push`.
3. No errors in the browser console or in the server log.

## Dev mode

```
mvn quarkus:dev
```

Also with the Vite dev server instead of the prebuilt dev bundle:

```
mvn quarkus:dev -Dvaadin.frontend.hotdeploy=true
```

Hot reload: change the text in `GreetService` while dev mode runs, then click
"Say hello" again. Quarkus restarts and shows the new text.

## Production build (JVM)

```
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

## Native image

Quarkus 4.0.1 needs GraalVM / Mandrel **25.0.4** or newer. With 25.0.2 the
build stops with:

```
Out of date version of GraalVM or Mandrel detected: 25.0.2.0. Quarkus currently supports 25.0.4.
```

```
mvn clean package -Pnative
./target/base-starter-flow-quarkus-1.0-SNAPSHOT-runner
```

## Results (2026-11-27)

Tested with JDK 21 (Temurin 21.0.12) for JVM modes and Mandrel 25.0.4.1 for
native, all checks run in headless Chromium.

| Mode                          | Build | CDI greet | Push over WebSocket | Errors |
|-------------------------------|-------|-----------|---------------------|--------|
| Dev mode (prebuilt dev bundle)| OK    | OK        | OK                  | none   |
| Dev mode, hot reload          | OK    | OK (new text after restart) | OK | none |
| Dev mode with Vite            | OK    | OK        | OK                  | none (see note) |
| Production JVM                | OK    | OK        | OK                  | none   |
| Native (Mandrel 25.0.4.1)     | OK (2m 52s, no extra config) | OK | OK  | none   |

Native app starts in 0.020s.

Note: in dev mode with Vite, Vert.x logged once
`Thread vert.x-eventloop-thread-1 has been blocked for 2880 ms`. The stack
shows the Push WebSocket `onOpen` (debug window connection, Copilot session
setup loading classes) running on the event loop.
`quarkus.websocket.dispatch-to-worker=true` covers frames, not `onOpen`.
This is not new in Quarkus 4: the handshake code is the same in
quarkus-http 5.5.0 (Quarkus 3.40) and 6.0.0.Alpha4 (Quarkus 4.0.1).
