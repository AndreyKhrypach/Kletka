# Журнал изменений

Все значимые изменения в проекте будут документированы в этом файле.

Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.0.0/),
и этот проект следует [Семантическому Версионированию](https://semver.org/lang/ru/spec/v2.0.0.html).

## [1.6.0] - 2026-10-07

### Добавлено

- **Сохранение позиции в PGN-файл** — вырезание интересного момента из партии (например, позиции перед матовой комбинацией) в отдельный файл с задачами. Меню **Файл → Сохранить позицию как PGN...**, горячая клавиша **Ctrl+Shift+E**. Позиция сохраняется с тегами `[SetUp "1"]` и `[FEN "..."]`, включая метаданные: игроков, событие, дату, ECO и дебют. Если файл уже существует — позиция добавляется в конец, не перезаписывая содержимое. Файл совместим с ChessBase, lichess, Scid и любым шахматным софтом

- **Отчёт о проблеме (Feedback)** — встроенная форма обратной связи. Меню **Помощь → Сообщить о проблеме...**. Пользователь выбирает тип проблемы (Bug / Feature / Performance / UI / Documentation / Other), описывает её и нажимает **«Перейти на GitHub»**. Открывается форма новой issue на GitHub с предзаполненным заголовком, телом и меткой. Автоматически добавляется информация о системе (версия Kletka, ОС, Java, движок) — с возможностью отключить. Никакие персональные данные не собираются и не отправляются. Также доступна кнопка **«Скопировать в буфер»** для ручной отправки

- **Информация о Stockfish в диалоге «О программе»** — теперь показывается установленная версия движка (например, «Stockfish 16.1»). Если движок не установлен — выводится сообщение «Stockfish не установлен»

- **Поддержка RPM-дистрибутивов Linux** — новый профиль сборки `-P linux-rpm`, создающий `.rpm` пакет для Fedora, RHEL, CentOS, Rocky Linux, AlmaLinux, openSUSE, Amazon Linux и Oracle Linux. Собирается через `jpackage` (требует установленного `rpm-build`)

- **Горячая клавиша Ctrl+Shift+E** — быстрый доступ к сохранению позиции из партии

### Исправлено

- **Взятие на проходе (en passant)** — теперь корректно записывается в нотации как взятие, а не как обычный ход пешки

- **Отображение вложенных вариантов в нотации** — подварианты (например, `3... Nf6 4. O-O`) больше не пропадают при добавлении новых веток на предыдущих ходах

- **Отображение дерева вариантов** — вложенность вариантов теперь корректно отображается в зависимости от реального уровня вложенности. Ранее подварианты главной линии (например, `1... c5` под `1. e4`) визуально отображались как вложенные в корневые варианты (например, `1. d4 ...`), создавая ложное впечатление иерархии

- **Логика добавления вариантов унифицирована** — ходы, сделанные пользователем и движком Stockfish, теперь добавляются в дерево одним и тем же кодом. Ранее ходы движка создавали вариант «напрямую», что приводило к потере связей в дереве (вариант появлялся в дереве, но не отображался в нотации). Теперь диалог выбора варианта появляется всегда, когда ход не совпадает с существующими

- **Проверка несохранённых изменений перед открытием файла** — при нажатии «Открыть PGN» диалог «Сохранить / Не сохранять / Отмена» появляется **до** выбора файла, а не после. Ранее файл открывался, а затем всплывал диалог, что создавало дискомфорт

- **Двойной диалог сохранения** — устранён баг, при котором при открытии файла диалог «Сохранить?» появлялся дважды. Причина — некорректная синхронизация хешей при выборе «Не сохранять»

- **Кружок в тренерских инструментах** — теперь корректно сбрасывается при закрытии панели тренера (ранее оставался активным, в отличие от крестика)

- **Тренерские инструменты при изменении размера доски** — автоматически закрываются при изменении масштаба, что устраняет смещение маркеров

### Изменено

- **Логика обхода дерева вариантов разделена** — отдельные методы обхода для отображения нотации (HTML-визитер) и для PGN-экспорта. Это позволяет независимо оптимизировать отображение и корректный PGN-вывод без компромиссов

- **Bitcoin-адрес для доната** — обновлён на бессрочный (non-expiring)

- **Русские имена в интерфейсе** — заменены все упоминания «Kletka» на «Клетка» в русской версии (диалоги «О программе», «Донат»)

- **Список горячих клавиш дополнен** — добавлен Ctrl+Shift+E (сохранение позиции в PGN)

### Известные проблемы

- **Fedora 41+ (Wayland): фокус в диалогах** — в Fedora 41 и последующих версиях GNOME по умолчанию использует Wayland и удалил сессию X11. JavaFX в среде Wayland может не получать фокус при отображении модальных диалоговых окон (например, окна выбора варианта) из-за ограничений **Focus Stealing Prevention** в GNOME.

  **Временное решение:** при появлении диалогового окна нажать **Tab** — фокус переведётся и дальше всё будет работать корректно.

  **Альтернатива:** использовать дистрибутив Linux с поддержкой X11-сессии (Kubuntu, Linux Mint и др.) или сессию Xorg, если она доступна.

  Это известный баг JavaFX, отслеживается в [JDK-8353643](https://bugs.openjdk.org/browse/JDK-8353643).

## [1.5.0] - 2026-09-23

### Добавлено

- **Экспорт дерева вариантов в Polyglot книгу** — создание личного дебютного репертуара из проанализированных вариантов. Меню Книги → Экспорт в Polyglot книгу.... Все ходы из дерева (главная линия + все варианты) попадают в книгу, транспозиции и дубликаты автоматически дедуплицируются. Полученную книгу можно загрузить обратно в Клетку или использовать в Stockfish, Leela, ChessBase, Scid, Arena и других программах

- **Копирование позиции (FEN + ASCII)** — текущая позиция копируется в буфер обмена в удобном формате: FEN в первой строке и ASCII-диаграмма ниже, обёрнутая в Markdown-блок кода. Меню Правка → Копировать позицию, горячая клавиша Ctrl+Shift+P. Удобно для обмена позициями в чатах, на форумах и для анализа с ИИ-ассистентами

- **Пустой кружок в инструментах тренера** — новый тип маркера (третий, наряду со стрелками и крестиками). Полезно для комментирования важных клеток без «зачёркивания» фигуры

- **Кликабельные ссылки в диалоге «О программе»** — ссылки на веб-сайт проекта, GitHub-репозиторий и текст лицензии GPL v3 открываются в системном браузере

- **Глобальные горячие клавиши** для всех ключевых действий: Ctrl+B (открыть PGN браузер), Ctrl+R (обновить браузер), Ctrl+Shift+P (копировать позицию), Ctrl+Shift+V (импорт из буфера обмена), Ctrl+E (экспорт партии), Ctrl+H (горячие клавиши), Ctrl+A (анализ движком). Раньше эти комбинации работали только при открытом меню

- **CI/CD**: GitHub Actions workflow для сборки установщиков

- **Windows**: Liberica JDK 17 Full + WiX Toolset, сборка .exe установщика с поддержкой апгрейда

- **Linux**: сборка .deb для Debian и Ubuntu через Docker с Liberica Full

- **macOS**: сборка .dmg

- **Артефакты и публикация релизов**

### Исправлено

- **Выгрузка Polyglot книги больше не показывает ложный диалог сохранения партии** — раньше при выгрузке уже сохранённой книги появлялся диалог «Сохранить партию?», хотя партия не менялась. Теперь выгрузка книги работает корректно и не затрагивает логику партии

- **Сохранение книги перед выгрузкой** — при выгрузке книги с несохранёнными изменениями показывается диалог «Сохранить / Не сохранять / Отмена»

- **Единое поведение проверки несохранённых изменений книги** — Ctrl+N (новая партия), закрытие программы и выгрузка книги теперь используют одну логику. Пользователь никогда не потеряет несохранённые ходы в книге случайно

- **Удалён глобальный кэш ходов для Polyglot книги** — варианты теперь корректно подтягиваются из книги для позиций, достигнутых разными путями ходов (транспозиции). Раньше второй путь к той же позиции ошибочно считался «уже загруженным»

- **Инструменты тренера при перевороте доски и скрытии координат** — инструменты тренера теперь корректно закрываются до изменения доски, что устраняет смещение маркеров

- **Экспорт дерева в Polyglot корректно обрабатывает развилки** — устранён баг, при котором продолжение главной линии, хранящееся в подвариантах развилки, экспортировалось дважды

- **ConcurrentModificationException при открытии меню** — исправлен краш при повторном использовании одного и того же SeparatorMenuItem в контекстном меню

### Изменено

- **Реорганизация меню**:

    - Файл: пункты «Открыть PGN браузер» (Ctrl+B) и «Обновить браузер» (Ctrl+R) перенесены

    - Окна: теперь содержит все пункты управления PGN браузерами (открыть, обновить, статус буфера обмена, очистить буфер, закрыть все, список открытых браузеров)

- **Расширенный диалог «О программе»** — добавлены кликабельные ссылки на веб-сайт, GitHub и лицензию

- **Обновлены README и сайт для отражения новых возможностей 1.5**

### Удалено

- **Глобальный кэш loadedKeys в BookManager** — заменён на проверку состояния конкретного узла дерева

## [1.4.0] - 2026-09-12

### Добавлено
- **Редактирование Polyglot книги** – возможность добавлять новые варианты прямо во время игры
- **Отмена добавленного хода** – можно отменить добавленный в книгу ход до сохранения книги
- **Кнопка сохранения книги** – сохранение изменений в Polyglot книгу
- **Переворот доски при расстановке позиции** – добавлена кнопка смены цвета при расстановке. Теперь можно расставлять позицию как за белых, так и за чёрных — это удобно, если вы играли чёрными и лучше помните позицию за этот цвет
- **Режим книги: анализ разрешён** – движок может анализировать позицию, но выполнение хода недоступно
- **MMap-загрузка книг** – большие и малые Polyglot книги загружаются практически мгновенно, даже на HDD

### Исправлено
- **Навигация по панели кнопок в режиме книги** – исправлена работа кнопок навигации
- **Загрузка Polyglot книги** – исправлена ошибка, из-за которой не все ходы подтягивались (отрицательное число при фильтрации)
- **Открытие страницы GitHub на Linux (Debian)** – исправлена проблема с открытием ссылки
- **Отображение хода при перетаскивании фигуры** – исправлено при создании нового варианта
- **Индексация при сохранении позиции** – исправлена индексация при сохранении позиции в неиндексированный PGN файл
- **Контекстное меню в режиме книги** – переделано для корректного удаления вариантов, ходов и перестроения дерева вариантов
- **Инструменты тренера: маркеры при скрытии координат** – исправлено несовпадение нарисованных стрелок и крестиков с клетками доски после скрытия координат и при перевороте доски
- **Выбор варианта на развилке главной линии** – исправлена ошибка, из-за которой при выборе варианта сразу после его создания всегда выбиралась главная линия вместо выбранного варианта

### Изменено
- **Производительность работы с книгами** – за счёт использования MappedByteBuffer загрузка и работа с книгами ускорена в разы
- **Убрано лишнее логирование** – оптимизирован вывод логов

### Удалено
- Лишние отладочные сообщения

---

## [1.3.0] - 2026-09-07

### Добавлено
- **Бинарный индекс PGN** – полная замена JSON-индекса на бинарный формат
- **Ленивая загрузка** партий через `LazyPgnIndex` для повышения производительности
- **Навигация по клику мыши** в дебютных книгах Polyglot (не только с клавиатуры)
- Полная кроссплатформенная поддержка Windows, Linux и macOS

### Исправлено
- **Закрытие диалогов** на macOS – исправлена проблема с закрытием диалога выбора вариантов
- **Закрытие диалогов** на Linux (Debian) – исправлена проблема с диалогом выбора вариантов
- Убрана ненужная зависимость `libjpeg-turbo` для Debian Linux

### Изменено
- **Полный переход** с JSON-индекса на бинарный
- **Улучшена производительность** – бинарный индекс работает в 5-10 раз быстрее
- **Снижено потребление памяти** – бинарный индекс потребляет значительно меньше ОЗУ
- Индексация PGN файлов теперь полностью на бинарной основе

### Удалено
- JSON-индекс (больше не используется)
- Файлы `.idx` от предыдущих версий можно безопасно удалить
- Ненужная зависимость `libjpeg-turbo` для Debian Linux

### Руководство по обновлению
Если вы обновляетесь с версии 1.2.x:
1. Установите новую версию
2. Старые файлы индекса `.idx` можно безопасно удалить – они больше не используются
3. Индекс будет перестроен автоматически

---

## [1.2.x] - Предыдущие версии

### Возможности
- PGN браузер
- Шахматная доска с анализом
- Поддержка дебютных книг Polyglot
- Многоязычная поддержка (русский, английский, китайский)

---

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.6.0] - 2026-10-07

### Added

- **Save position to PGN file** — extract an interesting moment from a game (e.g., the position before a mating combination) into a separate puzzle file. Menu **File → Save Position as PGN...**, shortcut **Ctrl+Shift+E**. The position is saved with `[SetUp "1"]` and `[FEN "..."]` tags, including metadata: players, event, date, ECO and opening. If the file already exists, the position is appended, not overwriting existing content. The file is compatible with ChessBase, lichess, Scid, and any chess software

- **Report a problem (Feedback)** — built-in feedback form. Menu **Help → Report a problem...**. The user selects the issue type (Bug / Feature / Performance / UI / Documentation / Other), describes it, and clicks **"Open on GitHub"**. A new issue form opens on GitHub with a pre-filled title, body, and label. System information (Kletka version, OS, Java, engine) is added automatically — with an option to disable. No personal data is collected or transmitted. A **"Copy to clipboard"** button is also available for manual submission

- **Stockfish version in the About dialog** — now shows the installed engine version (e.g., "Stockfish 16.1"). If the engine is not installed, a message "Stockfish is not installed" is displayed

- **RPM-based Linux distributions support** — new build profile `-P linux-rpm`, creating a `.rpm` package for Fedora, RHEL, CentOS, Rocky Linux, AlmaLinux, openSUSE, Amazon Linux, and Oracle Linux. Built with `jpackage` (requires `rpm-build`)

- **Ctrl+Shift+E shortcut** — quick access to saving a position from a game

### Fixed

- **En passant capture** — now correctly recorded in notation as a capture, not as a regular pawn move

- **Display of nested variations in notation** — sub-variations (e.g., `3... Nf6 4. O-O`) no longer disappear when new branches are added at earlier moves

- **Variation tree display** — variation nesting is now correctly rendered based on the actual nesting level. Previously, sub-variations of the main line (e.g., `1... c5` under `1. e4`) were visually shown as nested inside root variations (e.g., `1. d4 ...`), creating a false sense of hierarchy

- **Unified variation addition logic** — moves made by the user and by the Stockfish engine are now added to the tree with the same code. Previously, engine moves created variations "directly", leading to lost tree links (the variation appeared in the tree but not in the notation). The variation choice dialog now appears whenever a move doesn't match existing ones

- **Unsaved changes check before opening a file** — when clicking "Open PGN", the "Save / Don't Save / Cancel" dialog appears **before** file selection, not after. Previously, the file opened first and then a dialog popped up, creating a jarring experience

- **Double save dialog** — fixed a bug where the "Save?" dialog appeared twice when opening a file. Cause — incorrect hash synchronization on "Don't Save"

- **Circle in coach tools** — now correctly reset when the coach panel is closed (previously stayed active, unlike the cross marker)

- **Coach tools on board size change** — now close automatically on zoom, eliminating marker misalignment

### Changed

- **Variation tree traversal logic split** — separate traversal methods for notation display (HTML visitor) and for PGN export. This allows independent optimization of display and correct PGN output without compromise

- **Bitcoin donation address** — updated to a non-expiring address

- **Russian UI names** — all "Kletka" occurrences replaced with "Клетка" in the Russian version (About, Donate dialogs)

- **Shortcuts list extended** — added Ctrl+Shift+E (save position to PGN)

### Known Issues

- **Fedora 41+ (Wayland): dialog focus** — in Fedora 41 and later, GNOME uses Wayland by default and has removed the X11 session. JavaFX on Wayland may not receive focus when showing modal dialogs (e.g., the variation choice dialog) due to **Focus Stealing Prevention** in GNOME.

  **Workaround:** when a dialog appears, press **Tab** — focus will move and everything will work correctly afterward.

  **Alternative:** use a Linux distribution with X11 session support (Kubuntu, Linux Mint, etc.), or an Xorg session if available.

  This is a known JavaFX bug, tracked in [JDK-8353643](https://bugs.openjdk.org/browse/JDK-8353643).

## [1.5.0] - 2026-09-23

### Added

- **Export game tree to Polyglot book** — create a personal opening repertoire from analyzed variations. Menu Books → Export to Polyglot Book.... All moves from the tree (main line + all variations) go into the book, transpositions and duplicates are automatically deduplicated. The resulting book can be loaded back into Kletka or used in Stockfish, Leela, ChessBase, Scid, Arena, and other programs

- **Copy Position (FEN + ASCII)** — copies the current position to the clipboard in a convenient format: FEN on the first line and an ASCII diagram below, wrapped in a Markdown code block. Menu Edit → Copy Position, shortcut Ctrl+Shift+P. Useful for sharing positions in chats, forums, and for analysis with AI assistants

- **Empty circle in coach tools** — a new marker type (third, alongside arrows and crosses). Useful for commenting on important squares without crossing out the piece

- **Clickable links in the About dialog** — links to the project website, GitHub repository, and GPL v3 license text open in the system browser

- **Global hotkeys for all key actions**: Ctrl+B (open PGN browser), Ctrl+R (refresh browser), Ctrl+Shift+P (copy position), Ctrl+Shift+V (import from clipboard), Ctrl+E (export game), Ctrl+H (shortcuts), Ctrl+A (engine analysis). These shortcuts previously worked only when the menu was open

- **CI/CD**: GitHub Actions workflow for building installers

  - **Windows**: Liberica JDK 17 Full + WiX Toolset, .exe installer with upgrade support

  - **Linux**: .deb build for Debian and Ubuntu via Docker with Liberica Full

  - **macOS**: .dmg build

  - **Artifacts and release publishing**

### Fixed

- **Polyglot book unloading no longer shows a false game-save dialog** — previously, unloading an already-saved book could trigger a "Save game?" dialog even when the game wasn't modified. Book unloading now works correctly and doesn't touch game logic

- **Save book before unloading** — when unloading a book with unsaved changes, a "Save / Don't Save / Cancel" dialog appears

- **Unified unsaved-changes check for books** — Ctrl+N (new game), app close, and book unload now use the same logic. User will never lose unsaved book moves by accident

- **Removed global move cache for Polyglot books** — variations are now correctly loaded from the book for positions reached via different move paths (transpositions). Previously, the second path to the same position was incorrectly considered "already loaded"

- **Coach tools on board flip and coordinates hiding** — coach tools now close before the board changes, eliminating marker misalignment

- **Tree-to-Polyglot export correctly handles forks** — fixed a bug where the main line continuation, stored in fork sub-variations, was exported twice

- **ConcurrentModificationException when opening menus** — fixed a crash caused by reusing the same SeparatorMenuItem in a context menu

### Changed

- **Menu reorganization**:

  - **File**: "Open PGN Browser" (Ctrl+B) and "Refresh Browser" (Ctrl+R) items moved out

  - **Windows**: now contains all PGN browser management items (open, refresh, clipboard status, clear clipboard, close all, list of open browsers)

  - **Extended About dialog** — added clickable links to the website, GitHub, and license

  - **Updated README and website to reflect the new features in 1.5**

### Removed

- **Global loadedKeys cache in BookManager** — replaced with per-node tree state check

## [1.4.0] - 2026-09-12

### Added
- **Polyglot Book Editing** – ability to add new variations directly during the game
- **Undo added move** – can undo an added move before saving the book
- **Save book button** – saving changes to the Polyglot book
- **Board flip during position setup** – added a color swap button during position setup. Now you can set up the position from White's or Black's perspective — convenient if you played as Black and remember the position better from that side
- **Book mode: analysis allowed** – engine can analyze position, but move execution is disabled
- **MMap book loading** – large and small Polyglot books load almost instantly, even on HDD

### Fixed
- **Navigation panel buttons in book mode** – fixed navigation buttons behavior
- **Polyglot book loading** – fixed bug where not all moves were loaded (negative number during filtering)
- **GitHub page opening on Linux (Debian)** – fixed link opening issue
- **Move display when dragging a piece** – fixed when creating a new variation
- **Indexing on save** – fixed indexing when saving a position to a non-indexed PGN file
- **Context menu in book mode** – redesigned for correct deletion of variations, moves and tree rebuilding
- **Coach tools: markers when coordinates are hidden** – fixed misalignment of drawn arrows and crosses with board squares after hiding coordinates and when flipping the board
- **Variation selection at a main line fork** – fixed bug where selecting a variation right after creating it always picked the main line instead of the chosen variation

### Changed
- **Book performance** – due to MappedByteBuffer, book loading and operation accelerated significantly
- **Reduced logging** – optimized log output

### Removed
- Extra debug messages

---

## [1.3.0] - 2026-09-07

### Added
- **Binary PGN Index** – complete replacement of JSON index with binary format
- **Lazy loading** of games via `LazyPgnIndex` for better performance
- **Mouse click navigation** in Polyglot opening books (not just keyboard)
- Full cross-platform support for Windows, Linux and macOS

### Fixed
- **Dialog closing** on macOS – fixed variant selection dialog not closing properly
- **Dialog closing** on Linux (Debian) – fixed variant selection dialog issues
- Removed unnecessary `libjpeg-turbo` dependency for Debian Linux

### Changed
- **Complete migration** from JSON index to binary index
- **Performance improved** – binary index is 5-10x faster
- **Memory usage reduced** – binary index consumes significantly less RAM
- PGN file indexing is now fully binary-based

### Removed
- JSON index format (no longer used)
- `.idx` files from previous versions can be safely deleted
- Unnecessary `libjpeg-turbo` dependency for Debian Linux

### Migration Guide
If you are upgrading from version 1.2.x:
1. Install the new version
2. Old `.idx` index files can be safely deleted – they are no longer used
3. The index will be rebuilt automatically

---

## [1.2.x] - Previous versions

### Features
- PGN file browser
- Chess board with analysis
- Polyglot opening books support
- Multi-language support (English, Russian, Chinese)

---

# 更新日志

本文件记录项目的所有重要更改。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/spec/v2.0.0.html)。

## [1.6.0] - 2026-10-07

### 新增

- **将局面保存为 PGN 文件** — 从对局中提取有趣时刻（例如杀棋组合前的局面）到单独的题目文件。菜单 **文件 → 保存局面为 PGN...**，快捷键 **Ctrl+Shift+E**。局面保存时包含 `[SetUp "1"]` 和 `[FEN "..."]` 标签，以及元数据：棋手、赛事、日期、ECO 和开局。如果文件已存在，局面将追加到末尾，不覆盖已有内容。该文件兼容 ChessBase、lichess、Scid 以及任何国际象棋软件

- **报告问题（反馈）** — 内置反馈表单。菜单 **帮助 → 报告问题...**。用户选择问题类型（Bug / 功能 / 性能 / 界面 / 文档 / 其他），描述问题并点击 **"在 GitHub 上打开"**。浏览器将打开 GitHub 的新 issue 表单，其中包含预填的标题、正文和标签。系统会自动添加系统信息（Kletka 版本、操作系统、Java、引擎）— 可选择禁用。不收集或传输任何个人数据。还提供 **"复制到剪贴板"** 按钮，用于手动提交

- **「关于」对话框中的 Stockfish 版本** — 现在显示已安装的引擎版本（例如"Stockfish 16.1"）。如果未安装引擎，则显示"Stockfish 未安装"

- **支持 RPM 系列 Linux 发行版** — 新增构建配置 `-P linux-rpm`，生成 `.rpm` 软件包，适用于 Fedora、RHEL、CentOS、Rocky Linux、AlmaLinux、openSUSE、Amazon Linux 和 Oracle Linux。使用 `jpackage` 构建（需要 `rpm-build`）

- **Ctrl+Shift+E 快捷键** — 快速保存对局中的局面

### 修复

- **吃过路兵** — 现在正确记录为吃子走法，而不是普通兵走法

- **嵌套变着的显示** — 子变着（例如 `3... Nf6 4. O-O`）在更早的走法上添加新分支时不再消失

- **变例树显示** — 变着嵌套现在根据实际嵌套层级正确渲染。此前，主线的子变着（例如 `1. e4` 下的 `1... c5`）在视觉上显示为嵌套在根变着内（例如 `1. d4 ...`），造成错误的层级感

- **统一的变着添加逻辑** — 用户和 Stockfish 引擎走的走法现在使用相同的代码添加到树中。此前，引擎走法"直接"创建变着，导致树链接丢失（变着出现在树中但不在棋谱中）。现在只要走法与现有走法不匹配，就会显示变着选择对话框

- **打开文件前的未保存更改检查** — 点击"打开 PGN"时，"保存 / 不保存 / 取消"对话框现在出现在文件选择 **之前**，而不是之后。此前文件先打开，然后弹出对话框，体验不佳

- **双重保存对话框** — 修复了打开文件时"保存？"对话框出现两次的错误。原因 — "不保存"时哈希同步不正确

- **教练工具中的圆圈** — 现在关闭教练面板时正确重置（此前与叉号不同，保持激活状态）

- **棋盘尺寸改变时的教练工具** — 缩放时自动关闭，消除标记错位

### 变更

- **变例树遍历逻辑分离** — 为棋谱显示（HTML 访问器）和 PGN 导出使用单独的遍历方法。这允许独立优化显示和正确的 PGN 输出，无需妥协

- **比特币捐赠地址** — 更新为永久地址

- **俄语界面名称** — 俄语版本中所有 "Kletka" 均替换为 "Клетка"（关于、捐赠对话框）

- **快捷键列表扩展** — 添加 Ctrl+Shift+E（保存局面为 PGN）

### 已知问题

- **Fedora 41+（Wayland）：对话框焦点** — 在 Fedora 41 及更高版本中，GNOME 默认使用 Wayland 并移除了 X11 会话。由于 GNOME 的 **焦点窃取防护** 限制，JavaFX 在 Wayland 环境中显示模态对话框（例如变着选择对话框）时可能无法获得焦点。

  **临时解决方案：** 对话框出现时按 **Tab** 键 — 焦点将移动，之后一切正常。

  **替代方案：** 使用支持 X11 会话的 Linux 发行版（Kubuntu、Linux Mint 等），或使用 Xorg 会话（如果可用）。

  这是已知的 JavaFX 错误，跟踪于 [JDK-8353643](https://bugs.openjdk.org/browse/JDK-8353643)。

## [1.5.0] - 2026-09-23
### 新增

- **将变例树导出为 Polyglot 开局库** — 从分析过的变例中创建个人开局棋路。菜单 书籍 → 导出为 Polyglot 开局库...。树中的所有走法（主线 + 所有变着）都会进入开局库，转置和重复自动去重。生成的开局库可以重新加载到 Kletka，也可以在 Stockfish、Leela、ChessBase、Scid、Arena 等程序中使用

- **复制局面 (FEN + ASCII)** — 将当前局面以方便的形式复制到剪贴板：第一行为 FEN，下方为 ASCII 图表，包裹在 Markdown 代码块中。菜单 编辑 → 复制局面，快捷键 Ctrl+Shift+P。适用于在聊天、论坛中分享局面以及与 AI 助手进行分析

- **教练工具中的空心圆圈** — 新增的标记类型（第三种，与箭头和叉号并列）。适用于标记重要格子而无需划掉棋子

- **「关于」对话框中的可点击链接** — 项目网站、GitHub 仓库和 GPL v3 许可证文本链接可在系统浏览器中打开

- **全局快捷键 用于所有关键操作**：Ctrl+B（打开 PGN 浏览器）、Ctrl+R（刷新浏览器）、Ctrl+Shift+P（复制局面）、Ctrl+Shift+V（从剪贴板导入）、Ctrl+E（导出对局）、Ctrl+H（快捷键）、Ctrl+A（引擎分析）。这些快捷键以前仅在菜单打开时有效

- **CI/CD**：GitHub Actions 工作流用于构建安装程序

  - **Windows**：Liberica JDK 17 Full + WiX Toolset，带升级支持的 .exe 安装程序

  - **Linux**：通过 Docker 使用 Liberica Full 为 Debian 和 Ubuntu 构建 .deb

  - **macOS**：构建 .dmg

  - **工件和版本发布**

### 修复

- **卸载 Polyglot 开局库时不再显示错误的保存对局对话框** — 之前卸载已保存的开局库时会触发"保存对局？"对话框，即使对局未修改。现在卸载开局库正常工作，不影响对局逻辑

- **卸载前保存开局库** — 卸载有未保存更改的开局库时，会显示"保存 / 不保存 / 取消"对话框

- **开局库未保存更改检查的统一行为** — Ctrl+N（新对局）、关闭程序和卸载开局库现在使用相同的逻辑。用户永远不会意外丢失未保存的开局库走法

- **移除 Polyglot 开局库的全局走法缓存** — 现在可以通过不同走法路径（转置）到达的局面正确加载开局库中的变着。以前，到达同一局面的第二条路径会被错误地视为"已加载"

- **翻转棋盘和隐藏坐标时的教练工具** — 教练工具现在会在棋盘变化前关闭，消除标记错位

- **变例树导出到 Polyglot 时正确处理分支** — 修复了存储在分支子变着中的主线延续被导出两次的错误

- **打开菜单时的 ConcurrentModificationException** — 修复了在上下文菜单中重复使用同一个 SeparatorMenuItem 导致的崩溃

### 变更

- **菜单重组**：

  - **文件**：移出"打开 PGN 浏览器"(Ctrl+B) 和"刷新浏览器"(Ctrl+R) 项

  - **窗口**：现在包含所有 PGN 浏览器管理项（打开、刷新、剪贴板状态、清除剪贴板、关闭全部、已打开浏览器列表）

- **扩展的「关于」对话框** — 添加了指向网站、GitHub 和许可证的可点击链接

- **更新 README 和网站 以反映 1.5 的新功能**

### 移除

- **BookManager 中的全局 loadedKeys 缓存** — 替换为按节点树状态检查

## [1.4.0] - 2026-09-12

### 新增
- **Polyglot 开局库编辑** – 可以在对局中直接添加新变例
- **撤销添加的走法** – 可以在保存开局库之前撤销添加的走法
- **保存开局库按钮** – 将更改保存到 Polyglot 开局库
- **摆棋时翻转棋盘** – 在摆棋时添加了交换颜色按钮。现在您可以从白方或黑方的视角摆棋——如果您执黑棋且更容易从黑方角度记住局面，这会非常方便
- **开局库模式：允许分析** – 引擎可以分析局面，但禁止执行走法
- **MMap 开局库加载** – 大型和小型 Polyglot 开局库加载几乎瞬间完成，即使在 HDD 上

### 修复
- **开局库模式下导航面板按钮** – 修复导航按钮行为
- **Polyglot 开局库加载** – 修复了加载时并非所有走法都会被加载的错误（过滤时出现负数）
- **在 Linux (Debian) 上打开 GitHub 页面** – 修复链接打开问题
- **拖动棋子时走法显示** – 修复创建新变例时的问题
- **保存时索引** – 修复将局面保存到未索引的 PGN 文件时的索引问题
- **开局库模式下的上下文菜单** – 重新设计，以正确删除变例、走法和重建变例树
- **教练工具：隐藏坐标时的标记** – 修复隐藏坐标后和翻转棋盘时，绘制的箭头和叉号与棋盘格子不重合的问题
- **在主变分叉点选择变例** – 修复了刚创建变例后立即选择变例时，始终选中主变而不是所选变例的错误

### 变更
- **开局库性能** – 由于使用 MappedByteBuffer，开局库加载和操作速度显著提升
- **减少日志** – 优化日志输出

### 移除
- 多余的调试消息

---

## [1.3.0] - 2026-09-07

### 新增
- **二进制 PGN 索引** – 完全替代 JSON 索引格式
- **懒加载** 棋局功能，通过 `LazyPgnIndex` 提升性能
- **鼠标点击导航** 在 Polyglot 开局库中（不仅限键盘操作）
- 完整支持 Windows、Linux 和 macOS 跨平台

### 修复
- **macOS 对话框** – 修复变体选择对话框无法正常关闭的问题
- **Linux (Debian) 对话框** – 修复变体选择对话框问题
- 移除 Debian Linux 不必要的 `libjpeg-turbo` 依赖

### 变更
- **完全迁移** 从 JSON 索引迁移到二进制索引
- **性能提升** – 二进制索引速度提升 5-10 倍
- **内存优化** – 二进制索引消耗的内存大幅减少
- PGN 文件索引现在完全基于二进制格式

### 移除
- JSON 索引格式（不再使用）
- 旧版本的 `.idx` 索引文件可以安全删除
- Debian Linux 不必要的 `libjpeg-turbo` 依赖

### 升级指南
如果您从 1.2.x 版本升级：
1. 安装新版本
2. 旧的 `.idx` 索引文件可以安全删除 – 它们不再使用
3. 索引将自动重建

---

## [1.2.x] - 先前版本

### 功能
- PGN 文件浏览器
- 棋盘分析与引擎支持
- Polyglot 开局库支持
- 多语言支持（中文、英文、俄文）