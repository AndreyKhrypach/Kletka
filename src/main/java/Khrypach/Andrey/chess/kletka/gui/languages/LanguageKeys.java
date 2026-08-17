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

/**
 * Ключи для всех строковых ресурсов приложения
 */
public class LanguageKeys {
    // Названия приложения и версии
    public static final String APP_TITLE = "app.title";
    public static final String APP_VERSION = "app.version";
    public static final String APP_PLATFORM = "app.platform";
    public static final String APP_LIBRARY = "app.library";
    public static final String APP_COPYRIGHT = "app.copyright";

    // Меню
    public static final String MENU_FILE = "menu.file";
    public static final String MENU_BOARD = "menu.board";
    public static final String MENU_ASSISTANT = "menu.assistant";
    public static final String MENU_DATABASE = "menu.database";
    public static final String MENU_HELP = "menu.help";
    public static final String MENU_LANGUAGE = "menu.language";

    // Пункты меню File
    public static final String MENU_FILE_NEW_GAME = "menu.file.newGame";
    public static final String MENU_FILE_SETUP_POSITION = "menu.file.setupPosition";
    public static final String MENU_FILE_OPEN_PGN = "menu.file.openPgn";
    public static final String MENU_FILE_SAVE_PGN = "menu.file.savePgn";
    public static final String MENU_FILE_EXPORT_IMAGE = "menu.file.exportImage";
    public static final String MENU_FILE_EXIT = "menu.file.exit";
    public static final String MENU_FILE_OPEN_BROWSER = "menu.file.open.browser";
    public static final String MENU_FILE_REFRESH_BROWSER = "menu.file.refresh.browser";

    // Menu - Windows
    public static final String MENU_WINDOWS = "menu.windows";
    public static final String MENU_WINDOWS_CLIPBOARD_EMPTY = "menu.windows.clipboard.empty";
    public static final String MENU_WINDOWS_CLIPBOARD_CONTENT = "menu.windows.clipboard.content";
    public static final String MENU_WINDOWS_CLEAR_CLIPBOARD = "menu.windows.clear.clipboard";
    public static final String MENU_WINDOWS_CLOSE_ALL = "menu.windows.close.all";
    public static final String MENU_WINDOWS_NO_FILES = "menu.windows.no.files";
    public static final String MENU_WINDOWS_BROWSER_ITEM = "menu.windows.browser.item";

    // Browser display
    public static final String BROWSER_GAMES_COUNT = "browser.games.count";
    public static final String BROWSER_CLIPBOARD_COUNT = "browser.clipboard.count";

    // ========== FILE FILTERS ==========
    public static final String FILE_FILTER_PGN = "file.filter.pgn";
    public static final String FILE_FILTER_DATABASE_TITLE = "file.filter.database.title";
    public static final String FILE_FILTER_DATABASE = "file.filter.database";
    public static final String FILE_FILTER_ALL = "file.filter.all";

    // Пункты меню Board
    public static final String MENU_BOARD_SIZE = "menu.board.size";
    public static final String MENU_BOARD_SIZE_SMALL = "menu.board.size.small";
    public static final String MENU_BOARD_SIZE_MEDIUM = "menu.board.size.medium";
    public static final String MENU_BOARD_SIZE_LARGE = "menu.board.size.large";
    public static final String MENU_BOARD_SIZE_XLARGE = "menu.board.size.xlarge";
    public static final String MENU_BOARD_SIZE_CUSTOM = "menu.board.size.custom";
    public static final String MENU_BOARD_SHOW_COORDS = "menu.board.showCoordinates";
    public static final String MENU_BOARD_FLIP = "menu.board.flip";
    public static final String MENU_BOARD_THEME = "menu.board.theme";
    public static final String MENU_BOARD_THEME_WOOD = "menu.board.theme.wood";
    public static final String MENU_BOARD_THEME_CLASSIC = "menu.board.theme.classic";
    public static final String MENU_BOARD_THEME_GREEN = "menu.board.theme.green";
    public static final String MENU_BOARD_THEME_BLUE = "menu.board.theme.blue";

    // Пункты меню Assistant
    public static final String MENU_ASSISTANT_ENGINE = "menu.assistant.engine";
    public static final String MENU_ASSISTANT_ENGINE_STOCKFISH = "menu.assistant.engine.stockfish";
    public static final String MENU_ASSISTANT_ENGINE_LC0 = "menu.assistant.engine.lc0";
    public static final String MENU_ASSISTANT_ENGINE_CUSTOM = "menu.assistant.engine.custom";
    public static final String MENU_ASSISTANT_ANALYZE = "menu.assistant.analyze";
    public static final String MENU_ASSISTANT_BEST_MOVE = "menu.assistant.bestMove";
    public static final String MENU_ASSISTANT_SHOW_EVAL = "menu.assistant.showEval";
    public static final String MENU_ASSISTANT_CONFIGURE_ENGINE = "menu.assistant.configureEngine";

    // === Меню База данных ===
    public static final String MENU_DATABASE_CONNECT = "menu.database.connect";
    public static final String MENU_DATABASE_OPEN = "menu.database.open";
    public static final String MENU_DATABASE_SEARCH = "menu.database.search";
    public static final String MENU_DATABASE_IMPORT_PGN = "menu.database.importPgn";
    public static final String MENU_DATABASE_STATS = "menu.database.stats";
    public static final String MENU_DATABASE_OPEN_LAST = "menu.database.open.last";
    public static final String MENU_DATABASE_IMPORT = "menu.database.import";
    public static final String MENU_DATABASE_INFO = "menu.database.info";
    public static final String SHORTCUT_SEARCH_DB = "shortcut.search.db";

    // === Сообщения базы данных ===
    public static final String DB_NOT_INITIALIZED = "db.not.initialized";
    public static final String DB_CONNECT_SUCCESS = "db.connect.success";
    public static final String DB_CONNECT_ERROR = "db.connect.error";
    public static final String DB_OPEN_TITLE = "db.open.title";
    public static final String DB_OPEN_SUCCESS = "db.open.success";
    public static final String DB_OPEN_ERROR = "db.open.error";
    public static final String DB_OPEN_INVALID = "db.open.invalid";
    public static final String DB_SEARCH_TITLE = "db.search.title";
    public static final String DB_SEARCH_HEADER = "db.search.header";
    public static final String DB_SEARCH_BUTTON = "db.search.button";
    public static final String DB_SEARCH_WHITE = "db.search.white";
    public static final String DB_SEARCH_BLACK = "db.search.black";
    public static final String DB_SEARCH_RESULT = "db.search.result";
    public static final String DB_SEARCH_ECO = "db.search.eco";
    public static final String DB_SEARCH_OPENING = "db.search.opening";
    public static final String DB_SEARCH_ERROR = "db.search.error";
    public static final String DB_NO_RESULTS = "db.no.results";
    public static final String DB_NO_GAMES = "db.no.games";
    public static final String DB_RESULTS_TITLE = "db.results.title";
    public static final String DB_RESULTS_HEADER = "db.results.header";
    public static final String DB_LOAD_ERROR = "db.load.error";
    public static final String DB_LOAD_SUCCESS = "db.load.success";
    public static final String DB_LOAD_BUTTON = "db.load.button";
    public static final String DB_STATS_TITLE = "db.stats.title";
    public static final String DB_STATS_ERROR = "db.stats.error";
    public static final String DB_STATS_ECO = "db.stats.eco";
    public static final String DB_STATS_OPENING = "db.stats.opening";
    public static final String DB_INFO_TITLE = "db.info.title";
    public static final String DB_INFO_ERROR = "db.info.error";
    public static final String DB_INFO_PATH = "db.info.path";
    public static final String DB_INFO_GAMES = "db.info.games";
    public static final String DB_INFO_TYPE = "db.info.type";
    public static final String DB_INFO_VERSION = "db.info.version";
    public static final String DB_LOAD_PGN_FIRST = "db.load.pgn.first";
    public static final String DB_INDEX_NOT_LOADED = "db.index.not.loaded";
    public static final String DB_STATS_TOTAL = "db.stats.total";
    public static final String DB_INFO_FILENAME = "db.info.filename";
    public static final String DB_INFO_ACTIVE_GAMES = "db.info.active.games";
    public static final String DB_INFO_DELETED_GAMES = "db.info.deleted.games";
    public static final String DB_INFO_FILE_SIZE = "db.info.file.size";
    public static final String DB_INFO_INDEX_VERSION = "db.info.index.version";
    public static final String DB_INFO_GROWTH_RATIO = "db.info.growth.ratio";


    // === PGN сообщения ===
    public static final String PGN_IMPORT_TITLE = "pgn.import.title";
    public static final String PGN_IMPORT_PROGRESS = "pgn.import.progress";
    public static final String PGN_IMPORT_SUCCESS = "pgn.import.success";
    public static final String PGN_IMPORT_ERROR = "pgn.import.error";
    public static final String PGN_IMPORT_NO_GAMES = "pgn.import.no.games";
    public static final String PGN_IMPORT_CLIPBOARD_SUCCESS = "pgn.import.clipboard.success";
    public static final String PGN_CLIPBOARD_EMPTY = "pgn.clipboard.empty";
    public static final String PGN_LOAD_SUCCESS = "pgn.load.success";
    public static final String PGN_LOAD_EMPTY = "pgn.load.empty";
    public static final String PGN_LOAD_ERROR = "pgn.load.error";
    public static final String PGN_SAVE_SUCCESS = "pgn.save.success";
    public static final String PGN_SAVE_EMPTY = "pgn.save.empty";
    public static final String PGN_SAVE_ERROR = "pgn.save.error";

