# ♟️ Kletka — Cross-Platform Chess Analyzer

[![Latest Release](https://img.shields.io/github/v/release/AndreyKhrypach/Kletka?label=Latest%20Release&color=success&style=flat-square)](https://github.com/AndreyKhrypach/Kletka/releases/latest)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg?style=flat-square)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey?style=flat-square)]()

## 🌐 **Website:** [https://andreykhrypach.github.io/Kletka/](https://andreykhrypach.github.io/Kletka/)

**Read this in:**
[🇬🇧 English](README.md) |
[🇷🇺 Русский](README.ru.md) |
[🇨🇳 中文](README.zh.md)

---

**Kletka** is a cross-platform chess analysis tool that supports PGN files, variations, annotations, and the Stockfish engine. It features a modern, customizable interface and is available for Windows, Linux, and macOS.

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md) for a detailed history of changes.

---

## 🚀 Features

- 📁 Open, edit, and save PGN files
- 🧩 Full support for variations and annotations
- 📚 **Polyglot opening books** — load, browse, and **edit** opening variations
- 📤 **Export game tree to Polyglot book** — save your personal repertoire
- 🔍 Position analysis with **Stockfish** (UCI engine)
- 🎨 Customizable board themes
- 📋 **Copy position** as FEN + ASCII diagram (Ctrl+Shift+P)
- 🌍 Multilingual: English, Russian, Chinese
- 🖥️ Cross-platform: Windows, Linux, macOS

---

## 📺 Video Tutorials

Step-by-step guides on our YouTube channel:

**Windows:**
- [Kletka tutorial: Windows installation](https://www.youtube.com/watch?v=OSaSA-Qm0GY)
- [Kletka tutorial: Windows — setting up Stockfish](https://www.youtube.com/watch?v=TWhIM4zsmIY)
- [Kletka tutorial: How to use Polyglot opening books](https://www.youtube.com/watch?v=QAz95CSiDy0)

**Linux:**
- [Kletka tutorial: Linux — installation](https://www.youtube.com/watch?v=NXeR6J0_rXA)
- [Kletka tutorial: Linux — setting up Stockfish](https://www.youtube.com/watch?v=m-qWoTGNIXg)

More tutorials coming soon — macOS.

---

## 📚 Opening Books (Polyglot)

Kletka supports **Polyglot opening books** (`.bin` files), allowing you to explore and study chess openings interactively — and to build your own repertoire.

### How to use opening books:

1. Go to **Books → Load Book**
2. Select a Polyglot book file (`.bin`)
3. Navigate through variations using the keyboard:
   - **↑ / ↓** — move between variations
   - **→ / Enter** — select a variation
   - **←** — go back to the previous position

### How to edit a book:

Kletka allows you to add moves directly to the loaded book:

- Play a move on the board — it's automatically added to the book
- Press **Alt+Z** — undo the last added move
- Use **Books → Save Book** (Ctrl+Shift+S) — apply changes to the `.bin` file
- Use **Books → Clear Book** (Ctrl+Shift+C) — unload the book

### Export game tree to Polyglot book:

You can export your current analysis tree as a Polyglot book:

1. Analyse or set up a position with variations
2. Go to **Books → Export to Polyglot Book...**
3. Choose a folder and filename — the book is created in `.bin` format

All moves from the tree (main line + all variations) go into the book. Transpositions and duplicates are automatically deduplicated.

The resulting book can be loaded back into Kletka or used with Stockfish, Leela, ChessBase, Scid, Arena, and other compatible software.

### System Requirements for Books

For opening book functionality, Kletka requires:

- **Java 17 LTS** (Liberica Full JDK recommended)
- JVM arguments (automatically applied when running from the launcher):

```
--add-opens java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.base/sun.misc=ALL-UNNAMED
```

**Note:** If you're running Kletka from the command line, use:
```bash
java --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/sun.misc=ALL-UNNAMED -jar Kletka.jar
```

These arguments are required for fast Polyglot book operations using memory-mapped files.

### Linux: Known Issues

On **Debian Trixie / Ubuntu 24.04+** with **Wayland**, dialogs may not receive focus
(keyboard works, mouse doesn't). This is a **known JavaFX 17 + GTK 3 + Wayland bug**.

**Solution:** already included in Kletka — the app runs with

`-Djdk.gtk.version=2`,
which uses GTK 2 via XWayland.

---

### Recommended books:

For best results, we recommend using the **`uho-pohl.bin`** opening book, which contains extensive high-quality opening variations.

### Where to get Polyglot books:

You can download free opening books from the official Polyglot books repository:
🔗 **[Polyglot Books Repository](https://github.com/ChrisWhittington/polyglot-books)**

Other popular sources:
- [UHO-Pohl openings](https://www.chessdb.com/) — high-quality opening database
- [ChessTempo's Polyglot books](https://www.chesstempo.com/)
- Create your own using the Polyglot format

---

## 📋 Copy Position (FEN + ASCII)

You can copy the current position to the clipboard in a convenient format — **FEN** plus an **ASCII diagram**:

- Menu: **Edit → Copy Position**
- Shortcut: **Ctrl+Shift+P**

This is useful for:

- Sharing positions in chats, forums, or issue trackers
- Analyzing positions with AI assistants
- Documenting positions in text files

The output includes the FEN on the first line and an ASCII diagram below it, wrapped in a Markdown code block for easy pasting.

---

## 🐛 Known Issues on macOS

### Drag-and-drop: piece is grabbed by the corner

**Symptoms:**
- A piece is visually grabbed by the corner instead of the center.
- To make a move, you need to drag the mouse to the corner of the target square.
- After dropping, the cursor may disappear until the mouse enters the menu area.

**Cause:**
Known JavaFX bug **JDK-8333919** — `dragViewOffsetX/Y` are ignored on macOS.

**Fix:**
Fixed in **JavaFX 23** (requires **JDK 21+**).
Kletka currently uses **Java 17** + **JavaFX 17**.

**Workaround:**
- Use **click-to-move** (click on a piece → click on a square) instead of drag-and-drop.
- Or wait for Kletka to migrate to **JDK 25** + **JavaFX 25** (planned in future releases).

**Status:**
Tracked in issue #XXX. Will be fixed when we migrate to the new stack.

---

## 🖥️ Screenshots

### Quick Preview

<table>
  <tr>
    <td><img src="screenshots/en/Main_en.png" width="400" alt="Main interface"></td>
    <td><img src="screenshots/en/Browser_en.png" width="400" alt="PGN browser"></td>
  </tr>
  <tr>
    <td><em>Main interface</em></td>
    <td><em>PGN browser</em></td>
  </tr>
  <tr>
    <td><img src="screenshots/en/book_open_en.png" width="400" alt="Open Polyglot Book"></td>
    <td><img src="screenshots/en/book_loaded_en.png" width="400" alt="Polyglot Book Loaded"></td>
  </tr>
  <tr>
    <td><em>Open Polyglot Book</em></td>
    <td><em>Polyglot Book Loaded</em></td>
  </tr>
</table>

---

## 🖥️ Full Size

<img src="screenshots/en/Main_en.png" alt="Main interface">

<img src="screenshots/en/Browser_en.png" alt="PGN browser">

<img src="screenshots/en/book_open_en.png" alt="Open Polyglot Book">

<img src="screenshots/en/book_loaded_en.png" alt="Polyglot Book Loaded">

---

## 📦 Installation

### Windows
Download `Kletka.exe` from the [Releases](https://github.com/AndreyKhrypach/Kletka/releases) page and run the installer.

### macOS
Download `Kletka.dmg`, open it, and drag `Kletka.app` to the `Applications` folder.

### Linux (Debian/Ubuntu)
```bash
sudo dpkg -i kletka*.deb
```

## 🛠️ Building from Source

### Prerequisites
- **Java 17 (Liberica Full JDK recommended)** — download from [BellSoft](https://bell-sw.com/pages/downloads/#/java-17-lts)
- **Maven** — install via `brew install maven` (macOS) or `sudo apt install maven` (Linux)

### Important JVM Arguments for Development
When running from your IDE, add these VM options:

```
--add-opens java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.base/sun.misc=ALL-UNNAMED
```

This ensures full Polyglot book support during development.

```bash
git clone https://github.com/AndreyKhrypach/Kletka.git
cd Kletka
mvn clean package
```

Platform-specific builds:

```bash
# Windows
mvn clean package -P windows

# Linux
mvn clean package -P linux

# macOS
mvn clean package -P mac
```

---

## 🧠 Setting up Stockfish

Kletka uses the Stockfish UCI engine for analysis. You need to install it separately:

### Windows

1. Download Stockfish from the official website: https://stockfishchess.org/download/
2. Extract the archive
3. In Kletka, go to **Engine → Configure Engine** and select the `stockfish.exe` file

### Linux (Debian/Ubuntu)

```bash
sudo apt install stockfish
```

Then in Kletka, go to **Engine → Configure Engine** and select the `stockfish` binary.

### macOS

```bash
brew install stockfish
```

Then in Kletka, go to **Engine → Configure Engine** and select the `stockfish` binary.

---

## 📄 License

This project is licensed under the GNU General Public License v3.0.
See the [LICENSE](https://github.com/AndreyKhrypach/Kletka/blob/master/LICENSE) file for details.

---

## 👨‍💻 Author

Andrey Khrypach

---

## ⭐ Support

If you like this project, please ⭐ it on GitHub!