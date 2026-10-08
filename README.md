# Yu-Gi-Oh Client & Server

Team 43's Advanced Programming project, spring 2021, Sharif University of Technology. Built by **Foroozan Iraji, Amirali Moghadasi and Roya Daneshi**. This repository restores the later JavaFX/client-server phase; the separate `AP_Project_Phase1` repository contains the earlier console phase.

Features include registration/login, profile settings, card shop, deck management, marketplace, game board and card effects. Original FXML, card CSVs, images, sounds and fonts are included.

## Build and run

JDK 11+ (validated with JDK 21) and Maven. Run each command from the indicated directory so the legacy JSON storage files resolve correctly.

```bash
cd server
mvn test
mvn exec:java
```

In another terminal:

```bash
cd client
mvn package
mvn javafx:run
```

The server listens on loopback port 1227. To choose another port, start both with `-Dgame.port=1234`; the client also accepts `-Dgame.host=127.0.0.1`. Start the server before the GUI. The release starts with empty player/marketplace JSON data; create your own account in the application.

## Restoration and verification

Both Maven modules compile. Tests open independent TCP connections, register/login users, retrieve a serialized user and handle an invalid command. Shared model classes use explicit serialization identifiers; cards are serializable. The socket handshake flushes object-stream headers and each connection has separate I/O streams. Missing reward audio was replaced by an included sound. Old player data, passwords and authentication tokens are not included or logged.

This is a local course prototype. The original command handlers still contain global game state and plaintext local account storage. The server's loopback default reflects that scope. GUI interaction and complete multiplayer duels have not been tested for this release.

See [CREDITS.md](CREDITS.md) for team and asset attribution. No blanket license is applied to bundled third-party game assets.