    // === Файловое меню ===
    public static final String MENU_FILE_EXPORT_CURRENT = "menu.file.export.current";
    public static final String MENU_FILE_IMPORT_CLIPBOARD = "menu.file.import.clipboard";

    // === Меню Правка ===
    public static final String MENU_EDIT = "menu.edit";
    public static final String MENU_EDIT_UNDO = "menu.edit.undo";
    public static final String MENU_EDIT_REDO = "menu.edit.redo";
    public static final String MENU_EDIT_PREFERENCES = "menu.edit.preferences";
    public static final String PREFERENCES_TITLE = "preferences.title";

    // === Меню Вид ===
    public static final String MENU_VIEW = "menu.view";
    public static final String MENU_VIEW_FLIP_BOARD = "menu.view.flipBoard";
    public static final String MENU_VIEW_COORDINATES = "menu.view.coordinates";
    public static final String MENU_VIEW_ZOOM = "menu.view.zoom";
    public static final String MENU_VIEW_ZOOM_IN = "menu.view.zoom.in";
    public static final String MENU_VIEW_ZOOM_OUT = "menu.view.zoom.out";
    public static final String MENU_VIEW_ZOOM_RESET = "menu.view.zoom.reset";
    public static final String MENU_VIEW_TOGGLE_NOTATION = "menu.view.toggle.notation";

    // === Меню Движок ===
    public static final String MENU_ENGINE = "menu.engine";
    public static final String MENU_ENGINE_CONFIGURE = "menu.engine.configure";
    public static final String MENU_ENGINE_ANALYZE = "menu.engine.analyze";

    // Пункты меню Help
    public static final String MENU_HELP_SHORTCUTS = "menu.help.shortcuts";
    public static final String MENU_HELP_ABOUT = "menu.help.about";
    public static final String MENU_HELP_DONATE = "menu.help.donate";

    // Language menu
    public static final String MENU_LANGUAGE_RUSSIAN = "menu.language.russian";
    public static final String MENU_LANGUAGE_ENGLISH = "menu.language.english";
    public static final String MENU_LANGUAGE_CHINESE = "menu.language.chinese";
    public static final String MENU_LANGUAGE_CHANGE = "menu.language.change";
    // Language change dialog
    public static final String LANG_CHANGE_TITLE = "lang.change.title";
    public static final String LANG_CHANGE_HEADER = "lang.change.header";
    public static final String LANG_CHANGE_CONTENT = "lang.change.content";
    public static final String LANG_CHANGE_BUTTON_OK = "lang.change.button.ok";

    // Диалоги
    public static final String DIALOG_VARIATION_TITLE = "dialog.variation.title";
    public static final String DIALOG_VARIATION_CHOICE = "dialog.variation.choice";
    public static final String DIALOG_VARIATION_SELECT = "dialog.variation.select";
    public static final String DIALOG_VARIATION_CANCEL = "dialog.variation.cancel";
    public static final String DIALOG_VARIATION_NEW = "dialog.variation.new";
    public static final String DIALOG_VARIATION_REPLACE_MAIN = "dialog.variation.replaceMain";
    public static final String DIALOG_VARIATION_MAIN_LINE = "dialog.variation.mainLine";
    public static final String DIALOG_VARIATION_EXISTING = "dialog.variation.existing";
    public static final String DIALOG_VARIATION_MAKE_MAIN = "dialog.variation.makeMain";

    // Нотация
    public static final String NOTATION_TITLE = "notation.title";
    public static final String NOTATION_NEW_GAME = "notation.newGame";
    public static final String NOTATION_COPY_PGN = "notation.copyPgn";
    public static final String NOTATION_PGN_COPIED = "notation.pgnCopied";
    public static final String NOTATION_COPY_PGN_UNICODE = "notation.copyPgnUnicode";
    public static final String NOTATION_PGN_UNICODE_COPIED = "notation.pgnUnicodeCopied";

    public static final String NOTATION_TOGGLE_SHOW = "notation.toggle.show";
    public static final String NOTATION_TOGGLE_HIDE = "notation.toggle.hide";
    public static final String NOTATION_NO_MOVES = "notation.no.moves";
    public static final String NOTATION_NO_DATA = "notation.no.data";

    // Игровые сообщения
    public static final String GAME_CHECKMATE = "game.checkmate";
    public static final String GAME_STALEMATE = "game.stalemate";
    public static final String GAME_INSUFFICIENT_MATERIAL = "game.insufficientMaterial";
    public static final String GAME_WHITE = "game.white";
    public static final String GAME_BLACK = "game.black";
    public static final String GAME_WIN = "game.win";

    // Диалог превращения пешки
    public static final String PROMOTION_TITLE = "promotion.title";
    public static final String PROMOTION_CHOOSE = "promotion.choose";
    public static final String PROMOTION_IMAGE_LOAD_ERROR = "promotion.imageLoadError";

    // Диалог расстановки позиции
    public static final String SETUP_TITLE = "setup.title";
    public static final String SETUP_INSTRUCTION = "setup.instruction";
    public static final String SETUP_SELECT_PIECE = "setup.selectPiece";
    public static final String SETUP_WHITE = "setup.white";
    public static final String SETUP_BLACK = "setup.black";
    public static final String SETUP_DELETE_MODE = "setup.deleteMode";
    public static final String SETUP_MOVE_MODE = "setup.moveMode";
    public static final String SETUP_CLEAR_SELECTION = "setup.clearSelection";
    public static final String SETUP_SIDE_TO_MOVE = "setup.sideToMove";
    public static final String SETUP_FEN = "setup.fen";
    public static final String SETUP_COPY_FEN = "setup.copyFen";
    public static final String SETUP_APPLY = "setup.apply";
    public static final String SETUP_START_POS = "setup.startPos";
    public static final String SETUP_CLEAR_ALL = "setup.clearAll";
    public static final String SETUP_CANCEL = "setup.cancel";
    public static final String SETUP_FEN_INVALID = "setup.fenInvalid";
    public static final String SETUP_FEN_COPIED = "setup.fenCopied";
    public static final String SETUP_IMAGE_LOAD_ERROR = "setup.imageLoadError";
    public static final String SETUP_DELETE_INSTRUCTION = "setup.deleteInstruction";
    public static final String SETUP_DELETE_MODE_TOOLTIP = "setup.deleteModeTooltip";
    public static final String SETUP_CASTLING_RIGHTS = "setup.castlingRights";
    public static final String SETUP_CASTLING_WHITE_KING = "setup.castling.whiteKing";
    public static final String SETUP_CASTLING_WHITE_QUEEN = "setup.castling.whiteQueen";
    public static final String SETUP_CASTLING_BLACK_KING = "setup.castling.blackKing";
    public static final String SETUP_CASTLING_BLACK_QUEEN = "setup.castling.blackQueen";
    public static final String SETUP_RESET_CASTLING = "setup.resetCastling";
    public static final String SETUP_FEN_PROMPT = "setup.fenPrompt";
    public static final String SETUP_CONTROL = "setup.control";
    public static final String SETUP_KING_IN_CHECK = "setup.kingInCheck";

    // Навигация
    public static final String NAV_FIRST = "nav.first";
    public static final String NAV_PREV = "nav.prev";
    public static final String NAV_NEXT = "nav.next";
    public static final String NAV_LAST = "nav.last";
    public static final String NAV_TOOLTIP_FIRST = "nav.tooltip.first";
    public static final String NAV_TOOLTIP_PREV = "nav.tooltip.prev";
    public static final String NAV_TOOLTIP_NEXT = "nav.tooltip.next";
    public static final String NAV_TOOLTIP_LAST = "nav.tooltip.last";

    // Статус и уведомления
    public static final String NOTIFICATION_INFO = "notification.info";
    public static final String NOTIFICATION_ERROR = "notification.error";
    public static final String NOTIFICATION_DATABASE_CONNECT = "notification.database.connect";
    public static final String NOTIFICATION_SEARCH = "notification.search";
    public static final String NOTIFICATION_IMPORT = "notification.import";
    public static final String NOTIFICATION_ANALYSIS = "notification.analysis";
    public static final String NOTIFICATION_WARNING = "notification.warning";

    // Короткие клавиши
    public static final String SHORTCUTS_TITLE = "shortcuts.title";
    public static final String SHORTCUTS_CONTENT = "shortcuts.content";

    // В раздел с горячими клавишами (Shortcuts)

    public static final String SHORTCUT_FILE = "shortcut.file";
    public static final String SHORTCUT_VIEW = "shortcut.view";
    public static final String SHORTCUT_NAVIGATION = "shortcut.navigation";
    public static final String SHORTCUT_ENGINE = "shortcut.engine";
    public static final String SHORTCUT_PGN = "shortcut.pgn";
    public static final String SHORTCUT_DATABASE = "shortcut.database";
    public static final String SHORTCUT_WINDOWS = "shortcut.windows";

