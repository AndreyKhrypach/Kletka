# Журнал изменений

Все значимые изменения в проекте будут документированы в этом файле.

Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.0.0/),
и этот проект следует [Семантическому Версионированию](https://semver.org/lang/ru/spec/v2.0.0.html).

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