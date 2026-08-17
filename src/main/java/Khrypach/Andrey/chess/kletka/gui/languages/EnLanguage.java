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

public class EnLanguage implements Language {

    private final Map<String, String> strings = new HashMap<>();

    public EnLanguage() {
        // App title
        strings.put(LanguageKeys.APP_TITLE, "Kletka - Chess Analyzer");
        strings.put(LanguageKeys.APP_VERSION, "Version: 1.0");
        strings.put(LanguageKeys.APP_PLATFORM, "Platform: Java 17, OpenJFX");
        strings.put(LanguageKeys.APP_LIBRARY, "Library: chesslib 1.3.6");
        strings.put(LanguageKeys.APP_COPYRIGHT, "© 2026 Khrypach Andrey");

        // Menus
        strings.put(LanguageKeys.MENU_FILE, "File");
        strings.put(LanguageKeys.MENU_BOARD, "Board");
        strings.put(LanguageKeys.MENU_ASSISTANT, "Assistant");
        strings.put(LanguageKeys.MENU_DATABASE, "Database");
        strings.put(LanguageKeys.MENU_HELP, "Help");

        // File menu
        strings.put(LanguageKeys.MENU_FILE_NEW_GAME, "New Game");
        strings.put(LanguageKeys.MENU_FILE_SETUP_POSITION, "Setup Position...");
        strings.put(LanguageKeys.MENU_FILE_OPEN_PGN, "Open PGN...");
        strings.put(LanguageKeys.MENU_FILE_SAVE_PGN, "Save PGN...");
        strings.put(LanguageKeys.MENU_FILE_EXPORT_IMAGE, "Export as Image");
        strings.put(LanguageKeys.MENU_FILE_EXIT, "Exit");

        // ========== FILE FILTERS ==========
        strings.put(LanguageKeys.FILE_FILTER_PGN, "PGN files");
        strings.put(LanguageKeys.FILE_FILTER_DATABASE_TITLE, "Open Database");
        strings.put(LanguageKeys.FILE_FILTER_DATABASE, "SQLite databases");
        strings.put(LanguageKeys.FILE_FILTER_ALL, "All files");

        // Board menu
        strings.put(LanguageKeys.MENU_BOARD_SIZE, "Board Size");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_SMALL, "Small (40px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_MEDIUM, "Medium (60px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_LARGE, "Large (80px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_XLARGE, "Extra Large (100px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_CUSTOM, "Custom:");
        strings.put(LanguageKeys.MENU_BOARD_SHOW_COORDS, "Show Coordinates");
        strings.put(LanguageKeys.MENU_BOARD_FLIP, "Flip Board");
        strings.put(LanguageKeys.MENU_BOARD_THEME, "Board Theme");
        strings.put(LanguageKeys.MENU_BOARD_THEME_WOOD, "Wood");
        strings.put(LanguageKeys.MENU_BOARD_THEME_CLASSIC, "Classic");
        strings.put(LanguageKeys.MENU_BOARD_THEME_GREEN, "Green");
        strings.put(LanguageKeys.MENU_BOARD_THEME_BLUE, "Blue");

        // Assistant menu
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE, "Engine");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_STOCKFISH, "Stockfish");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_LC0, "Leela Chess Zero");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_CUSTOM, "Custom...");
        strings.put(LanguageKeys.MENU_ASSISTANT_ANALYZE, "Analyze Position");
        strings.put(LanguageKeys.MENU_ASSISTANT_BEST_MOVE, "Show Best Move");
        strings.put(LanguageKeys.MENU_ASSISTANT_SHOW_EVAL, "Show Evaluation");
        strings.put(LanguageKeys.MENU_ASSISTANT_CONFIGURE_ENGINE, "Configure Engine...");

        // === Database Menu ===
        strings.put(LanguageKeys.MENU_DATABASE_CONNECT, "Connect to Database");
        strings.put(LanguageKeys.MENU_DATABASE_OPEN, "Open Local Database...");
        strings.put(LanguageKeys.MENU_DATABASE_SEARCH, "Search Database");
        strings.put(LanguageKeys.MENU_DATABASE_IMPORT_PGN, "Import PGN to Database");
        strings.put(LanguageKeys.MENU_DATABASE_STATS, "Opening Statistics");
        strings.put(LanguageKeys.MENU_DATABASE_OPEN_LAST, "Open Last Database");
        strings.put(LanguageKeys.MENU_DATABASE_IMPORT, "Import PGN");
        strings.put(LanguageKeys.MENU_DATABASE_INFO, "Database Info");

        // === Database Messages ===
        strings.put(LanguageKeys.DB_NOT_INITIALIZED, "Database not initialized");
        strings.put(LanguageKeys.DB_CONNECT_SUCCESS, "Database connected");
        strings.put(LanguageKeys.DB_CONNECT_ERROR, "Database connection error");
        strings.put(LanguageKeys.DB_OPEN_TITLE, "Open Database");
        strings.put(LanguageKeys.DB_OPEN_SUCCESS, "Database opened");
        strings.put(LanguageKeys.DB_OPEN_ERROR, "Database open error");
        strings.put(LanguageKeys.DB_OPEN_INVALID, "Selected file is not a directory");
        strings.put(LanguageKeys.DB_SEARCH_TITLE, "Search Database");
        strings.put(LanguageKeys.DB_SEARCH_HEADER, "Enter search criteria");
        strings.put(LanguageKeys.DB_SEARCH_BUTTON, "Search");
        strings.put(LanguageKeys.DB_SEARCH_WHITE, "White (or part of name)");
        strings.put(LanguageKeys.DB_SEARCH_BLACK, "Black (or part of name)");
        strings.put(LanguageKeys.DB_SEARCH_RESULT, "Result (1-0, 0-1, 1/2-1/2)");
        strings.put(LanguageKeys.DB_SEARCH_ECO, "ECO code");
        strings.put(LanguageKeys.DB_SEARCH_OPENING, "Opening name");
        strings.put(LanguageKeys.DB_SEARCH_ERROR, "Search error");
        strings.put(LanguageKeys.DB_NO_RESULTS, "No results found");
        strings.put(LanguageKeys.DB_NO_GAMES, "No games in database");
        strings.put(LanguageKeys.DB_RESULTS_TITLE, "Search Results");
        strings.put(LanguageKeys.DB_RESULTS_HEADER, "Games found: %d");
        strings.put(LanguageKeys.DB_LOAD_ERROR, "Error loading game");
        strings.put(LanguageKeys.DB_LOAD_SUCCESS, "Game loaded");
        strings.put(LanguageKeys.DB_LOAD_BUTTON, "Load");
        strings.put(LanguageKeys.DB_STATS_TITLE, "Opening Statistics");
        strings.put(LanguageKeys.DB_STATS_ERROR, "Error getting statistics");
        strings.put(LanguageKeys.DB_STATS_ECO, "ECO Statistics");
        strings.put(LanguageKeys.DB_STATS_OPENING, "Opening Statistics");
        strings.put(LanguageKeys.DB_INFO_TITLE, "Database Info");
        strings.put(LanguageKeys.DB_INFO_ERROR, "Error getting database info");
        strings.put(LanguageKeys.DB_INFO_PATH, "Path");
        strings.put(LanguageKeys.DB_INFO_GAMES, "Games count");
        strings.put(LanguageKeys.DB_INFO_TYPE, "Storage type");
        strings.put(LanguageKeys.DB_INFO_VERSION, "Version");
        strings.put(LanguageKeys.DB_LOAD_PGN_FIRST, "Load a PGN file into the program");
        strings.put(LanguageKeys.DB_INDEX_NOT_LOADED , "Index not loaded");
        strings.put(LanguageKeys.DB_STATS_TOTAL  , "Total games");
        strings.put(LanguageKeys.DB_INFO_FILENAME  , "File name");
        strings.put(LanguageKeys.DB_INFO_ACTIVE_GAMES   , "Active games");
        strings.put(LanguageKeys.DB_INFO_DELETED_GAMES   , "Deleted games");
        strings.put(LanguageKeys.DB_INFO_FILE_SIZE, "File size");
        strings.put(LanguageKeys.DB_INFO_INDEX_VERSION, "Index version");
        strings.put(LanguageKeys.DB_INFO_GROWTH_RATIO, "Growth ratio");

        // === PGN Messages ===
        strings.put(LanguageKeys.PGN_IMPORT_TITLE, "Import PGN");
        strings.put(LanguageKeys.PGN_IMPORT_PROGRESS, "Importing PGN...");
        strings.put(LanguageKeys.PGN_IMPORT_SUCCESS, "Imported %d games from: %s");
        strings.put(LanguageKeys.PGN_IMPORT_ERROR, "PGN import error");
        strings.put(LanguageKeys.PGN_IMPORT_NO_GAMES, "No games found in file");
        strings.put(LanguageKeys.PGN_IMPORT_CLIPBOARD_SUCCESS, "Game imported from clipboard");
        strings.put(LanguageKeys.PGN_CLIPBOARD_EMPTY, "Clipboard is empty");
        strings.put(LanguageKeys.PGN_LOAD_SUCCESS, "Loaded %d games from: %s");
        strings.put(LanguageKeys.PGN_LOAD_EMPTY, "No games in file");
        strings.put(LanguageKeys.PGN_LOAD_ERROR, "PGN load error");
        strings.put(LanguageKeys.PGN_SAVE_SUCCESS, "Game saved to: ");
        strings.put(LanguageKeys.PGN_SAVE_EMPTY, "No moves to save");
        strings.put(LanguageKeys.PGN_SAVE_ERROR, "PGN save error");

        // === File Menu ===
        strings.put(LanguageKeys.MENU_FILE_EXPORT_CURRENT, "Export Current Game");
        strings.put(LanguageKeys.MENU_FILE_IMPORT_CLIPBOARD, "Import from Clipboard");

        // Help menu
        strings.put(LanguageKeys.MENU_HELP_SHORTCUTS, "Keyboard Shortcuts");
        strings.put(LanguageKeys.MENU_HELP_ABOUT, "About");

        // === Edit Menu ===
        strings.put(LanguageKeys.MENU_EDIT, "Edit");
        strings.put(LanguageKeys.MENU_EDIT_UNDO, "Undo");
        strings.put(LanguageKeys.MENU_EDIT_REDO, "Redo");
        strings.put(LanguageKeys.MENU_EDIT_PREFERENCES, "Preferences");
        strings.put(LanguageKeys.PREFERENCES_TITLE, "Preferences");

        // === View Menu ===
        strings.put(LanguageKeys.MENU_VIEW, "View");
        strings.put(LanguageKeys.MENU_VIEW_FLIP_BOARD, "Flip Board");
        strings.put(LanguageKeys.MENU_VIEW_COORDINATES, "Show Coordinates");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM, "Zoom");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_IN, "Zoom In");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_OUT, "Zoom Out");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_RESET, "Reset Zoom");

        // === Engine Menu ===
        strings.put(LanguageKeys.MENU_ENGINE, "Engine");
        strings.put(LanguageKeys.MENU_ENGINE_CONFIGURE, "Configure Engine");
        strings.put(LanguageKeys.MENU_ENGINE_ANALYZE, "Show Best Move");

        // Variation dialogs
        strings.put(LanguageKeys.DIALOG_VARIATION_TITLE, "Choose Variation");
        strings.put(LanguageKeys.DIALOG_VARIATION_CHOICE, "Select continuation (→ to select, ← to cancel):");
        strings.put(LanguageKeys.DIALOG_VARIATION_SELECT, "Select (→)");
        strings.put(LanguageKeys.DIALOG_VARIATION_CANCEL, "Cancel (←)");
        strings.put(LanguageKeys.DIALOG_VARIATION_NEW, "✦ NEW VARIATION (%s): %s");
        strings.put(LanguageKeys.DIALOG_VARIATION_REPLACE_MAIN, "✗ Replace Main Line (%d%s %s)");
        strings.put(LanguageKeys.DIALOG_VARIATION_MAIN_LINE, "▶ MAIN LINE (%s): %s");
        strings.put(LanguageKeys.DIALOG_VARIATION_EXISTING, "Replace Variation (move %d%s %s : %s)");
        strings.put(LanguageKeys.DIALOG_VARIATION_MAKE_MAIN, "★ MAKE MAIN (%s): %s");

        // Notation
        strings.put(LanguageKeys.NOTATION_TITLE, "Game Notation");
        strings.put(LanguageKeys.NOTATION_NEW_GAME, "New Game");
        strings.put(LanguageKeys.NOTATION_COPY_PGN, "Copy PGN");
        strings.put(LanguageKeys.NOTATION_PGN_COPIED, "PGN copied to clipboard");
        strings.put(LanguageKeys.NOTATION_COPY_PGN_UNICODE, "Copy PGN (Unicode)");
        strings.put(LanguageKeys.NOTATION_PGN_UNICODE_COPIED, "PGN (Unicode) copied to clipboard");
        strings.put(LanguageKeys.NOTATION_TOGGLE_SHOW, "Show game notation");
        strings.put(LanguageKeys.NOTATION_TOGGLE_HIDE, "Hide game notation");
        strings.put(LanguageKeys.NOTATION_NO_MOVES, "No moves");
        strings.put(LanguageKeys.NOTATION_NO_DATA, "No data");

        // Game messages
        strings.put(LanguageKeys.GAME_CHECKMATE, "Checkmate! %s win");
        strings.put(LanguageKeys.GAME_STALEMATE, "Stalemate! Draw");
        strings.put(LanguageKeys.GAME_INSUFFICIENT_MATERIAL, "Insufficient material. Draw");
        strings.put(LanguageKeys.GAME_WHITE, "White");
        strings.put(LanguageKeys.GAME_BLACK, "Black");
        strings.put(LanguageKeys.GAME_WIN, "%s win");

        // Promotion dialog
        strings.put(LanguageKeys.PROMOTION_TITLE, "Promotion");
        strings.put(LanguageKeys.PROMOTION_CHOOSE, "Choose promotion piece:");
        strings.put(LanguageKeys.PROMOTION_IMAGE_LOAD_ERROR, "Failed to load image for");

        // Position setup dialog
        strings.put(LanguageKeys.SETUP_TITLE, "Position Setup");
        strings.put(LanguageKeys.SETUP_INSTRUCTION,
                """
                        LMB: place piece   RMB: opposite color
                        Delete mode: click to delete
                        Move mode: click on piece, then on target square
                        RMB in move mode - cancel""");
        strings.put(LanguageKeys.SETUP_SELECT_PIECE, "Select piece:");
        strings.put(LanguageKeys.SETUP_WHITE, "White:");
        strings.put(LanguageKeys.SETUP_BLACK, "Black:");
        strings.put(LanguageKeys.SETUP_DELETE_MODE, "Delete mode");
        strings.put(LanguageKeys.SETUP_MOVE_MODE, "Move mode");
        strings.put(LanguageKeys.SETUP_CLEAR_SELECTION, "Clear selection");
        strings.put(LanguageKeys.SETUP_SIDE_TO_MOVE, "Side to move:");
        strings.put(LanguageKeys.SETUP_FEN, "FEN:");
        strings.put(LanguageKeys.SETUP_COPY_FEN, "Copy");
        strings.put(LanguageKeys.SETUP_APPLY, "Apply");
        strings.put(LanguageKeys.SETUP_START_POS, "Start Position");
        strings.put(LanguageKeys.SETUP_CLEAR_ALL, "Clear All");
        strings.put(LanguageKeys.SETUP_CANCEL, "Cancel");
        strings.put(LanguageKeys.SETUP_FEN_INVALID, "Invalid FEN format!");
        strings.put(LanguageKeys.SETUP_FEN_COPIED, "FEN copied to clipboard");
        strings.put(LanguageKeys.SETUP_IMAGE_LOAD_ERROR, "Failed to load image for");
        strings.put(LanguageKeys.SETUP_DELETE_INSTRUCTION, "Delete mode: click to delete a piece");
        strings.put(LanguageKeys.SETUP_DELETE_MODE_TOOLTIP, "Delete mode active - click on pieces to remove them");
        strings.put(LanguageKeys.SETUP_CASTLING_RIGHTS, "Castling rights:");
        strings.put(LanguageKeys.SETUP_CASTLING_WHITE_KING, "0-0 (kingside)");
        strings.put(LanguageKeys.SETUP_CASTLING_WHITE_QUEEN, "0-0-0 (queenside)");
        strings.put(LanguageKeys.SETUP_CASTLING_BLACK_KING, "0-0 (kingside)");
        strings.put(LanguageKeys.SETUP_CASTLING_BLACK_QUEEN, "0-0-0 (queenside)");
        strings.put(LanguageKeys.SETUP_RESET_CASTLING, "Reset castling rights");
        strings.put(LanguageKeys.SETUP_FEN_PROMPT, "Enter FEN...");
        strings.put(LanguageKeys.SETUP_CONTROL, "Controls:");
        strings.put(LanguageKeys.SETUP_KING_IN_CHECK, "Cannot set %s to move - king is in check!");

        // Navigation
        strings.put(LanguageKeys.NAV_FIRST, "⏮");
        strings.put(LanguageKeys.NAV_PREV, "◀");
        strings.put(LanguageKeys.NAV_NEXT, "▶");
        strings.put(LanguageKeys.NAV_LAST, "⏭");
        strings.put(LanguageKeys.NAV_TOOLTIP_FIRST, "First move (↑)");
        strings.put(LanguageKeys.NAV_TOOLTIP_PREV, "Previous (←)");
        strings.put(LanguageKeys.NAV_TOOLTIP_NEXT, "Next (→)");
        strings.put(LanguageKeys.NAV_TOOLTIP_LAST, "Last (↓)");

        // Notifications
        strings.put(LanguageKeys.NOTIFICATION_INFO, "Information");
        strings.put(LanguageKeys.NOTIFICATION_ERROR, "Error");
        strings.put(LanguageKeys.NOTIFICATION_DATABASE_CONNECT, "Database connection will be implemented");
        strings.put(LanguageKeys.NOTIFICATION_SEARCH, "Database search will be implemented");
        strings.put(LanguageKeys.NOTIFICATION_IMPORT, "PGN import to database will be implemented");
        strings.put(LanguageKeys.NOTIFICATION_ANALYSIS, "Position analysis will be implemented");
        strings.put(LanguageKeys.NOTIFICATION_WARNING, "Warning");

        // Shortcuts
        strings.put(LanguageKeys.SHORTCUTS_CONTENT,
                """
                        Keyboard Shortcuts:
                        Ctrl+N - New Game
                        Ctrl+O - Open PGN
                        Ctrl+S - Save PGN
                        Ctrl+F - Flip Board
                        Ctrl+D - Database Search
                        F1 - Help
                        Ctrl+Q - Exit""");

        // ========== MOVE ANNOTATIONS (Chess Informant) ==========

        // Move evaluation
        strings.put(LanguageKeys.ANNOTATION_BRILLIANT_MOVE, "Brilliant move");
        strings.put(LanguageKeys.ANNOTATION_GOOD_MOVE, "Good move");
        strings.put(LanguageKeys.ANNOTATION_INTERESTING_MOVE, "Interesting move");
        strings.put(LanguageKeys.ANNOTATION_DUBIOUS_MOVE, "Dubious move");
        strings.put(LanguageKeys.ANNOTATION_BAD_MOVE, "Mistake");
        strings.put(LanguageKeys.ANNOTATION_BLUNDER, "Blunder");

        // Position evaluation
        strings.put(LanguageKeys.ANNOTATION_CLEAR_ADVANTAGE_WHITE, "White has a clear advantage");
        strings.put(LanguageKeys.ANNOTATION_WINNING_WHITE, "White has a winning position");
        strings.put(LanguageKeys.ANNOTATION_SLIGHT_ADVANTAGE_WHITE, "White has a slight advantage");
        strings.put(LanguageKeys.ANNOTATION_EQUALITY, "Equal position");
        strings.put(LanguageKeys.ANNOTATION_SLIGHT_ADVANTAGE_BLACK, "Black has a slight advantage");
        strings.put(LanguageKeys.ANNOTATION_CLEAR_ADVANTAGE_BLACK, "Black has a clear advantage");
        strings.put(LanguageKeys.ANNOTATION_WINNING_BLACK, "Black has a winning position");
        strings.put(LanguageKeys.ANNOTATION_UNCLEAR_POSITION, "Unclear position");
        strings.put(LanguageKeys.ANNOTATION_WITH_COMPENSATION, "With compensation for material");

        // Commentary
        strings.put(LanguageKeys.ANNOTATION_ONLY_MOVE, "Only move");
        strings.put(LanguageKeys.ANNOTATION_THEORETICAL_NOVELTY, "Theoretical novelty");
        strings.put(LanguageKeys.ANNOTATION_ONLY_AND_BEST_MOVE, "Only and best move");
        strings.put(LanguageKeys.ANNOTATION_WITH_IDEA, "With the idea/threat...");
        strings.put(LanguageKeys.ANNOTATION_WITH_INITIATIVE, "With initiative");
        strings.put(LanguageKeys.ANNOTATION_WITH_COUNTERPLAY, "With counterplay");
        strings.put(LanguageKeys.ANNOTATION_DEVELOPMENT_ADVANTAGE, "With development advantage");
        strings.put(LanguageKeys.ANNOTATION_BETTER_WAS, "Better was");
        strings.put(LanguageKeys.ANNOTATION_MATE, "mate");
        strings.put(LanguageKeys.ANNOTATION_CHECK, "check");
        strings.put(LanguageKeys.ANNOTATION_DOUBLE_CHECK, "double check");

        // Bishops
        strings.put(LanguageKeys.ANNOTATION_TWO_BISHOPS, "Two bishops");
        strings.put(LanguageKeys.ANNOTATION_BISHOP_PAIR_WHITE_BLACK, "Opposite-colored bishops");
        strings.put(LanguageKeys.ANNOTATION_CENTER_CONTROL, "Center control");

        // Annotation dialog
        strings.put(LanguageKeys.ANNOTATION_DIALOG_TITLE, "Move Annotation");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_SELECT, "Select annotation for the move:");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_COMMENT, "Comment:");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_COMMENT_PROMPT, "Enter comment for the move...");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_CLEAR, "Clear");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_OK, "OK");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_CANCEL, "Cancel");

        // Annotation dialog (additional keys)
        strings.put(LanguageKeys.ANNOTATION_DIALOG_MOVE, "Move:");
        strings.put(LanguageKeys.ANNOTATION_TAB_MOVE_EVAL, "Move evaluation");
        strings.put(LanguageKeys.ANNOTATION_TAB_POSITION_EVAL, "Position evaluation");
        strings.put(LanguageKeys.ANNOTATION_TAB_COMMENTARY, "Commentary");

        // Context menu
        strings.put(LanguageKeys.CONTEXT_MENU_MAKE_MAIN, "📌 Make this variation main");
        strings.put(LanguageKeys.CONTEXT_MENU_ADD_ANNOTATION, "🏷️ Add annotation / comment");
        strings.put(LanguageKeys.CONTEXT_MENU_REMOVE_ANNOTATION, "🗑️ Remove annotation / comment");
        strings.put(LanguageKeys.CONTEXT_MENU_EDIT_COMMENT, "✏️ Edit comment");
        strings.put(LanguageKeys.CONTEXT_MENU_DELETE_VARIATION, "🗑️ Delete variation");
        strings.put(LanguageKeys.CONTEXT_MENU_DELETE_AFTER, "✂️ Delete all moves after this");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_WHITE_WIN, "🏆 1-0 (White wins)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_BLACK_WIN, "🏆 0-1 (Black wins)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_DRAW, "🏆 1/2-1/2 (Draw)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_UNKNOWN, "🏆 * (Result unknown)");
        strings.put(LanguageKeys.CONTEXT_MENU_CANNOT_DELETE_MAIN, "Cannot delete main line!");

        // Variation
        strings.put(LanguageKeys.MAIN_LINE, "Main line");
        strings.put(LanguageKeys.ROOT, "ROOT");
        strings.put(LanguageKeys.VARIATION_DEFAULT_NAME, "Variation");

        // ========== ENGINE MESSAGES ==========
        strings.put(LanguageKeys.ENGINE_SEND_POSITION_ERROR, "Failed to send position");
        strings.put(LanguageKeys.ENGINE_TIMEOUT_ERROR, "Engine didn't respond within the time limit");
        strings.put(LanguageKeys.ENGINE_ANALYSIS_START_ERROR, "Failed to start analysis");
        strings.put(LanguageKeys.ENGINE_STOP_ERROR, "Failed to stop analysis");
        strings.put(LanguageKeys.ENGINE_INVALID_UCI_MOVE, "Invalid UCI move");
        strings.put(LanguageKeys.ENGINE_CONVERT_UCI_ERROR, "Failed to convert UCI move");
        strings.put(LanguageKeys.ENGINE_CONVERT_NOTATION_ERROR, "Error converting to chess notation");
        strings.put(LanguageKeys.ENGINE_IMAGE_LOAD_ERROR, "Failed to load image for");
        strings.put(LanguageKeys.ENGINE_ANALYSIS_NOT_ACTIVE, "Analysis is not active. Press Enter to start analysis");
        strings.put(LanguageKeys.ENGINE_TERMINAL_POSITION, "Terminal position, no moves possible");
        strings.put(LanguageKeys.ENGINE_NOT_ANALYZED, "Engine hasn't analyzed the position yet");
        strings.put(LanguageKeys.ENGINE_ILLEGAL_MOVE, "Engine proposed an illegal move");
        strings.put(LanguageKeys.ENGINE_MOVE_ERROR, "Error executing move");

        // ========== ENGINE SETUP DIALOG ==========
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_TITLE, "Chess Engine Setup");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_HEADER, "Select a UCI-compatible chess engine");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_INFO, "Supported: Stockfish, Leela Chess Zero, Komodo and others");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_PATH_LABEL, "Engine path:");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_PATH_PROMPT, "Select engine file...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_BROWSE, "Browse...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_TEST, "Test Engine");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_OK, "OK");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_CANCEL, "Cancel");

        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_CHOOSER_TITLE, "Select engine executable file");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_FILTER_EXECUTABLE, "Executable files");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_FILTER_ALL, "All files");

        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_SELECT_FILE, "❌ Please select an engine file first");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_FILE_NOT_EXISTS, "❌ File does not exist");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_CHECKING, "⏳ Checking engine...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_READY, "✅ Engine is ready!");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_FAILED, "❌ Failed to start engine. Make sure it's a UCI-compatible engine");

        // ========== ANALYSIS PANEL ==========
        strings.put(LanguageKeys.ANALYSIS_TITLE, "Engine Analysis");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_STOPPED, "STOPPED");
        strings.put(LanguageKeys.ANALYSIS_ANALYZING, "ANALYZING");
        strings.put(LanguageKeys.ANALYSIS_CURRENT_EVAL, "Current Evaluation");
        strings.put(LanguageKeys.ANALYSIS_DEPTH, "depth");
        strings.put(LanguageKeys.ANALYSIS_ADD_LINE_TOOLTIP, "Add analysis line (max. %d)");
        strings.put(LanguageKeys.ANALYSIS_REMOVE_LINE_TOOLTIP, "Remove analysis line (min. %d)");
        strings.put(LanguageKeys.ANALYSIS_TOGGLE_TOOLTIP, "Start/Stop analysis (Enter)");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_TITLE, "Engine not running");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_HEADER, "Chess engine is not running");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_CONTENT, "Please set up the engine via Assistant → Configure Engine");

        // ========== CONFIRM DIALOGS ==========
        strings.put(LanguageKeys.CONFIRM_DELETE_TITLE, "Confirm Deletion");
        strings.put(LanguageKeys.CONFIRM_DELETE_AFTER_HEADER, "Delete all moves after %s?");
        strings.put(LanguageKeys.CONFIRM_DELETE_CONTENT, "This action cannot be undone!");
        strings.put(LanguageKeys.CONFIRM_DELETE_YES, "Yes, delete");
        strings.put(LanguageKeys.CONFIRM_DELETE_NO, "Cancel");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_TITLE, "Confirm Deletion");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_HEADER, "Delete variation \"%s\"?");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_CONTENT, "This action cannot be undone!\nAll moves and sub-variations will be deleted.");

        // ========== TIMER ==========
        strings.put(LanguageKeys.TIMER_SELECT_TIME, "Select time");
        strings.put(LanguageKeys.TIMER_PRESET_1_MIN, "1 min");
        strings.put(LanguageKeys.TIMER_PRESET_2_MIN, "2 min");
        strings.put(LanguageKeys.TIMER_PRESET_3_MIN, "3 min");
        strings.put(LanguageKeys.TIMER_PRESET_5_MIN, "5 min");
        strings.put(LanguageKeys.TIMER_PRESET_10_MIN, "10 min");
        strings.put(LanguageKeys.TIMER_CUSTOM_TOOLTIP, "Custom setting (seconds)");
        strings.put(LanguageKeys.TIMER_CUSTOM_TITLE, "Time Settings");
        strings.put(LanguageKeys.TIMER_CUSTOM_HEADER, "Enter time in seconds");
        strings.put(LanguageKeys.TIMER_CUSTOM_CONTENT, "Seconds:");
        strings.put(LanguageKeys.TIMER_MAX_TIME_WARNING, "Maximum time is 60 minutes (3600 seconds)");
        strings.put(LanguageKeys.TIMER_INVALID_NUMBER, "Please enter a valid number");

        // ========== MAIN CONTROLLER ==========
        strings.put(LanguageKeys.NEW_GAME_TITLE, "New Game");
        strings.put(LanguageKeys.NEW_GAME_HEADER, "Game Over");
        strings.put(LanguageKeys.NEW_GAME_CONTENT, "Start a new game?");

        strings.put(LanguageKeys.SAVE_GAME_TITLE, "Save Game");
        strings.put(LanguageKeys.SAVE_GAME_HEADER, "Game not saved");
        strings.put(LanguageKeys.SAVE_GAME_CONTENT, "Save current game before starting a new one?");
        strings.put(LanguageKeys.SAVE_GAME_SAVE, "Save");
        strings.put(LanguageKeys.SAVE_GAME_DONT_SAVE, "Don't save");
        strings.put(LanguageKeys.SAVE_GAME_CANCEL, "Cancel");

        strings.put(LanguageKeys.SAVE_GAME_DIALOG_TITLE, "Save Game");
        strings.put(LanguageKeys.SAVE_GAME_DIALOG_HEADER, "Enter game information");
        strings.put(LanguageKeys.SAVE_GAME_DIALOG_SAVE, "Save");

        strings.put(LanguageKeys.SAVE_GAME_PROMPT_WHITE, "White");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_BLACK, "Black");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_WHITE_ELO, "White rating");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_BLACK_ELO, "Black rating");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_EVENT, "Event");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_SITE, "Site");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_ROUND, "Round");

        strings.put(LanguageKeys.SAVE_GAME_LABEL_WHITE, "White");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_BLACK, "Black");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_RATING, "Rating");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_EVENT, "Event");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_SITE, "Site");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_ROUND, "Round");

        strings.put(LanguageKeys.SAVE_GAME_SUCCESS, "Game saved (will be added to DB later)");

        strings.put(LanguageKeys.FEATURE_NOT_IMPLEMENTED, "This feature will be implemented in the future");
        strings.put(LanguageKeys.PGN_LOAD_MESSAGE, "Loading PGN");
        strings.put(LanguageKeys.PGN_SAVE_MESSAGE, "Saving PGN");
        strings.put(LanguageKeys.DB_OPEN_MESSAGE, "Opening DB");

        // ========== MAIN CONTROLLER MESSAGES ==========
        strings.put(LanguageKeys.MAIN_LOAD_GAME_EMPTY_TREE, "Failed to load game: empty tree");
        strings.put(LanguageKeys.MAIN_LOADED_POSITION, "Position loaded");
        strings.put(LanguageKeys.MAIN_PGN_EMPTY, "PGN is empty");
        strings.put(LanguageKeys.MAIN_CLOSE_BUTTON, "Close");
        strings.put(LanguageKeys.MAIN_NO_CHANGES, "No changes detected, save not required");
        strings.put(LanguageKeys.MAIN_GAME_SAVED_FILE, "Game saved to file: %s");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_TITLE, "Save Game");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_HEADER, "Choose save location");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_CONTENT, "Where to save the game?");
        strings.put(LanguageKeys.MAIN_SAVE_TO_PGN_FILE, "💾 Save to PGN file");
        strings.put(LanguageKeys.MAIN_SAVE_TO_DATABASE, "📁 Save to database");
        strings.put(LanguageKeys.MAIN_SAVE_CANCEL, "Cancel");
        strings.put(LanguageKeys.MAIN_SAVE_PGN_FILE_TITLE, "Save game to PGN file");
        strings.put(LanguageKeys.MAIN_DB_NOT_INITIALIZED_MSG, "Database not initialized");
        strings.put(LanguageKeys.MAIN_GAME_SAVED_TO_DB, "Game saved to database");
        strings.put(LanguageKeys.MAIN_OPEN_PGN_ERROR_MSG, "Error opening PGN");
        strings.put(LanguageKeys.MAIN_BROWSER_LIMIT_MSG, "Browser limit");
        strings.put(LanguageKeys.MAIN_OPEN_ERROR_MSG, "Open error");
        strings.put(LanguageKeys.MAIN_INDEXING_TITLE_MSG, "Indexing PGN file");
        strings.put(LanguageKeys.MAIN_INDEXING_COMPLETE, "Indexing complete!\nGames found: %d\nActive: %d");
        strings.put(LanguageKeys.MAIN_INDEXING_ERROR_MSG, "Indexing error");
        strings.put(LanguageKeys.MAIN_POSITION_SOLVE_HINT, " (press → to solve)");
        strings.put(LanguageKeys.MAIN_REFRESHED_MSG, "List refreshed");
        strings.put(LanguageKeys.MAIN_NO_ACTIVE_BROWSER_MSG, "No active browser");
        strings.put(LanguageKeys.MAIN_UNKNOWN_PATH, "Unknown");
        strings.put(LanguageKeys.MAIN_POSITION_LOADED, "Position loaded: %s%s");
        strings.put(LanguageKeys.MAIN_GAME_LOADED, "Game loaded: ");

        strings.put(LanguageKeys.ABOUT_TITLE, "About Kletka");
        strings.put(LanguageKeys.ABOUT_CONTENT,
                """
                    ♔ Kletka Chess ♔
                    Version: 1.0
                    Platform: Java 17, OpenJFX
                    Library: chesslib 1.3.6 (GPL v3)
                    
                    Cross-platform chess analyzer
                    with SQLite database support
                    
                    © 2026 Khrypach Andrey
                    
                    License: GNU General Public License v3.0
                    https://www.gnu.org/licenses/gpl-3.0.html
                    """);

        strings.put(LanguageKeys.POSITION_SET_SUCCESS, "Position set. %s to move");

        strings.put(LanguageKeys.ENGINE_SETUP_TITLE, "Engine Setup");
        strings.put(LanguageKeys.ENGINE_SETUP_HEADER, "Chess engine not configured");
        strings.put(LanguageKeys.ENGINE_SETUP_CONTENT,
                """
                        To use analysis features, you need to set up a UCI-compatible engine.
                        
                        Would you like to select an engine now?""");
        strings.put(LanguageKeys.ENGINE_SETUP_BUTTON, "Set up");
        strings.put(LanguageKeys.ENGINE_SETUP_LATER, "Later");
        strings.put(LanguageKeys.ENGINE_SETUP_SUCCESS, "Engine configured successfully!");
        strings.put(LanguageKeys.ENGINE_SETUP_ERROR_TITLE, "Error");
        strings.put(LanguageKeys.ENGINE_SETUP_ERROR_CONTENT, "Failed to start engine");

        strings.put(LanguageKeys.ENGINE_SWITCH_TITLE, "Switch Engine");
        strings.put(LanguageKeys.ENGINE_SWITCH_HEADER, "Current engine will be stopped");
        strings.put(LanguageKeys.ENGINE_SWITCH_CONTENT, "Continue?");
        strings.put(LanguageKeys.ENGINE_SWITCH_SUCCESS, "Engine switched successfully!");

        strings.put(LanguageKeys.ENGINE_NOT_RUNNING,
                "Engine is not running. Check that the engine file exists in the engines/ folder");

        strings.put(LanguageKeys.ANALYSIS_BEST_MOVE, "Best move: %s (%s)");
        strings.put(LanguageKeys.ANALYSIS_ERROR_TITLE, "Error");
        strings.put(LanguageKeys.ANALYSIS_ERROR_CONTENT, "Failed to parse move");

        strings.put(LanguageKeys.CONFIRM_YES, "Yes");
        strings.put(LanguageKeys.CONFIRM_NO, "No");

        // Logging
        strings.put(LanguageKeys.LOG_INITIALIZED, "Logging initialized");
        strings.put(LanguageKeys.LOG_STARTING_GUI, "Starting KletkaGui");
        strings.put(LanguageKeys.LOG_GUI_LOADED, "GUI loaded successfully");
        strings.put(LanguageKeys.LOG_GUI_ERROR, "Error starting GUI");
        strings.put(LanguageKeys.LOG_SHUTTING_DOWN, "Kletka is shutting down...");

        // ========== SPLASH SCREEN ==========
        strings.put(LanguageKeys.SPLASH_LOADING_ENGINE, "Loading engine...");
        strings.put(LanguageKeys.SPLASH_INITIALIZING_BOARD, "Initializing board...");
        strings.put(LanguageKeys.SPLASH_LOADING_GUI, "Loading GUI...");
        strings.put(LanguageKeys.SPLASH_READY, "Ready!");

        // ========== GAME TYPE POSITION ==========
        strings.put(LanguageKeys.GAME_TYPE_POSITION, "Position");
        strings.put(LanguageKeys.GAME_TYPE_STUDY, "Study");
        strings.put(LanguageKeys.GAME_TYPE_PROBLEM, "Problem");
        strings.put(LanguageKeys.GAME_TYPE_GAME, "Game");
        strings.put(LanguageKeys.DEFAULT_PLAYER_NAME, "Player");

        // ========== PGN KEYWORD MATE ==========
        strings.put(LanguageKeys.PGN_KEYWORD_MATE, "mate");
        strings.put(LanguageKeys.PGN_KEYWORD_STUDY, "study");

        // ========== REPO ERROR ==========
        strings.put(LanguageKeys.REPO_ERROR_CREATE_DIR, "Failed to create directory: %s");
        strings.put(LanguageKeys.REPO_ERROR_GAME_NULL, "Game cannot be null");
        strings.put(LanguageKeys.REPO_ERROR_SAVE_GAME, "Failed to save game: %s");
        strings.put(LanguageKeys.REPO_ERROR_READ_DIR, "Failed to read directory: %s");
        strings.put(LanguageKeys.REPO_ERROR_DELETE_GAME, "Failed to delete game: %s");
        strings.put(LanguageKeys.REPO_ERROR_DELETE_ALL, "Failed to delete games");
        strings.put(LanguageKeys.REPO_ERROR_COUNT, "Failed to count games");
        strings.put(LanguageKeys.REPO_ERROR_READ_FILE, "Failed to read file: %s");
        strings.put(LanguageKeys.REPO_ERROR_PARSE_PGN, "PGN parsing error: %s");
        strings.put(LanguageKeys.REPO_ERROR_GAMES_EMPTY, "Game list is empty");
        strings.put(LanguageKeys.REPO_ERROR_EXPORT, "Failed to export games to: %s");

        // ========== EXPORT ERROR ==========
        strings.put(LanguageKeys.EXPORT_ERROR_GAMES_EMPTY, "Game list is empty");
        strings.put(LanguageKeys.EXPORT_ERROR_FILE_NULL, "File cannot be null");
        strings.put(LanguageKeys.EXPORT_ERROR_EXPORT_FAILED, "Failed to export games to: %s");
        strings.put(LanguageKeys.EXPORT_ERROR_NO_GAMES, "No games to export");
        strings.put(LanguageKeys.EXPORT_ERROR_CREATE_DIR, "Failed to create directory: %s");
        strings.put(LanguageKeys.EXPORT_ERROR_GAME_NULL, "Game cannot be null");

        // ========== IMPORT ERROR ==========
        strings.put(LanguageKeys.IMPORT_ERROR_FILE_NOT_FOUND, "File not found: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_READ_FILE, "Failed to read file: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_PARSE_PGN, "PGN parsing error in file: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_PGN_EMPTY, "PGN content is empty");
        strings.put(LanguageKeys.IMPORT_ERROR_PARSE_GENERAL, "PGN parsing error");
        strings.put(LanguageKeys.IMPORT_ERROR_DIR_NOT_FOUND, "Directory not found: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_DIR_NOT_FOUND_SIMPLE, "Directory not found");
        strings.put(LanguageKeys.IMPORT_ERROR_READ_DIR, "Failed to read directory: %s");

        // PGN service errors
        strings.put(LanguageKeys.PGN_SERVICE_ERROR_GAME_NULL, "Game cannot be null");
        strings.put(LanguageKeys.PGN_SERVICE_ERROR_GAMES_EMPTY, "Game list is empty");

        // Donate dialog
        strings.put(LanguageKeys.DONATE_TITLE, "☕ Support the project");
        strings.put(LanguageKeys.DONATE_HEADER, "☕ Support Kletka project");
        strings.put(LanguageKeys.DONATE_DESCRIPTION, "Thank you for using Kletka!\nYour support helps the project grow.");
        strings.put(LanguageKeys.DONATE_HINT, "💡 Click «Copy» to copy the address to clipboard.");
        strings.put(LanguageKeys.DONATE_CLOSE, "Close");
        strings.put(LanguageKeys.DONATE_PAYPAL, "📧 PayPal");
        strings.put(LanguageKeys.DONATE_COPY, "📋 Copy");
        strings.put(LanguageKeys.DONATE_OPEN, "🌐 Open");
        strings.put(LanguageKeys.DONATE_BITCOIN, "₿ Bitcoin");
        strings.put(LanguageKeys.DONATE_QR, "📱 QR code");
        strings.put(LanguageKeys.DONATE_QR_TITLE, "QR code for Bitcoin");
        strings.put(LanguageKeys.DONATE_QR_HINT, "Scan the QR code in your Bitcoin wallet");
        strings.put(LanguageKeys.DONATE_TOAST_COPIED_EMAIL, "Email copied!");
        strings.put(LanguageKeys.DONATE_TOAST_COPIED_BITCOIN, "Bitcoin address copied!");
        strings.put(LanguageKeys.DONATE_TOAST_OPEN_BROWSER, "Failed to open browser");
        strings.put(LanguageKeys.DONATE_TOAST_QR_ERROR, "QR code generation error");

        // Position setup dialog
        strings.put(LanguageKeys.SETUP_DELETE_MODE_ACTIVE_TOOLTIP, "First turn off delete mode");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_NOTIFICATION, "Move automatically changed to %s (king of %s was in check)");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_WHITE, "white");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_BLACK, "black");
        strings.put(LanguageKeys.SETUP_LOAD_POSITION_ERROR, "Error loading position: %s");

        // SaveGameDialog - Titles
        strings.put(LanguageKeys.SAVE_DIALOG_TITLE_EDIT, "✏️ Edit Game");
        strings.put(LanguageKeys.SAVE_DIALOG_TITLE_SAVE, "💾 Save Game");
        strings.put(LanguageKeys.SAVE_DIALOG_HEADER_EDIT, "Edit game information");
        strings.put(LanguageKeys.SAVE_DIALOG_HEADER_SAVE, "Enter game information");

        // Tabs
        strings.put(LanguageKeys.SAVE_TAB_PLAYERS, "Players & Result");
        strings.put(LanguageKeys.SAVE_TAB_TOURNAMENT, "Tournament");
        strings.put(LanguageKeys.SAVE_TAB_DETAILS, "Details");

        // Players tab
        strings.put(LanguageKeys.SAVE_LABEL_WHITE, "White:");
        strings.put(LanguageKeys.SAVE_LABEL_BLACK, "Black:");
        strings.put(LanguageKeys.SAVE_LABEL_ELO_WHITE, "White Elo:");
        strings.put(LanguageKeys.SAVE_LABEL_ELO_BLACK, "Black Elo:");
        strings.put(LanguageKeys.SAVE_LABEL_WHITE_TEAM, "White Team:");
        strings.put(LanguageKeys.SAVE_LABEL_BLACK_TEAM, "Black Team:");
        strings.put(LanguageKeys.SAVE_LABEL_ANNOTATOR, "Annotator:");
        strings.put(LanguageKeys.SAVE_LABEL_RESULT, "Result:");
        strings.put(LanguageKeys.SAVE_RESULT_1_0, "1-0");
        strings.put(LanguageKeys.SAVE_RESULT_0_1, "0-1");
        strings.put(LanguageKeys.SAVE_RESULT_DRAW, "1/2-1/2");
        strings.put(LanguageKeys.SAVE_RESULT_UNKNOWN, "*");

        // Tournament tab
        strings.put(LanguageKeys.SAVE_LABEL_EVENT, "Event:");
        strings.put(LanguageKeys.SAVE_LABEL_SITE, "Site:");
        strings.put(LanguageKeys.SAVE_LABEL_ROUND, "Round:");
        strings.put(LanguageKeys.SAVE_LABEL_SUBROUND, "Subround:");
        strings.put(LanguageKeys.SAVE_LABEL_DATE, "Date:");
        strings.put(LanguageKeys.SAVE_LABEL_YEAR, "Year:");
        strings.put(LanguageKeys.SAVE_LABEL_MONTH, "Month:");
        strings.put(LanguageKeys.SAVE_LABEL_DAY, "Day:");
        strings.put(LanguageKeys.SAVE_BUTTON_RESET_DATE, "Reset");

        // Details tab
        strings.put(LanguageKeys.SAVE_LABEL_ECO, "ECO code:");
        strings.put(LanguageKeys.SAVE_LABEL_OPENING, "Opening:");
        strings.put(LanguageKeys.SAVE_LABEL_VARIATION, "Variation:");
        strings.put(LanguageKeys.SAVE_LABEL_TIME_CONTROL, "Time Control:");
        strings.put(LanguageKeys.SAVE_LABEL_SOURCE, "Source:");
        strings.put(LanguageKeys.SAVE_LABEL_FEN, "FEN:");
        strings.put(LanguageKeys.SAVE_CHECKBOX_SETUP, "SetUp (position)");
        strings.put(LanguageKeys.SAVE_LABEL_TYPE, "Type:");
        strings.put(LanguageKeys.SAVE_BUTTON_DETECT_OPENING, "🎯 Detect Opening");

        // Buttons
        strings.put(LanguageKeys.SAVE_BUTTON_SAVE, "Save");
        strings.put(LanguageKeys.SAVE_BUTTON_SAVE_CHANGES, "Save Changes");
        strings.put(LanguageKeys.SAVE_BUTTON_CANCEL, "Cancel");
        strings.put(LanguageKeys.SAVE_BUTTON_HELP, "Help");

        // Messages
        strings.put(LanguageKeys.SAVE_MSG_ECO_NOT_LOADED, "Opening database not loaded yet. Please try later.");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_FOUND, "Opening found: %s - %s");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_NOT_FOUND, "Opening not found");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_ERROR, "Error detecting opening: %s");

        // Help dialog
        strings.put(LanguageKeys.SAVE_HELP_TITLE, "Help");
        strings.put(LanguageKeys.SAVE_HELP_HEADER, "Saving game to PGN");
        strings.put(LanguageKeys.SAVE_HELP_CONTENT,
                """
                        Fill in the game information:
                        
                        • Players - names of white and black
                        • Ratings - ELO rating of each player
                        • Result - game outcome
                        • Tournament - name, site, round
                        • Date - year, month, day
                        • ECO - opening code from encyclopedia
                        • Opening - opening name
                        • Variation - opening variation
                        
                        FOR POSITIONS:
                        • FEN - position in FEN format
                        • SetUp - mark as a position
                        • Type - game, position, study, problem
                        
                        Fields with "?" will be replaced with default values.""");

        // Type options
        strings.put(LanguageKeys.SAVE_TYPE_GAME, "Game");
        strings.put(LanguageKeys.SAVE_TYPE_POSITION, "Position");
        strings.put(LanguageKeys.SAVE_TYPE_STUDY, "Study");
        strings.put(LanguageKeys.SAVE_TYPE_PROBLEM, "Problem");

        // Logo
        strings.put(LanguageKeys.LOGO_TITLE, "KLETKA");
        strings.put(LanguageKeys.LOGO_SUBTITLE_LINE1, "CHESS");
        strings.put(LanguageKeys.LOGO_SUBTITLE_LINE2, "ANALYZER");

        //Menu
        strings.put(LanguageKeys.MENU_WINDOWS, "Windows");
        strings.put(LanguageKeys.MENU_WINDOWS_CLIPBOARD_EMPTY, "📋 Clipboard: empty");
        strings.put(LanguageKeys.MENU_WINDOWS_CLIPBOARD_CONTENT, "📋 Clipboard: %d games from '%s'");
        strings.put(LanguageKeys.MENU_WINDOWS_CLEAR_CLIPBOARD, "🧹 Clear clipboard");
        strings.put(LanguageKeys.MENU_WINDOWS_CLOSE_ALL, "✕ Close all");
        strings.put(LanguageKeys.MENU_WINDOWS_NO_FILES, "(no open files)");
        strings.put(LanguageKeys.MENU_WINDOWS_BROWSER_ITEM, "📁 %s (%d games)");

        strings.put(LanguageKeys.MENU_FILE_OPEN_BROWSER, "Open PGN Browser");
        strings.put(LanguageKeys.MENU_FILE_REFRESH_BROWSER, "Refresh Browser");

        strings.put(LanguageKeys.MENU_VIEW_TOGGLE_NOTATION, "Show/Hide Notation");

        strings.put(LanguageKeys.MENU_HELP_DONATE, "☕ Support the project");

        strings.put(LanguageKeys.BROWSER_GAMES_COUNT, "games");
        strings.put(LanguageKeys.BROWSER_CLIPBOARD_COUNT, "games");

        // PGN Browser Manager
        strings.put(LanguageKeys.PGN_BROWSER_LIMIT_REACHED, "Browser limit reached (%d). Close one of the files.");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_LIMIT, "Cannot copy more than %d games at once");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_UNAVAILABLE, "Paste is not available");
        strings.put(LanguageKeys.PGN_BROWSER_NO_INDEX, "Target file has no index: %s");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_ERROR, "❌ Not enough disk space!");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_CHECK, "Required: ~%.1f MB\nAvailable: %.1f MB\n\nFree up disk space and try again.");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_INSUFFICIENT, "❌ Not enough disk space!\n\nPaste interrupted after %d games.\nThe file and index will be automatically restored on next open.\n\nFree up disk space and try again.");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_INTERRUPTED, "❌ Error while pasting games\n\n%d games were added.\nThe file index will be updated on next open.\n\nReason: %s");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_ERROR, "Error pasting games: %s");

        // Progress messages
        strings.put(LanguageKeys.PGN_BROWSER_COPY_PREPARING, "Preparing to copy...");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_TOTAL, "Total: %d games");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_COMPLETE, "✅ Copied %d games");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_SOURCE, "Source: %s");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_PREPARING, "Preparing to paste...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_TOTAL, "Total: %d games");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_PROGRESS, "Pasting: %d of %d games");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_ADDED, "Added %d games");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_COMPLETE, "✅ Pasted %d games");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_TARGET, "Target file: %s");

        // Indexing Progress
        strings.put(LanguageKeys.INDEXING_PROGRESS_MESSAGE, "Processed %d of %d games (%.1f%%)");
        strings.put(LanguageKeys.INDEXING_STATUS_STARTING, "Starting indexing...");
        strings.put(LanguageKeys.INDEXING_STATUS_COMPLETE, "Indexing complete!");

        // PGN Game Operation
        strings.put(LanguageKeys.PGN_OP_EDIT_SUCCESS, "Game #%d edited");
        strings.put(LanguageKeys.PGN_OP_DELETE_SUCCESS, "Game #%d deleted");
        strings.put(LanguageKeys.PGN_OP_ADD_SUCCESS, "New game #%d added");
        strings.put(LanguageKeys.PGN_OP_DUPLICATE_SUCCESS, "Game #%d duplicated as #%d");

        // Delete Confirm Dialog
        strings.put(LanguageKeys.DELETE_CONFIRM_TITLE, "Delete Confirmation");
        strings.put(LanguageKeys.DELETE_CONFIRM_SINGLE_TITLE, "🗑️ Delete Game");
        strings.put(LanguageKeys.DELETE_CONFIRM_MULTIPLE_TITLE, "🗑️ Delete %d Games");
        strings.put(LanguageKeys.DELETE_CONFIRM_SINGLE_MESSAGE, "Are you sure you want to delete game #%d?\n\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_MULTIPLE_MESSAGE, "Are you sure you want to delete %d games?\n\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_WHITE, "White: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_BLACK, "Black: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_RESULT, "Result: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_AND_MORE, "  ... and %d more games\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_WARNING, "\n⚠️ This action is irreversible until the next repack.");
        strings.put(LanguageKeys.DELETE_CONFIRM_DELETE_BUTTON, "🗑️ Delete");
        strings.put(LanguageKeys.DELETE_CONFIRM_CANCEL_BUTTON, "Cancel");
        strings.put(LanguageKeys.DELETE_CONFIRM_UNKNOWN, "?");
        strings.put(LanguageKeys.DELETE_CONFIRM_GAME_PREFIX, "  #%d: %s vs %s (%s)\n");

        // Indexing Progress Dialog
        strings.put(LanguageKeys.INDEXING_DIALOG_TITLE, "Indexing PGN file");
        strings.put(LanguageKeys.INDEXING_DIALOG_STATUS_PREPARING, "Preparing for indexing...");
        strings.put(LanguageKeys.INDEXING_DIALOG_GAMES_PROCESSED, "%d games processed");
        strings.put(LanguageKeys.INDEXING_DIALOG_COMPLETE, "Complete!");
        strings.put(LanguageKeys.INDEXING_DIALOG_CANCEL, "Cancel");
        strings.put(LanguageKeys.INDEXING_DIALOG_ERROR, "❌ %s");
        strings.put(LanguageKeys.INDEXING_DIALOG_PROGRESS_FORMAT, "%d / %d (%.1f%%)");

        // ========== PGN BROWSER - TABLE COLUMNS ==========
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_ID, "#");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_WHITE, "White");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_BLACK, "Black");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_RESULT, "Result");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_YEAR, "Year");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_EVENT, "Event");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_ECO, "ECO");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_OPENING, "Opening");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_BODY, "Game");

        // ========== PGN BROWSER - WINDOW ==========
        strings.put(LanguageKeys.PGN_BROWSER_TITLE, "PGN Browser - %s");
        strings.put(LanguageKeys.PGN_BROWSER_TITLE_ACTIVE, " ✅ Active");
        strings.put(LanguageKeys.PGN_BROWSER_TITLE_GAMES, " (%d games)");

        // ========== PGN BROWSER - SEARCH ==========
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_LABEL, "🔍 Search:");
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_PROMPT, "Enter player name, opening or ECO...");
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_CLEAR, "Clear");

        // ========== PGN BROWSER - STATUS ==========
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING, "Loading...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_TOTAL, "Total: %d games");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_SELECTED, "Selected: %d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_SHOWN, "Shown: %d of %d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_CLOSE, "✕ Close");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_READY, "Ready");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ERROR, "Error: %s");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_INDEX, "Loading index...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_PARSING, "Parsing PGN file...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_MORE, "Loading games...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ALL_LOADED, "All games loaded");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADED, "Loaded %d of %d games");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_CHECKING_INDEX, "Checking index...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_GAME, "Loading game...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_READY_WITH_COUNT, "Ready (%d games)");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ALL_LOADED_WITH_COUNT, "All games loaded (%d)");

        // ========== PGN BROWSER - BUTTONS ==========
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_EDIT, "✏️ Edit");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DELETE, "🗑️ Delete");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DELETE_COUNT, "🗑️ Delete (%d)");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DUPLICATE, "📋 Duplicate");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_COPY, "📋 Copy");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_PASTE, "📋 Paste");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK, "🔄 Repack");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK_COUNT, "🔄 Repack (%d)");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK_IN_PROGRESS, "⏳ Repacking...");

        // ========== PGN BROWSER - CONTEXT MENU ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_LOAD, "Load game");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_COPY, "📋 Copy");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_DELETE, "🗑️ Delete");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_SELECT_ALL, "Select all (Ctrl+A)");

        // ========== PGN BROWSER - MESSAGES ==========
        strings.put(LanguageKeys.PGN_BROWSER_MSG_REPACK_IN_PROGRESS, "⏳ Repack in progress, please wait...");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_ONE, "Select ONE game to edit");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_UNAVAILABLE, "Editing temporarily unavailable");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_GAMES, "Select games to delete");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_UNAVAILABLE, "Delete not available in this mode");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_LIMIT, "Cannot copy more than 1000 games at once. Selected: %d");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_UNAVAILABLE, "Copy not available in this mode");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_UNAVAILABLE, "Duplicate not available");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_UNAVAILABLE, "Paste not available. Clipboard is empty or target file matches source.");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_CLIPBOARD_EMPTY, "Clipboard is empty");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_NO_DELETED_GAMES, "No deleted games to repack");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_SUCCESS, "Copied %d games from '%s'");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_SUCCESS, "Pasted %d games to '%s'");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_SUCCESS, "Deleted %d games");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_SUCCESS, "Game duplicated as #%d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ERROR_LOADING, "Loading error: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_ONE_DUPLICATE, "Select ONE game to duplicate");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_ERROR, "Duplicate error: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_GAMES_COPY, "Select games to copy");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_ERROR, "Copy error: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY, "📋 Copy games");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_START, "Starting copy...");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_ERROR, "Paste error: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_GAMES, "📋 Paste games");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_START, "Starting paste...");

        // ========== PGN BROWSER - PROGRESS ==========
        strings.put(LanguageKeys.PGN_BROWSER_DELETING, "Deleting %d games...");
        strings.put(LanguageKeys.PGN_BROWSER_DELETING_PROCEED, "Deleting: %d of %d games");
        strings.put(LanguageKeys.PGN_BROWSER_DELETED, "Deleted %d games");
        strings.put(LanguageKeys.PGN_BROWSER_DUPLICATING, "Duplicating game...");
        strings.put(LanguageKeys.PGN_BROWSER_COPYING, "Copying %d games...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTING, "Pasting %d games...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTED, "Pasted %d games");
        strings.put(LanguageKeys.PGN_BROWSER_REPACKING, "Repacking...");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_COMPLETE, "✅ Repack completed successfully!\nActive games: %d");
        strings.put(LanguageKeys.PGN_BROWSER_START_DELETING, "Starting deletion...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_OPERATION_FINISHED, "OOperation complete");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_ERROR, "Delete error: %s");

        // ========== PGN BROWSER - CONFIRM PASTE ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_TITLE, "Paste Games");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_HEADER, "Paste %d games?");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_SOURCE, "Source: %s");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_TARGET, "Target file: %s");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_COUNT, "Games: %d");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_FREE_SPACE, "Free disk space: %s MB");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_INFO, "Games will be appended to the end of the file.");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_YES, "✅ Paste");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_NO, "Cancel");

        // ========== PGN BROWSER - CONFIRM REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_TITLE, "Repack PGN file");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_HEADER, "Perform manual repack?");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_CONTENT, "Found %d deleted games.\nActive games: %d\nSize ratio: %.1fx\n\nA new file without deleted games will be created.\n⚠️ Editing will be locked during repack.");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_YES, "✅ Repack");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_NO, "Cancel");

        // ========== PGN BROWSER - AUTO REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_TITLE, "⚠️ Repack required");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_HEADER, "PGN file has grown by %.1f times!");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_CONTENT, "File contains %d active and %d deleted games.\n\nIt is recommended to repack for optimization.");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_YES, "✅ Yes, repack");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_NO, "Later");

        // ========== PGN BROWSER - FILTER ==========
        strings.put(LanguageKeys.PGN_BROWSER_FILTER_TOTAL, "Total: %d games");
        strings.put(LanguageKeys.PGN_BROWSER_FILTER_FOUND, "Found: %d of %d games");

        // ========== PGN BROWSER - REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_TITLE, "🔄 Repack PGN file");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_IN_PROGRESS, "Repack in progress, please wait...");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_ERROR, "❌ Repack error: %s");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_SUCCESS, "✅ Repack completed successfully!\nActive games: %d");

        // Repack Progress Dialog
        strings.put(LanguageKeys.REPACK_DIALOG_TITLE, "🔄 Repack PGN file");
        strings.put(LanguageKeys.REPACK_DIALOG_STATUS_PREPARING, "Preparing for repack...");
        strings.put(LanguageKeys.REPACK_DIALOG_PROGRESS_FORMAT, "%d / %d (%.1f%%)");
        strings.put(LanguageKeys.REPACK_DIALOG_GAMES_PROCESSED, "%d games processed");
        strings.put(LanguageKeys.REPACK_DIALOG_COMPLETE, "✅ Complete!");
        strings.put(LanguageKeys.REPACK_DIALOG_ERROR, "❌ %s");

        // Repack Status Widget
        strings.put(LanguageKeys.REPACK_STATUS_OPTIMAL, "Optimal");
        strings.put(LanguageKeys.REPACK_STATUS_NO_DELETED, "✅ No deleted");
        strings.put(LanguageKeys.REPACK_STATUS_HAS_DELETED, "✅ Has deleted");
        strings.put(LanguageKeys.REPACK_STATUS_WARNING, "⚠️ Repack recommended");
        strings.put(LanguageKeys.REPACK_STATUS_CRITICAL, "🔴 Repack required!");
        strings.put(LanguageKeys.REPACK_STATUS_UNKNOWN, "Unknown");
        strings.put(LanguageKeys.REPACK_STATUS_REPACKING, "🔄 Repacking...");
        strings.put(LanguageKeys.REPACK_STATUS_DELETED_COUNT, "🗑️ %d");
        strings.put(LanguageKeys.REPACK_STATUS_TOOLTIP, "File size to active data ratio: %.1fx\nDeleted games: %d\nActive games: %d");
        strings.put(LanguageKeys.REPACK_STATUS_REPACKING_TOOLTIP, "Repack in progress");
        strings.put(LanguageKeys.REPACK_STATUS_RATIO, "%.1fx");
        strings.put(LanguageKeys.REPACK_STATUS_LOADING, "...");

        // Indexing
        strings.put(LanguageKeys.INDEXING_STATUS_SCANNING_FILE, "Indexing: scanning file...");
        strings.put(LanguageKeys.INDEXING_STATUS_CREATING_INDEX, "Indexing: creating index...");
        strings.put(LanguageKeys.INDEXING_STATUS_PREPARING_FILE, "Preparing file: adding [Deleted] tag");
        strings.put(LanguageKeys.INDEXING_STATUS_SCANNING_GAMES, "Indexing: scanning %d games...");
        strings.put(LanguageKeys.INDEXING_STATUS_PROCESSED, "Indexing: processed %d of %d games");
        strings.put(LanguageKeys.INDEXING_STATUS_PREPARING_ADD_DELETED, "Preparing file: adding [Deleted]");

        // File Preparation
        strings.put(LanguageKeys.PREPARE_STATUS_SCANNING, "Preparing file: scanning...");
        strings.put(LanguageKeys.PREPARE_STATUS_PROCESSED, "Preparing file: processed %d of %d games");
        strings.put(LanguageKeys.PREPARE_STATUS_BUILDING, "Preparing file: building...");
        strings.put(LanguageKeys.PREPARE_STATUS_BUILDING_BLOCKS, "Preparing file: building %d of %d blocks");
        strings.put(LanguageKeys.PREPARE_STATUS_SAVING, "Preparing file: saving...");
        strings.put(LanguageKeys.PREPARE_STATUS_COMPLETE, "Preparation complete: %d games, %d garbage blocks");

        // Indexing Facade
        strings.put(LanguageKeys.INDEXING_STEP1, "Step 1/3: Preparing file...");
        strings.put(LanguageKeys.INDEXING_STEP2, "Step 2/3: Creating index...");
        strings.put(LanguageKeys.INDEXING_STEP3, "Step 3/3: Saving index...");
        strings.put(LanguageKeys.INDEXING_COMPLETE_SUCCESS, "Indexing completed successfully!");

        // Repack
        strings.put(LanguageKeys.REPACK_STATUS_READING, "Repack: reading games...");
        strings.put(LanguageKeys.REPACK_STATUS_PROCESSED, "Repack: processed %d of %d games");
        strings.put(LanguageKeys.REPACK_STATUS_WRITING, "Repack: writing new file...");
        strings.put(LanguageKeys.REPACK_STATUS_CREATING_INDEX, "Repack: creating index...");
        strings.put(LanguageKeys.REPACK_STATUS_SAVING_INDEX, "Repack: saving index...");
        strings.put(LanguageKeys.REPACK_STATUS_REPLACING, "Repack: replacing files...");
        strings.put(LanguageKeys.REPACK_STATUS_COMPLETE, "✅ Repack complete! %d games, size: %.2f KB");

        // Repack Status Descriptions
        strings.put(LanguageKeys.REPACK_DESC_NO_GAMES, "No games");
        strings.put(LanguageKeys.REPACK_DESC_NO_DELETED, "✅ No deleted games");
        strings.put(LanguageKeys.REPACK_DESC_HAS_DELETED, "✅ Has deleted (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_WARNING, "⚠️ Recommended (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_CRITICAL, "🔴 Required! (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_DELETED_COUNT, ", %d deleted");

        // ========== LANGUAGE MENU ==========
        strings.put(LanguageKeys.MENU_LANGUAGE, "Language");
        strings.put(LanguageKeys.MENU_LANGUAGE_RUSSIAN, "Russian");
        strings.put(LanguageKeys.MENU_LANGUAGE_ENGLISH, "English");
        strings.put(LanguageKeys.MENU_LANGUAGE_CHINESE, "Chinese");
        strings.put(LanguageKeys.MENU_LANGUAGE_CHANGE, "Change Language");

        // ========== LANGUAGE CHANGE DIALOG ==========
        strings.put(LanguageKeys.LANG_CHANGE_TITLE, "Language Saved");
        strings.put(LanguageKeys.LANG_CHANGE_HEADER, "Language changed to: %s");
        strings.put(LanguageKeys.LANG_CHANGE_CONTENT, "Please restart the application for changes to take effect.");
        strings.put(LanguageKeys.LANG_CHANGE_BUTTON_OK, "OK");

        // ========== PGN BROWSER - EDIT HEADERS ==========
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_HEADERS_TITLE, "✏️ Edit Headers");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_HEADERS_HEADER, "Edit game headers #%s");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVE, "💾 Save Headers");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_CANCEL, "❌ Cancel");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_LOADING, "⏳ Loading game for editing...");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVING, "⏳ Saving changes...");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVED, "✅ Changes saved");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_BODY_INFO, "📄 Game body (%d moves) - read only");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_BODY_EMPTY, "📄 Game body is empty");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_WHITE, "White player name");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_BLACK, "Black player name");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_EVENT, "Event name");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_SITE, "Site");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_ROUND, "Round number");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_OPENING, "Opening name");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_VARIATION, "Opening variation");

        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_SUCCESS, "✅ Game headers updated successfully");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_ERROR, "❌ Error while editing: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_READ_ONLY, "Read only");

        // ========== SHORTCUTS ==========
        strings.put(LanguageKeys.SHORTCUTS_TITLE, "Keyboard Shortcuts");

        StringBuilder shortcutsContent = createShortcutsContent();

        strings.put(LanguageKeys.SHORTCUTS_CONTENT, shortcutsContent.toString());

        // Отдельные ключи
        strings.put(LanguageKeys.SHORTCUT_FILE, "📁 File");
        strings.put(LanguageKeys.SHORTCUT_VIEW, "👁️ View");
        strings.put(LanguageKeys.SHORTCUT_NAVIGATION, "🧭 Navigation");
        strings.put(LanguageKeys.SHORTCUT_ENGINE, "⚙️ Engine");
        strings.put(LanguageKeys.SHORTCUT_PGN, "📚 PGN/Browser");
        strings.put(LanguageKeys.SHORTCUT_DATABASE, "🗄️ Database");
        strings.put(LanguageKeys.SHORTCUT_WINDOWS, "🪟 Windows");

        strings.put(LanguageKeys.SHORTCUT_NEW_GAME, "Ctrl+N - New Game");
        strings.put(LanguageKeys.SHORTCUT_OPEN_PGN, "Ctrl+O - Open PGN");
        strings.put(LanguageKeys.SHORTCUT_SAVE_PGN, "Ctrl+S - Save PGN");
        strings.put(LanguageKeys.SHORTCUT_EXPORT_CURRENT, "Ctrl+E - Export Current Game");
        strings.put(LanguageKeys.SHORTCUT_IMPORT_CLIPBOARD, "Ctrl+Shift+V - Import from Clipboard");
        strings.put(LanguageKeys.SHORTCUT_SETUP_POSITION, "Ctrl+P - Setup Position");
        strings.put(LanguageKeys.SHORTCUT_EXIT, "Alt+F4 - Exit");

        strings.put(LanguageKeys.SHORTCUT_FLIP_BOARD, "Ctrl+F - Flip Board");
        strings.put(LanguageKeys.SHORTCUT_COORDINATES, "Ctrl+Shift+C - Show Coordinates");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_IN, "Ctrl+= - Zoom In");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_OUT, "Ctrl+- - Zoom Out");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_RESET, "Ctrl+0 - Reset Zoom");
        strings.put(LanguageKeys.SHORTCUT_TOGGLE_NOTATION, "H - Toggle Notation");

        strings.put(LanguageKeys.SHORTCUT_NAV_PREV, "← - Previous Move");
        strings.put(LanguageKeys.SHORTCUT_NAV_NEXT, "→ - Next Move");
        strings.put(LanguageKeys.SHORTCUT_NAV_FIRST, "↑ - First Move");
        strings.put(LanguageKeys.SHORTCUT_NAV_LAST, "↓ - Last Move");

        strings.put(LanguageKeys.SHORTCUT_ENGINE_MOVE, "Space - Engine Move");
        strings.put(LanguageKeys.SHORTCUT_ENGINE_ANALYZE, "Shift+Enter - Toggle Analysis");
        strings.put(LanguageKeys.SHORTCUT_ENGINE_CONFIGURE, "Ctrl+Shift+E - Configure Engine");

        strings.put(LanguageKeys.SHORTCUT_OPEN_BROWSER, "Ctrl+B - Open PGN Browser");
        strings.put(LanguageKeys.SHORTCUT_REFRESH_BROWSER, "Ctrl+R - Refresh Browser");
        strings.put(LanguageKeys.SHORTCUT_NEXT_GAME, "F11 - Next Game");
        strings.put(LanguageKeys.SHORTCUT_PREV_GAME, "Ctrl+F11 - Previous Game");
        strings.put(LanguageKeys.SHORTCUT_NEXT_BROWSER, "Ctrl+Tab - Next Browser");
        strings.put(LanguageKeys.SHORTCUT_PREV_BROWSER, "Ctrl+Shift+Tab - Previous Browser");
        strings.put(LanguageKeys.SHORTCUT_CLOSE_BROWSER, "Ctrl+W - Close Browser");
        strings.put(LanguageKeys.SHORTCUT_MINIMIZE_BROWSER, "Minimize Browser");
        strings.put(LanguageKeys.SHORTCUT_MAXIMIZE_BROWSER, "Maximize Browser");

        strings.put(LanguageKeys.SHORTCUT_CONNECT_DB, "Ctrl+D - Connect to Database");
        strings.put(LanguageKeys.SHORTCUT_IMPORT_DB, "Ctrl+I - Import to Database");
        strings.put(LanguageKeys.SHORTCUT_SEARCH_DB, "Ctrl+Shift+F - Search Database");

        strings.put(LanguageKeys.SHORTCUT_CLOSE_ALL_BROWSERS, "Ctrl+Shift+W - Close All Browsers");

        strings.put(LanguageKeys.FAILED_TOKENIZE_PGN, "Failed to tokenize PGN");
        strings.put(LanguageKeys.ERROR_PARSING_MOVE, "Error parsing move: %s");
        strings.put(LanguageKeys.ERROR_PARSING_PGN_TO_TREE, "Error parsing PGN to tree: %s");
    }

    private static StringBuilder createShortcutsContent() {
        StringBuilder shortcutsContent = new StringBuilder();
        shortcutsContent.append("═══════════════════════════════════════\n");
        shortcutsContent.append("📁 File:\n");
        shortcutsContent.append("  Ctrl+N - New Game\n");
        shortcutsContent.append("  Ctrl+O - Open PGN\n");
        shortcutsContent.append("  Ctrl+S - Save PGN\n");
        shortcutsContent.append("  Ctrl+E - Export Current Game\n");
        shortcutsContent.append("  Ctrl+Shift+V - Import from Clipboard\n");
        shortcutsContent.append("  Ctrl+P - Setup Position\n");
        shortcutsContent.append("  Alt+F4 - Exit\n");

        shortcutsContent.append("\n👁️ View:\n");
        shortcutsContent.append("  Ctrl+F - Flip Board\n");
        shortcutsContent.append("  Ctrl+= - Zoom In\n");
        shortcutsContent.append("  Ctrl+- - Zoom Out\n");
        shortcutsContent.append("  Ctrl+0 - Reset Zoom\n");
        shortcutsContent.append("  H - Toggle Notation\n");

        shortcutsContent.append("\n🧭 Navigation:\n");
        shortcutsContent.append("  ← - Previous Move\n");
        shortcutsContent.append("  → - Next Move\n");
        shortcutsContent.append("  ↑ - First Move\n");
        shortcutsContent.append("  ↓ - Last Move\n");

        shortcutsContent.append("\n⚙️ Engine:\n");
        shortcutsContent.append("  Space - Engine Move\n");
        shortcutsContent.append("  Shift+Enter - Toggle Analysis\n");
        shortcutsContent.append("  Ctrl+Shift+E - Configure Engine\n");

        shortcutsContent.append("\n📚 PGN/Browser:\n");
        shortcutsContent.append("  Ctrl+B - Open PGN Browser\n");
        shortcutsContent.append("  Ctrl+R - Refresh Browser\n");
        shortcutsContent.append("  F11 - Next Game\n");
        shortcutsContent.append("  Ctrl+F11 - Previous Game\n");
        shortcutsContent.append("  Ctrl+Tab - Next Browser\n");
        shortcutsContent.append("  Ctrl+Shift+Tab - Previous Browser\n");
        shortcutsContent.append("  Ctrl+W - Close Browser\n");

        shortcutsContent.append("\n🗄️ Database:\n");
        shortcutsContent.append("  Ctrl+D - Connect to Database\n");
        shortcutsContent.append("  Ctrl+I - Import to Database\n");
        shortcutsContent.append("  Ctrl+Shift+F - Search Database\n");

        shortcutsContent.append("\n🪟 Windows:\n");
        shortcutsContent.append("  Ctrl+Shift+W - Close All Browsers\n");

        shortcutsContent.append("\n═══════════════════════════════════════");
        return shortcutsContent;
    }

    @Override
    public String getCode() {
        return "en";
    }

    @Override
    public String getDisplayName() {
        return "English";
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