    // Отдельные хоткеи
    public static final String SHORTCUT_NEW_GAME = "shortcut.new.game";
    public static final String SHORTCUT_OPEN_PGN = "shortcut.open.pgn";
    public static final String SHORTCUT_SAVE_PGN = "shortcut.save.pgn";
    public static final String SHORTCUT_EXPORT_CURRENT = "shortcut.export.current";
    public static final String SHORTCUT_IMPORT_CLIPBOARD = "shortcut.import.clipboard";
    public static final String SHORTCUT_SETUP_POSITION = "shortcut.setup.position";
    public static final String SHORTCUT_EXIT = "shortcut.exit";

    public static final String SHORTCUT_FLIP_BOARD = "shortcut.flip.board";
    public static final String SHORTCUT_COORDINATES = "shortcut.coordinates";
    public static final String SHORTCUT_ZOOM_IN = "shortcut.zoom.in";
    public static final String SHORTCUT_ZOOM_OUT = "shortcut.zoom.out";
    public static final String SHORTCUT_ZOOM_RESET = "shortcut.zoom.reset";
    public static final String SHORTCUT_TOGGLE_NOTATION = "shortcut.toggle.notation";

    public static final String SHORTCUT_NAV_PREV = "shortcut.nav.prev";
    public static final String SHORTCUT_NAV_NEXT = "shortcut.nav.next";
    public static final String SHORTCUT_NAV_FIRST = "shortcut.nav.first";
    public static final String SHORTCUT_NAV_LAST = "shortcut.nav.last";

    public static final String SHORTCUT_ENGINE_MOVE = "shortcut.engine.move";
    public static final String SHORTCUT_ENGINE_ANALYZE = "shortcut.engine.analyze";
    public static final String SHORTCUT_ENGINE_CONFIGURE = "shortcut.engine.configure";

    public static final String SHORTCUT_OPEN_BROWSER = "shortcut.open.browser";
    public static final String SHORTCUT_REFRESH_BROWSER = "shortcut.refresh.browser";
    public static final String SHORTCUT_NEXT_GAME = "shortcut.next.game";
    public static final String SHORTCUT_PREV_GAME = "shortcut.prev.game";
    public static final String SHORTCUT_NEXT_BROWSER = "shortcut.next.browser";
    public static final String SHORTCUT_PREV_BROWSER = "shortcut.prev.browser";
    public static final String SHORTCUT_CLOSE_BROWSER = "shortcut.close.browser";
    public static final String SHORTCUT_CLOSE_ALL_BROWSERS = "shortcut.close.all.browsers";
    public static final String SHORTCUT_MINIMIZE_BROWSER = "shortcut.minimize.browser";
    public static final String SHORTCUT_MAXIMIZE_BROWSER = "shortcut.maximize.browser";

    public static final String SHORTCUT_CONNECT_DB = "shortcut.connect.db";
    public static final String SHORTCUT_IMPORT_DB = "shortcut.import.db";

    // ========== ENGINE MESSAGES ==========
    public static final String ENGINE_SEND_POSITION_ERROR = "engine.sendPositionError";
    public static final String ENGINE_TIMEOUT_ERROR = "engine.timeoutError";
    public static final String ENGINE_ANALYSIS_START_ERROR = "engine.analysisStartError";
    public static final String ENGINE_STOP_ERROR = "engine.stopError";
    public static final String ENGINE_INVALID_UCI_MOVE = "engine.invalidUciMove";
    public static final String ENGINE_CONVERT_UCI_ERROR = "engine.convertUciError";
    public static final String ENGINE_CONVERT_NOTATION_ERROR = "engine.convertNotationError";
    public static final String ENGINE_IMAGE_LOAD_ERROR = "engine.imageLoadError";
    public static final String ENGINE_ANALYSIS_NOT_ACTIVE = "engine.analysisNotActive";
    public static final String ENGINE_TERMINAL_POSITION = "engine.terminalPosition";
    public static final String ENGINE_NOT_ANALYZED = "engine.notAnalyzed";
    public static final String ENGINE_ILLEGAL_MOVE = "engine.illegalMove";
    public static final String ENGINE_MOVE_ERROR = "engine.moveError";

    // ========== ENGINE SETUP DIALOG ==========
    public static final String ENGINE_SETUP_DIALOG_TITLE = "engine.setup.dialog.title";
    public static final String ENGINE_SETUP_DIALOG_HEADER = "engine.setup.dialog.header";
    public static final String ENGINE_SETUP_DIALOG_INFO = "engine.setup.dialog.info";
    public static final String ENGINE_SETUP_DIALOG_PATH_LABEL = "engine.setup.dialog.pathLabel";
    public static final String ENGINE_SETUP_DIALOG_PATH_PROMPT = "engine.setup.dialog.pathPrompt";
    public static final String ENGINE_SETUP_DIALOG_BROWSE = "engine.setup.dialog.browse";
    public static final String ENGINE_SETUP_DIALOG_TEST = "engine.setup.dialog.test";
    public static final String ENGINE_SETUP_DIALOG_OK = "engine.setup.dialog.ok";
    public static final String ENGINE_SETUP_DIALOG_CANCEL = "engine.setup.dialog.cancel";

    public static final String ENGINE_SETUP_DIALOG_FILE_CHOOSER_TITLE = "engine.setup.dialog.fileChooser.title";
    public static final String ENGINE_SETUP_DIALOG_FILE_FILTER_EXECUTABLE = "engine.setup.dialog.fileFilter.executable";
    public static final String ENGINE_SETUP_DIALOG_FILE_FILTER_ALL = "engine.setup.dialog.fileFilter.all";

    public static final String ENGINE_SETUP_DIALOG_STATUS_SELECT_FILE = "engine.setup.dialog.status.selectFile";
    public static final String ENGINE_SETUP_DIALOG_STATUS_FILE_NOT_EXISTS = "engine.setup.dialog.status.fileNotExists";
    public static final String ENGINE_SETUP_DIALOG_STATUS_CHECKING = "engine.setup.dialog.status.checking";
    public static final String ENGINE_SETUP_DIALOG_STATUS_READY = "engine.setup.dialog.status.ready";
    public static final String ENGINE_SETUP_DIALOG_STATUS_FAILED = "engine.setup.dialog.status.failed";

    // ========== ANALYSIS PANEL ==========
    public static final String ANALYSIS_TITLE = "analysis.title";
    public static final String ANALYSIS_ENGINE_STOPPED = "analysis.engineStopped";
    public static final String ANALYSIS_ANALYZING = "analysis.analyzing";
    public static final String ANALYSIS_CURRENT_EVAL = "analysis.currentEval";
    public static final String ANALYSIS_DEPTH = "analysis.depth";
    public static final String ANALYSIS_ADD_LINE_TOOLTIP = "analysis.addLineTooltip";
    public static final String ANALYSIS_REMOVE_LINE_TOOLTIP = "analysis.removeLineTooltip";
    public static final String ANALYSIS_TOGGLE_TOOLTIP = "analysis.toggleTooltip";
    public static final String ANALYSIS_ENGINE_NOT_RUNNING_TITLE = "analysis.engineNotRunningTitle";
    public static final String ANALYSIS_ENGINE_NOT_RUNNING_HEADER = "analysis.engineNotRunningHeader";
    public static final String ANALYSIS_ENGINE_NOT_RUNNING_CONTENT = "analysis.engineNotRunningContent";

    // ========== АННОТАЦИИ ХОДОВ (Шахматный информатор) ==========

    // Оценка хода
    public static final String ANNOTATION_BRILLIANT_MOVE = "annotation.brilliantMove";
    public static final String ANNOTATION_GOOD_MOVE = "annotation.goodMove";
    public static final String ANNOTATION_INTERESTING_MOVE = "annotation.interestingMove";
    public static final String ANNOTATION_DUBIOUS_MOVE = "annotation.dubiousMove";
    public static final String ANNOTATION_BAD_MOVE = "annotation.badMove";
    public static final String ANNOTATION_BLUNDER = "annotation.blunder";

    // Оценка позиции
    public static final String ANNOTATION_CLEAR_ADVANTAGE_WHITE = "annotation.clearAdvantageWhite";
    public static final String ANNOTATION_WINNING_WHITE = "annotation.winningWhite";
    public static final String ANNOTATION_SLIGHT_ADVANTAGE_WHITE = "annotation.slightAdvantageWhite";
    public static final String ANNOTATION_EQUALITY = "annotation.equality";
    public static final String ANNOTATION_SLIGHT_ADVANTAGE_BLACK = "annotation.slightAdvantageBlack";
    public static final String ANNOTATION_CLEAR_ADVANTAGE_BLACK = "annotation.clearAdvantageBlack";
    public static final String ANNOTATION_WINNING_BLACK = "annotation.winningBlack";
    public static final String ANNOTATION_UNCLEAR_POSITION = "annotation.unclearPosition";
    public static final String ANNOTATION_WITH_COMPENSATION = "annotation.withCompensation";

