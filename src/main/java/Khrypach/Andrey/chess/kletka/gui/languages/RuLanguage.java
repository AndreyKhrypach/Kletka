/*
 *
 *  * Copyright (c) 2025-2026 Andrey Khrypach
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package Khrypach.Andrey.chess.kletka.gui.languages;

import java.util.HashMap;
import java.util.Map;

public class RuLanguage implements Language {

    private final Map<String, String> strings = new HashMap<>();

    public RuLanguage() {
        // Названия приложения
        strings.put(LanguageKeys.APP_TITLE, "Kletka - Шахматный анализатор");
        strings.put(LanguageKeys.APP_VERSION, "Версия: 1.0");
        strings.put(LanguageKeys.APP_PLATFORM, "Платформа: Java 17, OpenJFX");
        strings.put(LanguageKeys.APP_LIBRARY, "Библиотека: chesslib 1.3.6");
        strings.put(LanguageKeys.APP_COPYRIGHT, "© 2026 Хрипач Андрей");

        // Меню
        strings.put(LanguageKeys.MENU_FILE, "Файл");
        strings.put(LanguageKeys.MENU_BOARD, "Доска");
        strings.put(LanguageKeys.MENU_ASSISTANT, "Ассистент");
        strings.put(LanguageKeys.MENU_DATABASE, "База данных");
        strings.put(LanguageKeys.MENU_HELP, "Помощь");

        // Пункты меню File
        strings.put(LanguageKeys.MENU_FILE_NEW_GAME, "Новая партия");
        strings.put(LanguageKeys.MENU_FILE_SETUP_POSITION, "Расставить позицию...");
        strings.put(LanguageKeys.MENU_FILE_OPEN_PGN, "Открыть PGN...");
        strings.put(LanguageKeys.MENU_FILE_SAVE_PGN, "Сохранить PGN...");
        strings.put(LanguageKeys.MENU_FILE_EXPORT_IMAGE, "Экспорт как изображение");
        strings.put(LanguageKeys.MENU_FILE_EXIT, "Выход");

        // ========== FILE FILTERS ==========
        strings.put(LanguageKeys.FILE_FILTER_PGN, "PGN файлы");
        strings.put(LanguageKeys.FILE_FILTER_DATABASE_TITLE, "Открыть базу данных");
        strings.put(LanguageKeys.FILE_FILTER_DATABASE, "SQLite базы");
        strings.put(LanguageKeys.FILE_FILTER_ALL, "Все файлы");

        // Пункты меню Board
        strings.put(LanguageKeys.MENU_BOARD_SIZE, "Размер доски");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_SMALL, "Маленькая (40px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_MEDIUM, "Средняя (60px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_LARGE, "Большая (80px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_XLARGE, "Очень большая (100px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_CUSTOM, "Точная настройка:");
        strings.put(LanguageKeys.MENU_BOARD_SHOW_COORDS, "Показывать координаты");
        strings.put(LanguageKeys.MENU_BOARD_FLIP, "Перевернуть доску");
        strings.put(LanguageKeys.MENU_BOARD_THEME, "Тема доски");
        strings.put(LanguageKeys.MENU_BOARD_THEME_WOOD, "Деревянная");
        strings.put(LanguageKeys.MENU_BOARD_THEME_CLASSIC, "Классическая");
        strings.put(LanguageKeys.MENU_BOARD_THEME_GREEN, "Зеленая");
        strings.put(LanguageKeys.MENU_BOARD_THEME_BLUE, "Синяя");

        // Пункты меню Assistant
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE, "Движок");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_STOCKFISH, "Stockfish");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_LC0, "Leela Chess Zero");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_CUSTOM, "Пользовательский...");
        strings.put(LanguageKeys.MENU_ASSISTANT_ANALYZE, "Анализировать позицию");
        strings.put(LanguageKeys.MENU_ASSISTANT_BEST_MOVE, "Показать лучший ход");
        strings.put(LanguageKeys.MENU_ASSISTANT_SHOW_EVAL, "Показывать оценку");
        strings.put(LanguageKeys.MENU_ASSISTANT_CONFIGURE_ENGINE, "Настроить движок...");

        // ========== ENGINE SETUP DIALOG ==========
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_TITLE, "Настройка шахматного движка");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_HEADER, "Выберите UCI-совместимый шахматный движок");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_INFO, "Поддерживаются: Stockfish, Leela Chess Zero, Komodo и другие");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_PATH_LABEL, "Путь к движку:");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_PATH_PROMPT, "Выберите файл движка...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_BROWSE, "Обзор...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_TEST, "Проверить движок");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_OK, "OK");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_CANCEL, "Отмена");

        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_CHOOSER_TITLE, "Выберите исполняемый файл движка");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_FILTER_EXECUTABLE, "Исполняемые файлы");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_FILTER_ALL, "Все файлы");

        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_SELECT_FILE, "❌ Сначала выберите файл движка");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_FILE_NOT_EXISTS, "❌ Файл не существует");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_CHECKING, "⏳ Проверка движка...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_READY, "✅ Движок готов к работе!");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_FAILED, "❌ Не удалось запустить движок. Убедитесь, что это UCI-совместимый движок");

        // === Меню База данных ===
        strings.put(LanguageKeys.MENU_DATABASE_CONNECT, "Подключиться к базе");
        strings.put(LanguageKeys.MENU_DATABASE_OPEN, "Открыть локальную базу...");
        strings.put(LanguageKeys.MENU_DATABASE_SEARCH, "Поиск по базе");
        strings.put(LanguageKeys.MENU_DATABASE_IMPORT_PGN, "Импорт PGN в базу");
        strings.put(LanguageKeys.MENU_DATABASE_STATS, "Статистика дебютов");
        strings.put(LanguageKeys.MENU_DATABASE_OPEN_LAST, "Открыть последнюю базу");
        strings.put(LanguageKeys.MENU_DATABASE_IMPORT, "Импортировать PGN");
        strings.put(LanguageKeys.MENU_DATABASE_INFO, "Информация о базе");

        // === Сообщения базы данных ===
        strings.put(LanguageKeys.DB_NOT_INITIALIZED, "База данных не инициализирована");
        strings.put(LanguageKeys.DB_CONNECT_SUCCESS, "База данных подключена");
        strings.put(LanguageKeys.DB_CONNECT_ERROR, "Ошибка подключения к базе данных");
        strings.put(LanguageKeys.DB_OPEN_TITLE, "Открыть базу данных");
        strings.put(LanguageKeys.DB_OPEN_SUCCESS, "База данных открыта");
        strings.put(LanguageKeys.DB_OPEN_ERROR, "Ошибка открытия базы данных");
        strings.put(LanguageKeys.DB_OPEN_INVALID, "Выбранный файл не является директорией");
        strings.put(LanguageKeys.DB_SEARCH_TITLE, "Поиск в базе данных");
        strings.put(LanguageKeys.DB_SEARCH_HEADER, "Введите критерии поиска");
        strings.put(LanguageKeys.DB_SEARCH_BUTTON, "Найти");
        strings.put(LanguageKeys.DB_SEARCH_WHITE, "Белые (или часть имени)");
        strings.put(LanguageKeys.DB_SEARCH_BLACK, "Черные (или часть имени)");
        strings.put(LanguageKeys.DB_SEARCH_RESULT, "Результат (1-0, 0-1, 1/2-1/2)");
        strings.put(LanguageKeys.DB_SEARCH_ECO, "Код ECO");
        strings.put(LanguageKeys.DB_SEARCH_OPENING, "Название дебюта");
        strings.put(LanguageKeys.DB_SEARCH_ERROR, "Ошибка поиска");
        strings.put(LanguageKeys.DB_NO_RESULTS, "Ничего не найдено");
        strings.put(LanguageKeys.DB_NO_GAMES, "Нет игр в базе данных");
        strings.put(LanguageKeys.DB_RESULTS_TITLE, "Результаты поиска");
        strings.put(LanguageKeys.DB_RESULTS_HEADER, "Найдено партий: %d");
        strings.put(LanguageKeys.DB_LOAD_ERROR, "Ошибка загрузки партии");
        strings.put(LanguageKeys.DB_LOAD_SUCCESS, "Партия загружена");
        strings.put(LanguageKeys.DB_LOAD_BUTTON, "Загрузить");
        strings.put(LanguageKeys.DB_STATS_TITLE, "Статистика дебютов");
        strings.put(LanguageKeys.DB_STATS_ERROR, "Ошибка получения статистики");
        strings.put(LanguageKeys.DB_STATS_ECO, "Статистика по ECO");
        strings.put(LanguageKeys.DB_STATS_OPENING, "Статистика по дебютам");
        strings.put(LanguageKeys.DB_INFO_TITLE, "Информация о базе данных");
        strings.put(LanguageKeys.DB_INFO_ERROR, "Ошибка получения информации");
        strings.put(LanguageKeys.DB_INFO_PATH, "Путь");
        strings.put(LanguageKeys.DB_INFO_GAMES, "Количество партий");
        strings.put(LanguageKeys.DB_INFO_TYPE, "Тип хранилища");
        strings.put(LanguageKeys.DB_INFO_VERSION, "Версия");
        strings.put(LanguageKeys.DB_LOAD_PGN_FIRST, "Загрузите PGN файл в программу");
        strings.put(LanguageKeys.DB_INDEX_NOT_LOADED , "Индекс не загружен");
        strings.put(LanguageKeys.DB_STATS_TOTAL  , "Всего игр:");

        strings.put(LanguageKeys.DB_INFO_FILENAME  , "Имя файла");
        strings.put(LanguageKeys.DB_INFO_ACTIVE_GAMES   , "Активных партий");
        strings.put(LanguageKeys.DB_INFO_DELETED_GAMES   , "Удаленных партий");
        strings.put(LanguageKeys.DB_INFO_FILE_SIZE   , "Размер файла");
        strings.put(LanguageKeys.DB_INFO_INDEX_VERSION   , "Версия индекса");
        strings.put(LanguageKeys.DB_INFO_GROWTH_RATIO    , "Избыточность (рост)");

        // === PGN сообщения ===
        strings.put(LanguageKeys.PGN_IMPORT_TITLE, "Импорт PGN");
        strings.put(LanguageKeys.PGN_IMPORT_PROGRESS, "Импорт PGN...");
        strings.put(LanguageKeys.PGN_IMPORT_SUCCESS, "Импортировано %d партий из файла: %s");
        strings.put(LanguageKeys.PGN_IMPORT_ERROR, "Ошибка импорта PGN");
        strings.put(LanguageKeys.PGN_IMPORT_NO_GAMES, "В файле не найдено партий");
        strings.put(LanguageKeys.PGN_IMPORT_CLIPBOARD_SUCCESS, "Партия успешно импортирована из буфера обмена");
        strings.put(LanguageKeys.PGN_CLIPBOARD_EMPTY, "Буфер обмена пуст");
        strings.put(LanguageKeys.PGN_LOAD_SUCCESS, "Загружено %d партий из: %s");
        strings.put(LanguageKeys.PGN_LOAD_EMPTY, "В файле нет партий");
        strings.put(LanguageKeys.PGN_LOAD_ERROR, "Ошибка загрузки PGN");
        strings.put(LanguageKeys.PGN_SAVE_SUCCESS, "Партия сохранена");
        strings.put(LanguageKeys.PGN_SAVE_EMPTY, "Нет ходов для сохранения");
        strings.put(LanguageKeys.PGN_SAVE_ERROR, "Ошибка сохранения");

        // === Файловое меню ===
        strings.put(LanguageKeys.MENU_FILE_EXPORT_CURRENT, "Экспорт текущей партии");
        strings.put(LanguageKeys.MENU_FILE_IMPORT_CLIPBOARD, "Импорт из буфера обмена");

        // Пункты меню Help
        strings.put(LanguageKeys.MENU_HELP_SHORTCUTS, "Горячие клавиши");
        strings.put(LanguageKeys.MENU_HELP_ABOUT, "О программе");

        // === Меню Правка ===
        strings.put(LanguageKeys.MENU_EDIT, "Правка");
        strings.put(LanguageKeys.MENU_EDIT_UNDO, "Отменить");
        strings.put(LanguageKeys.MENU_EDIT_REDO, "Повторить");
        strings.put(LanguageKeys.MENU_EDIT_PREFERENCES, "Настройки");
        strings.put(LanguageKeys.PREFERENCES_TITLE, "Настройки");

        // === Меню Вид ===
        strings.put(LanguageKeys.MENU_VIEW, "Вид");
        strings.put(LanguageKeys.MENU_VIEW_FLIP_BOARD, "Перевернуть доску");
        strings.put(LanguageKeys.MENU_VIEW_COORDINATES, "Показывать координаты");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM, "Масштаб");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_IN, "Увеличить");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_OUT, "Уменьшить");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_RESET, "Сбросить");

        // === Меню Движок ===
        strings.put(LanguageKeys.MENU_ENGINE, "Движок");
        strings.put(LanguageKeys.MENU_ENGINE_CONFIGURE, "Настроить движок");
        strings.put(LanguageKeys.MENU_ENGINE_ANALYZE, "Показать лучший ход");

        // Диалоги вариантов
        strings.put(LanguageKeys.DIALOG_VARIATION_TITLE, "Выбор варианта");
        strings.put(LanguageKeys.DIALOG_VARIATION_CHOICE, "Выберите продолжение (→ для выбора, ← для отмены):");
        strings.put(LanguageKeys.DIALOG_VARIATION_SELECT, "Выбрать (→)");
        strings.put(LanguageKeys.DIALOG_VARIATION_CANCEL, "Отмена (←)");
        strings.put(LanguageKeys.DIALOG_VARIATION_NEW, "✦ НОВЫЙ ВАРИАНТ (%s): %s");
        strings.put(LanguageKeys.DIALOG_VARIATION_REPLACE_MAIN, "✗ Заменить главную линию (ход %d%s %s)");
        strings.put(LanguageKeys.DIALOG_VARIATION_MAIN_LINE, "▶ ГЛАВНАЯ ЛИНИЯ (%s): %s");
        strings.put(LanguageKeys.DIALOG_VARIATION_EXISTING, "Заменить Вариант (ход %d%s %s : %s)");
        strings.put(LanguageKeys.DIALOG_VARIATION_MAKE_MAIN, "★ СДЕЛАТЬ ГЛАВНЫМ (%s): %s");

        // Нотация
        strings.put(LanguageKeys.NOTATION_TITLE, "Запись партии");
        strings.put(LanguageKeys.NOTATION_NEW_GAME, "Новая партия");
        strings.put(LanguageKeys.NOTATION_COPY_PGN, "Копировать PGN");
        strings.put(LanguageKeys.NOTATION_PGN_COPIED, "PGN скопирован в буфер обмена");
        strings.put(LanguageKeys.NOTATION_COPY_PGN_UNICODE, "Копировать PGN (Unicode)");
        strings.put(LanguageKeys.NOTATION_PGN_UNICODE_COPIED, "PGN (Unicode) скопирован в буфер обмена");
        strings.put(LanguageKeys.NOTATION_TOGGLE_SHOW, "Показать запись партии");
        strings.put(LanguageKeys.NOTATION_TOGGLE_HIDE, "Скрыть запись партии");
        strings.put(LanguageKeys.NOTATION_NO_MOVES, "Нет ходов");
        strings.put(LanguageKeys.NOTATION_NO_DATA, "Нет данных");

        // Игровые сообщения
        strings.put(LanguageKeys.GAME_CHECKMATE, "Мат! Победили %s");
        strings.put(LanguageKeys.GAME_STALEMATE, "Пат! Ничья");
        strings.put(LanguageKeys.GAME_INSUFFICIENT_MATERIAL, "Недостаточно материала для мата. Ничья");
        strings.put(LanguageKeys.GAME_WHITE, "Белые");
        strings.put(LanguageKeys.GAME_BLACK, "Черные");
        strings.put(LanguageKeys.GAME_WIN, "Победили %s");

        // Диалог превращения пешки
        strings.put(LanguageKeys.PROMOTION_TITLE, "Выбор фигуры");
        strings.put(LanguageKeys.PROMOTION_CHOOSE, "Выберите фигуру для превращения пешки:");
        strings.put(LanguageKeys.PROMOTION_IMAGE_LOAD_ERROR, "Не удалось загрузить изображение для");

        // Диалог расстановки позиции
        strings.put(LanguageKeys.SETUP_TITLE, "Расстановка позиции");
        strings.put(LanguageKeys.SETUP_INSTRUCTION,
                """
                        ЛКМ: поставить фигуру   ПКМ: противоположный цвет
                        Режим удаления: клик для удаления
                        Режим перемещения: клик на фигуре, затем на целевой клетке
                        ПКМ в режиме перемещения - отмена""");
        strings.put(LanguageKeys.SETUP_SELECT_PIECE, "Выберите фигуру:");
        strings.put(LanguageKeys.SETUP_WHITE, "Белые:");
        strings.put(LanguageKeys.SETUP_BLACK, "Черные:");
        strings.put(LanguageKeys.SETUP_DELETE_MODE, "Режим удаления");
        strings.put(LanguageKeys.SETUP_MOVE_MODE, "Режим перемещения");
        strings.put(LanguageKeys.SETUP_CLEAR_SELECTION, "Сбросить выбор");
        strings.put(LanguageKeys.SETUP_SIDE_TO_MOVE, "Очередь хода:");
        strings.put(LanguageKeys.SETUP_FEN, "FEN:");
        strings.put(LanguageKeys.SETUP_COPY_FEN, "Копировать");
        strings.put(LanguageKeys.SETUP_APPLY, "Применить");
        strings.put(LanguageKeys.SETUP_START_POS, "Начальная");
        strings.put(LanguageKeys.SETUP_CLEAR_ALL, "Очистить всё");
        strings.put(LanguageKeys.SETUP_CANCEL, "Отмена");
        strings.put(LanguageKeys.SETUP_FEN_INVALID, "Неверный формат FEN!");
        strings.put(LanguageKeys.SETUP_FEN_COPIED, "FEN скопирован в буфер обмена");
        strings.put(LanguageKeys.SETUP_IMAGE_LOAD_ERROR, "Не удалось загрузить изображение для");
        strings.put(LanguageKeys.SETUP_DELETE_INSTRUCTION, "Режим удаления: клик для удаления фигуры");
        strings.put(LanguageKeys.SETUP_DELETE_MODE_TOOLTIP, "Режим удаления активен - кликайте на фигуры для удаления");
        strings.put(LanguageKeys.SETUP_CASTLING_RIGHTS, "Права рокировки:");
        strings.put(LanguageKeys.SETUP_CASTLING_WHITE_KING, "0-0 (короткая)");
        strings.put(LanguageKeys.SETUP_CASTLING_WHITE_QUEEN, "0-0-0 (длинная)");
        strings.put(LanguageKeys.SETUP_CASTLING_BLACK_KING, "0-0 (короткая)");
        strings.put(LanguageKeys.SETUP_CASTLING_BLACK_QUEEN, "0-0-0 (длинная)");
        strings.put(LanguageKeys.SETUP_RESET_CASTLING, "Сбросить рокировки");
        strings.put(LanguageKeys.SETUP_FEN_PROMPT, "Введите FEN...");
        strings.put(LanguageKeys.SETUP_CONTROL, "Управление:");
        strings.put(LanguageKeys.SETUP_KING_IN_CHECK, "Невозможно поставить ход %s - король под шахом!");

        // Навигация
        strings.put(LanguageKeys.NAV_FIRST, "⏮");
        strings.put(LanguageKeys.NAV_PREV, "◀");
        strings.put(LanguageKeys.NAV_NEXT, "▶");
        strings.put(LanguageKeys.NAV_LAST, "⏭");
        strings.put(LanguageKeys.NAV_TOOLTIP_FIRST, "В начало (↑)");
        strings.put(LanguageKeys.NAV_TOOLTIP_PREV, "Назад (←)");
        strings.put(LanguageKeys.NAV_TOOLTIP_NEXT, "Вперед (→)");
        strings.put(LanguageKeys.NAV_TOOLTIP_LAST, "В конец (↓)");

        // Уведомления
        strings.put(LanguageKeys.NOTIFICATION_INFO, "Информация");
        strings.put(LanguageKeys.NOTIFICATION_ERROR, "Ошибка");
        strings.put(LanguageKeys.NOTIFICATION_DATABASE_CONNECT, "Подключение к БД будет реализовано");
        strings.put(LanguageKeys.NOTIFICATION_SEARCH, "Поиск по БД будет реализован");
        strings.put(LanguageKeys.NOTIFICATION_IMPORT, "Импорт PGN в БД будет реализован");
        strings.put(LanguageKeys.NOTIFICATION_ANALYSIS, "Анализ позиции будет реализован");
        strings.put(LanguageKeys.NOTIFICATION_WARNING, "Предупреждение");

        // ========== АННОТАЦИИ ХОДОВ (Шахматный информатор) ==========

        // Оценка хода
        strings.put(LanguageKeys.ANNOTATION_BRILLIANT_MOVE, "Отличный ход");
        strings.put(LanguageKeys.ANNOTATION_GOOD_MOVE, "Хороший ход");
        strings.put(LanguageKeys.ANNOTATION_INTERESTING_MOVE, "Интересный ход");
        strings.put(LanguageKeys.ANNOTATION_DUBIOUS_MOVE, "Сомнительный ход");
        strings.put(LanguageKeys.ANNOTATION_BAD_MOVE, "Ошибка");
        strings.put(LanguageKeys.ANNOTATION_BLUNDER, "Грубая ошибка");

        // Оценка позиции
        strings.put(LanguageKeys.ANNOTATION_CLEAR_ADVANTAGE_WHITE, "У белых серьезное преимущество");
        strings.put(LanguageKeys.ANNOTATION_WINNING_WHITE, "У белых решающее преимущество (выигранная позиция)");
        strings.put(LanguageKeys.ANNOTATION_SLIGHT_ADVANTAGE_WHITE, "У белых небольшое преимущество");
        strings.put(LanguageKeys.ANNOTATION_EQUALITY, "Позиция примерно равная");
        strings.put(LanguageKeys.ANNOTATION_SLIGHT_ADVANTAGE_BLACK, "У черных небольшое преимущество");
        strings.put(LanguageKeys.ANNOTATION_CLEAR_ADVANTAGE_BLACK, "У черных серьезное преимущество");
        strings.put(LanguageKeys.ANNOTATION_WINNING_BLACK, "У черных решающее преимущество (выигранная позиция)");
        strings.put(LanguageKeys.ANNOTATION_UNCLEAR_POSITION, "Позиция неясная");
        strings.put(LanguageKeys.ANNOTATION_WITH_COMPENSATION, "С компенсацией за материал");

        // Комментарии и пояснения
        strings.put(LanguageKeys.ANNOTATION_ONLY_MOVE, "Единственный ход");
        strings.put(LanguageKeys.ANNOTATION_THEORETICAL_NOVELTY, "Теоретическая новинка");
        strings.put(LanguageKeys.ANNOTATION_ONLY_AND_BEST_MOVE, "Единственный и лучший ход");
        strings.put(LanguageKeys.ANNOTATION_WITH_IDEA, "С идеей/угрозой...");
        strings.put(LanguageKeys.ANNOTATION_WITH_INITIATIVE, "С инициативой");
        strings.put(LanguageKeys.ANNOTATION_WITH_COUNTERPLAY, "С контригрой");
        strings.put(LanguageKeys.ANNOTATION_DEVELOPMENT_ADVANTAGE, "С перевесом в развитии");
        strings.put(LanguageKeys.ANNOTATION_BETTER_WAS, "Лучше было бы");
        strings.put(LanguageKeys.ANNOTATION_MATE, "мат");
        strings.put(LanguageKeys.ANNOTATION_CHECK, "шах");
        strings.put(LanguageKeys.ANNOTATION_DOUBLE_CHECK, "двойной шах");

        // Слоны
        strings.put(LanguageKeys.ANNOTATION_TWO_BISHOPS, "Два слона");
        strings.put(LanguageKeys.ANNOTATION_BISHOP_PAIR_WHITE_BLACK, "Разноцветные слоны");
        strings.put(LanguageKeys.ANNOTATION_CENTER_CONTROL, "Контроль центра");

        // Диалог аннотаций
        strings.put(LanguageKeys.ANNOTATION_DIALOG_TITLE, "Аннотация хода");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_SELECT, "Выберите аннотацию для хода:");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_COMMENT, "Комментарий:");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_COMMENT_PROMPT, "Введите комментарий к ходу...");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_CLEAR, "Очистить");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_OK, "OK");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_CANCEL, "Отмена");

        // Контекстное меню для ходов
        strings.put(LanguageKeys.CONTEXT_MENU_MAKE_MAIN, "📌 Сделать этот вариант главным");
        strings.put(LanguageKeys.CONTEXT_MENU_ADD_ANNOTATION, "🏷️ Добавить аннотацию / комментарий");
        strings.put(LanguageKeys.CONTEXT_MENU_REMOVE_ANNOTATION, "🗑️ Удалить аннотацию / комментарий");
        strings.put(LanguageKeys.CONTEXT_MENU_EDIT_COMMENT, "✏️ Редактировать комментарий");
        strings.put(LanguageKeys.CONTEXT_MENU_DELETE_VARIATION, "🗑️ Удалить вариант");
        strings.put(LanguageKeys.CONTEXT_MENU_DELETE_AFTER, "✂️ Удалить все после этого хода");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_WHITE_WIN, "🏆 1-0 (Победа белых)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_BLACK_WIN, "🏆 0-1 (Победа черных)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_DRAW, "🏆 1/2-1/2 (Ничья)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_UNKNOWN, "🏆 * (Результат не известен)");
        strings.put(LanguageKeys.CONTEXT_MENU_CANNOT_DELETE_MAIN, "Нельзя удалить главную линию!");

        // Диалог аннотаций (дополнительные ключи)
        strings.put(LanguageKeys.ANNOTATION_DIALOG_MOVE, "Ход:");
        strings.put(LanguageKeys.ANNOTATION_TAB_MOVE_EVAL, "Оценка хода");
        strings.put(LanguageKeys.ANNOTATION_TAB_POSITION_EVAL, "Оценка позиции");
        strings.put(LanguageKeys.ANNOTATION_TAB_COMMENTARY, "Комментарии");

        // Главная линия
        strings.put(LanguageKeys.MAIN_LINE, "Главная линия");
        strings.put(LanguageKeys.ROOT, "КОРЕНЬ");
        strings.put(LanguageKeys.VARIATION_DEFAULT_NAME, "Вариант");

        // Короткие клавиши
        strings.put(LanguageKeys.SHORTCUTS_CONTENT,
                """
                        Горячие клавиши:
                        Ctrl+N - Новая партия
                        Ctrl+O - Открыть PGN
                        Ctrl+S - Сохранить PGN
                        Ctrl+F - Перевернуть доску
                        Ctrl+D - Поиск по базе
                        F1 - Справка
                        Ctrl+Q - Выход""");

        // ========== ENGINE MESSAGES ==========
        strings.put(LanguageKeys.ENGINE_SEND_POSITION_ERROR, "Не удалось отправить позицию");
        strings.put(LanguageKeys.ENGINE_TIMEOUT_ERROR, "Движок не ответил в течение отведённого времени");
        strings.put(LanguageKeys.ENGINE_ANALYSIS_START_ERROR, "Не удалось запустить анализ");
        strings.put(LanguageKeys.ENGINE_STOP_ERROR, "Не удалось остановить анализ");
        strings.put(LanguageKeys.ENGINE_INVALID_UCI_MOVE, "Неверный UCI ход");
        strings.put(LanguageKeys.ENGINE_CONVERT_UCI_ERROR, "Не удалось конвертировать UCI ход");
        strings.put(LanguageKeys.ENGINE_CONVERT_NOTATION_ERROR, "Ошибка конвертации в шахматную нотацию");
        strings.put(LanguageKeys.ENGINE_IMAGE_LOAD_ERROR, "Не удалось загрузить изображение для");
        strings.put(LanguageKeys.ENGINE_ANALYSIS_NOT_ACTIVE, "Анализ не активен. Нажмите Enter для запуска анализа");
        strings.put(LanguageKeys.ENGINE_TERMINAL_POSITION, "Позиция терминальная, ход невозможен");
        strings.put(LanguageKeys.ENGINE_NOT_ANALYZED, "Движок ещё не проанализировал позицию");
        strings.put(LanguageKeys.ENGINE_ILLEGAL_MOVE, "Движок предложил нелегальный ход");
        strings.put(LanguageKeys.ENGINE_MOVE_ERROR, "Ошибка выполнения хода");

        // ========== ANALYSIS PANEL ==========
        strings.put(LanguageKeys.ANALYSIS_TITLE, "Анализ движка");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_STOPPED, "ОСТАНОВЛЕН");
        strings.put(LanguageKeys.ANALYSIS_ANALYZING, "АНАЛИЗИРУЕТ");
        strings.put(LanguageKeys.ANALYSIS_CURRENT_EVAL, "Текущая оценка");
        strings.put(LanguageKeys.ANALYSIS_DEPTH, "глубина");
        strings.put(LanguageKeys.ANALYSIS_ADD_LINE_TOOLTIP, "Добавить линию анализа (макс. %d)");
        strings.put(LanguageKeys.ANALYSIS_REMOVE_LINE_TOOLTIP, "Удалить линию анализа (мин. %d)");
        strings.put(LanguageKeys.ANALYSIS_TOGGLE_TOOLTIP, "Запустить/остановить анализ (Enter)");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_TITLE, "Движок не запущен");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_HEADER, "Шахматный движок не запущен");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_CONTENT, "Пожалуйста, настройте движок через меню Ассистент → Настроить движок");

        // ========== CONFIRM DIALOGS ==========
        strings.put(LanguageKeys.CONFIRM_DELETE_TITLE, "Подтверждение удаления");
        strings.put(LanguageKeys.CONFIRM_DELETE_AFTER_HEADER, "Удалить все ходы после %s?");
        strings.put(LanguageKeys.CONFIRM_DELETE_CONTENT, "Это действие нельзя отменить!");
        strings.put(LanguageKeys.CONFIRM_DELETE_YES, "Да, удалить");
        strings.put(LanguageKeys.CONFIRM_DELETE_NO, "Отмена");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_TITLE, "Подтверждение удаления");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_HEADER, "Удалить вариант \"%s\"?");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_CONTENT, "Это действие нельзя отменить!\nВсе ходы и подварианты будут удалены.");

        // ========== TIMER ==========
        strings.put(LanguageKeys.TIMER_SELECT_TIME, "Выберите время");
        strings.put(LanguageKeys.TIMER_PRESET_1_MIN, "1 мин");
        strings.put(LanguageKeys.TIMER_PRESET_2_MIN, "2 мин");
        strings.put(LanguageKeys.TIMER_PRESET_3_MIN, "3 мин");
        strings.put(LanguageKeys.TIMER_PRESET_5_MIN, "5 мин");
        strings.put(LanguageKeys.TIMER_PRESET_10_MIN, "10 мин");
        strings.put(LanguageKeys.TIMER_CUSTOM_TOOLTIP, "Пользовательская настройка (секунды)");
        strings.put(LanguageKeys.TIMER_CUSTOM_TITLE, "Настройка времени");
        strings.put(LanguageKeys.TIMER_CUSTOM_HEADER, "Введите время в секундах");
        strings.put(LanguageKeys.TIMER_CUSTOM_CONTENT, "Секунды:");
        strings.put(LanguageKeys.TIMER_MAX_TIME_WARNING, "Максимальное время - 60 минут (3600 секунд)");
        strings.put(LanguageKeys.TIMER_INVALID_NUMBER, "Пожалуйста, введите корректное число");

        // ========== MAIN CONTROLLER ==========
        strings.put(LanguageKeys.NEW_GAME_TITLE, "Новая партия");
        strings.put(LanguageKeys.NEW_GAME_HEADER, "Игра закончена");
        strings.put(LanguageKeys.NEW_GAME_CONTENT, "Начать новую партию?");

        strings.put(LanguageKeys.SAVE_GAME_TITLE, "Сохранение партии");
        strings.put(LanguageKeys.SAVE_GAME_HEADER, "Партия не сохранена");
        strings.put(LanguageKeys.SAVE_GAME_CONTENT, "Сохранить текущую партию перед созданием новой?");
        strings.put(LanguageKeys.SAVE_GAME_SAVE, "Сохранить");
        strings.put(LanguageKeys.SAVE_GAME_DONT_SAVE, "Не сохранять");
        strings.put(LanguageKeys.SAVE_GAME_CANCEL, "Отмена");

        strings.put(LanguageKeys.SAVE_GAME_DIALOG_TITLE, "Сохранение партии");
        strings.put(LanguageKeys.SAVE_GAME_DIALOG_HEADER, "Введите информацию о партии");
        strings.put(LanguageKeys.SAVE_GAME_DIALOG_SAVE, "Сохранить");

        strings.put(LanguageKeys.SAVE_GAME_PROMPT_WHITE, "Белые");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_BLACK, "Черные");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_WHITE_ELO, "Рейтинг белых");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_BLACK_ELO, "Рейтинг черных");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_EVENT, "Событие");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_SITE, "Место");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_ROUND, "Раунд");

        strings.put(LanguageKeys.SAVE_GAME_LABEL_WHITE, "Белые");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_BLACK, "Черные");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_RATING, "Рейтинг");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_EVENT, "Событие");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_SITE, "Место");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_ROUND, "Раунд");

        strings.put(LanguageKeys.SAVE_GAME_SUCCESS, "Партия сохранена (в БД будет добавлено позже)");

        strings.put(LanguageKeys.FEATURE_NOT_IMPLEMENTED, "Эта функция будет реализована в будущем");
        strings.put(LanguageKeys.PGN_LOAD_MESSAGE, "Загрузка PGN");
        strings.put(LanguageKeys.PGN_SAVE_MESSAGE, "Сохранение PGN");
        strings.put(LanguageKeys.DB_OPEN_MESSAGE, "Открытие БД");

        // ========== MAIN CONTROLLER MESSAGES ==========
        strings.put(LanguageKeys.MAIN_LOAD_GAME_EMPTY_TREE, "Не удалось загрузить партию: дерево пустое");
        strings.put(LanguageKeys.MAIN_LOADED_POSITION, "Загружена позиция");
        strings.put(LanguageKeys.MAIN_PGN_EMPTY, "PGN пуст");
        strings.put(LanguageKeys.MAIN_CLOSE_BUTTON, "Закрыть");
        strings.put(LanguageKeys.MAIN_NO_CHANGES, "Изменений не обнаружено, сохранение не требуется");
        strings.put(LanguageKeys.MAIN_GAME_SAVED_FILE, "Партия сохранена в файл: %s");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_TITLE, "Сохранение партии");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_HEADER, "Выберите место сохранения");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_CONTENT, "Куда сохранить партию?");
        strings.put(LanguageKeys.MAIN_SAVE_TO_PGN_FILE, "💾 Сохранить в PGN файл");
        strings.put(LanguageKeys.MAIN_SAVE_TO_DATABASE, "📁 Сохранить в базу данных");
        strings.put(LanguageKeys.MAIN_SAVE_CANCEL, "Отмена");
        strings.put(LanguageKeys.MAIN_SAVE_PGN_FILE_TITLE, "Сохранить партию в PGN файл");
        strings.put(LanguageKeys.MAIN_DB_NOT_INITIALIZED_MSG, "База данных не инициализирована");
        strings.put(LanguageKeys.MAIN_GAME_SAVED_TO_DB, "Партия сохранена в базу данных");
        strings.put(LanguageKeys.MAIN_OPEN_PGN_ERROR_MSG, "Ошибка открытия PGN");
        strings.put(LanguageKeys.MAIN_BROWSER_LIMIT_MSG, "Лимит браузеров");
        strings.put(LanguageKeys.MAIN_OPEN_ERROR_MSG, "Ошибка открытия");
        strings.put(LanguageKeys.MAIN_INDEXING_TITLE_MSG, "Индексация PGN файла");
        strings.put(LanguageKeys.MAIN_INDEXING_COMPLETE, "Индексация завершена!\nНайдено партий: %d\nАктивных: %d");
        strings.put(LanguageKeys.MAIN_INDEXING_ERROR_MSG, "Ошибка индексации");
        strings.put(LanguageKeys.MAIN_POSITION_SOLVE_HINT, " (нажмите → для решения)");
        strings.put(LanguageKeys.MAIN_REFRESHED_MSG, "Список обновлен");
        strings.put(LanguageKeys.MAIN_NO_ACTIVE_BROWSER_MSG, "Нет активного браузера");
        strings.put(LanguageKeys.MAIN_UNKNOWN_PATH, "Неизвестно");
        strings.put(LanguageKeys.MAIN_POSITION_LOADED, "Загружена позиция: %s%s");
        strings.put(LanguageKeys.MAIN_GAME_LOADED, "Загружена партия:");

        strings.put(LanguageKeys.ABOUT_TITLE, "О программе Kletka");
        strings.put(LanguageKeys.ABOUT_CONTENT,
                """
                    ♔ Kletka Chess ♔
                    Версия: 1.0
                    Платформа: Java 17, OpenJFX
                    Библиотека: chesslib 1.3.6 (GPL v3)
                    
                    Кроссплатформенный шахматный анализатор
                    с поддержкой баз данных SQLite
                    
                    © 2026 Khrypach Andrey
                    
                    ---------------------------------------------------
                    Лицензия: GNU General Public License v3.0
                    
                    Этот проект является свободным программным обеспечением.
                    Вы можете распространять и/или изменять его
                    в соответствии с условиями GNU General Public License
                    версии 3 или любой более поздней версии.
                    
                    Подробнее: https://www.gnu.org/licenses/gpl-3.0.html
                    ---------------------------------------------------
                    """);

        strings.put(LanguageKeys.POSITION_SET_SUCCESS, "Позиция установлена. Ход %s");

        strings.put(LanguageKeys.ENGINE_SETUP_TITLE, "Настройка движка");
        strings.put(LanguageKeys.ENGINE_SETUP_HEADER, "Шахматный движок не настроен");
        strings.put(LanguageKeys.ENGINE_SETUP_CONTENT,
                """
                        Для работы функции анализа необходимо настроить UCI-совместимый движок.
                        
                        Хотите выбрать движок сейчас?""");
        strings.put(LanguageKeys.ENGINE_SETUP_BUTTON, "Настроить");
        strings.put(LanguageKeys.ENGINE_SETUP_LATER, "Позже");
        strings.put(LanguageKeys.ENGINE_SETUP_SUCCESS, "Движок успешно настроен!");
        strings.put(LanguageKeys.ENGINE_SETUP_ERROR_TITLE, "Ошибка");
        strings.put(LanguageKeys.ENGINE_SETUP_ERROR_CONTENT, "Не удалось запустить движок");

        strings.put(LanguageKeys.ENGINE_SWITCH_TITLE, "Смена движка");
        strings.put(LanguageKeys.ENGINE_SWITCH_HEADER, "Текущий движок будет остановлен");
        strings.put(LanguageKeys.ENGINE_SWITCH_CONTENT, "Продолжить?");
        strings.put(LanguageKeys.ENGINE_SWITCH_SUCCESS, "Движок успешно сменен!");

        strings.put(LanguageKeys.ENGINE_NOT_RUNNING,
                "Движок не запущен. Проверьте наличие файла движка в папке engines/");

        strings.put(LanguageKeys.ANALYSIS_BEST_MOVE, "Лучший ход: %s (%s)");
        strings.put(LanguageKeys.ANALYSIS_ERROR_TITLE, "Ошибка");
        strings.put(LanguageKeys.ANALYSIS_ERROR_CONTENT, "Не удалось распознать ход");

        strings.put(LanguageKeys.CONFIRM_YES, "Да");
        strings.put(LanguageKeys.CONFIRM_NO, "Нет");

        // Логирование
        strings.put(LanguageKeys.LOG_INITIALIZED, "Логирование инициализировано");
        strings.put(LanguageKeys.LOG_STARTING_GUI, "Запуск KletkaGui");
        strings.put(LanguageKeys.LOG_GUI_LOADED, "GUI успешно загружен");
        strings.put(LanguageKeys.LOG_GUI_ERROR, "Ошибка при запуске GUI");
        strings.put(LanguageKeys.LOG_SHUTTING_DOWN, "Kletka завершает работу...");

        // ========== SPLASH SCREEN ==========
        strings.put(LanguageKeys.SPLASH_LOADING_ENGINE, "Загрузка движка...");
        strings.put(LanguageKeys.SPLASH_INITIALIZING_BOARD, "Инициализация доски...");
        strings.put(LanguageKeys.SPLASH_LOADING_GUI, "Загрузка интерфейса...");
        strings.put(LanguageKeys.SPLASH_READY, "Готово!");

        // ==========  GAME TYPE POSITION ==========
        strings.put(LanguageKeys.GAME_TYPE_POSITION, "Позиция");
        strings.put(LanguageKeys.GAME_TYPE_STUDY, "Этюд");
        strings.put(LanguageKeys.GAME_TYPE_PROBLEM, "Задача");
        strings.put(LanguageKeys.GAME_TYPE_GAME, "Партия");
        strings.put(LanguageKeys.DEFAULT_PLAYER_NAME, "Игрок");

        // ==========  PGN KEYWORD MATE ==========
        strings.put(LanguageKeys.PGN_KEYWORD_MATE, "мат");
        strings.put(LanguageKeys.PGN_KEYWORD_STUDY, "этюд");

        // ==========  REPO ERROR ==========
        strings.put(LanguageKeys.REPO_ERROR_CREATE_DIR, "Не удалось создать директорию: %s");
        strings.put(LanguageKeys.REPO_ERROR_GAME_NULL, "Игра не может быть null");
        strings.put(LanguageKeys.REPO_ERROR_SAVE_GAME, "Не удалось сохранить игру: %s");
        strings.put(LanguageKeys.REPO_ERROR_READ_DIR, "Не удалось прочитать директорию: %s");
        strings.put(LanguageKeys.REPO_ERROR_DELETE_GAME, "Не удалось удалить игру: %s");
        strings.put(LanguageKeys.REPO_ERROR_DELETE_ALL, "Не удалось удалить игры");
        strings.put(LanguageKeys.REPO_ERROR_COUNT, "Не удалось подсчитать игры");
        strings.put(LanguageKeys.REPO_ERROR_READ_FILE, "Не удалось прочитать файл: %s");
        strings.put(LanguageKeys.REPO_ERROR_PARSE_PGN, "Ошибка парсинга PGN: %s");
        strings.put(LanguageKeys.REPO_ERROR_GAMES_EMPTY, "Список игр пуст");
        strings.put(LanguageKeys.REPO_ERROR_EXPORT, "Не удалось экспортировать игры в: %s");

        // ========== EXPORT ERROR ==========
        strings.put(LanguageKeys.EXPORT_ERROR_GAMES_EMPTY, "Список игр пуст");
        strings.put(LanguageKeys.EXPORT_ERROR_FILE_NULL, "Файл не может быть null");
        strings.put(LanguageKeys.EXPORT_ERROR_EXPORT_FAILED, "Не удалось экспортировать игры в: %s");
        strings.put(LanguageKeys.EXPORT_ERROR_NO_GAMES, "Нет игр для экспорта");
        strings.put(LanguageKeys.EXPORT_ERROR_CREATE_DIR, "Не удалось создать директорию: %s");
        strings.put(LanguageKeys.EXPORT_ERROR_GAME_NULL, "Игра не может быть null");

        // ========== IMPORT ERROR ==========
        strings.put(LanguageKeys.IMPORT_ERROR_FILE_NOT_FOUND, "Файл не найден: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_READ_FILE, "Не удалось прочитать файл: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_PARSE_PGN, "Ошибка парсинга PGN в файле: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_PGN_EMPTY, "PGN содержимое пустое");
        strings.put(LanguageKeys.IMPORT_ERROR_PARSE_GENERAL, "Ошибка парсинга PGN");
        strings.put(LanguageKeys.IMPORT_ERROR_DIR_NOT_FOUND, "Директория не найдена: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_DIR_NOT_FOUND_SIMPLE, "Директория не найдена");
        strings.put(LanguageKeys.IMPORT_ERROR_READ_DIR, "Не удалось прочитать директорию: %s");

        // PGN service errors
        strings.put(LanguageKeys.PGN_SERVICE_ERROR_GAME_NULL, "Игра не может быть null");
        strings.put(LanguageKeys.PGN_SERVICE_ERROR_GAMES_EMPTY, "Список игр пуст");

        // Donate dialog
        strings.put(LanguageKeys.DONATE_TITLE, "☕ Поддержать проект");
        strings.put(LanguageKeys.DONATE_HEADER, "☕ Поддержать проект Kletka");
        strings.put(LanguageKeys.DONATE_DESCRIPTION, "Спасибо, что используете Kletka!\nВаша поддержка помогает развитию проекта.");
        strings.put(LanguageKeys.DONATE_HINT, "💡 Нажмите «Копировать», чтобы скопировать адрес в буфер обмена.");
        strings.put(LanguageKeys.DONATE_CLOSE, "Закрыть");
        strings.put(LanguageKeys.DONATE_PAYPAL, "📧 PayPal");
        strings.put(LanguageKeys.DONATE_COPY, "📋 Копировать");
        strings.put(LanguageKeys.DONATE_OPEN, "🌐 Открыть");
        strings.put(LanguageKeys.DONATE_BITCOIN, "₿ Bitcoin");
        strings.put(LanguageKeys.DONATE_QR, "📱 QR-код");
        strings.put(LanguageKeys.DONATE_QR_TITLE, "QR-код для Bitcoin");
        strings.put(LanguageKeys.DONATE_QR_HINT, "Отсканируйте QR-код в вашем Bitcoin кошельке");
        strings.put(LanguageKeys.DONATE_TOAST_COPIED_EMAIL, "Email скопирован!");
        strings.put(LanguageKeys.DONATE_TOAST_COPIED_BITCOIN, "Bitcoin адрес скопирован!");
        strings.put(LanguageKeys.DONATE_TOAST_OPEN_BROWSER, "Не удалось открыть браузер");
        strings.put(LanguageKeys.DONATE_TOAST_QR_ERROR, "Ошибка генерации QR-кода");

        // Position setup dialog
        strings.put(LanguageKeys.SETUP_DELETE_MODE_ACTIVE_TOOLTIP, "Сначала выключите режим удаления");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_NOTIFICATION, "Ход автоматически изменен на %s (король %s был под шахом)");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_WHITE, "белых");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_BLACK, "черных");
        strings.put(LanguageKeys.SETUP_LOAD_POSITION_ERROR, "Ошибка при загрузке позиции: %s");

        // SaveGameDialog - Titles
        strings.put(LanguageKeys.SAVE_DIALOG_TITLE_EDIT, "✏️ Редактирование партии");
        strings.put(LanguageKeys.SAVE_DIALOG_TITLE_SAVE, "💾 Сохранение партии");
        strings.put(LanguageKeys.SAVE_DIALOG_HEADER_EDIT, "Редактирование информации о партии");
        strings.put(LanguageKeys.SAVE_DIALOG_HEADER_SAVE, "Введите информацию о партии");

        // Tabs
        strings.put(LanguageKeys.SAVE_TAB_PLAYERS, "Игроки и результат");
        strings.put(LanguageKeys.SAVE_TAB_TOURNAMENT, "Турнир");
        strings.put(LanguageKeys.SAVE_TAB_DETAILS, "Детали");

        // Players tab
        strings.put(LanguageKeys.SAVE_LABEL_WHITE, "Белые:");
        strings.put(LanguageKeys.SAVE_LABEL_BLACK, "Черные:");
        strings.put(LanguageKeys.SAVE_LABEL_ELO_WHITE, "Рейтинг белых:");
        strings.put(LanguageKeys.SAVE_LABEL_ELO_BLACK, "Рейтинг черных:");
        strings.put(LanguageKeys.SAVE_LABEL_WHITE_TEAM, "Команда белых:");
        strings.put(LanguageKeys.SAVE_LABEL_BLACK_TEAM, "Команда черных:");
        strings.put(LanguageKeys.SAVE_LABEL_ANNOTATOR, "Комментатор:");
        strings.put(LanguageKeys.SAVE_LABEL_RESULT, "Результат:");
        strings.put(LanguageKeys.SAVE_RESULT_1_0, "1-0");
        strings.put(LanguageKeys.SAVE_RESULT_0_1, "0-1");
        strings.put(LanguageKeys.SAVE_RESULT_DRAW, "1/2-1/2");
        strings.put(LanguageKeys.SAVE_RESULT_UNKNOWN, "*");

        // Tournament tab
        strings.put(LanguageKeys.SAVE_LABEL_EVENT, "Событие:");
        strings.put(LanguageKeys.SAVE_LABEL_SITE, "Место:");
        strings.put(LanguageKeys.SAVE_LABEL_ROUND, "Раунд:");
        strings.put(LanguageKeys.SAVE_LABEL_SUBROUND, "Подраунд:");
        strings.put(LanguageKeys.SAVE_LABEL_DATE, "Дата:");
        strings.put(LanguageKeys.SAVE_LABEL_YEAR, "Год:");
        strings.put(LanguageKeys.SAVE_LABEL_MONTH, "Месяц:");
        strings.put(LanguageKeys.SAVE_LABEL_DAY, "День:");
        strings.put(LanguageKeys.SAVE_BUTTON_RESET_DATE, "Сбросить");

        // Details tab
        strings.put(LanguageKeys.SAVE_LABEL_ECO, "Код ECO:");
        strings.put(LanguageKeys.SAVE_LABEL_OPENING, "Дебют:");
        strings.put(LanguageKeys.SAVE_LABEL_VARIATION, "Вариант:");
        strings.put(LanguageKeys.SAVE_LABEL_TIME_CONTROL, "Контроль времени:");
        strings.put(LanguageKeys.SAVE_LABEL_SOURCE, "Источник:");
        strings.put(LanguageKeys.SAVE_LABEL_FEN, "FEN:");
        strings.put(LanguageKeys.SAVE_CHECKBOX_SETUP, "SetUp (позиция)");
        strings.put(LanguageKeys.SAVE_LABEL_TYPE, "Тип:");
        strings.put(LanguageKeys.SAVE_BUTTON_DETECT_OPENING, "🎯 Определить дебют");

        // Buttons
        strings.put(LanguageKeys.SAVE_BUTTON_SAVE, "Сохранить");
        strings.put(LanguageKeys.SAVE_BUTTON_SAVE_CHANGES, "Сохранить изменения");
        strings.put(LanguageKeys.SAVE_BUTTON_CANCEL, "Отмена");
        strings.put(LanguageKeys.SAVE_BUTTON_HELP, "Помощь");

        // Messages
        strings.put(LanguageKeys.SAVE_MSG_ECO_NOT_LOADED, "База дебютов еще не загружена. Попробуйте позже.");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_FOUND, "Найден дебют: %s - %s");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_NOT_FOUND, "Дебют не найден");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_ERROR, "Ошибка определения дебюта: %s");

        // Help dialog
        strings.put(LanguageKeys.SAVE_HELP_TITLE, "Помощь");
        strings.put(LanguageKeys.SAVE_HELP_HEADER, "Сохранение партии в PGN");
        strings.put(LanguageKeys.SAVE_HELP_CONTENT,
                """
                        Заполните информацию о партии:
                        
                        • Игроки - имена белых и черных
                        • Рейтинги - рейтинг ELO каждого игрока
                        • Результат - исход партии
                        • Турнир - название, место, раунд
                        • Дата - год, месяц, день
                        • ECO - код дебюта по энциклопедии
                        • Дебют - название дебюта
                        • Вариант - вариант дебюта
                        
                        ДЛЯ ПОЗИЦИЙ:
                        • FEN - позиция в формате FEN
                        • SetUp - отметить, что это позиция
                        • Тип - game, position, study, problem
                        
                        Поля со знаком "?" будут заменены на стандартные значения.""");

        // Type options
        strings.put(LanguageKeys.SAVE_TYPE_GAME, "Партия");
        strings.put(LanguageKeys.SAVE_TYPE_POSITION, "Позиция");
        strings.put(LanguageKeys.SAVE_TYPE_STUDY, "Этюд");
        strings.put(LanguageKeys.SAVE_TYPE_PROBLEM, "Задача");

        // Logo
        strings.put(LanguageKeys.LOGO_TITLE, "КЛЕТКА");
        strings.put(LanguageKeys.LOGO_SUBTITLE_LINE1, "ШАХМАТНЫЙ");
        strings.put(LanguageKeys.LOGO_SUBTITLE_LINE2, "АНАЛИЗАТОР");

        //Menu
        strings.put(LanguageKeys.MENU_WINDOWS, "Окна");
        strings.put(LanguageKeys.MENU_WINDOWS_CLIPBOARD_EMPTY, "📋 Буфер: пуст");
        strings.put(LanguageKeys.MENU_WINDOWS_CLIPBOARD_CONTENT, "📋 Буфер: %d партий из '%s'");
        strings.put(LanguageKeys.MENU_WINDOWS_CLEAR_CLIPBOARD, "🧹 Очистить буфер");
        strings.put(LanguageKeys.MENU_WINDOWS_CLOSE_ALL, "✕ Закрыть все");
        strings.put(LanguageKeys.MENU_WINDOWS_NO_FILES, "(нет открытых файлов)");
        strings.put(LanguageKeys.MENU_WINDOWS_BROWSER_ITEM, "📁 %s (%d партий)");

        strings.put(LanguageKeys.MENU_FILE_OPEN_BROWSER, "Открыть браузер PGN");
        strings.put(LanguageKeys.MENU_FILE_REFRESH_BROWSER, "Обновить браузер");

        strings.put(LanguageKeys.MENU_VIEW_TOGGLE_NOTATION, "Показать/скрыть нотацию");

        strings.put(LanguageKeys.MENU_HELP_DONATE, "☕ Поддержать проект");

        strings.put(LanguageKeys.BROWSER_GAMES_COUNT, "партий");
        strings.put(LanguageKeys.BROWSER_CLIPBOARD_COUNT, "партий");

        // PGN Browser Manager
        strings.put(LanguageKeys.PGN_BROWSER_LIMIT_REACHED, "Достигнут лимит открытых браузеров (%d). Закройте один из файлов.");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_LIMIT, "Нельзя скопировать более %d партий за раз");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_UNAVAILABLE, "Вставка недоступна");
        strings.put(LanguageKeys.PGN_BROWSER_NO_INDEX, "Целевой файл не имеет индекса: %s");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_ERROR, "❌ Недостаточно места на диске!");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_CHECK, "Требуется: ~%.1f MB\nДоступно: %.1f MB\n\nОсвободите место на диске и попробуйте снова.");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_INSUFFICIENT, "❌ Недостаточно места на диске!\n\nВставка прервана после %d партий.\nФайл и индекс будут автоматически восстановлены при следующем открытии.\n\nОсвободите место на диске и попробуйте снова.");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_INTERRUPTED, "❌ Ошибка при вставке партий\n\nБыло добавлено %d партий.\nИндекс файла будет обновлен при следующем открытии.\n\nПричина: %s");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_ERROR, "Ошибка при вставке партий: %s");

        // Progress messages
        strings.put(LanguageKeys.PGN_BROWSER_COPY_PREPARING, "Подготовка к копированию...");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_TOTAL, "Всего: %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_COMPLETE, "✅ Скопировано %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_SOURCE, "Источник: %s");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_PREPARING, "Подготовка к вставке...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_TOTAL, "Всего: %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_PROGRESS, "Вставка: %d из %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_ADDED, "Добавлено %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_COMPLETE, "✅ Вставлено %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_TARGET, "Целевой файл: %s");

        // Indexing Progress
        strings.put(LanguageKeys.INDEXING_PROGRESS_MESSAGE, "Обработано %d из %d партий (%.1f%%)");
        strings.put(LanguageKeys.INDEXING_STATUS_STARTING, "Начало индексации...");
        strings.put(LanguageKeys.INDEXING_STATUS_COMPLETE, "Индексация завершена!");
        // PGN Game Operation
        strings.put(LanguageKeys.PGN_OP_EDIT_SUCCESS, "Партия #%d отредактирована");
        strings.put(LanguageKeys.PGN_OP_DELETE_SUCCESS, "Партия #%d удалена");
        strings.put(LanguageKeys.PGN_OP_ADD_SUCCESS, "Новая партия #%d добавлена");
        strings.put(LanguageKeys.PGN_OP_DUPLICATE_SUCCESS, "Партия #%d дублирована как #%d");

        // Delete Confirm Dialog
        strings.put(LanguageKeys.DELETE_CONFIRM_TITLE, "Подтверждение удаления");
        strings.put(LanguageKeys.DELETE_CONFIRM_SINGLE_TITLE, "🗑️ Удаление партии");
        strings.put(LanguageKeys.DELETE_CONFIRM_MULTIPLE_TITLE, "🗑️ Удаление %d партий");
        strings.put(LanguageKeys.DELETE_CONFIRM_SINGLE_MESSAGE, "Вы уверены, что хотите удалить партию #%d?\n\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_MULTIPLE_MESSAGE, "Вы уверены, что хотите удалить %d партий?\n\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_WHITE, "Белые: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_BLACK, "Черные: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_RESULT, "Результат: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_AND_MORE, "  ... и ещё %d партий\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_WARNING, "\n⚠️ Это действие необратимо до следующей перепаковки.");
        strings.put(LanguageKeys.DELETE_CONFIRM_DELETE_BUTTON, "🗑️ Удалить");
        strings.put(LanguageKeys.DELETE_CONFIRM_CANCEL_BUTTON, "Отмена");
        strings.put(LanguageKeys.DELETE_CONFIRM_UNKNOWN, "?");
        strings.put(LanguageKeys.DELETE_CONFIRM_GAME_PREFIX, "  #%d: %s vs %s (%s)\n");

        // Indexing Progress Dialog
        strings.put(LanguageKeys.INDEXING_DIALOG_TITLE, "Индексация PGN файла");
        strings.put(LanguageKeys.INDEXING_DIALOG_STATUS_PREPARING, "Подготовка к индексации...");
        strings.put(LanguageKeys.INDEXING_DIALOG_GAMES_PROCESSED, "%d партий обработано");
        strings.put(LanguageKeys.INDEXING_DIALOG_COMPLETE, "Завершено!");
        strings.put(LanguageKeys.INDEXING_DIALOG_CANCEL, "Отмена");
        strings.put(LanguageKeys.INDEXING_DIALOG_ERROR, "❌ %s");
        strings.put(LanguageKeys.INDEXING_DIALOG_PROGRESS_FORMAT, "%d / %d (%.1f%%)");

        // ========== PGN BROWSER - TABLE COLUMNS ==========
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_ID, "№");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_WHITE, "Белые");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_BLACK, "Черные");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_RESULT, "Результат");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_YEAR, "Год");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_EVENT, "Турнир");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_ECO, "ECO");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_OPENING, "Дебют");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_BODY, "Партия");

        // ========== PGN BROWSER - WINDOW ==========
        strings.put(LanguageKeys.PGN_BROWSER_TITLE, "PGN Браузер - %s");
        strings.put(LanguageKeys.PGN_BROWSER_TITLE_ACTIVE, " ✅ Активен");
        strings.put(LanguageKeys.PGN_BROWSER_TITLE_GAMES, " (%d партий)");

        // ========== PGN BROWSER - SEARCH ==========
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_LABEL, "🔍 Поиск:");
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_PROMPT, "Введите имя игрока, дебют или ECO...");
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_CLEAR, "Сброс");

        // ========== PGN BROWSER - STATUS ==========
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING, "Загрузка...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_TOTAL, "Всего: %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_SELECTED, "Выбрано: %d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_SHOWN, "Показано: %d из %d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_CLOSE, "✕ Закрыть");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_READY, "Готово");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ERROR, "Ошибка: %s");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_INDEX, "Загрузка индекса...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_PARSING, "Парсинг PGN файла...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_MORE, "Загрузка партий...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ALL_LOADED, "Все партии загружены");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADED, "Загружено %d из %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_CHECKING_INDEX, "Проверка индекса...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_GAME, "Загрузка партии...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_READY_WITH_COUNT, "Готово (%d партий)");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ALL_LOADED_WITH_COUNT, "Все партии загружены (%d)");

        // ========== PGN BROWSER - BUTTONS ==========
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_EDIT, "✏️ Редактировать");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DELETE, "🗑️ Удалить");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DELETE_COUNT, "🗑️ Удалить (%d)");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DUPLICATE, "📋 Дублировать");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_COPY, "📋 Копировать");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_PASTE, "📋 Вставить");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK, "🔄 Перепаковать");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK_COUNT, "🔄 Перепаковать (%d)");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK_IN_PROGRESS, "⏳ Перепаковка...");

        // ========== PGN BROWSER - CONTEXT MENU ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_LOAD, "Загрузить партию");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_COPY, "📋 Копировать");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_DELETE, "🗑️ Удалить");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_SELECT_ALL, "Выбрать все (Ctrl+A)");

        // ========== PGN BROWSER - MESSAGES ==========
        strings.put(LanguageKeys.PGN_BROWSER_MSG_REPACK_IN_PROGRESS, "⏳ Перепаковка выполняется, подождите...");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_ONE, "Выберите ОДНУ партию для редактирования");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_UNAVAILABLE, "Редактирование временно недоступно");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_GAMES, "Выберите партии для удаления");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_UNAVAILABLE, "Удаление недоступно для этого режима");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_LIMIT, "Нельзя скопировать более 1000 партий за раз. Выбрано: %d");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_UNAVAILABLE, "Копирование недоступно для этого режима");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_UNAVAILABLE, "Дублирование недоступно");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_UNAVAILABLE, "Вставка недоступна. Буфер пуст или целевой файл совпадает с источником.");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_CLIPBOARD_EMPTY, "Буфер пуст");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_NO_DELETED_GAMES, "Нет удалённых партий для перепаковки");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_SUCCESS, "Скопировано %d партий из '%s'");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_SUCCESS, "Вставлено %d партий в '%s'");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_SUCCESS, "Удалено %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_SUCCESS, "Партия дублирована как #%d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ERROR_LOADING, "Ошибка загрузки: %s");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_OPERATION_FINISHED, "Операция завершена");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_ERROR, "Ошибка удаления: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_ONE_DUPLICATE, "Выберите ОДНУ партию для дублирования");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_ERROR, "Ошибка дублирования: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_GAMES_COPY, "Выберите партии для копирования");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_ERROR, "Ошибка копирования: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY, "📋 Копирование партий");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_START, "Начинаем копирование...");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_ERROR, "Ошибка вставки: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_GAMES, "📋 Вставка партий");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_START, "Начинаем вставку...");

        // ========== PGN BROWSER - PROGRESS ==========
        strings.put(LanguageKeys.PGN_BROWSER_DELETING, "Удаление %d партий...");
        strings.put(LanguageKeys.PGN_BROWSER_DELETING_PROCEED, "Удаление: %d из %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_DELETED, "Удалено %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_DUPLICATING, "Дублирование партии...");
        strings.put(LanguageKeys.PGN_BROWSER_COPYING, "Копирование %d партий...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTING, "Вставка %d партий...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTED, "Вставлено %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_REPACKING, "Перепаковка...");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_COMPLETE, "✅ Перепаковка завершена успешно!\nАктивных партий: %d");
        strings.put(LanguageKeys.PGN_BROWSER_START_DELETING, "Начинаем удаление...");

        // ========== PGN BROWSER - CONFIRM PASTE ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_TITLE, "Вставка партий");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_HEADER, "Вставить %d партий?");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_SOURCE, "Источник: %s");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_TARGET, "Целевой файл: %s");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_COUNT, "Партий: %d");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_FREE_SPACE, "Свободно на диске: %s MB");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_INFO, "Партии будут добавлены в конец файла.");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_YES, "✅ Вставить");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_NO, "Отмена");

        // ========== PGN BROWSER - CONFIRM REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_TITLE, "Перепаковка PGN файла");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_HEADER, "Выполнить ручную перепаковку?");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_CONTENT, "Обнаружено %d удалённых партий.\nАктивных партий: %d\nСоотношение размера: %.1fx\n\nБудет создан новый файл без удалённых партий.\n⚠️ Во время перепаковки редактирование будет заблокировано.");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_YES, "✅ Перепаковать");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_NO, "Отмена");

        // ========== PGN BROWSER - AUTO REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_TITLE, "⚠️ Требуется перепаковка");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_HEADER, "PGN файл разросся в %.1f раз!");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_CONTENT, "Файл содержит %d активных и %d удалённых партий.\n\nРекомендуется выполнить перепаковку для оптимизации.");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_YES, "✅ Да, перепаковать");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_NO, "Позже");

        // ========== PGN BROWSER - FILTER ==========
        strings.put(LanguageKeys.PGN_BROWSER_FILTER_TOTAL, "Всего: %d партий");
        strings.put(LanguageKeys.PGN_BROWSER_FILTER_FOUND, "Найдено: %d из %d партий");

        // ========== PGN BROWSER - REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_TITLE, "🔄 Перепаковка PGN файла");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_IN_PROGRESS, "Перепаковка выполняется, пожалуйста, подождите...");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_ERROR, "❌ Ошибка перепаковки: %s");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_SUCCESS, "✅ Перепаковка завершена успешно!\nАктивных партий: %d");

        // Repack Progress Dialog
        strings.put(LanguageKeys.REPACK_DIALOG_TITLE, "🔄 Перепаковка PGN файла");
        strings.put(LanguageKeys.REPACK_DIALOG_STATUS_PREPARING, "Подготовка к перепаковке...");
        strings.put(LanguageKeys.REPACK_DIALOG_PROGRESS_FORMAT, "%d / %d (%.1f%%)");
        strings.put(LanguageKeys.REPACK_DIALOG_GAMES_PROCESSED, "%d партий обработано");
        strings.put(LanguageKeys.REPACK_DIALOG_COMPLETE, "✅ Завершено!");
        strings.put(LanguageKeys.REPACK_DIALOG_ERROR, "❌ %s");

        // Repack Status Widget
        strings.put(LanguageKeys.REPACK_STATUS_OPTIMAL, "Оптимально");
        strings.put(LanguageKeys.REPACK_STATUS_NO_DELETED, "✅ Нет удалённых");
        strings.put(LanguageKeys.REPACK_STATUS_HAS_DELETED, "✅ Есть удалённые");
        strings.put(LanguageKeys.REPACK_STATUS_WARNING, "⚠️ Рекомендуется перепаковка");
        strings.put(LanguageKeys.REPACK_STATUS_CRITICAL, "🔴 Требуется перепаковка!");
        strings.put(LanguageKeys.REPACK_STATUS_UNKNOWN, "Неизвестно");
        strings.put(LanguageKeys.REPACK_STATUS_REPACKING, "🔄 Перепаковка...");
        strings.put(LanguageKeys.REPACK_STATUS_DELETED_COUNT, "🗑️ %d");
        strings.put(LanguageKeys.REPACK_STATUS_TOOLTIP, "Соотношение размера файла к активным данным: %.1fx\nУдалённых партий: %d\nАктивных партий: %d");
        strings.put(LanguageKeys.REPACK_STATUS_REPACKING_TOOLTIP, "Перепаковка выполняется");
        strings.put(LanguageKeys.REPACK_STATUS_RATIO, "%.1fx");
        strings.put(LanguageKeys.REPACK_STATUS_LOADING, "...");

        // Indexing
        strings.put(LanguageKeys.INDEXING_STATUS_SCANNING_FILE, "Индексация: сканирование файла...");
        strings.put(LanguageKeys.INDEXING_STATUS_CREATING_INDEX, "Индексация: создание индекса...");
        strings.put(LanguageKeys.INDEXING_STATUS_PREPARING_FILE, "Подготовка файла: добавление тега [Deleted]");
        strings.put(LanguageKeys.INDEXING_STATUS_SCANNING_GAMES, "Индексация: сканирование %d партий...");
        strings.put(LanguageKeys.INDEXING_STATUS_PROCESSED, "Индексация: обработано %d из %d партий");
        strings.put(LanguageKeys.INDEXING_STATUS_PREPARING_ADD_DELETED, "Подготовка файла: добавление [Deleted]");

        // File Preparation
        strings.put(LanguageKeys.PREPARE_STATUS_SCANNING, "Подготовка файла: сканирование...");
        strings.put(LanguageKeys.PREPARE_STATUS_PROCESSED, "Подготовка файла: обработано %d из %d партий");
        strings.put(LanguageKeys.PREPARE_STATUS_BUILDING, "Подготовка файла: сборка...");
        strings.put(LanguageKeys.PREPARE_STATUS_BUILDING_BLOCKS, "Подготовка файла: сборка %d из %d блоков");
        strings.put(LanguageKeys.PREPARE_STATUS_SAVING, "Подготовка файла: сохранение...");
        strings.put(LanguageKeys.PREPARE_STATUS_COMPLETE, "Подготовка завершена: %d партий, %d мусорных блоков");

        // Indexing Facade
        strings.put(LanguageKeys.INDEXING_STEP1, "Шаг 1/3: Подготовка файла...");
        strings.put(LanguageKeys.INDEXING_STEP2, "Шаг 2/3: Создание индекса...");
        strings.put(LanguageKeys.INDEXING_STEP3, "Шаг 3/3: Сохранение индекса...");
        strings.put(LanguageKeys.INDEXING_COMPLETE_SUCCESS, "Индексация завершена успешно!");

        // Repack
        strings.put(LanguageKeys.REPACK_STATUS_READING, "Перепаковка: чтение партий...");
        strings.put(LanguageKeys.REPACK_STATUS_PROCESSED, "Перепаковка: обработано %d из %d партий");
        strings.put(LanguageKeys.REPACK_STATUS_WRITING, "Перепаковка: запись нового файла...");
        strings.put(LanguageKeys.REPACK_STATUS_CREATING_INDEX, "Перепаковка: создание индекса...");
        strings.put(LanguageKeys.REPACK_STATUS_SAVING_INDEX, "Перепаковка: сохранение индекса...");
        strings.put(LanguageKeys.REPACK_STATUS_REPLACING, "Перепаковка: замена файлов...");
        strings.put(LanguageKeys.REPACK_STATUS_COMPLETE, "✅ Перепаковка завершена! %d партий, размер: %.2f KB");

        // Repack Status Descriptions
        strings.put(LanguageKeys.REPACK_DESC_NO_GAMES, "Нет партий");
        strings.put(LanguageKeys.REPACK_DESC_NO_DELETED, "✅ Нет удалённых партий");
        strings.put(LanguageKeys.REPACK_DESC_HAS_DELETED, "✅ Есть удалённые (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_WARNING, "⚠️ Рекомендуется (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_CRITICAL, "🔴 Требуется! (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_DELETED_COUNT, ", %d удалённых");

        // ========== LANGUAGE MENU ==========
        strings.put(LanguageKeys.MENU_LANGUAGE, "Язык");
        strings.put(LanguageKeys.MENU_LANGUAGE_RUSSIAN, "Русский");
        strings.put(LanguageKeys.MENU_LANGUAGE_ENGLISH, "Английский");
        strings.put(LanguageKeys.MENU_LANGUAGE_CHINESE, "Китайский");
        strings.put(LanguageKeys.MENU_LANGUAGE_CHANGE, "Сменить язык");

        // ========== LANGUAGE CHANGE DIALOG ==========
        strings.put(LanguageKeys.LANG_CHANGE_TITLE, "Язык сохранен");
        strings.put(LanguageKeys.LANG_CHANGE_HEADER, "Язык изменен на: %s");
        strings.put(LanguageKeys.LANG_CHANGE_CONTENT, "Для применения всех изменений перезапустите приложение.");
        strings.put(LanguageKeys.LANG_CHANGE_BUTTON_OK, "OK");

        // ========== PGN BROWSER - EDIT HEADERS ==========
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_HEADERS_TITLE, "✏️ Редактирование заголовков");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_HEADERS_HEADER, "Редактирование заголовков партии #%s");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVE, "💾 Сохранить заголовки");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_CANCEL, "❌ Отмена");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_LOADING, "⏳ Загрузка партии для редактирования...");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVING, "⏳ Сохранение изменений...");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVED, "✅ Изменения сохранены");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_BODY_INFO, "📄 Тело партии (%d ходов) - только для чтения");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_BODY_EMPTY, "📄 Тело партии пусто");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_WHITE, "Имя белого игрока");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_BLACK, "Имя черного игрока");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_EVENT, "Название турнира");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_SITE, "Место проведения");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_ROUND, "Номер тура");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_OPENING, "Название дебюта");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_VARIATION, "Вариант дебюта");

        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_SUCCESS, "✅ Заголовки партии успешно обновлены");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_ERROR, "❌ Ошибка при редактировании: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_READ_ONLY, "Только для чтения");

        // ========== SHORTCUTS ==========
        strings.put(LanguageKeys.SHORTCUTS_TITLE, "Горячие клавиши");

        // Собираем полный список горячих клавиш с группировкой
        StringBuilder shortcutsContent = createShortcutsContent();

        strings.put(LanguageKeys.SHORTCUTS_CONTENT, shortcutsContent.toString());

        // Отдельные ключи для категорий (опционально, для использования в других местах)
        strings.put(LanguageKeys.SHORTCUT_FILE, "📁 Файл");
        strings.put(LanguageKeys.SHORTCUT_VIEW, "👁️ Вид");
        strings.put(LanguageKeys.SHORTCUT_NAVIGATION, "🧭 Навигация");
        strings.put(LanguageKeys.SHORTCUT_ENGINE, "⚙️ Движок");
        strings.put(LanguageKeys.SHORTCUT_PGN, "📚 PGN/Браузер");
        strings.put(LanguageKeys.SHORTCUT_DATABASE, "🗄️ База данных");
        strings.put(LanguageKeys.SHORTCUT_WINDOWS, "🪟 Окна");

        // Отдельные хоткеи
        strings.put(LanguageKeys.SHORTCUT_NEW_GAME, "Ctrl+N - Новая партия");
        strings.put(LanguageKeys.SHORTCUT_OPEN_PGN, "Ctrl+O - Открыть PGN");
        strings.put(LanguageKeys.SHORTCUT_SAVE_PGN, "Ctrl+S - Сохранить PGN");
        strings.put(LanguageKeys.SHORTCUT_EXPORT_CURRENT, "Ctrl+E - Экспорт текущей партии");
        strings.put(LanguageKeys.SHORTCUT_IMPORT_CLIPBOARD, "Ctrl+Shift+V - Импорт из буфера обмена");
        strings.put(LanguageKeys.SHORTCUT_SETUP_POSITION, "Ctrl+P - Расстановка позиции");
        strings.put(LanguageKeys.SHORTCUT_EXIT, "Alt+F4 - Выход");

        strings.put(LanguageKeys.SHORTCUT_FLIP_BOARD, "Ctrl+F - Перевернуть доску");
        strings.put(LanguageKeys.SHORTCUT_COORDINATES, "Ctrl+Shift+C - Показать координаты");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_IN, "Ctrl+= - Увеличить");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_OUT, "Ctrl+- - Уменьшить");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_RESET, "Ctrl+0 - Сброс масштаба");
        strings.put(LanguageKeys.SHORTCUT_TOGGLE_NOTATION, "H - Показать/скрыть нотацию");

        strings.put(LanguageKeys.SHORTCUT_NAV_PREV, "← - Предыдущий ход");
        strings.put(LanguageKeys.SHORTCUT_NAV_NEXT, "→ - Следующий ход");
        strings.put(LanguageKeys.SHORTCUT_NAV_FIRST, "↑ - Первый ход");
        strings.put(LanguageKeys.SHORTCUT_NAV_LAST, "↓ - Последний ход");

        strings.put(LanguageKeys.SHORTCUT_ENGINE_MOVE, "Space - Ход движка");
        strings.put(LanguageKeys.SHORTCUT_ENGINE_ANALYZE, "Shift+Enter - Включить/выключить анализ");
        strings.put(LanguageKeys.SHORTCUT_ENGINE_CONFIGURE, "Ctrl+Shift+E - Настройка движка");

        strings.put(LanguageKeys.SHORTCUT_OPEN_BROWSER, "Ctrl+B - Открыть браузер PGN");
        strings.put(LanguageKeys.SHORTCUT_REFRESH_BROWSER, "Ctrl+R - Обновить браузер");
        strings.put(LanguageKeys.SHORTCUT_NEXT_GAME, "F11 - Следующая партия");
        strings.put(LanguageKeys.SHORTCUT_PREV_GAME, "Ctrl+F11 - Предыдущая партия");
        strings.put(LanguageKeys.SHORTCUT_NEXT_BROWSER, "Ctrl+Tab - Следующий браузер");
        strings.put(LanguageKeys.SHORTCUT_PREV_BROWSER, "Ctrl+Shift+Tab - Предыдущий браузер");
        strings.put(LanguageKeys.SHORTCUT_CLOSE_BROWSER, "Ctrl+W - Закрыть браузер");
        strings.put(LanguageKeys.SHORTCUT_MINIMIZE_BROWSER, "Свернуть браузер");
        strings.put(LanguageKeys.SHORTCUT_MAXIMIZE_BROWSER, "Развернуть браузер");

        strings.put(LanguageKeys.SHORTCUT_CONNECT_DB, "Ctrl+D - Подключиться к БД");
        strings.put(LanguageKeys.SHORTCUT_IMPORT_DB, "Ctrl+I - Импорт в БД");
        strings.put(LanguageKeys.SHORTCUT_SEARCH_DB, "Ctrl+Shift+F - Поиск в БД");

        strings.put(LanguageKeys.SHORTCUT_CLOSE_ALL_BROWSERS, "Ctrl+Shift+W - Закрыть все браузеры");

        strings.put(LanguageKeys.FAILED_TOKENIZE_PGN, "Невозможно токенизировать PGN");
        strings.put(LanguageKeys.ERROR_PARSING_MOVE, "Ошибка парсинга хода: %s");
        strings.put(LanguageKeys.ERROR_PARSING_PGN_TO_TREE, "Ошибка парсинга PGN в дерево вариантов: %s");
    }

    private static StringBuilder createShortcutsContent() {
        StringBuilder shortcutsContent = new StringBuilder();

        // 📁 Файл
        shortcutsContent.append("═══════════════════════════════════════\n");
        shortcutsContent.append("📁 Файл:\n");
        shortcutsContent.append("  Ctrl+N - Новая партия\n");
        shortcutsContent.append("  Ctrl+O - Открыть PGN\n");
        shortcutsContent.append("  Ctrl+S - Сохранить PGN\n");
        shortcutsContent.append("  Ctrl+E - Экспорт текущей партии\n");
        shortcutsContent.append("  Ctrl+Shift+V - Импорт из буфера обмена\n");
        shortcutsContent.append("  Ctrl+P - Расстановка позиции\n");
        shortcutsContent.append("  Alt+F4 - Выход\n");

        // 👁️ Вид
        shortcutsContent.append("\n👁️ Вид:\n");
        shortcutsContent.append("  Ctrl+F - Перевернуть доску\n");
        shortcutsContent.append("  Ctrl+= - Увеличить\n");
        shortcutsContent.append("  Ctrl+- - Уменьшить\n");
        shortcutsContent.append("  Ctrl+0 - Сброс масштаба\n");
        shortcutsContent.append("  H - Показать/скрыть нотацию\n");

        // 🧭 Навигация
        shortcutsContent.append("\n🧭 Навигация:\n");
        shortcutsContent.append("  ← - Предыдущий ход\n");
        shortcutsContent.append("  → - Следующий ход\n");
        shortcutsContent.append("  ↑ - Первый ход\n");
        shortcutsContent.append("  ↓ - Последний ход\n");

        // ⚙️ Движок
        shortcutsContent.append("\n⚙️ Движок:\n");
        shortcutsContent.append("  Space - Ход движка\n");
        shortcutsContent.append("  Shift+Enter - Включить/выключить анализ\n");
        shortcutsContent.append("  Ctrl+Shift+E - Настройка движка\n");

        // 📚 PGN/Браузер
        shortcutsContent.append("\n📚 PGN/Браузер:\n");
        shortcutsContent.append("  Ctrl+B - Открыть браузер PGN\n");
        shortcutsContent.append("  Ctrl+R - Обновить браузер\n");
        shortcutsContent.append("  F11 - Следующая партия\n");
        shortcutsContent.append("  Ctrl+F11 - Предыдущая партия\n");
        shortcutsContent.append("  Ctrl+Tab - Следующий браузер\n");
        shortcutsContent.append("  Ctrl+Shift+Tab - Предыдущий браузер\n");
        shortcutsContent.append("  Ctrl+W - Закрыть браузер\n");

        // 🗄️ База данных
        shortcutsContent.append("\n🗄️ База данных:\n");
        shortcutsContent.append("  Ctrl+D - Подключиться к БД\n");
        shortcutsContent.append("  Ctrl+I - Импорт в БД\n");
        shortcutsContent.append("  Ctrl+Shift+F - Поиск в БД\n");

        // 🪟 Окна
        shortcutsContent.append("\n🪟 Окна:\n");
        shortcutsContent.append("  Ctrl+Shift+W - Закрыть все браузеры\n");

        shortcutsContent.append("\n═══════════════════════════════════════");
        return shortcutsContent;
    }

    @Override
    public String getCode() {
        return "ru";
    }

    @Override
    public String getDisplayName() {
        return "Русский";
    }

    @Override
    public String get(String key) {
        return strings.getOrDefault(key, "???" + key + "???");
    }

    @Override
    public Map<String, String> getAllStrings() {
        return new HashMap<>(strings);
    }
}