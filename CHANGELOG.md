# Журнал изменений

Все значимые изменения в проекте будут документированы в этом файле.

Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.0.0/),
и этот проект следует [Семантическому Версионированию](https://semver.org/lang/ru/spec/v2.0.0.html).

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