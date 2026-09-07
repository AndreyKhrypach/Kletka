# ♟️ Kletka — Кроссплатформенный шахматный анализатор

**Kletka** — это кроссплатформенный шахматный анализатор с поддержкой PGN файлов, вариантов, аннотаций и движка Stockfish. Программа имеет современный настраиваемый интерфейс и доступна для Windows, Linux и macOS.

## 📝 Журнал изменений

Подробную историю изменений смотрите в [CHANGELOG.md](CHANGELOG.md).

---

## 🚀 Возможности

- 📁 Открытие, редактирование и сохранение PGN файлов
- 🧩 Полная поддержка вариантов и аннотаций
- 📚 **Дебютные книги Polyglot** — загрузка и просмотр дебютных вариантов
- 🔍 Анализ позиции с **Stockfish** (UCI движок)
- 🎨 Настраиваемые темы доски
- 🌍 Мультиязычность: Русский, English, 中文
- 🖥️ Кроссплатформенность: Windows, Linux, macOS

---

## 📚 Дебютные книги (Polyglot)

Kletka поддерживает **дебютные книги Polyglot** (файлы `.bin`), что позволяет вам интерактивно изучать шахматные дебюты.

### Как использовать дебютные книги:

1. Перейдите в меню **Книги → Загрузить книгу**
2. Выберите файл Polyglot книги (`.bin`)
3. Навигация по вариантам с помощью клавиатуры:
    - **↑ / ↓** — перемещение между вариантами
    - **→ / Enter** — выбор варианта
    - **←** — возврат на предыдущую позицию

### Рекомендуемые книги:

Для наилучших результатов мы рекомендуем использовать книгу **`uho-pohl.bin`**, которая содержит обширные качественные дебютные варианты.

### Где взять Polyglot книги:

Вы можете скачать бесплатные дебютные книги из официального репозитория Polyglot:
🔗 **[Polyglot Books Repository](https://github.com/ChrisWhittington/polyglot-books)**

Другие популярные источники:
- [UHO-Pohl openings](https://www.chessdb.com/) — качественная база дебютов
- [ChessTempo's Polyglot books](https://www.chesstempo.com/)
- Создайте свою собственную книгу в формате Polyglot

---

## 🖥️ Скриншоты

### English
| ![Главное окно](screenshots/Main_en.png) | ![Обозреватель PGN](screenshots/Browser_en.png) |
|-------------------------------------------|--------------------------------------------------|
| *Главный интерфейс*                       | *Обозреватель PGN*                               |

### Русский
| ![Главное окно](screenshots/Main_ru.png) | ![Обозреватель PGN](screenshots/Browser_ru.png) |
|-------------------------------------------|--------------------------------------------------|
| *Главный интерфейс*                       | *Обозреватель PGN*                               |

### 中文 (Chinese)
| ![Главное окно](screenshots/Main_zh.png) | ![Обозреватель PGN](screenshots/Browser_zh.png) |
|-------------------------------------------|--------------------------------------------------|
| *Главный интерфейс*                       | *Обозреватель PGN*                               |

---

## 📦 Установка

### Windows
Скачайте `Kletka.exe` со страницы [Releases](https://github.com/AndreyKhrypach/Kletka/releases) и запустите установщик.

### macOS
Скачайте `Kletka.dmg`, откройте его и перетащите `Kletka.app` в папку `Applications`.

### Linux (Debian/Ubuntu)
```bash
sudo dpkg -i kletka_1.2.0-1_amd64.deb
```
---

🛠️ Сборка из исходников

Требования

    Java 17 (рекомендуется Liberica Full JDK) — скачайте с BellSoft

    Maven — установите через brew install maven (macOS) или sudo apt install maven (Linux)
---

```bash
git clone https://github.com/AndreyKhrypach/Kletka.git
cd Kletka
mvn clean package
```
Сборка для конкретной платформы

```bash

# Windows
mvn clean package -P windows

# Linux
mvn clean package -P linux

# macOS
mvn clean package -P mac
```

🧠 Настройка Stockfish

Kletka использует движок Stockfish для анализа. Вам нужно установить его отдельно.
Windows

    Скачайте Stockfish с официального сайта: https://stockfishchess.org/download/

    Распакуйте архив

    В Kletka перейдите в Движок → Настроить движок и выберите файл stockfish.exe

Linux (Debian/Ubuntu)
```bash
sudo apt install stockfish
```

Затем в Kletka перейдите в Движок → Настроить движок и выберите бинарный файл stockfish.

macOS
```bash

brew install stockfish
```

Затем в Kletka перейдите в Движок → Настроить движок и выберите бинарный файл stockfish.

---

📄 Лицензия

Этот проект распространяется под лицензией GNU General Public License v3.0.
Подробнее см. файл LICENSE.

---

👨‍💻 Автор

Andrey Khrypach

GitHub

---

⭐ Поддержка

Если вам нравится проект, поставьте ⭐ на GitHub!