    // Комментарии и пояснения
    public static final String ANNOTATION_ONLY_MOVE = "annotation.onlyMove";
    public static final String ANNOTATION_THEORETICAL_NOVELTY = "annotation.theoreticalNovelty";
    public static final String ANNOTATION_ONLY_AND_BEST_MOVE = "annotation.onlyAndBestMove";
    public static final String ANNOTATION_WITH_IDEA = "annotation.withIdea";
    public static final String ANNOTATION_WITH_INITIATIVE = "annotation.withInitiative";
    public static final String ANNOTATION_WITH_COUNTERPLAY = "annotation.withCounterplay";
    public static final String ANNOTATION_DEVELOPMENT_ADVANTAGE = "annotation.developmentAdvantage";
    public static final String ANNOTATION_BETTER_WAS = "annotation.betterWas";
    public static final String ANNOTATION_MATE = "annotation.mate";
    public static final String ANNOTATION_CHECK = "annotation.check";
    public static final String ANNOTATION_DOUBLE_CHECK = "annotation.double_check";

    // Слоны
    public static final String ANNOTATION_TWO_BISHOPS = "annotation.twoBishops";
    public static final String ANNOTATION_BISHOP_PAIR_WHITE_BLACK = "annotation.bishopPairWhiteBlack";
    public static final String ANNOTATION_CENTER_CONTROL = "annotation.centerControl";

    // Диалог аннотаций
    public static final String ANNOTATION_DIALOG_TITLE = "annotation.dialog.title";
    public static final String ANNOTATION_DIALOG_SELECT = "annotation.dialog.select";
    public static final String ANNOTATION_DIALOG_COMMENT = "annotation.dialog.comment";
    public static final String ANNOTATION_DIALOG_COMMENT_PROMPT = "annotation.dialog.commentPrompt";
    public static final String ANNOTATION_DIALOG_CLEAR = "annotation.dialog.clear";
    public static final String ANNOTATION_DIALOG_OK = "annotation.dialog.ok";
    public static final String ANNOTATION_DIALOG_CANCEL = "annotation.dialog.cancel";

    // Контекстное меню для ходов
    public static final String CONTEXT_MENU_MAKE_MAIN = "contextMenu.makeMain";
    public static final String CONTEXT_MENU_ADD_ANNOTATION = "contextMenu.addAnnotation";
    public static final String CONTEXT_MENU_REMOVE_ANNOTATION = "contextMenu.removeAnnotation";
    public static final String CONTEXT_MENU_EDIT_COMMENT = "contextMenu.editComment";
    public static final String CONTEXT_MENU_DELETE_VARIATION = "contextMenu.deleteVariation";
    public static final String CONTEXT_MENU_DELETE_AFTER = "contextMenu.deleteAfter";
    public static final String CONTEXT_MENU_RESULT_WHITE_WIN = "contextMenu.resultWhiteWin";
    public static final String CONTEXT_MENU_RESULT_BLACK_WIN = "contextMenu.resultBlackWin";
    public static final String CONTEXT_MENU_RESULT_DRAW = "contextMenu.resultDraw";
    public static final String CONTEXT_MENU_RESULT_UNKNOWN = "contextMenu.resultUnknown";
    public static final String CONTEXT_MENU_CANNOT_DELETE_MAIN = "contextMenu.cannotDeleteMain";

    // Диалог аннотаций (дополнительные ключи)
    public static final String ANNOTATION_DIALOG_MOVE = "annotation.dialog.move";
    public static final String ANNOTATION_TAB_MOVE_EVAL = "annotation.tab.move.eval";
    public static final String ANNOTATION_TAB_POSITION_EVAL = "annotation.tab.position.eval";
    public static final String ANNOTATION_TAB_COMMENTARY = "annotation.tab.commentary";

    // Варианты
    public static final String MAIN_LINE = "mainLine";
    public static final String ROOT = "root";
    public static final String VARIATION_DEFAULT_NAME = "variation.defaultName";

    // ========== CONFIRM DIALOGS ==========
    public static final String CONFIRM_DELETE_TITLE = "confirm.delete.title";
    public static final String CONFIRM_DELETE_AFTER_HEADER = "confirm.delete.after.header";
    public static final String CONFIRM_DELETE_CONTENT = "confirm.delete.content";
    public static final String CONFIRM_DELETE_YES = "confirm.delete.yes";
    public static final String CONFIRM_DELETE_NO = "confirm.delete.no";
    public static final String CONFIRM_DELETE_VARIATION_TITLE = "confirm.delete.variation.title";
    public static final String CONFIRM_DELETE_VARIATION_HEADER = "confirm.delete.variation.header";
    public static final String CONFIRM_DELETE_VARIATION_CONTENT = "confirm.delete.variation.content";

    // ========== TIMER ==========
    public static final String TIMER_SELECT_TIME = "timer.selectTime";
    public static final String TIMER_PRESET_1_MIN = "timer.preset.1min";
    public static final String TIMER_PRESET_2_MIN = "timer.preset.2min";
    public static final String TIMER_PRESET_3_MIN = "timer.preset.3min";
    public static final String TIMER_PRESET_5_MIN = "timer.preset.5min";
    public static final String TIMER_PRESET_10_MIN = "timer.preset.10min";
    public static final String TIMER_CUSTOM_TOOLTIP = "timer.custom.tooltip";
    public static final String TIMER_CUSTOM_TITLE = "timer.custom.title";
    public static final String TIMER_CUSTOM_HEADER = "timer.custom.header";
    public static final String TIMER_CUSTOM_CONTENT = "timer.custom.content";
    public static final String TIMER_MAX_TIME_WARNING = "timer.maxTimeWarning";
    public static final String TIMER_INVALID_NUMBER = "timer.invalidNumber";

    // ========== MAIN CONTROLLER ==========
    public static final String NEW_GAME_TITLE = "main.newGame.title";
    public static final String NEW_GAME_HEADER = "main.newGame.header";
    public static final String NEW_GAME_CONTENT = "main.newGame.content";

    public static final String SAVE_GAME_TITLE = "main.saveGame.title";
    public static final String SAVE_GAME_HEADER = "main.saveGame.header";
    public static final String SAVE_GAME_CONTENT = "main.saveGame.content";
    public static final String SAVE_GAME_SAVE = "main.saveGame.save";
    public static final String SAVE_GAME_DONT_SAVE = "main.saveGame.dontSave";
    public static final String SAVE_GAME_CANCEL = "main.saveGame.cancel";

    public static final String SAVE_GAME_DIALOG_TITLE = "main.saveGame.dialog.title";
    public static final String SAVE_GAME_DIALOG_HEADER = "main.saveGame.dialog.header";
    public static final String SAVE_GAME_DIALOG_SAVE = "main.saveGame.dialog.save";

    public static final String SAVE_GAME_PROMPT_WHITE = "main.saveGame.prompt.white";
    public static final String SAVE_GAME_PROMPT_BLACK = "main.saveGame.prompt.black";
    public static final String SAVE_GAME_PROMPT_WHITE_ELO = "main.saveGame.prompt.whiteElo";
    public static final String SAVE_GAME_PROMPT_BLACK_ELO = "main.saveGame.prompt.blackElo";
    public static final String SAVE_GAME_PROMPT_EVENT = "main.saveGame.prompt.event";
    public static final String SAVE_GAME_PROMPT_SITE = "main.saveGame.prompt.site";
    public static final String SAVE_GAME_PROMPT_ROUND = "main.saveGame.prompt.round";

    public static final String SAVE_GAME_LABEL_WHITE = "main.saveGame.label.white";
    public static final String SAVE_GAME_LABEL_BLACK = "main.saveGame.label.black";
    public static final String SAVE_GAME_LABEL_RATING = "main.saveGame.label.rating";
    public static final String SAVE_GAME_LABEL_EVENT = "main.saveGame.label.event";
    public static final String SAVE_GAME_LABEL_SITE = "main.saveGame.label.site";
    public static final String SAVE_GAME_LABEL_ROUND = "main.saveGame.label.round";

    public static final String SAVE_GAME_SUCCESS = "main.saveGame.success";

    public static final String FEATURE_NOT_IMPLEMENTED = "main.feature.notImplemented";
    public static final String PGN_LOAD_MESSAGE = "main.pgn.load";
    public static final String PGN_SAVE_MESSAGE = "main.pgn.save";
    public static final String DB_OPEN_MESSAGE = "main.db.open";

