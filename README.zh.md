
## 📝 Содержимое README.zh.md:

markdown
# ♟️ Kletka — 跨平台国际象棋分析工具

**Kletka** 是一款跨平台国际象棋分析工具，支持 PGN 文件、变着、注释和 Stockfish 引擎。它具有现代可定制的界面，适用于 Windows、Linux 和 macOS。

## 📝 更新日志

详细更改历史请参阅 [CHANGELOG.md](CHANGELOG.md)。

---

## 🚀 功能特点

- 📁 打开、编辑和保存 PGN 文件
- 🧩 完整支持变着和注释
- 📚 **Polyglot 开局库** — 加载和浏览开局变着
- 🔍 使用 **Stockfish** (UCI 引擎) 进行局面分析
- 🎨 可定制的棋盘主题
- 🌍 多语言支持：英语、俄语、中文
- 🖥️ 跨平台：Windows、Linux、macOS

---

## 📚 开局库 (Polyglot)

Kletka 支持 **Polyglot 开局库** (`.bin` 文件)，让您可以交互式地探索和学习国际象棋开局。

### 如何使用开局库：

1. 进入 **书籍 → 加载书籍**
2. 选择 Polyglot 书籍文件 (`.bin`)
3. 使用键盘浏览变着：
    - **↑ / ↓** — 在变着之间移动
    - **→ / Enter** — 选择变着
    - **←** — 返回上一位置

### 推荐书籍：

为获得最佳效果，我们推荐使用 **`uho-pohl.bin`** 开局库，其中包含大量高质量的开局变着。

### 如何获取 Polyglot 书籍：

您可以从官方 Polyglot 书籍仓库下载免费的开局库：
🔗 **[Polyglot Books Repository](https://github.com/ChrisWhittington/polyglot-books)**

其他热门来源：
- [UHO-Pohl openings](https://www.chessdb.com/) — 高质量开局数据库
- [ChessTempo's Polyglot books](https://www.chesstempo.com/)
- 使用 Polyglot 格式创建自己的书籍

---

## 🖥️ 截图

### English
| ![主窗口](screenshots/Main_en.png) | ![PGN 浏览器](screenshots/Browser_en.png) |
|-------------------------------------|-------------------------------------------|
| *主界面*                            | *PGN 浏览器*                              |

### Русский
| ![主窗口](screenshots/Main_ru.png) | ![PGN 浏览器](screenshots/Browser_ru.png) |
|-------------------------------------|-------------------------------------------|
| *主界面*                            | *PGN 浏览器*                              |

### 中文 (Chinese)
| ![主窗口](screenshots/Main_zh.png) | ![PGN 浏览器](screenshots/Browser_zh.png) |
|-------------------------------------|-------------------------------------------|
| *主界面*                            | *PGN 浏览器*                              |

---

## 📦 安装

### Windows
从 [Releases](https://github.com/AndreyKhrypach/Kletka/releases) 页面下载 `Kletka.exe` 并运行安装程序。

### macOS
下载 `Kletka.dmg`，打开并将 `Kletka.app` 拖到 `Applications` 文件夹。

### Linux (Debian/Ubuntu)
```bash
sudo dpkg -i kletka_1.2.0-1_amd64.deb
```

---
🛠️ 从源码构建
环境要求

    Java 17 (推荐 Liberica Full JDK) — 从 BellSoft 下载

    Maven — 通过 brew install maven (macOS) 或 sudo apt install maven (Linux) 安装

```bash
git clone https://github.com/AndreyKhrypach/Kletka.git
cd Kletka
mvn clean package
```

特定平台构建

```bash
# Windows
mvn clean package -P windows

# Linux
mvn clean package -P linux

# macOS
mvn clean package -P mac
```
---

🧠 配置 Stockfish

Kletka 使用 Stockfish UCI 引擎进行分析。您需要单独安装它。
Windows

    从官方网站下载 Stockfish：https://stockfishchess.org/download/

    解压归档文件

    在 Kletka 中，进入 引擎 → 配置引擎 并选择 stockfish.exe 文件

Linux (Debian/Ubuntu)
```bash
sudo apt install stockfish
```

然后在 Kletka 中，进入 引擎 → 配置引擎 并选择 stockfish 二进制文件。

macOS
```bash
brew install stockfish
```

然后在 Kletka 中，进入 引擎 → 配置引擎 并选择 stockfish 二进制文件。

---
📄 许可证

本项目采用 GNU General Public License v3.0 许可证。
详见 LICENSE 文件。

---

👨‍💻 作者

Andrey Khrypach
GitHub

---

⭐ 支持

如果您喜欢这个项目，请在 GitHub 上给它一个星标！