    // MainController messages
    public static final String MAIN_LOAD_GAME_EMPTY_TREE = "main.load.game.empty.tree";
    public static final String MAIN_LOADED_POSITION = "main.loaded.position";
    public static final String MAIN_PGN_EMPTY = "main.pgn.empty";
    public static final String MAIN_CLOSE_BUTTON = "main.close.button";
    public static final String MAIN_NO_CHANGES = "main.no.changes";
    public static final String MAIN_GAME_SAVED_FILE = "main.game.saved.file";
    public static final String MAIN_SAVE_GAME_CHOICE_TITLE = "main.save.game.choice.title";
    public static final String MAIN_SAVE_GAME_CHOICE_HEADER = "main.save.game.choice.header";
    public static final String MAIN_SAVE_GAME_CHOICE_CONTENT = "main.save.game.choice.content";
    public static final String MAIN_SAVE_TO_PGN_FILE = "main.save.to.pgn.file";
    public static final String MAIN_SAVE_TO_DATABASE = "main.save.to.database";
    public static final String MAIN_SAVE_CANCEL = "main.save.cancel";
    public static final String MAIN_SAVE_PGN_FILE_TITLE = "main.save.pgn.file.title";
    public static final String MAIN_DB_NOT_INITIALIZED_MSG = "main.db.not.initialized.msg";
    public static final String MAIN_GAME_SAVED_TO_DB = "main.game.saved.to.db";
    public static final String MAIN_OPEN_PGN_ERROR_MSG = "main.open.pgn.error.msg";
    public static final String MAIN_BROWSER_LIMIT_MSG = "main.browser.limit.msg";
    public static final String MAIN_OPEN_ERROR_MSG = "main.open.error.msg";
    public static final String MAIN_INDEXING_TITLE_MSG = "main.indexing.title.msg";
    public static final String MAIN_INDEXING_COMPLETE = "main.indexing.complete";
    public static final String MAIN_INDEXING_ERROR_MSG = "main.indexing.error.msg";
    public static final String MAIN_POSITION_SOLVE_HINT = "main.position.solve.hint";
    public static final String MAIN_REFRESHED_MSG = "main.refreshed.msg";
    public static final String MAIN_NO_ACTIVE_BROWSER_MSG = "main.no.active.browser.msg";
    public static final String MAIN_UNKNOWN_PATH = "main.unknown.path";
    public static final String MAIN_POSITION_LOADED = "main.position.loaded";
    public static final String MAIN_GAME_LOADED = "main.game.loaded";

    public static final String ABOUT_TITLE = "main.about.title";
    public static final String ABOUT_CONTENT = "main.about.content";

    public static final String POSITION_SET_SUCCESS = "main.position.set.success";

    public static final String ENGINE_SETUP_TITLE = "main.engine.setup.title";
    public static final String ENGINE_SETUP_HEADER = "main.engine.setup.header";
    public static final String ENGINE_SETUP_CONTENT = "main.engine.setup.content";
    public static final String ENGINE_SETUP_BUTTON = "main.engine.setup.button";
    public static final String ENGINE_SETUP_LATER = "main.engine.setup.later";
    public static final String ENGINE_SETUP_SUCCESS = "main.engine.setup.success";
    public static final String ENGINE_SETUP_ERROR_TITLE = "main.engine.setup.error.title";
    public static final String ENGINE_SETUP_ERROR_CONTENT = "main.engine.setup.error.content";

    public static final String ENGINE_SWITCH_TITLE = "main.engine.switch.title";
    public static final String ENGINE_SWITCH_HEADER = "main.engine.switch.header";
    public static final String ENGINE_SWITCH_CONTENT = "main.engine.switch.content";
    public static final String ENGINE_SWITCH_SUCCESS = "main.engine.switch.success";

    public static final String ENGINE_NOT_RUNNING = "main.engine.notRunning";

    public static final String ANALYSIS_BEST_MOVE = "main.analysis.bestMove";
    public static final String ANALYSIS_ERROR_TITLE = "main.analysis.error.title";
    public static final String ANALYSIS_ERROR_CONTENT = "main.analysis.error.content";

    public static final String CONFIRM_YES = "main.confirm.yes";
    public static final String CONFIRM_NO = "main.confirm.no";

    // ========== LOG MESSAGES ==========
    public static final String LOG_INITIALIZED = "log.initialized";
    public static final String LOG_STARTING_GUI = "log.starting.gui";
    public static final String LOG_GUI_LOADED = "log.gui.loaded";
    public static final String LOG_GUI_ERROR = "log.gui.error";
    public static final String LOG_SHUTTING_DOWN = "log.shutting.down";

    // ========== SPLASH SCREEN ==========
    public static final String SPLASH_LOADING_ENGINE = "splash.loading_engine";
    public static final String SPLASH_INITIALIZING_BOARD = "splash.initializing_board";
    public static final String SPLASH_LOADING_GUI = "splash.loading_gui";
    public static final String SPLASH_READY = "splash.ready";

    // ========== GAME_TYPE_POSITION ==========
    public static final String GAME_TYPE_POSITION = "game.type.position";
    public static final String GAME_TYPE_STUDY = "game.type.study";
    public static final String GAME_TYPE_PROBLEM = "game.type.problem";
    public static final String GAME_TYPE_GAME = "game.type.game";
    public static final String DEFAULT_PLAYER_NAME = "default.player.name";

    public static final String PGN_KEYWORD_MATE = "pgn.keyword.mate";
    public static final String PGN_KEYWORD_STUDY = "pgn.keyword.study";

    // Repository errors
    public static final String REPO_ERROR_CREATE_DIR = "repo.error.create.dir";
    public static final String REPO_ERROR_GAME_NULL = "repo.error.game.null";
    public static final String REPO_ERROR_SAVE_GAME = "repo.error.save.game";
    public static final String REPO_ERROR_READ_DIR = "repo.error.read.dir";
    public static final String REPO_ERROR_DELETE_GAME = "repo.error.delete.game";
    public static final String REPO_ERROR_DELETE_ALL = "repo.error.delete.all";
    public static final String REPO_ERROR_COUNT = "repo.error.count";
    public static final String REPO_ERROR_READ_FILE = "repo.error.read.file";
    public static final String REPO_ERROR_PARSE_PGN = "repo.error.parse.pgn";
    public static final String REPO_ERROR_GAMES_EMPTY = "repo.error.games.empty";
    public static final String REPO_ERROR_EXPORT = "repo.error.export";

    // Export service errors
    public static final String EXPORT_ERROR_GAMES_EMPTY = "export.error.games.empty";
    public static final String EXPORT_ERROR_FILE_NULL = "export.error.file.null";
    public static final String EXPORT_ERROR_EXPORT_FAILED = "export.error.export.failed";
    public static final String EXPORT_ERROR_NO_GAMES = "export.error.no.games";
    public static final String EXPORT_ERROR_CREATE_DIR = "export.error.create.dir";
    public static final String EXPORT_ERROR_GAME_NULL = "export.error.game.null";

    // Import service errors
    public static final String IMPORT_ERROR_FILE_NOT_FOUND = "import.error.file.not.found";
    public static final String IMPORT_ERROR_READ_FILE = "import.error.read.file";
    public static final String IMPORT_ERROR_PARSE_PGN = "import.error.parse.pgn";
    public static final String IMPORT_ERROR_PGN_EMPTY = "import.error.pgn.empty";
    public static final String IMPORT_ERROR_PARSE_GENERAL = "import.error.parse.general";
    public static final String IMPORT_ERROR_DIR_NOT_FOUND = "import.error.dir.not.found";
    public static final String IMPORT_ERROR_DIR_NOT_FOUND_SIMPLE = "import.error.dir.not.found.simple";
    public static final String IMPORT_ERROR_READ_DIR = "import.error.read.dir";

    // PGN service errors
    public static final String PGN_SERVICE_ERROR_GAME_NULL = "pgn.service.error.game.null";
    public static final String PGN_SERVICE_ERROR_GAMES_EMPTY = "pgn.service.error.games.empty";

    // Donate dialog
    public static final String DONATE_TITLE = "donate.title";
    public static final String DONATE_HEADER = "donate.header";
    public static final String DONATE_DESCRIPTION = "donate.description";
    public static final String DONATE_HINT = "donate.hint";
    public static final String DONATE_CLOSE = "donate.close";
    public static final String DONATE_PAYPAL = "donate.paypal";
    public static final String DONATE_COPY = "donate.copy";
    public static final String DONATE_OPEN = "donate.open";
    public static final String DONATE_BITCOIN = "donate.bitcoin";
    public static final String DONATE_QR = "donate.qr";
    public static final String DONATE_QR_TITLE = "donate.qr.title";
    public static final String DONATE_QR_HINT = "donate.qr.hint";
    public static final String DONATE_TOAST_COPIED_EMAIL = "donate.toast.copied.email";
    public static final String DONATE_TOAST_COPIED_BITCOIN = "donate.toast.copied.bitcoin";
    public static final String DONATE_TOAST_OPEN_BROWSER = "donate.toast.open.browser";
    public static final String DONATE_TOAST_QR_ERROR = "donate.toast.qr.error";

    // Position setup dialog
    public static final String SETUP_DELETE_MODE_ACTIVE_TOOLTIP = "setup.delete.mode.active.tooltip";
    public static final String SETUP_SIDE_CHANGED_NOTIFICATION = "setup.side.changed.notification";
    public static final String SETUP_SIDE_CHANGED_WHITE = "setup.side.changed.white";
    public static final String SETUP_SIDE_CHANGED_BLACK = "setup.side.changed.black";
    public static final String SETUP_LOAD_POSITION_ERROR = "setup.load.position.error";

    // SaveGameDialog - Titles
    public static final String SAVE_DIALOG_TITLE_EDIT = "save.dialog.title.edit";
    public static final String SAVE_DIALOG_TITLE_SAVE = "save.dialog.title.save";
    public static final String SAVE_DIALOG_HEADER_EDIT = "save.dialog.header.edit";
    public static final String SAVE_DIALOG_HEADER_SAVE = "save.dialog.header.save";

    // Tabs
    public static final String SAVE_TAB_PLAYERS = "save.tab.players";
    public static final String SAVE_TAB_TOURNAMENT = "save.tab.tournament";
    public static final String SAVE_TAB_DETAILS = "save.tab.details";

    // Players tab
    public static final String SAVE_LABEL_WHITE = "save.label.white";
    public static final String SAVE_LABEL_BLACK = "save.label.black";
    public static final String SAVE_LABEL_ELO_WHITE = "save.label.elo.white";
    public static final String SAVE_LABEL_ELO_BLACK = "save.label.elo.black";
    public static final String SAVE_LABEL_WHITE_TEAM = "save.label.white.team";
    public static final String SAVE_LABEL_BLACK_TEAM = "save.label.black.team";
    public static final String SAVE_LABEL_ANNOTATOR = "save.label.annotator";
    public static final String SAVE_LABEL_RESULT = "save.label.result";
    public static final String SAVE_RESULT_1_0 = "save.result.1-0";
    public static final String SAVE_RESULT_0_1 = "save.result.0-1";
    public static final String SAVE_RESULT_DRAW = "save.result.draw";
    public static final String SAVE_RESULT_UNKNOWN = "save.result.unknown";

    // Tournament tab
    public static final String SAVE_LABEL_EVENT = "save.label.event";
    public static final String SAVE_LABEL_SITE = "save.label.site";
    public static final String SAVE_LABEL_ROUND = "save.label.round";
    public static final String SAVE_LABEL_SUBROUND = "save.label.subround";
    public static final String SAVE_LABEL_DATE = "save.label.date";
    public static final String SAVE_LABEL_YEAR = "save.label.year";
    public static final String SAVE_LABEL_MONTH = "save.label.month";
    public static final String SAVE_LABEL_DAY = "save.label.day";
    public static final String SAVE_BUTTON_RESET_DATE = "save.button.reset.date";

    // Details tab
    public static final String SAVE_LABEL_ECO = "save.label.eco";
    public static final String SAVE_LABEL_OPENING = "save.label.opening";
    public static final String SAVE_LABEL_VARIATION = "save.label.variation";
    public static final String SAVE_LABEL_TIME_CONTROL = "save.label.time.control";
    public static final String SAVE_LABEL_SOURCE = "save.label.source";
    public static final String SAVE_LABEL_FEN = "save.label.fen";
    public static final String SAVE_CHECKBOX_SETUP = "save.checkbox.setup";
    public static final String SAVE_LABEL_TYPE = "save.label.type";
    public static final String SAVE_BUTTON_DETECT_OPENING = "save.button.detect.opening";

    // Buttons
    public static final String SAVE_BUTTON_SAVE = "save.button.save";
    public static final String SAVE_BUTTON_SAVE_CHANGES = "save.button.save.changes";
    public static final String SAVE_BUTTON_CANCEL = "save.button.cancel";
    public static final String SAVE_BUTTON_HELP = "save.button.help";

    // Messages
    public static final String SAVE_MSG_ECO_NOT_LOADED = "save.msg.eco.not.loaded";
    public static final String SAVE_MSG_OPENING_FOUND = "save.msg.opening.found";
    public static final String SAVE_MSG_OPENING_NOT_FOUND = "save.msg.opening.not.found";
    public static final String SAVE_MSG_OPENING_ERROR = "save.msg.opening.error";

    // Help dialog
    public static final String SAVE_HELP_TITLE = "save.help.title";
    public static final String SAVE_HELP_HEADER = "save.help.header";
    public static final String SAVE_HELP_CONTENT = "save.help.content";

    // Type options
    public static final String SAVE_TYPE_GAME = "save.type.game";
    public static final String SAVE_TYPE_POSITION = "save.type.position";
    public static final String SAVE_TYPE_STUDY = "save.type.study";
    public static final String SAVE_TYPE_PROBLEM = "save.type.problem";

    // Logo
    public static final String LOGO_TITLE = "logo.title";
    public static final String LOGO_SUBTITLE_LINE1 = "logo.subtitle.line1";
    public static final String LOGO_SUBTITLE_LINE2 = "logo.subtitle.line2";

    // PGN Browser Manager
    public static final String PGN_BROWSER_LIMIT_REACHED = "pgn.browser.limit.reached";
    public static final String PGN_BROWSER_COPY_LIMIT = "pgn.browser.copy.limit";
    public static final String PGN_BROWSER_PASTE_UNAVAILABLE = "pgn.browser.paste.unavailable";
    public static final String PGN_BROWSER_NO_INDEX = "pgn.browser.no.index";
    public static final String PGN_BROWSER_DISK_SPACE_ERROR = "pgn.browser.disk.space.error";
    public static final String PGN_BROWSER_DISK_SPACE_CHECK = "pgn.browser.disk.space.check";
    public static final String PGN_BROWSER_DISK_SPACE_INSUFFICIENT = "pgn.browser.disk.space.insufficient";
    public static final String PGN_BROWSER_PASTE_INTERRUPTED = "pgn.browser.paste.interrupted";
    public static final String PGN_BROWSER_PASTE_ERROR = "pgn.browser.paste.error";

    // Progress messages
    public static final String PGN_BROWSER_COPY_PREPARING = "pgn.browser.copy.preparing";
    public static final String PGN_BROWSER_COPY_TOTAL = "pgn.browser.copy.total";
    public static final String PGN_BROWSER_COPY_COMPLETE = "pgn.browser.copy.complete";
    public static final String PGN_BROWSER_COPY_SOURCE = "pgn.browser.copy.source";
    public static final String PGN_BROWSER_PASTE_PREPARING = "pgn.browser.paste.preparing";
    public static final String PGN_BROWSER_PASTE_TOTAL = "pgn.browser.paste.total";
    public static final String PGN_BROWSER_PASTE_PROGRESS = "pgn.browser.paste.progress";
    public static final String PGN_BROWSER_PASTE_ADDED = "pgn.browser.paste.added";
    public static final String PGN_BROWSER_PASTE_COMPLETE = "pgn.browser.paste.complete";
    public static final String PGN_BROWSER_PASTE_TARGET = "pgn.browser.paste.target";

    // Indexing Progress
    public static final String INDEXING_PROGRESS_MESSAGE = "indexing.progress.message";
    public static final String INDEXING_STATUS_STARTING = "indexing.status.starting";
    public static final String INDEXING_STATUS_COMPLETE = "indexing.status.complete";

    // PGN Game Operation
    public static final String PGN_OP_EDIT_SUCCESS = "pgn.op.edit.success";
    public static final String PGN_OP_DELETE_SUCCESS = "pgn.op.delete.success";
    public static final String PGN_OP_ADD_SUCCESS = "pgn.op.add.success";
    public static final String PGN_OP_DUPLICATE_SUCCESS = "pgn.op.duplicate.success";

    // Delete Confirm Dialog
    public static final String DELETE_CONFIRM_TITLE = "delete.confirm.title";
    public static final String DELETE_CONFIRM_SINGLE_TITLE = "delete.confirm.single.title";
    public static final String DELETE_CONFIRM_MULTIPLE_TITLE = "delete.confirm.multiple.title";
    public static final String DELETE_CONFIRM_SINGLE_MESSAGE = "delete.confirm.single.message";
    public static final String DELETE_CONFIRM_MULTIPLE_MESSAGE = "delete.confirm.multiple.message";
    public static final String DELETE_CONFIRM_WHITE = "delete.confirm.white";
    public static final String DELETE_CONFIRM_BLACK = "delete.confirm.black";
    public static final String DELETE_CONFIRM_RESULT = "delete.confirm.result";
    public static final String DELETE_CONFIRM_AND_MORE = "delete.confirm.and.more";
    public static final String DELETE_CONFIRM_WARNING = "delete.confirm.warning";
    public static final String DELETE_CONFIRM_DELETE_BUTTON = "delete.confirm.delete.button";
    public static final String DELETE_CONFIRM_CANCEL_BUTTON = "delete.confirm.cancel.button";
    public static final String DELETE_CONFIRM_UNKNOWN = "delete.confirm.unknown";
    public static final String DELETE_CONFIRM_GAME_PREFIX = "delete.confirm.game.prefix";

    // Indexing Progress Dialog
    public static final String INDEXING_DIALOG_TITLE = "indexing.dialog.title";
    public static final String INDEXING_DIALOG_STATUS_PREPARING = "indexing.dialog.status.preparing";
    public static final String INDEXING_DIALOG_GAMES_PROCESSED = "indexing.dialog.games.processed";
    public static final String INDEXING_DIALOG_COMPLETE = "indexing.dialog.complete";
    public static final String INDEXING_DIALOG_CANCEL = "indexing.dialog.cancel";
    public static final String INDEXING_DIALOG_ERROR = "indexing.dialog.error";
    public static final String INDEXING_DIALOG_PROGRESS_FORMAT = "indexing.dialog.progress.format";

    // PGN File Browser - Table Columns
    public static final String PGN_BROWSER_COLUMN_ID = "pgn.browser.column.id";
    public static final String PGN_BROWSER_COLUMN_WHITE = "pgn.browser.column.white";
    public static final String PGN_BROWSER_COLUMN_BLACK = "pgn.browser.column.black";
    public static final String PGN_BROWSER_COLUMN_RESULT = "pgn.browser.column.result";
    public static final String PGN_BROWSER_COLUMN_YEAR = "pgn.browser.column.year";
    public static final String PGN_BROWSER_COLUMN_EVENT = "pgn.browser.column.event";
    public static final String PGN_BROWSER_COLUMN_ECO = "pgn.browser.column.eco";
    public static final String PGN_BROWSER_COLUMN_OPENING = "pgn.browser.column.opening";
    public static final String PGN_BROWSER_COLUMN_BODY = "pgn.browser.column.body";

    // PGN Browser - Window
    public static final String PGN_BROWSER_TITLE = "pgn.browser.title";
    public static final String PGN_BROWSER_TITLE_ACTIVE = "pgn.browser.title.active";
    public static final String PGN_BROWSER_TITLE_GAMES = "pgn.browser.title.games";

    // PGN Browser - Search
    public static final String PGN_BROWSER_SEARCH_LABEL = "pgn.browser.search.label";
    public static final String PGN_BROWSER_SEARCH_PROMPT = "pgn.browser.search.prompt";
    public static final String PGN_BROWSER_SEARCH_CLEAR = "pgn.browser.search.clear";

    // PGN Browser - Status
    public static final String PGN_BROWSER_STATUS_LOADING = "pgn.browser.status.loading";
    public static final String PGN_BROWSER_STATUS_TOTAL = "pgn.browser.status.total";
    public static final String PGN_BROWSER_STATUS_SELECTED = "pgn.browser.status.selected";
    public static final String PGN_BROWSER_STATUS_SHOWN = "pgn.browser.status.shown";
    public static final String PGN_BROWSER_STATUS_CLOSE = "pgn.browser.status.close";
    public static final String PGN_BROWSER_STATUS_READY = "pgn.browser.status.ready";
    public static final String PGN_BROWSER_STATUS_ERROR = "pgn.browser.status.error";
    public static final String PGN_BROWSER_STATUS_LOADING_INDEX = "pgn.browser.status.loading.index";
    public static final String PGN_BROWSER_STATUS_PARSING = "pgn.browser.status.parsing";
    public static final String PGN_BROWSER_STATUS_LOADING_MORE = "pgn.browser.status.loading.more";
    public static final String PGN_BROWSER_STATUS_ALL_LOADED = "pgn.browser.status.all.loaded";
    public static final String PGN_BROWSER_STATUS_LOADED = "pgn.browser.status.loaded";
    public static final String PGN_BROWSER_STATUS_CHECKING_INDEX = "pgn.browser.status.checking.index";
    public static final String PGN_BROWSER_STATUS_LOADING_GAME =  "pgn.browser.status.loading.game";
    public static final String PGN_BROWSER_STATUS_READY_WITH_COUNT = "pgn.browser.status.ready_with_count";
    public static final String PGN_BROWSER_STATUS_ALL_LOADED_WITH_COUNT = "pgn.browser.status.all.loaded_with_count";

    // PGN Browser - Buttons
    public static final String PGN_BROWSER_BUTTON_EDIT = "pgn.browser.button.edit";
    public static final String PGN_BROWSER_BUTTON_DELETE = "pgn.browser.button.delete";
    public static final String PGN_BROWSER_BUTTON_DELETE_COUNT = "pgn.browser.button.delete.count";
    public static final String PGN_BROWSER_BUTTON_DUPLICATE = "pgn.browser.button.duplicate";
    public static final String PGN_BROWSER_BUTTON_COPY = "pgn.browser.button.copy";
    public static final String PGN_BROWSER_BUTTON_PASTE = "pgn.browser.button.paste";
    public static final String PGN_BROWSER_BUTTON_REPACK = "pgn.browser.button.repack";
    public static final String PGN_BROWSER_BUTTON_REPACK_COUNT = "pgn.browser.button.repack.count";
    public static final String PGN_BROWSER_BUTTON_REPACK_IN_PROGRESS = "pgn.browser.button.repack.in.progress";

    // PGN Browser - Context Menu
    public static final String PGN_BROWSER_CONTEXT_LOAD = "pgn.browser.context.load";
    public static final String PGN_BROWSER_CONTEXT_COPY = "pgn.browser.context.copy";
    public static final String PGN_BROWSER_CONTEXT_DELETE = "pgn.browser.context.delete";
    public static final String PGN_BROWSER_CONTEXT_SELECT_ALL = "pgn.browser.context.select.all";

    // PGN Browser - Messages
    public static final String PGN_BROWSER_MSG_REPACK_IN_PROGRESS = "pgn.browser.msg.repack.in.progress";
    public static final String PGN_BROWSER_MSG_SELECT_ONE = "pgn.browser.msg.select.one";
    public static final String PGN_BROWSER_MSG_EDIT_UNAVAILABLE = "pgn.browser.msg.edit.unavailable";
    public static final String PGN_BROWSER_MSG_SELECT_GAMES = "pgn.browser.msg.select.games";
    public static final String PGN_BROWSER_MSG_DELETE_UNAVAILABLE = "pgn.browser.msg.delete.unavailable";
    public static final String PGN_BROWSER_MSG_COPY_LIMIT = "pgn.browser.msg.copy.limit";
    public static final String PGN_BROWSER_MSG_COPY_UNAVAILABLE = "pgn.browser.msg.copy.unavailable";
    public static final String PGN_BROWSER_MSG_DUPLICATE_UNAVAILABLE = "pgn.browser.msg.duplicate.unavailable";
    public static final String PGN_BROWSER_MSG_PASTE_UNAVAILABLE = "pgn.browser.msg.paste.unavailable";
    public static final String PGN_BROWSER_MSG_CLIPBOARD_EMPTY = "pgn.browser.msg.clipboard.empty";
    public static final String PGN_BROWSER_MSG_NO_DELETED_GAMES = "pgn.browser.msg.no.deleted.games";
    public static final String PGN_BROWSER_MSG_COPY_SUCCESS = "pgn.browser.msg.copy.success";
    public static final String PGN_BROWSER_MSG_PASTE_SUCCESS =  "pgn.browser.msg.paste.success";
    public static final String PGN_BROWSER_MSG_DELETE_SUCCESS = "pgn.browser.msg.delete.success";
    public static final String PGN_BROWSER_MSG_DUPLICATE_SUCCESS = "pgn.browser.msg.duplicate.success";
    public static final String PGN_BROWSER_STATUS_ERROR_LOADING =  "pgn.browser.status.error.loading";
    public static final String PGN_BROWSER_STATUS_OPERATION_FINISHED =  "pgn.browser.status.operation.finished";
    public static final String PGN_BROWSER_MSG_DELETE_ERROR =  "pgn.browser.msg.delete.error";
    public static final String PGN_BROWSER_MSG_SELECT_ONE_DUPLICATE = "pgn.browser.msg.select.one.duplicate";
    public static final String PGN_BROWSER_MSG_DUPLICATE_ERROR = "pgn.browser.msg.duplicate.error";
    public static final String PGN_BROWSER_MSG_SELECT_GAMES_COPY = "pgn.browser.msg.select.games.copy";
    public static final String PGN_BROWSER_MSG_COPY_ERROR =  "pgn.browser.msg.copy.error";
    public static final String PGN_BROWSER_MSG_COPY = "pgn.browser.msg.copy";
    public static final String PGN_BROWSER_MSG_COPY_START = "pgn.browser.msg.copy.start";
    public static final String PGN_BROWSER_MSG_PASTE_ERROR =  "pgn.browser.msg.paste.error";
    public static final String PGN_BROWSER_MSG_PASTE_GAMES = "pgn.browser.msg.paste.games";
    public static final String PGN_BROWSER_MSG_PASTE_START = "pgn.browser.msg.paste.start";

    // PGN Browser - Progress
    public static final String PGN_BROWSER_DELETING = "pgn.browser.deleting";
    public static final String PGN_BROWSER_DELETING_PROCEED = "pgn.browser.deleting.poe";
    public static final String PGN_BROWSER_DELETED = "pgn.browser.deleted";
    public static final String PGN_BROWSER_DUPLICATING = "pgn.browser.duplicating";
    public static final String PGN_BROWSER_COPYING = "pgn.browser.copying";
    public static final String PGN_BROWSER_PASTING = "pgn.browser.pasting";
    public static final String PGN_BROWSER_PASTED = "pgn.browser.pasted";
    public static final String PGN_BROWSER_REPACKING = "pgn.browser.repacking";
    public static final String PGN_BROWSER_REPACK_COMPLETE = "pgn.browser.repack.complete";
    public static final String PGN_BROWSER_START_DELETING = "pgn.browser.start.deleting";

    // PGN Browser - Confirm
    public static final String PGN_BROWSER_CONFIRM_PASTE_TITLE = "pgn.browser.confirm.paste.title";
    public static final String PGN_BROWSER_CONFIRM_PASTE_HEADER = "pgn.browser.confirm.paste.header";
    public static final String PGN_BROWSER_CONFIRM_PASTE_SOURCE = "pgn.browser.confirm.paste.source";
    public static final String PGN_BROWSER_CONFIRM_PASTE_TARGET = "pgn.browser.confirm.paste.target";
    public static final String PGN_BROWSER_CONFIRM_PASTE_COUNT = "pgn.browser.confirm.paste.count";
    public static final String PGN_BROWSER_CONFIRM_PASTE_FREE_SPACE = "pgn.browser.confirm.paste.free.space";
    public static final String PGN_BROWSER_CONFIRM_PASTE_INFO = "pgn.browser.confirm.paste.info";
    public static final String PGN_BROWSER_CONFIRM_PASTE_YES = "pgn.browser.confirm.paste.yes";
    public static final String PGN_BROWSER_CONFIRM_PASTE_NO = "pgn.browser.confirm.paste.no";
    public static final String PGN_BROWSER_CONFIRM_REPACK_TITLE = "pgn.browser.confirm.repack.title";
    public static final String PGN_BROWSER_CONFIRM_REPACK_HEADER = "pgn.browser.confirm.repack.header";
    public static final String PGN_BROWSER_CONFIRM_REPACK_CONTENT = "pgn.browser.confirm.repack.content";
    public static final String PGN_BROWSER_CONFIRM_REPACK_YES = "pgn.browser.confirm.repack.yes";
    public static final String PGN_BROWSER_CONFIRM_REPACK_NO = "pgn.browser.confirm.repack.no";

    // PGN Browser - Auto Repack
    public static final String PGN_BROWSER_AUTO_REPACK_TITLE = "pgn.browser.auto.repack.title";
    public static final String PGN_BROWSER_AUTO_REPACK_HEADER = "pgn.browser.auto.repack.header";
    public static final String PGN_BROWSER_AUTO_REPACK_CONTENT = "pgn.browser.auto.repack.content";
    public static final String PGN_BROWSER_AUTO_REPACK_YES = "pgn.browser.auto.repack.yes";
    public static final String PGN_BROWSER_AUTO_REPACK_NO = "pgn.browser.auto.repack.no";

    // PGN Browser - Filter
    public static final String PGN_BROWSER_FILTER_TOTAL = "pgn.browser.filter.total";
    public static final String PGN_BROWSER_FILTER_FOUND = "pgn.browser.filter.found";

    // PGN Browser - Repack
    public static final String PGN_BROWSER_REPACK_TITLE = "pgn.browser.repack.title";
    public static final String PGN_BROWSER_REPACK_IN_PROGRESS = "pgn.browser.repack.in.progress";
    public static final String PGN_BROWSER_REPACK_ERROR = "pgn.browser.repack.error";
    public static final String PGN_BROWSER_REPACK_SUCCESS = "pgn.browser.repack.success";

    // Repack Progress Dialog
    public static final String REPACK_DIALOG_TITLE = "repack.dialog.title";
    public static final String REPACK_DIALOG_STATUS_PREPARING = "repack.dialog.status.preparing";
    public static final String REPACK_DIALOG_PROGRESS_FORMAT = "repack.dialog.progress.format";
    public static final String REPACK_DIALOG_GAMES_PROCESSED = "repack.dialog.games.processed";
    public static final String REPACK_DIALOG_COMPLETE = "repack.dialog.complete";
    public static final String REPACK_DIALOG_ERROR = "repack.dialog.error";

    // Repack Status Widget
    public static final String REPACK_STATUS_OPTIMAL = "repack.status.optimal";
    public static final String REPACK_STATUS_NO_DELETED = "repack.status.no.deleted";
    public static final String REPACK_STATUS_HAS_DELETED = "repack.status.has.deleted";
    public static final String REPACK_STATUS_WARNING = "repack.status.warning";
    public static final String REPACK_STATUS_CRITICAL = "repack.status.critical";
    public static final String REPACK_STATUS_UNKNOWN = "repack.status.unknown";
    public static final String REPACK_STATUS_REPACKING = "repack.status.repacking";
    public static final String REPACK_STATUS_DELETED_COUNT = "repack.status.deleted.count";
    public static final String REPACK_STATUS_TOOLTIP = "repack.status.tooltip";
    public static final String REPACK_STATUS_REPACKING_TOOLTIP = "repack.status.repacking.tooltip";
    public static final String REPACK_STATUS_RATIO = "repack.status.ratio";
    public static final String REPACK_STATUS_LOADING = "repack.status.loading";

    // Indexing
    public static final String INDEXING_STATUS_SCANNING_FILE = "indexing.status.scanning.file";
    public static final String INDEXING_STATUS_CREATING_INDEX = "indexing.status.creating.index";
    public static final String INDEXING_STATUS_PREPARING_FILE = "indexing.status.preparing.file";
    public static final String INDEXING_STATUS_SCANNING_GAMES = "indexing.status.scanning.games";
    public static final String INDEXING_STATUS_PROCESSED = "indexing.status.processed";
    public static final String INDEXING_STATUS_PREPARING_ADD_DELETED = "indexing.status.preparing.add.deleted";

    // File Preparation
    public static final String PREPARE_STATUS_SCANNING = "prepare.status.scanning";
    public static final String PREPARE_STATUS_PROCESSED = "prepare.status.processed";
    public static final String PREPARE_STATUS_BUILDING = "prepare.status.building";
    public static final String PREPARE_STATUS_BUILDING_BLOCKS = "prepare.status.building.blocks";
    public static final String PREPARE_STATUS_SAVING = "prepare.status.saving";
    public static final String PREPARE_STATUS_COMPLETE = "prepare.status.complete";

    // Indexing Facade
    public static final String INDEXING_STEP1 = "indexing.step1";
    public static final String INDEXING_STEP2 = "indexing.step2";
    public static final String INDEXING_STEP3 = "indexing.step3";
    public static final String INDEXING_COMPLETE_SUCCESS = "indexing.complete.success";

    // Repack
    public static final  String REPACK_STATUS_READING = "repack.status.reading";
    public static final String REPACK_STATUS_PROCESSED = "repack.status.processed";
    public static final String REPACK_STATUS_WRITING = "repack.status.writing";
    public static final String REPACK_STATUS_CREATING_INDEX = "repack.status.creating.index";
    public static final String REPACK_STATUS_SAVING_INDEX = "repack.status.saving.index";
    public static final String REPACK_STATUS_REPLACING = "repack.status.replacing";
    public static final String REPACK_STATUS_COMPLETE = "repack.status.complete";

    // Repack Status Descriptions
    public static final String REPACK_DESC_NO_GAMES = "repack.desc.no.games";
    public static final String REPACK_DESC_NO_DELETED = "repack.desc.no.deleted";
    public static final String REPACK_DESC_HAS_DELETED = "repack.desc.has.deleted";
    public static final String REPACK_DESC_WARNING = "repack.desc.warning";
    public static final String REPACK_DESC_CRITICAL = "repack.desc.critical";
    public static final String REPACK_DESC_DELETED_COUNT = "repack.desc.deleted.count";

    // ========== PGN BROWSER - EDIT HEADERS ==========
    public static final String PGN_BROWSER_EDIT_HEADERS_TITLE = "edit.headers.title";
    public static final String PGN_BROWSER_EDIT_HEADERS_HEADER = "edit.headers.header";
    public static final String PGN_BROWSER_EDIT_SAVE = "edit.headers.save";
    public static final String PGN_BROWSER_EDIT_CANCEL = "edit.headers.cancel";
    public static final String PGN_BROWSER_EDIT_LOADING = "edit.headers.loading";
    public static final String PGN_BROWSER_EDIT_SAVING = "edit.headers.saving";
    public static final String PGN_BROWSER_EDIT_SAVED =  "edit.headers.saved";
    public static final String PGN_BROWSER_EDIT_BODY_INFO = "edit.headers.body.info";
    public static final String PGN_BROWSER_EDIT_BODY_EMPTY = "edit.headers.body.empty";
    public static final String PGN_BROWSER_EDIT_PROMPT_WHITE =  "edit.headers.prompt.white";
    public static final String PGN_BROWSER_EDIT_PROMPT_BLACK = "edit.headers.prompt.black";
    public static final String PGN_BROWSER_EDIT_PROMPT_EVENT = "edit.headers.prompt.event";
    public static final String PGN_BROWSER_EDIT_PROMPT_SITE = "edit.headers.prompt.site";
    public static final String PGN_BROWSER_EDIT_PROMPT_ROUND = "edit.headers.prompt.round";
    public static final String PGN_BROWSER_EDIT_PROMPT_OPENING = "edit.headers.prompt.opening";
    public static final String PGN_BROWSER_EDIT_PROMPT_VARIATION = "edit.headers.prompt.variation";

    public static final String PGN_BROWSER_MSG_EDIT_SUCCESS = "edit.headers.msg.success";
    public static final String PGN_BROWSER_MSG_EDIT_ERROR = "edit.headers.msg.error";
    public static final String PGN_BROWSER_MSG_EDIT_READ_ONLY = "edit.headers.msg.read.only";

    public static final String FAILED_TOKENIZE_PGN = "pgn.exeption.msg.tokenize";
    public static final String ERROR_PARSING_MOVE = "pgn.exeption.msg.move.parsing";
    public static final String ERROR_PARSING_PGN_TO_TREE = "pgn.exeption.msg.parsing.tree";
}