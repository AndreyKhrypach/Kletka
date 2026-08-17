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

public class ZhLanguage implements Language {

    private final Map<String, String> strings = new HashMap<>();

    public ZhLanguage() {
        // ========== APP TITLE ==========
        strings.put(LanguageKeys.APP_TITLE, "Kletka - 国际象棋分析器");
        strings.put(LanguageKeys.APP_VERSION, "版本: 1.0");
        strings.put(LanguageKeys.APP_PLATFORM, "平台: Java 17, OpenJFX");
        strings.put(LanguageKeys.APP_LIBRARY, "库: chesslib 1.3.6");
        strings.put(LanguageKeys.APP_COPYRIGHT, "© 2026 赫里帕奇·安德烈");

        // ========== MENU ==========
        strings.put(LanguageKeys.MENU_FILE, "文件");
        strings.put(LanguageKeys.MENU_BOARD, "棋盘");
        strings.put(LanguageKeys.MENU_ASSISTANT, "助手");
        strings.put(LanguageKeys.MENU_DATABASE, "数据库");
        strings.put(LanguageKeys.MENU_HELP, "帮助");

        // ========== FILE MENU ==========
        strings.put(LanguageKeys.MENU_FILE_NEW_GAME, "新对局");
        strings.put(LanguageKeys.MENU_FILE_SETUP_POSITION, "摆子...");
        strings.put(LanguageKeys.MENU_FILE_OPEN_PGN, "打开PGN...");
        strings.put(LanguageKeys.MENU_FILE_SAVE_PGN, "保存PGN...");
        strings.put(LanguageKeys.MENU_FILE_EXPORT_IMAGE, "导出为图片");
        strings.put(LanguageKeys.MENU_FILE_EXIT, "退出");

        // ========== FILE FILTERS ==========
        strings.put(LanguageKeys.FILE_FILTER_PGN, "PGN文件");
        strings.put(LanguageKeys.FILE_FILTER_DATABASE_TITLE, "打开数据库");
        strings.put(LanguageKeys.FILE_FILTER_DATABASE, "SQLite数据库");
        strings.put(LanguageKeys.FILE_FILTER_ALL, "所有文件");

        // ========== BOARD MENU ==========
        strings.put(LanguageKeys.MENU_BOARD_SIZE, "棋盘大小");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_SMALL, "小 (40px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_MEDIUM, "中 (60px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_LARGE, "大 (80px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_XLARGE, "超大 (100px)");
        strings.put(LanguageKeys.MENU_BOARD_SIZE_CUSTOM, "自定义:");
        strings.put(LanguageKeys.MENU_BOARD_SHOW_COORDS, "显示坐标");
        strings.put(LanguageKeys.MENU_BOARD_FLIP, "翻转棋盘");
        strings.put(LanguageKeys.MENU_BOARD_THEME, "棋盘主题");
        strings.put(LanguageKeys.MENU_BOARD_THEME_WOOD, "木质");
        strings.put(LanguageKeys.MENU_BOARD_THEME_CLASSIC, "经典");
        strings.put(LanguageKeys.MENU_BOARD_THEME_GREEN, "绿色");
        strings.put(LanguageKeys.MENU_BOARD_THEME_BLUE, "蓝色");

        // ========== ASSISTANT MENU ==========
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE, "引擎");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_STOCKFISH, "Stockfish");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_LC0, "Leela Chess Zero");
        strings.put(LanguageKeys.MENU_ASSISTANT_ENGINE_CUSTOM, "自定义...");
        strings.put(LanguageKeys.MENU_ASSISTANT_ANALYZE, "分析局面");
        strings.put(LanguageKeys.MENU_ASSISTANT_BEST_MOVE, "显示最佳着法");
        strings.put(LanguageKeys.MENU_ASSISTANT_SHOW_EVAL, "显示评估");
        strings.put(LanguageKeys.MENU_ASSISTANT_CONFIGURE_ENGINE, "配置引擎...");

        // ========== ENGINE SETUP DIALOG ==========
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_TITLE, "国际象棋引擎设置");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_HEADER, "选择UCI兼容的国际象棋引擎");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_INFO, "支持: Stockfish, Leela Chess Zero, Komodo等");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_PATH_LABEL, "引擎路径:");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_PATH_PROMPT, "选择引擎文件...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_BROWSE, "浏览...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_TEST, "测试引擎");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_OK, "确定");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_CANCEL, "取消");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_CHOOSER_TITLE, "选择引擎可执行文件");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_FILTER_EXECUTABLE, "可执行文件");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_FILE_FILTER_ALL, "所有文件");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_SELECT_FILE, "❌ 请先选择引擎文件");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_FILE_NOT_EXISTS, "❌ 文件不存在");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_CHECKING, "⏳ 正在检查引擎...");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_READY, "✅ 引擎已就绪!");
        strings.put(LanguageKeys.ENGINE_SETUP_DIALOG_STATUS_FAILED, "❌ 无法启动引擎。请确保它是UCI兼容的引擎");

        // ========== DATABASE MENU ==========
        strings.put(LanguageKeys.MENU_DATABASE_CONNECT, "连接数据库");
        strings.put(LanguageKeys.MENU_DATABASE_OPEN, "打开本地数据库...");
        strings.put(LanguageKeys.MENU_DATABASE_SEARCH, "搜索数据库");
        strings.put(LanguageKeys.MENU_DATABASE_IMPORT_PGN, "导入PGN到数据库");
        strings.put(LanguageKeys.MENU_DATABASE_STATS, "开局统计");
        strings.put(LanguageKeys.MENU_DATABASE_OPEN_LAST, "打开最近的数据库");
        strings.put(LanguageKeys.MENU_DATABASE_IMPORT, "导入PGN");
        strings.put(LanguageKeys.MENU_DATABASE_INFO, "数据库信息");

        // ========== DATABASE MESSAGES ==========
        strings.put(LanguageKeys.DB_NOT_INITIALIZED, "数据库未初始化");
        strings.put(LanguageKeys.DB_CONNECT_SUCCESS, "数据库已连接");
        strings.put(LanguageKeys.DB_CONNECT_ERROR, "数据库连接错误");
        strings.put(LanguageKeys.DB_OPEN_TITLE, "打开数据库");
        strings.put(LanguageKeys.DB_OPEN_SUCCESS, "数据库已打开");
        strings.put(LanguageKeys.DB_OPEN_ERROR, "数据库打开错误");
        strings.put(LanguageKeys.DB_OPEN_INVALID, "所选文件不是目录");
        strings.put(LanguageKeys.DB_SEARCH_TITLE, "搜索数据库");
        strings.put(LanguageKeys.DB_SEARCH_HEADER, "输入搜索条件");
        strings.put(LanguageKeys.DB_SEARCH_BUTTON, "搜索");
        strings.put(LanguageKeys.DB_SEARCH_WHITE, "白方 (或部分名字)");
        strings.put(LanguageKeys.DB_SEARCH_BLACK, "黑方 (或部分名字)");
        strings.put(LanguageKeys.DB_SEARCH_RESULT, "结果 (1-0, 0-1, 1/2-1/2)");
        strings.put(LanguageKeys.DB_SEARCH_ECO, "ECO代码");
        strings.put(LanguageKeys.DB_SEARCH_OPENING, "开局名称");
        strings.put(LanguageKeys.DB_SEARCH_ERROR, "搜索错误");
        strings.put(LanguageKeys.DB_NO_RESULTS, "未找到结果");
        strings.put(LanguageKeys.DB_NO_GAMES, "数据库中没有对局");
        strings.put(LanguageKeys.DB_RESULTS_TITLE, "搜索结果");
        strings.put(LanguageKeys.DB_RESULTS_HEADER, "找到对局: %d");
        strings.put(LanguageKeys.DB_LOAD_ERROR, "加载对局错误");
        strings.put(LanguageKeys.DB_LOAD_SUCCESS, "对局已加载");
        strings.put(LanguageKeys.DB_LOAD_BUTTON, "加载");
        strings.put(LanguageKeys.DB_STATS_TITLE, "开局统计");
        strings.put(LanguageKeys.DB_STATS_ERROR, "获取统计信息错误");
        strings.put(LanguageKeys.DB_STATS_ECO, "ECO统计");
        strings.put(LanguageKeys.DB_STATS_OPENING, "开局统计");
        strings.put(LanguageKeys.DB_INFO_TITLE, "数据库信息");
        strings.put(LanguageKeys.DB_INFO_ERROR, "获取数据库信息错误");
        strings.put(LanguageKeys.DB_INFO_PATH, "路径");
        strings.put(LanguageKeys.DB_INFO_GAMES, "对局数");
        strings.put(LanguageKeys.DB_INFO_TYPE, "存储类型");
        strings.put(LanguageKeys.DB_INFO_VERSION, "版本");
        strings.put(LanguageKeys.DB_LOAD_PGN_FIRST, "将PGN文件加载到程序中");
        strings.put(LanguageKeys.DB_INDEX_NOT_LOADED , "索引未加载");
        strings.put(LanguageKeys.DB_STATS_TOTAL  , "总游戏数");
        strings.put(LanguageKeys.DB_INFO_FILENAME  , "文件名");
        strings.put(LanguageKeys.DB_INFO_ACTIVE_GAMES   , "活跃游戏数");
        strings.put(LanguageKeys.DB_INFO_DELETED_GAMES   , "已删除游戏数");
        strings.put(LanguageKeys.DB_INFO_FILE_SIZE   , "文件大小");
        strings.put(LanguageKeys.DB_INFO_INDEX_VERSION   , "索引版本");
        strings.put(LanguageKeys.DB_INFO_GROWTH_RATIO    , "增长比率");

        // ========== PGN MESSAGES ==========
        strings.put(LanguageKeys.PGN_IMPORT_TITLE, "导入PGN");
        strings.put(LanguageKeys.PGN_IMPORT_PROGRESS, "正在导入PGN...");
        strings.put(LanguageKeys.PGN_IMPORT_SUCCESS, "从文件导入 %d 局: %s");
        strings.put(LanguageKeys.PGN_IMPORT_ERROR, "PGN导入错误");
        strings.put(LanguageKeys.PGN_IMPORT_NO_GAMES, "文件中未找到对局");
        strings.put(LanguageKeys.PGN_IMPORT_CLIPBOARD_SUCCESS, "从剪贴板成功导入对局");
        strings.put(LanguageKeys.PGN_CLIPBOARD_EMPTY, "剪贴板为空");
        strings.put(LanguageKeys.PGN_LOAD_SUCCESS, "从 %s 加载 %d 局");
        strings.put(LanguageKeys.PGN_LOAD_EMPTY, "文件中没有对局");
        strings.put(LanguageKeys.PGN_LOAD_ERROR, "PGN加载错误");
        strings.put(LanguageKeys.PGN_SAVE_SUCCESS, "对局已保存");
        strings.put(LanguageKeys.PGN_SAVE_EMPTY, "没有着法可保存");
        strings.put(LanguageKeys.PGN_SAVE_ERROR, "PGN保存错误");

        // ========== FILE MENU EXTRA ==========
        strings.put(LanguageKeys.MENU_FILE_EXPORT_CURRENT, "导出当前对局");
        strings.put(LanguageKeys.MENU_FILE_IMPORT_CLIPBOARD, "从剪贴板导入");

        // ========== HELP MENU ==========
        strings.put(LanguageKeys.MENU_HELP_SHORTCUTS, "快捷键");
        strings.put(LanguageKeys.MENU_HELP_ABOUT, "关于");

        // ========== EDIT MENU ==========
        strings.put(LanguageKeys.MENU_EDIT, "编辑");
        strings.put(LanguageKeys.MENU_EDIT_UNDO, "撤销");
        strings.put(LanguageKeys.MENU_EDIT_REDO, "重做");
        strings.put(LanguageKeys.MENU_EDIT_PREFERENCES, "偏好设置");
        strings.put(LanguageKeys.PREFERENCES_TITLE, "偏好设置");

        // ========== VIEW MENU ==========
        strings.put(LanguageKeys.MENU_VIEW, "视图");
        strings.put(LanguageKeys.MENU_VIEW_FLIP_BOARD, "翻转棋盘");
        strings.put(LanguageKeys.MENU_VIEW_COORDINATES, "显示坐标");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM, "缩放");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_IN, "放大");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_OUT, "缩小");
        strings.put(LanguageKeys.MENU_VIEW_ZOOM_RESET, "重置缩放");

        // ========== ENGINE MENU ==========
        strings.put(LanguageKeys.MENU_ENGINE, "引擎");
        strings.put(LanguageKeys.MENU_ENGINE_CONFIGURE, "配置引擎");
        strings.put(LanguageKeys.MENU_ENGINE_ANALYZE, "显示最佳着法");

        // ========== VARIATION DIALOGS ==========
        strings.put(LanguageKeys.DIALOG_VARIATION_TITLE, "选择变着");
        strings.put(LanguageKeys.DIALOG_VARIATION_CHOICE, "选择续着 (→选择, ←取消):");
        strings.put(LanguageKeys.DIALOG_VARIATION_SELECT, "选择 (→)");
        strings.put(LanguageKeys.DIALOG_VARIATION_CANCEL, "取消 (←)");
        strings.put(LanguageKeys.DIALOG_VARIATION_NEW, "✦ 新变着 (%s): %s");
        strings.put(LanguageKeys.DIALOG_VARIATION_REPLACE_MAIN, "✗ 替换主线 (第%d%s步 %s)");
        strings.put(LanguageKeys.DIALOG_VARIATION_MAIN_LINE, "▶ 主线 (%s): %s");
        strings.put(LanguageKeys.DIALOG_VARIATION_EXISTING, "替换变着 (第%d%s步 %s : %s)");
        strings.put(LanguageKeys.DIALOG_VARIATION_MAKE_MAIN, "★ 设为主线 (%s): %s");

        // ========== NOTATION ==========
        strings.put(LanguageKeys.NOTATION_TITLE, "对局记录");
        strings.put(LanguageKeys.NOTATION_NEW_GAME, "新对局");
        strings.put(LanguageKeys.NOTATION_COPY_PGN, "复制PGN");
        strings.put(LanguageKeys.NOTATION_PGN_COPIED, "PGN已复制到剪贴板");
        strings.put(LanguageKeys.NOTATION_COPY_PGN_UNICODE, "复制PGN (Unicode)");
        strings.put(LanguageKeys.NOTATION_PGN_UNICODE_COPIED, "PGN (Unicode)已复制到剪贴板");
        strings.put(LanguageKeys.NOTATION_TOGGLE_SHOW, "显示对局记录");
        strings.put(LanguageKeys.NOTATION_TOGGLE_HIDE, "隐藏对局记录");
        strings.put(LanguageKeys.NOTATION_NO_MOVES, "没有着法");
        strings.put(LanguageKeys.NOTATION_NO_DATA, "没有数据");

        // ========== GAME MESSAGES ==========
        strings.put(LanguageKeys.GAME_CHECKMATE, "将杀! %s获胜");
        strings.put(LanguageKeys.GAME_STALEMATE, "逼和! 平局");
        strings.put(LanguageKeys.GAME_INSUFFICIENT_MATERIAL, "子力不足. 平局");
        strings.put(LanguageKeys.GAME_WHITE, "白方");
        strings.put(LanguageKeys.GAME_BLACK, "黑方");
        strings.put(LanguageKeys.GAME_WIN, "%s获胜");

        // ========== PROMOTION ==========
        strings.put(LanguageKeys.PROMOTION_TITLE, "升变");
        strings.put(LanguageKeys.PROMOTION_CHOOSE, "选择升变棋子:");
        strings.put(LanguageKeys.PROMOTION_IMAGE_LOAD_ERROR, "加载图片失败");

        // ========== POSITION SETUP ==========
        strings.put(LanguageKeys.SETUP_TITLE, "摆子");
        strings.put(LanguageKeys.SETUP_INSTRUCTION,
                """
                        左键: 放置棋子   右键: 切换颜色
                        删除模式: 点击删除
                        移动模式: 点击棋子，然后点击目标格
                        移动模式下右键 - 取消""");
        strings.put(LanguageKeys.SETUP_SELECT_PIECE, "选择棋子:");
        strings.put(LanguageKeys.SETUP_WHITE, "白方:");
        strings.put(LanguageKeys.SETUP_BLACK, "黑方:");
        strings.put(LanguageKeys.SETUP_DELETE_MODE, "删除模式");
        strings.put(LanguageKeys.SETUP_MOVE_MODE, "移动模式");
        strings.put(LanguageKeys.SETUP_CLEAR_SELECTION, "清除选择");
        strings.put(LanguageKeys.SETUP_SIDE_TO_MOVE, "走棋方:");
        strings.put(LanguageKeys.SETUP_FEN, "FEN:");
        strings.put(LanguageKeys.SETUP_COPY_FEN, "复制");
        strings.put(LanguageKeys.SETUP_APPLY, "应用");
        strings.put(LanguageKeys.SETUP_START_POS, "初始局面");
        strings.put(LanguageKeys.SETUP_CLEAR_ALL, "清空所有");
        strings.put(LanguageKeys.SETUP_CANCEL, "取消");
        strings.put(LanguageKeys.SETUP_FEN_INVALID, "FEN格式无效!");
        strings.put(LanguageKeys.SETUP_FEN_COPIED, "FEN已复制到剪贴板");
        strings.put(LanguageKeys.SETUP_IMAGE_LOAD_ERROR, "加载图片失败");
        strings.put(LanguageKeys.SETUP_DELETE_INSTRUCTION, "删除模式: 点击棋子删除");
        strings.put(LanguageKeys.SETUP_DELETE_MODE_TOOLTIP, "删除模式已激活 - 点击棋子移除");
        strings.put(LanguageKeys.SETUP_CASTLING_RIGHTS, "王车易位权利:");
        strings.put(LanguageKeys.SETUP_CASTLING_WHITE_KING, "0-0 (短易位)");
        strings.put(LanguageKeys.SETUP_CASTLING_WHITE_QUEEN, "0-0-0 (长易位)");
        strings.put(LanguageKeys.SETUP_CASTLING_BLACK_KING, "0-0 (短易位)");
        strings.put(LanguageKeys.SETUP_CASTLING_BLACK_QUEEN, "0-0-0 (长易位)");
        strings.put(LanguageKeys.SETUP_RESET_CASTLING, "重置易位权利");
        strings.put(LanguageKeys.SETUP_FEN_PROMPT, "输入FEN...");
        strings.put(LanguageKeys.SETUP_CONTROL, "控制:");
        strings.put(LanguageKeys.SETUP_KING_IN_CHECK, "无法设置%s走棋 - 王被将军!");

        // ========== NAVIGATION ==========
        strings.put(LanguageKeys.NAV_FIRST, "⏮");
        strings.put(LanguageKeys.NAV_PREV, "◀");
        strings.put(LanguageKeys.NAV_NEXT, "▶");
        strings.put(LanguageKeys.NAV_LAST, "⏭");
        strings.put(LanguageKeys.NAV_TOOLTIP_FIRST, "第一步 (↑)");
        strings.put(LanguageKeys.NAV_TOOLTIP_PREV, "上一步 (←)");
        strings.put(LanguageKeys.NAV_TOOLTIP_NEXT, "下一步 (→)");
        strings.put(LanguageKeys.NAV_TOOLTIP_LAST, "最后一步 (↓)");

        // ========== NOTIFICATIONS ==========
        strings.put(LanguageKeys.NOTIFICATION_INFO, "信息");
        strings.put(LanguageKeys.NOTIFICATION_ERROR, "错误");
        strings.put(LanguageKeys.NOTIFICATION_DATABASE_CONNECT, "数据库连接将很快实现");
        strings.put(LanguageKeys.NOTIFICATION_SEARCH, "数据库搜索将很快实现");
        strings.put(LanguageKeys.NOTIFICATION_IMPORT, "PGN导入到数据库将很快实现");
        strings.put(LanguageKeys.NOTIFICATION_ANALYSIS, "局面分析将很快实现");
        strings.put(LanguageKeys.NOTIFICATION_WARNING, "警告");

        // ========== MOVE ANNOTATIONS ==========
        strings.put(LanguageKeys.ANNOTATION_BRILLIANT_MOVE, "妙手");
        strings.put(LanguageKeys.ANNOTATION_GOOD_MOVE, "好棋");
        strings.put(LanguageKeys.ANNOTATION_INTERESTING_MOVE, "有趣的着法");
        strings.put(LanguageKeys.ANNOTATION_DUBIOUS_MOVE, "疑问着");
        strings.put(LanguageKeys.ANNOTATION_BAD_MOVE, "错误");
        strings.put(LanguageKeys.ANNOTATION_BLUNDER, "大漏");

        strings.put(LanguageKeys.ANNOTATION_CLEAR_ADVANTAGE_WHITE, "白方有明显优势");
        strings.put(LanguageKeys.ANNOTATION_WINNING_WHITE, "白方胜势");
        strings.put(LanguageKeys.ANNOTATION_SLIGHT_ADVANTAGE_WHITE, "白方稍优");
        strings.put(LanguageKeys.ANNOTATION_EQUALITY, "均势");
        strings.put(LanguageKeys.ANNOTATION_SLIGHT_ADVANTAGE_BLACK, "黑方稍优");
        strings.put(LanguageKeys.ANNOTATION_CLEAR_ADVANTAGE_BLACK, "黑方有明显优势");
        strings.put(LanguageKeys.ANNOTATION_WINNING_BLACK, "黑方胜势");
        strings.put(LanguageKeys.ANNOTATION_UNCLEAR_POSITION, "局面不明");
        strings.put(LanguageKeys.ANNOTATION_WITH_COMPENSATION, "有补偿");

        strings.put(LanguageKeys.ANNOTATION_ONLY_MOVE, "唯一着法");
        strings.put(LanguageKeys.ANNOTATION_THEORETICAL_NOVELTY, "理论新着");
        strings.put(LanguageKeys.ANNOTATION_ONLY_AND_BEST_MOVE, "唯一且最佳着法");
        strings.put(LanguageKeys.ANNOTATION_WITH_IDEA, "有思路/威胁...");
        strings.put(LanguageKeys.ANNOTATION_WITH_INITIATIVE, "有主动权");
        strings.put(LanguageKeys.ANNOTATION_WITH_COUNTERPLAY, "有反击");
        strings.put(LanguageKeys.ANNOTATION_DEVELOPMENT_ADVANTAGE, "出子优势");
        strings.put(LanguageKeys.ANNOTATION_BETTER_WAS, "更好的走法");
        strings.put(LanguageKeys.ANNOTATION_MATE, "将杀");
        strings.put(LanguageKeys.ANNOTATION_CHECK, "将军");
        strings.put(LanguageKeys.ANNOTATION_DOUBLE_CHECK, "双将");

        strings.put(LanguageKeys.ANNOTATION_TWO_BISHOPS, "双象");
        strings.put(LanguageKeys.ANNOTATION_BISHOP_PAIR_WHITE_BLACK, "异色格象");
        strings.put(LanguageKeys.ANNOTATION_CENTER_CONTROL, "中心控制");

        // ========== ANNOTATION DIALOG ==========
        strings.put(LanguageKeys.ANNOTATION_DIALOG_TITLE, "着法注释");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_SELECT, "选择着法注释:");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_COMMENT, "注释:");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_COMMENT_PROMPT, "输入着法注释...");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_CLEAR, "清除");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_OK, "确定");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_CANCEL, "取消");
        strings.put(LanguageKeys.ANNOTATION_DIALOG_MOVE, "着法:");
        strings.put(LanguageKeys.ANNOTATION_TAB_MOVE_EVAL, "着法评估");
        strings.put(LanguageKeys.ANNOTATION_TAB_POSITION_EVAL, "局面评估");
        strings.put(LanguageKeys.ANNOTATION_TAB_COMMENTARY, "评论");

        // ========== CONTEXT MENU ==========
        strings.put(LanguageKeys.CONTEXT_MENU_MAKE_MAIN, "📌 将此变着设为主线");
        strings.put(LanguageKeys.CONTEXT_MENU_ADD_ANNOTATION, "🏷️ 添加注释/评论");
        strings.put(LanguageKeys.CONTEXT_MENU_REMOVE_ANNOTATION, "🗑️ 删除注释/评论");
        strings.put(LanguageKeys.CONTEXT_MENU_EDIT_COMMENT, "✏️ 编辑评论");
        strings.put(LanguageKeys.CONTEXT_MENU_DELETE_VARIATION, "🗑️ 删除变着");
        strings.put(LanguageKeys.CONTEXT_MENU_DELETE_AFTER, "✂️ 删除此后的所有着法");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_WHITE_WIN, "🏆 1-0 (白胜)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_BLACK_WIN, "🏆 0-1 (黑胜)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_DRAW, "🏆 1/2-1/2 (平局)");
        strings.put(LanguageKeys.CONTEXT_MENU_RESULT_UNKNOWN, "🏆 * (结果未知)");
        strings.put(LanguageKeys.CONTEXT_MENU_CANNOT_DELETE_MAIN, "不能删除主线!");

        // ========== VARIATION ==========
        strings.put(LanguageKeys.MAIN_LINE, "主线");
        strings.put(LanguageKeys.ROOT, "根");
        strings.put(LanguageKeys.VARIATION_DEFAULT_NAME, "变着");

        // ========== SHORTCUTS ==========
        strings.put(LanguageKeys.SHORTCUTS_CONTENT,
                """
                        快捷键:
                        Ctrl+N - 新对局
                        Ctrl+O - 打开PGN
                        Ctrl+S - 保存PGN
                        Ctrl+F - 翻转棋盘
                        Ctrl+D - 搜索数据库
                        F1 - 帮助
                        Ctrl+Q - 退出""");

        // ========== ENGINE MESSAGES ==========
        strings.put(LanguageKeys.ENGINE_SEND_POSITION_ERROR, "发送局面失败");
        strings.put(LanguageKeys.ENGINE_TIMEOUT_ERROR, "引擎在限定时间内未响应");
        strings.put(LanguageKeys.ENGINE_ANALYSIS_START_ERROR, "启动分析失败");
        strings.put(LanguageKeys.ENGINE_STOP_ERROR, "停止分析失败");
        strings.put(LanguageKeys.ENGINE_INVALID_UCI_MOVE, "无效的UCI着法");
        strings.put(LanguageKeys.ENGINE_CONVERT_UCI_ERROR, "转换UCI着法失败");
        strings.put(LanguageKeys.ENGINE_CONVERT_NOTATION_ERROR, "转换为棋谱记法错误");
        strings.put(LanguageKeys.ENGINE_IMAGE_LOAD_ERROR, "加载图片失败");
        strings.put(LanguageKeys.ENGINE_ANALYSIS_NOT_ACTIVE, "分析未激活。按Enter启动分析");
        strings.put(LanguageKeys.ENGINE_TERMINAL_POSITION, "终局局面，无法走棋");
        strings.put(LanguageKeys.ENGINE_NOT_ANALYZED, "引擎尚未分析此局面");
        strings.put(LanguageKeys.ENGINE_ILLEGAL_MOVE, "引擎建议非法着法");
        strings.put(LanguageKeys.ENGINE_MOVE_ERROR, "执行着法错误");

        // ========== ANALYSIS PANEL ==========
        strings.put(LanguageKeys.ANALYSIS_TITLE, "引擎分析");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_STOPPED, "已停止");
        strings.put(LanguageKeys.ANALYSIS_ANALYZING, "分析中");
        strings.put(LanguageKeys.ANALYSIS_CURRENT_EVAL, "当前评估");
        strings.put(LanguageKeys.ANALYSIS_DEPTH, "深度");
        strings.put(LanguageKeys.ANALYSIS_ADD_LINE_TOOLTIP, "添加分析线 (最大 %d)");
        strings.put(LanguageKeys.ANALYSIS_REMOVE_LINE_TOOLTIP, "移除分析线 (最小 %d)");
        strings.put(LanguageKeys.ANALYSIS_TOGGLE_TOOLTIP, "启动/停止分析 (Enter)");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_TITLE, "引擎未运行");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_HEADER, "国际象棋引擎未运行");
        strings.put(LanguageKeys.ANALYSIS_ENGINE_NOT_RUNNING_CONTENT, "请通过 助手 → 配置引擎 设置引擎");

        // ========== CONFIRM DIALOGS ==========
        strings.put(LanguageKeys.CONFIRM_DELETE_TITLE, "确认删除");
        strings.put(LanguageKeys.CONFIRM_DELETE_AFTER_HEADER, "删除%s之后的所有着法?");
        strings.put(LanguageKeys.CONFIRM_DELETE_CONTENT, "此操作不可撤销!");
        strings.put(LanguageKeys.CONFIRM_DELETE_YES, "是的，删除");
        strings.put(LanguageKeys.CONFIRM_DELETE_NO, "取消");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_TITLE, "确认删除");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_HEADER, "删除变着 \"%s\"?");
        strings.put(LanguageKeys.CONFIRM_DELETE_VARIATION_CONTENT, "此操作不可撤销!\n所有着法和子变着将被删除。");

        // ========== TIMER ==========
        strings.put(LanguageKeys.TIMER_SELECT_TIME, "选择时间");
        strings.put(LanguageKeys.TIMER_PRESET_1_MIN, "1分钟");
        strings.put(LanguageKeys.TIMER_PRESET_2_MIN, "2分钟");
        strings.put(LanguageKeys.TIMER_PRESET_3_MIN, "3分钟");
        strings.put(LanguageKeys.TIMER_PRESET_5_MIN, "5分钟");
        strings.put(LanguageKeys.TIMER_PRESET_10_MIN, "10分钟");
        strings.put(LanguageKeys.TIMER_CUSTOM_TOOLTIP, "自定义设置 (秒)");
        strings.put(LanguageKeys.TIMER_CUSTOM_TITLE, "时间设置");
        strings.put(LanguageKeys.TIMER_CUSTOM_HEADER, "输入秒数");
        strings.put(LanguageKeys.TIMER_CUSTOM_CONTENT, "秒:");
        strings.put(LanguageKeys.TIMER_MAX_TIME_WARNING, "最大时间为60分钟 (3600秒)");
        strings.put(LanguageKeys.TIMER_INVALID_NUMBER, "请输入有效的数字");

        // ========== MAIN CONTROLLER ==========
        strings.put(LanguageKeys.NEW_GAME_TITLE, "新对局");
        strings.put(LanguageKeys.NEW_GAME_HEADER, "对局结束");
        strings.put(LanguageKeys.NEW_GAME_CONTENT, "开始新对局?");

        strings.put(LanguageKeys.SAVE_GAME_TITLE, "保存对局");
        strings.put(LanguageKeys.SAVE_GAME_HEADER, "对局未保存");
        strings.put(LanguageKeys.SAVE_GAME_CONTENT, "在开始新对局前保存当前对局?");
        strings.put(LanguageKeys.SAVE_GAME_SAVE, "保存");
        strings.put(LanguageKeys.SAVE_GAME_DONT_SAVE, "不保存");
        strings.put(LanguageKeys.SAVE_GAME_CANCEL, "取消");

        strings.put(LanguageKeys.SAVE_GAME_DIALOG_TITLE, "保存对局");
        strings.put(LanguageKeys.SAVE_GAME_DIALOG_HEADER, "输入对局信息");
        strings.put(LanguageKeys.SAVE_GAME_DIALOG_SAVE, "保存");

        strings.put(LanguageKeys.SAVE_GAME_PROMPT_WHITE, "白方");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_BLACK, "黑方");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_WHITE_ELO, "白方等级分");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_BLACK_ELO, "黑方等级分");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_EVENT, "赛事");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_SITE, "地点");
        strings.put(LanguageKeys.SAVE_GAME_PROMPT_ROUND, "轮次");

        strings.put(LanguageKeys.SAVE_GAME_LABEL_WHITE, "白方");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_BLACK, "黑方");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_RATING, "等级分");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_EVENT, "赛事");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_SITE, "地点");
        strings.put(LanguageKeys.SAVE_GAME_LABEL_ROUND, "轮次");

        strings.put(LanguageKeys.SAVE_GAME_SUCCESS, "对局已保存 (稍后添加到数据库)");

        strings.put(LanguageKeys.FEATURE_NOT_IMPLEMENTED, "此功能将在未来实现");
        strings.put(LanguageKeys.PGN_LOAD_MESSAGE, "加载PGN");
        strings.put(LanguageKeys.PGN_SAVE_MESSAGE, "保存PGN");
        strings.put(LanguageKeys.DB_OPEN_MESSAGE, "打开数据库");

        // ========== MAIN CONTROLLER MESSAGES ==========
        strings.put(LanguageKeys.MAIN_LOAD_GAME_EMPTY_TREE, "加载对局失败: 树为空");
        strings.put(LanguageKeys.MAIN_LOADED_POSITION, "局面已加载");
        strings.put(LanguageKeys.MAIN_PGN_EMPTY, "PGN为空");
        strings.put(LanguageKeys.MAIN_CLOSE_BUTTON, "关闭");
        strings.put(LanguageKeys.MAIN_NO_CHANGES, "未检测到更改，无需保存");
        strings.put(LanguageKeys.MAIN_GAME_SAVED_FILE, "对局已保存到文件: %s");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_TITLE, "保存对局");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_HEADER, "选择保存位置");
        strings.put(LanguageKeys.MAIN_SAVE_GAME_CHOICE_CONTENT, "保存到哪里?");
        strings.put(LanguageKeys.MAIN_SAVE_TO_PGN_FILE, "💾 保存到PGN文件");
        strings.put(LanguageKeys.MAIN_SAVE_TO_DATABASE, "📁 保存到数据库");
        strings.put(LanguageKeys.MAIN_SAVE_CANCEL, "取消");
        strings.put(LanguageKeys.MAIN_SAVE_PGN_FILE_TITLE, "保存对局到PGN文件");
        strings.put(LanguageKeys.MAIN_DB_NOT_INITIALIZED_MSG, "数据库未初始化");
        strings.put(LanguageKeys.MAIN_GAME_SAVED_TO_DB, "对局已保存到数据库");
        strings.put(LanguageKeys.MAIN_OPEN_PGN_ERROR_MSG, "打开PGN错误");
        strings.put(LanguageKeys.MAIN_BROWSER_LIMIT_MSG, "浏览器限制");
        strings.put(LanguageKeys.MAIN_OPEN_ERROR_MSG, "打开错误");
        strings.put(LanguageKeys.MAIN_INDEXING_TITLE_MSG, "索引PGN文件");
        strings.put(LanguageKeys.MAIN_INDEXING_COMPLETE, "索引完成!\n找到对局: %d\n活跃: %d");
        strings.put(LanguageKeys.MAIN_INDEXING_ERROR_MSG, "索引错误");
        strings.put(LanguageKeys.MAIN_POSITION_SOLVE_HINT, " (按→解题)");
        strings.put(LanguageKeys.MAIN_REFRESHED_MSG, "列表已刷新");
        strings.put(LanguageKeys.MAIN_NO_ACTIVE_BROWSER_MSG, "没有活动的浏览器");
        strings.put(LanguageKeys.MAIN_UNKNOWN_PATH, "未知");
        strings.put(LanguageKeys.MAIN_POSITION_LOADED, "已加载局面: %s%s");
        strings.put(LanguageKeys.MAIN_GAME_LOADED, "已加载对局: ");

        // ========== ABOUT ==========
        strings.put(LanguageKeys.ABOUT_TITLE, "关于 Kletka");
        strings.put(LanguageKeys.ABOUT_CONTENT,
                """
                    ♔ Kletka Chess ♔
                    版本: 1.0
                    平台: Java 17, OpenJFX
                    库: chesslib 1.3.6 (GPL v3)
                    
                    跨平台国际象棋分析器
                    支持SQLite数据库
                    
                    © 2026 赫里帕奇·安德烈
                    
                    许可证: GNU通用公共许可证 v3.0
                    https://www.gnu.org/licenses/gpl-3.0.html
                    """);

        // ========== POSITION SET ==========
        strings.put(LanguageKeys.POSITION_SET_SUCCESS, "局面已设置。%s走棋");

        // ========== ENGINE SETUP ==========
        strings.put(LanguageKeys.ENGINE_SETUP_TITLE, "引擎设置");
        strings.put(LanguageKeys.ENGINE_SETUP_HEADER, "国际象棋引擎未配置");
        strings.put(LanguageKeys.ENGINE_SETUP_CONTENT,
                """
                        要使用分析功能，您需要配置UCI兼容的引擎。
                        
                        现在要选择引擎吗?""");
        strings.put(LanguageKeys.ENGINE_SETUP_BUTTON, "设置");
        strings.put(LanguageKeys.ENGINE_SETUP_LATER, "稍后");
        strings.put(LanguageKeys.ENGINE_SETUP_SUCCESS, "引擎配置成功!");
        strings.put(LanguageKeys.ENGINE_SETUP_ERROR_TITLE, "错误");
        strings.put(LanguageKeys.ENGINE_SETUP_ERROR_CONTENT, "启动引擎失败");

        strings.put(LanguageKeys.ENGINE_SWITCH_TITLE, "切换引擎");
        strings.put(LanguageKeys.ENGINE_SWITCH_HEADER, "当前引擎将被停止");
        strings.put(LanguageKeys.ENGINE_SWITCH_CONTENT, "继续?");
        strings.put(LanguageKeys.ENGINE_SWITCH_SUCCESS, "引擎切换成功!");

        strings.put(LanguageKeys.ENGINE_NOT_RUNNING,
                "引擎未运行。请检查engines/文件夹中是否存在引擎文件");

        strings.put(LanguageKeys.ANALYSIS_BEST_MOVE, "最佳着法: %s (%s)");
        strings.put(LanguageKeys.ANALYSIS_ERROR_TITLE, "错误");
        strings.put(LanguageKeys.ANALYSIS_ERROR_CONTENT, "解析着法失败");

        strings.put(LanguageKeys.CONFIRM_YES, "是");
        strings.put(LanguageKeys.CONFIRM_NO, "否");

        // ========== LOGGING ==========
        strings.put(LanguageKeys.LOG_INITIALIZED, "日志已初始化");
        strings.put(LanguageKeys.LOG_STARTING_GUI, "启动KletkaGui");
        strings.put(LanguageKeys.LOG_GUI_LOADED, "GUI加载成功");
        strings.put(LanguageKeys.LOG_GUI_ERROR, "启动GUI错误");
        strings.put(LanguageKeys.LOG_SHUTTING_DOWN, "Kletka正在关闭...");

        // ========== SPLASH SCREEN ==========
        strings.put(LanguageKeys.SPLASH_LOADING_ENGINE, "加载引擎...");
        strings.put(LanguageKeys.SPLASH_INITIALIZING_BOARD, "初始化棋盘...");
        strings.put(LanguageKeys.SPLASH_LOADING_GUI, "加载界面...");
        strings.put(LanguageKeys.SPLASH_READY, "就绪!");

        // ========== GAME TYPE POSITION ==========
        strings.put(LanguageKeys.GAME_TYPE_POSITION, "局面");
        strings.put(LanguageKeys.GAME_TYPE_STUDY, "习题");
        strings.put(LanguageKeys.GAME_TYPE_PROBLEM, "题目");
        strings.put(LanguageKeys.GAME_TYPE_GAME, "对局");
        strings.put(LanguageKeys.DEFAULT_PLAYER_NAME, "棋手");

        // ========== PGN KEYWORD ==========
        strings.put(LanguageKeys.PGN_KEYWORD_MATE, "将杀");
        strings.put(LanguageKeys.PGN_KEYWORD_STUDY, "习题");

        // ========== REPO ERROR ==========
        strings.put(LanguageKeys.REPO_ERROR_CREATE_DIR, "创建目录失败: %s");
        strings.put(LanguageKeys.REPO_ERROR_GAME_NULL, "对局不能为null");
        strings.put(LanguageKeys.REPO_ERROR_SAVE_GAME, "保存对局失败: %s");
        strings.put(LanguageKeys.REPO_ERROR_READ_DIR, "读取目录失败: %s");
        strings.put(LanguageKeys.REPO_ERROR_DELETE_GAME, "删除对局失败: %s");
        strings.put(LanguageKeys.REPO_ERROR_DELETE_ALL, "删除对局失败");
        strings.put(LanguageKeys.REPO_ERROR_COUNT, "统计对局失败");
        strings.put(LanguageKeys.REPO_ERROR_READ_FILE, "读取文件失败: %s");
        strings.put(LanguageKeys.REPO_ERROR_PARSE_PGN, "PGN解析错误: %s");
        strings.put(LanguageKeys.REPO_ERROR_GAMES_EMPTY, "对局列表为空");
        strings.put(LanguageKeys.REPO_ERROR_EXPORT, "导出对局到 %s 失败");

        // ========== EXPORT ERROR ==========
        strings.put(LanguageKeys.EXPORT_ERROR_GAMES_EMPTY, "对局列表为空");
        strings.put(LanguageKeys.EXPORT_ERROR_FILE_NULL, "文件不能为null");
        strings.put(LanguageKeys.EXPORT_ERROR_EXPORT_FAILED, "导出对局到 %s 失败");
        strings.put(LanguageKeys.EXPORT_ERROR_NO_GAMES, "没有对局可导出");
        strings.put(LanguageKeys.EXPORT_ERROR_CREATE_DIR, "创建目录失败: %s");
        strings.put(LanguageKeys.EXPORT_ERROR_GAME_NULL, "对局不能为null");

        // ========== IMPORT ERROR ==========
        strings.put(LanguageKeys.IMPORT_ERROR_FILE_NOT_FOUND, "文件未找到: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_READ_FILE, "读取文件失败: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_PARSE_PGN, "文件中PGN解析错误: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_PGN_EMPTY, "PGN内容为空");
        strings.put(LanguageKeys.IMPORT_ERROR_PARSE_GENERAL, "PGN解析错误");
        strings.put(LanguageKeys.IMPORT_ERROR_DIR_NOT_FOUND, "目录未找到: %s");
        strings.put(LanguageKeys.IMPORT_ERROR_DIR_NOT_FOUND_SIMPLE, "目录未找到");
        strings.put(LanguageKeys.IMPORT_ERROR_READ_DIR, "读取目录失败: %s");

        // ========== PGN SERVICE ERRORS ==========
        strings.put(LanguageKeys.PGN_SERVICE_ERROR_GAME_NULL, "对局不能为null");
        strings.put(LanguageKeys.PGN_SERVICE_ERROR_GAMES_EMPTY, "对局列表为空");

        // ========== DONATE ==========
        strings.put(LanguageKeys.DONATE_TITLE, "☕ 支持项目");
        strings.put(LanguageKeys.DONATE_HEADER, "☕ 支持Kletka项目");
        strings.put(LanguageKeys.DONATE_DESCRIPTION, "感谢您使用Kletka!\n您的支持有助于项目发展。");
        strings.put(LanguageKeys.DONATE_HINT, "💡 点击「复制」将地址复制到剪贴板。");
        strings.put(LanguageKeys.DONATE_CLOSE, "关闭");
        strings.put(LanguageKeys.DONATE_PAYPAL, "📧 PayPal");
        strings.put(LanguageKeys.DONATE_COPY, "📋 复制");
        strings.put(LanguageKeys.DONATE_OPEN, "🌐 打开");
        strings.put(LanguageKeys.DONATE_BITCOIN, "₿ Bitcoin");
        strings.put(LanguageKeys.DONATE_QR, "📱 二维码");
        strings.put(LanguageKeys.DONATE_QR_TITLE, "Bitcoin二维码");
        strings.put(LanguageKeys.DONATE_QR_HINT, "在您的Bitcoin钱包中扫描二维码");
        strings.put(LanguageKeys.DONATE_TOAST_COPIED_EMAIL, "邮箱已复制!");
        strings.put(LanguageKeys.DONATE_TOAST_COPIED_BITCOIN, "Bitcoin地址已复制!");
        strings.put(LanguageKeys.DONATE_TOAST_OPEN_BROWSER, "打开浏览器失败");
        strings.put(LanguageKeys.DONATE_TOAST_QR_ERROR, "生成二维码错误");

        // ========== POSITION SETUP DIALOG ==========
        strings.put(LanguageKeys.SETUP_DELETE_MODE_ACTIVE_TOOLTIP, "请先关闭删除模式");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_NOTIFICATION, "走棋方自动更改为%s (%s的王被将军)");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_WHITE, "白方");
        strings.put(LanguageKeys.SETUP_SIDE_CHANGED_BLACK, "黑方");
        strings.put(LanguageKeys.SETUP_LOAD_POSITION_ERROR, "加载局面错误: %s");

        // ========== SAVE GAME DIALOG ==========
        strings.put(LanguageKeys.SAVE_DIALOG_TITLE_EDIT, "✏️ 编辑对局");
        strings.put(LanguageKeys.SAVE_DIALOG_TITLE_SAVE, "💾 保存对局");
        strings.put(LanguageKeys.SAVE_DIALOG_HEADER_EDIT, "编辑对局信息");
        strings.put(LanguageKeys.SAVE_DIALOG_HEADER_SAVE, "输入对局信息");

        strings.put(LanguageKeys.SAVE_TAB_PLAYERS, "棋手与结果");
        strings.put(LanguageKeys.SAVE_TAB_TOURNAMENT, "赛事");
        strings.put(LanguageKeys.SAVE_TAB_DETAILS, "详情");

        strings.put(LanguageKeys.SAVE_LABEL_WHITE, "白方:");
        strings.put(LanguageKeys.SAVE_LABEL_BLACK, "黑方:");
        strings.put(LanguageKeys.SAVE_LABEL_ELO_WHITE, "白方等级分:");
        strings.put(LanguageKeys.SAVE_LABEL_ELO_BLACK, "黑方等级分:");
        strings.put(LanguageKeys.SAVE_LABEL_WHITE_TEAM, "白方队伍:");
        strings.put(LanguageKeys.SAVE_LABEL_BLACK_TEAM, "黑方队伍:");
        strings.put(LanguageKeys.SAVE_LABEL_ANNOTATOR, "评论员:");
        strings.put(LanguageKeys.SAVE_LABEL_RESULT, "结果:");
        strings.put(LanguageKeys.SAVE_RESULT_1_0, "1-0");
        strings.put(LanguageKeys.SAVE_RESULT_0_1, "0-1");
        strings.put(LanguageKeys.SAVE_RESULT_DRAW, "1/2-1/2");
        strings.put(LanguageKeys.SAVE_RESULT_UNKNOWN, "*");

        strings.put(LanguageKeys.SAVE_LABEL_EVENT, "赛事:");
        strings.put(LanguageKeys.SAVE_LABEL_SITE, "地点:");
        strings.put(LanguageKeys.SAVE_LABEL_ROUND, "轮次:");
        strings.put(LanguageKeys.SAVE_LABEL_SUBROUND, "小轮次:");
        strings.put(LanguageKeys.SAVE_LABEL_DATE, "日期:");
        strings.put(LanguageKeys.SAVE_LABEL_YEAR, "年:");
        strings.put(LanguageKeys.SAVE_LABEL_MONTH, "月:");
        strings.put(LanguageKeys.SAVE_LABEL_DAY, "日:");
        strings.put(LanguageKeys.SAVE_BUTTON_RESET_DATE, "重置");

        strings.put(LanguageKeys.SAVE_LABEL_ECO, "ECO代码:");
        strings.put(LanguageKeys.SAVE_LABEL_OPENING, "开局:");
        strings.put(LanguageKeys.SAVE_LABEL_VARIATION, "变着:");
        strings.put(LanguageKeys.SAVE_LABEL_TIME_CONTROL, "时间控制:");
        strings.put(LanguageKeys.SAVE_LABEL_SOURCE, "来源:");
        strings.put(LanguageKeys.SAVE_LABEL_FEN, "FEN:");
        strings.put(LanguageKeys.SAVE_CHECKBOX_SETUP, "SetUp (局面)");
        strings.put(LanguageKeys.SAVE_LABEL_TYPE, "类型:");
        strings.put(LanguageKeys.SAVE_BUTTON_DETECT_OPENING, "🎯 检测开局");

        strings.put(LanguageKeys.SAVE_BUTTON_SAVE, "保存");
        strings.put(LanguageKeys.SAVE_BUTTON_SAVE_CHANGES, "保存更改");
        strings.put(LanguageKeys.SAVE_BUTTON_CANCEL, "取消");
        strings.put(LanguageKeys.SAVE_BUTTON_HELP, "帮助");

        strings.put(LanguageKeys.SAVE_MSG_ECO_NOT_LOADED, "开局数据库尚未加载。请稍后再试。");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_FOUND, "找到开局: %s - %s");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_NOT_FOUND, "未找到开局");
        strings.put(LanguageKeys.SAVE_MSG_OPENING_ERROR, "检测开局错误: %s");

        strings.put(LanguageKeys.SAVE_HELP_TITLE, "帮助");
        strings.put(LanguageKeys.SAVE_HELP_HEADER, "保存对局到PGN");
        strings.put(LanguageKeys.SAVE_HELP_CONTENT,
                """
                        填写对局信息:
                        
                        • 棋手 - 白方和黑方的姓名
                        • 等级分 - 每位棋手的ELO等级分
                        • 结果 - 对局结果
                        • 赛事 - 名称、地点、轮次
                        • 日期 - 年、月、日
                        • ECO - 百科全书中的开局代码
                        • 开局 - 开局名称
                        • 变着 - 开局变着
                        
                        局面设置:
                        • FEN - 局面FEN格式
                        • SetUp - 标记为局面
                        • 类型 - 对局、局面、习题、题目
                        
                        字段中的 "?" 将替换为默认值。""");

        strings.put(LanguageKeys.SAVE_TYPE_GAME, "对局");
        strings.put(LanguageKeys.SAVE_TYPE_POSITION, "局面");
        strings.put(LanguageKeys.SAVE_TYPE_STUDY, "习题");
        strings.put(LanguageKeys.SAVE_TYPE_PROBLEM, "题目");

        // ========== LOGO ==========
        strings.put(LanguageKeys.LOGO_TITLE, "KLETKA");
        strings.put(LanguageKeys.LOGO_SUBTITLE_LINE1, "国际");
        strings.put(LanguageKeys.LOGO_SUBTITLE_LINE2, "象棋分析器");

        // ========== MENU ==========
        strings.put(LanguageKeys.MENU_WINDOWS, "窗口");
        strings.put(LanguageKeys.MENU_WINDOWS_CLIPBOARD_EMPTY, "📋 剪贴板: 空");
        strings.put(LanguageKeys.MENU_WINDOWS_CLIPBOARD_CONTENT, "📋 剪贴板: %d 局来自 '%s'");
        strings.put(LanguageKeys.MENU_WINDOWS_CLEAR_CLIPBOARD, "🧹 清空剪贴板");
        strings.put(LanguageKeys.MENU_WINDOWS_CLOSE_ALL, "✕ 关闭所有");
        strings.put(LanguageKeys.MENU_WINDOWS_NO_FILES, "(没有打开的文件)");
        strings.put(LanguageKeys.MENU_WINDOWS_BROWSER_ITEM, "📁 %s (%d 局)");

        strings.put(LanguageKeys.MENU_FILE_OPEN_BROWSER, "打开PGN浏览器");
        strings.put(LanguageKeys.MENU_FILE_REFRESH_BROWSER, "刷新浏览器");

        strings.put(LanguageKeys.MENU_VIEW_TOGGLE_NOTATION, "显示/隐藏记录");

        strings.put(LanguageKeys.MENU_HELP_DONATE, "☕ 支持项目");

        strings.put(LanguageKeys.BROWSER_GAMES_COUNT, "局");
        strings.put(LanguageKeys.BROWSER_CLIPBOARD_COUNT, "局");

        // ========== PGN BROWSER MANAGER ==========
        strings.put(LanguageKeys.PGN_BROWSER_LIMIT_REACHED, "达到浏览器限制 (%d)。请关闭一个文件。");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_LIMIT, "一次不能复制超过%d局");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_UNAVAILABLE, "粘贴不可用");
        strings.put(LanguageKeys.PGN_BROWSER_NO_INDEX, "目标文件没有索引: %s");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_ERROR, "❌ 磁盘空间不足!");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_CHECK, "需要: ~%.1f MB\n可用: %.1f MB\n\n请释放磁盘空间后重试。");
        strings.put(LanguageKeys.PGN_BROWSER_DISK_SPACE_INSUFFICIENT, "❌ 磁盘空间不足!\n\n粘贴在%d局后中断。\n文件将在下次打开时自动恢复。\n\n请释放磁盘空间后重试。");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_INTERRUPTED, "❌ 粘贴对局时出错\n\n已添加%d局。\n文件索引将在下次打开时更新。\n\n原因: %s");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_ERROR, "粘贴对局错误: %s");

        strings.put(LanguageKeys.PGN_BROWSER_COPY_PREPARING, "准备复制...");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_TOTAL, "总计: %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_COMPLETE, "✅ 已复制 %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_COPY_SOURCE, "来源: %s");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_PREPARING, "准备粘贴...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_TOTAL, "总计: %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_PROGRESS, "粘贴: %d / %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_ADDED, "已添加 %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_COMPLETE, "✅ 已粘贴 %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_PASTE_TARGET, "目标文件: %s");

        // ========== INDEXING PROGRESS ==========
        strings.put(LanguageKeys.INDEXING_PROGRESS_MESSAGE, "已处理 %d / %d 局 (%.1f%%)");
        strings.put(LanguageKeys.INDEXING_STATUS_STARTING, "开始索引...");

        // ========== PGN GAME OPERATION ==========
        strings.put(LanguageKeys.PGN_OP_EDIT_SUCCESS, "对局 #%d 已编辑");
        strings.put(LanguageKeys.PGN_OP_DELETE_SUCCESS, "对局 #%d 已删除");
        strings.put(LanguageKeys.PGN_OP_ADD_SUCCESS, "新对局 #%d 已添加");
        strings.put(LanguageKeys.PGN_OP_DUPLICATE_SUCCESS, "对局 #%d 已复制为 #%d");

        // ========== DELETE CONFIRM ==========
        strings.put(LanguageKeys.DELETE_CONFIRM_TITLE, "确认删除");
        strings.put(LanguageKeys.DELETE_CONFIRM_SINGLE_TITLE, "🗑️ 删除对局");
        strings.put(LanguageKeys.DELETE_CONFIRM_MULTIPLE_TITLE, "🗑️ 删除 %d 局");
        strings.put(LanguageKeys.DELETE_CONFIRM_SINGLE_MESSAGE, "确定要删除对局 #%d?\n\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_MULTIPLE_MESSAGE, "确定要删除 %d 局?\n\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_WHITE, "白方: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_BLACK, "黑方: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_RESULT, "结果: %s");
        strings.put(LanguageKeys.DELETE_CONFIRM_AND_MORE, "  ... 还有 %d 局\n");
        strings.put(LanguageKeys.DELETE_CONFIRM_WARNING, "\n⚠️ 此操作在下次重新打包前不可撤销。");
        strings.put(LanguageKeys.DELETE_CONFIRM_DELETE_BUTTON, "🗑️ 删除");
        strings.put(LanguageKeys.DELETE_CONFIRM_CANCEL_BUTTON, "取消");
        strings.put(LanguageKeys.DELETE_CONFIRM_UNKNOWN, "?");
        strings.put(LanguageKeys.DELETE_CONFIRM_GAME_PREFIX, "  #%d: %s vs %s (%s)\n");

        // ========== INDEXING DIALOG ==========
        strings.put(LanguageKeys.INDEXING_DIALOG_TITLE, "索引PGN文件");
        strings.put(LanguageKeys.INDEXING_DIALOG_STATUS_PREPARING, "准备索引...");
        strings.put(LanguageKeys.INDEXING_DIALOG_GAMES_PROCESSED, "已处理 %d 局");
        strings.put(LanguageKeys.INDEXING_DIALOG_COMPLETE, "完成!");
        strings.put(LanguageKeys.INDEXING_DIALOG_CANCEL, "取消");
        strings.put(LanguageKeys.INDEXING_DIALOG_ERROR, "❌ %s");
        strings.put(LanguageKeys.INDEXING_DIALOG_PROGRESS_FORMAT, "%d / %d (%.1f%%)");

        // ========== PGN BROWSER - TABLE COLUMNS ==========
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_ID, "#");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_WHITE, "白方");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_BLACK, "黑方");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_RESULT, "结果");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_YEAR, "年份");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_EVENT, "赛事");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_ECO, "ECO");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_OPENING, "开局");
        strings.put(LanguageKeys.PGN_BROWSER_COLUMN_BODY, "对局");

        // ========== PGN BROWSER - WINDOW ==========
        strings.put(LanguageKeys.PGN_BROWSER_TITLE, "PGN浏览器 - %s");
        strings.put(LanguageKeys.PGN_BROWSER_TITLE_ACTIVE, " ✅ 活跃");
        strings.put(LanguageKeys.PGN_BROWSER_TITLE_GAMES, " (%d 局)");

        // ========== PGN BROWSER - SEARCH ==========
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_LABEL, "🔍 搜索:");
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_PROMPT, "输入棋手名、开局或ECO...");
        strings.put(LanguageKeys.PGN_BROWSER_SEARCH_CLEAR, "清除");

        // ========== PGN BROWSER - STATUS ==========
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING, "加载中...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_TOTAL, "总计: %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_SELECTED, "已选: %d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_SHOWN, "显示: %d / %d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_CLOSE, "✕ 关闭");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_READY, "就绪");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ERROR, "错误: %s");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_INDEX, "加载索引...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_PARSING, "解析PGN文件...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_MORE, "加载对局...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ALL_LOADED, "所有对局已加载");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADED, "已加载 %d / %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_CHECKING_INDEX, "检查索引...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_LOADING_GAME, "加载对局...");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_READY_WITH_COUNT, "就绪 (%d 局)");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ALL_LOADED_WITH_COUNT, "所有对局已加载 (%d)");

        // ========== PGN BROWSER - BUTTONS ==========
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_EDIT, "✏️ 编辑");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DELETE, "🗑️ 删除");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DELETE_COUNT, "🗑️ 删除 (%d)");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_DUPLICATE, "📋 复制");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_COPY, "📋 复制");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_PASTE, "📋 粘贴");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK, "🔄 重新打包");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK_COUNT, "🔄 重新打包 (%d)");
        strings.put(LanguageKeys.PGN_BROWSER_BUTTON_REPACK_IN_PROGRESS, "⏳ 重新打包中...");

        // ========== PGN BROWSER - CONTEXT MENU ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_LOAD, "加载对局");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_COPY, "📋 复制");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_DELETE, "🗑️ 删除");
        strings.put(LanguageKeys.PGN_BROWSER_CONTEXT_SELECT_ALL, "全选 (Ctrl+A)");

        // ========== PGN BROWSER - MESSAGES ==========
        strings.put(LanguageKeys.PGN_BROWSER_MSG_REPACK_IN_PROGRESS, "⏳ 正在重新打包，请稍候...");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_ONE, "请选择一局进行编辑");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_UNAVAILABLE, "编辑暂时不可用");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_GAMES, "选择要删除的对局");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_UNAVAILABLE, "此模式下删除不可用");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_LIMIT, "一次不能复制超过1000局。已选: %d");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_UNAVAILABLE, "此模式下复制不可用");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_UNAVAILABLE, "复制不可用");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_UNAVAILABLE, "粘贴不可用。剪贴板为空或目标文件与源文件相同。");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_CLIPBOARD_EMPTY, "剪贴板为空");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_NO_DELETED_GAMES, "没有已删除的对局需要重新打包");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_SUCCESS, "从 '%s' 复制了 %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_SUCCESS, "粘贴了 %d 局到 '%s'");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_SUCCESS, "已删除 %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_SUCCESS, "对局已复制为 #%d");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_ERROR_LOADING, "加载错误: %s");
        strings.put(LanguageKeys.PGN_BROWSER_STATUS_OPERATION_FINISHED, "操作完成");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DELETE_ERROR, "删除错误: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_ONE_DUPLICATE, "请选择一局进行复制");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_DUPLICATE_ERROR, "复制错误: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_SELECT_GAMES_COPY, "选择要复制的对局");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_ERROR, "复制错误: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY, "📋 复制对局");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_COPY_START, "开始复制...");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_ERROR, "粘贴错误: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_GAMES, "📋 粘贴对局");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_PASTE_START, "开始粘贴...");

        // ========== PGN BROWSER - PROGRESS ==========
        strings.put(LanguageKeys.PGN_BROWSER_DELETING, "删除 %d 局...");
        strings.put(LanguageKeys.PGN_BROWSER_DELETING_PROCEED, "删除中: %d / %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_DELETED, "已删除 %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_DUPLICATING, "复制对局中...");
        strings.put(LanguageKeys.PGN_BROWSER_COPYING, "复制 %d 局...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTING, "粘贴 %d 局...");
        strings.put(LanguageKeys.PGN_BROWSER_PASTED, "已粘贴 %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_REPACKING, "重新打包中...");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_COMPLETE, "✅ 重新打包成功完成!\n活跃对局: %d");
        strings.put(LanguageKeys.PGN_BROWSER_START_DELETING, "开始删除...");

        // ========== PGN BROWSER - CONFIRM PASTE ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_TITLE, "粘贴对局");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_HEADER, "粘贴 %d 局?");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_SOURCE, "来源: %s");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_TARGET, "目标文件: %s");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_COUNT, "对局: %d");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_FREE_SPACE, "可用磁盘空间: %s MB");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_INFO, "对局将添加到文件末尾。");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_YES, "✅ 粘贴");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_PASTE_NO, "取消");

        // ========== PGN BROWSER - CONFIRM REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_TITLE, "重新打包PGN文件");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_HEADER, "执行手动重新打包?");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_CONTENT, "发现 %d 局已删除。\n活跃对局: %d\n大小比例: %.1fx\n\n将创建不含已删除对局的新文件。\n⚠️ 重新打包期间将锁定编辑。");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_YES, "✅ 重新打包");
        strings.put(LanguageKeys.PGN_BROWSER_CONFIRM_REPACK_NO, "取消");

        // ========== PGN BROWSER - AUTO REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_TITLE, "⚠️ 需要重新打包");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_HEADER, "PGN文件已增长 %.1f 倍!");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_CONTENT, "文件包含 %d 个活跃和 %d 个已删除对局。\n\n建议进行重新打包以优化。");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_YES, "✅ 是的，重新打包");
        strings.put(LanguageKeys.PGN_BROWSER_AUTO_REPACK_NO, "稍后");

        // ========== PGN BROWSER - FILTER ==========
        strings.put(LanguageKeys.PGN_BROWSER_FILTER_TOTAL, "总计: %d 局");
        strings.put(LanguageKeys.PGN_BROWSER_FILTER_FOUND, "找到: %d / %d 局");

        // ========== PGN BROWSER - REPACK ==========
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_TITLE, "🔄 重新打包PGN文件");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_IN_PROGRESS, "正在重新打包，请稍候...");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_ERROR, "❌ 重新打包错误: %s");
        strings.put(LanguageKeys.PGN_BROWSER_REPACK_SUCCESS, "✅ 重新打包成功完成!\n活跃对局: %d");

        // ========== REPACK PROGRESS DIALOG ==========
        strings.put(LanguageKeys.REPACK_DIALOG_TITLE, "🔄 重新打包PGN文件");
        strings.put(LanguageKeys.REPACK_DIALOG_STATUS_PREPARING, "准备重新打包...");
        strings.put(LanguageKeys.REPACK_DIALOG_PROGRESS_FORMAT, "%d / %d (%.1f%%)");
        strings.put(LanguageKeys.REPACK_DIALOG_GAMES_PROCESSED, "已处理 %d 局");
        strings.put(LanguageKeys.REPACK_DIALOG_COMPLETE, "✅ 完成!");
        strings.put(LanguageKeys.REPACK_DIALOG_ERROR, "❌ %s");

        // ========== REPACK STATUS WIDGET ==========
        strings.put(LanguageKeys.REPACK_STATUS_OPTIMAL, "最佳");
        strings.put(LanguageKeys.REPACK_STATUS_NO_DELETED, "✅ 无已删除");
        strings.put(LanguageKeys.REPACK_STATUS_HAS_DELETED, "✅ 有已删除");
        strings.put(LanguageKeys.REPACK_STATUS_WARNING, "⚠️ 建议重新打包");
        strings.put(LanguageKeys.REPACK_STATUS_CRITICAL, "🔴 需要重新打包!");
        strings.put(LanguageKeys.REPACK_STATUS_UNKNOWN, "未知");
        strings.put(LanguageKeys.REPACK_STATUS_REPACKING, "🔄 重新打包中...");
        strings.put(LanguageKeys.REPACK_STATUS_DELETED_COUNT, "🗑️ %d");
        strings.put(LanguageKeys.REPACK_STATUS_TOOLTIP, "文件大小与活跃数据比例: %.1fx\n已删除对局: %d\n活跃对局: %d");
        strings.put(LanguageKeys.REPACK_STATUS_REPACKING_TOOLTIP, "正在重新打包");
        strings.put(LanguageKeys.REPACK_STATUS_RATIO, "%.1fx");
        strings.put(LanguageKeys.REPACK_STATUS_LOADING, "...");

        // ========== INDEXING ==========
        strings.put(LanguageKeys.INDEXING_STATUS_SCANNING_FILE, "索引中: 扫描文件...");
        strings.put(LanguageKeys.INDEXING_STATUS_CREATING_INDEX, "索引中: 创建索引...");
        strings.put(LanguageKeys.INDEXING_STATUS_PREPARING_FILE, "准备文件: 添加 [Deleted] 标签");
        strings.put(LanguageKeys.INDEXING_STATUS_SCANNING_GAMES, "索引中: 扫描 %d 局...");
        strings.put(LanguageKeys.INDEXING_STATUS_PROCESSED, "索引中: 已处理 %d / %d 局");
        strings.put(LanguageKeys.INDEXING_STATUS_PREPARING_ADD_DELETED, "准备文件: 添加 [Deleted]");
        strings.put(LanguageKeys.INDEXING_STATUS_COMPLETE, "索引完成: %d 局");

        // ========== FILE PREPARATION ==========
        strings.put(LanguageKeys.PREPARE_STATUS_SCANNING, "准备文件: 扫描...");
        strings.put(LanguageKeys.PREPARE_STATUS_PROCESSED, "准备文件: 已处理 %d / %d 局");
        strings.put(LanguageKeys.PREPARE_STATUS_BUILDING, "准备文件: 构建...");
        strings.put(LanguageKeys.PREPARE_STATUS_BUILDING_BLOCKS, "准备文件: 构建 %d / %d 块");
        strings.put(LanguageKeys.PREPARE_STATUS_SAVING, "准备文件: 保存...");
        strings.put(LanguageKeys.PREPARE_STATUS_COMPLETE, "准备完成: %d 局, %d 个垃圾块");

        // ========== INDEXING FACADE ==========
        strings.put(LanguageKeys.INDEXING_STEP1, "步骤 1/3: 准备文件...");
        strings.put(LanguageKeys.INDEXING_STEP2, "步骤 2/3: 创建索引...");
        strings.put(LanguageKeys.INDEXING_STEP3, "步骤 3/3: 保存索引...");
        strings.put(LanguageKeys.INDEXING_COMPLETE_SUCCESS, "索引成功完成!");

        // ========== REPACK ==========
        strings.put(LanguageKeys.REPACK_STATUS_READING, "重新打包: 读取对局...");
        strings.put(LanguageKeys.REPACK_STATUS_PROCESSED, "重新打包: 已处理 %d / %d 局");
        strings.put(LanguageKeys.REPACK_STATUS_WRITING, "重新打包: 写入新文件...");
        strings.put(LanguageKeys.REPACK_STATUS_CREATING_INDEX, "重新打包: 创建索引...");
        strings.put(LanguageKeys.REPACK_STATUS_SAVING_INDEX, "重新打包: 保存索引...");
        strings.put(LanguageKeys.REPACK_STATUS_REPLACING, "重新打包: 替换文件...");
        strings.put(LanguageKeys.REPACK_STATUS_COMPLETE, "✅ 重新打包完成! %d 局, 大小: %.2f KB");

        // ========== REPACK STATUS DESCRIPTIONS ==========
        strings.put(LanguageKeys.REPACK_DESC_NO_GAMES, "无对局");
        strings.put(LanguageKeys.REPACK_DESC_NO_DELETED, "✅ 无已删除对局");
        strings.put(LanguageKeys.REPACK_DESC_HAS_DELETED, "✅ 有已删除 (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_WARNING, "⚠️ 建议 (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_CRITICAL, "🔴 需要! (%.1fx)");
        strings.put(LanguageKeys.REPACK_DESC_DELETED_COUNT, ", %d 已删除");

        // ========== LANGUAGE MENU ==========
        strings.put(LanguageKeys.MENU_LANGUAGE, "语言");
        strings.put(LanguageKeys.MENU_LANGUAGE_RUSSIAN, "俄语");
        strings.put(LanguageKeys.MENU_LANGUAGE_ENGLISH, "英语");
        strings.put(LanguageKeys.MENU_LANGUAGE_CHINESE, "中文");
        strings.put(LanguageKeys.MENU_LANGUAGE_CHANGE, "切换语言");

        // ========== LANGUAGE CHANGE DIALOG ==========
        strings.put(LanguageKeys.LANG_CHANGE_TITLE, "语言已保存");
        strings.put(LanguageKeys.LANG_CHANGE_HEADER, "语言已更改为: %s");
        strings.put(LanguageKeys.LANG_CHANGE_CONTENT, "请重新启动应用程序以使更改生效。");
        strings.put(LanguageKeys.LANG_CHANGE_BUTTON_OK, "确定");

        // ========== PGN BROWSER - EDIT HEADERS ==========
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_HEADERS_TITLE, "✏️ 编辑标题");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_HEADERS_HEADER, "编辑棋局标题 #%s");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVE, "💾 保存标题");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_CANCEL, "❌ 取消");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_LOADING, "⏳ 正在加载棋局进行编辑...");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVING, "⏳ 正在保存更改...");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_SAVED, "✅ 更改已保存");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_BODY_INFO, "📄 棋局主体 (%d 步) - 只读");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_BODY_EMPTY, "📄 棋局主体为空");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_WHITE, "白方棋手姓名");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_BLACK, "黑方棋手姓名");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_EVENT, "赛事名称");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_SITE, "比赛地点");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_ROUND, "轮次");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_OPENING, "开局名称");
        strings.put(LanguageKeys.PGN_BROWSER_EDIT_PROMPT_VARIATION, "开局变例");

        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_SUCCESS, "✅ 棋局标题已成功更新");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_ERROR, "❌ 编辑时出错: %s");
        strings.put(LanguageKeys.PGN_BROWSER_MSG_EDIT_READ_ONLY, "只读");

        // ========== SHORTCUTS ==========
        strings.put(LanguageKeys.SHORTCUTS_TITLE, "快捷键");

        StringBuilder shortcutsContent = createShortcutsContent();

        strings.put(LanguageKeys.SHORTCUTS_CONTENT, shortcutsContent.toString());

        // Отдельные ключи
        strings.put(LanguageKeys.SHORTCUT_FILE, "📁 文件");
        strings.put(LanguageKeys.SHORTCUT_VIEW, "👁️ 视图");
        strings.put(LanguageKeys.SHORTCUT_NAVIGATION, "🧭 导航");
        strings.put(LanguageKeys.SHORTCUT_ENGINE, "⚙️ 引擎");
        strings.put(LanguageKeys.SHORTCUT_PGN, "📚 PGN/浏览器");
        strings.put(LanguageKeys.SHORTCUT_DATABASE, "🗄️ 数据库");
        strings.put(LanguageKeys.SHORTCUT_WINDOWS, "🪟 窗口");

        strings.put(LanguageKeys.SHORTCUT_NEW_GAME, "Ctrl+N - 新对局");
        strings.put(LanguageKeys.SHORTCUT_OPEN_PGN, "Ctrl+O - 打开PGN");
        strings.put(LanguageKeys.SHORTCUT_SAVE_PGN, "Ctrl+S - 保存PGN");
        strings.put(LanguageKeys.SHORTCUT_EXPORT_CURRENT, "Ctrl+E - 导出当前对局");
        strings.put(LanguageKeys.SHORTCUT_IMPORT_CLIPBOARD, "Ctrl+Shift+V - 从剪贴板导入");
        strings.put(LanguageKeys.SHORTCUT_SETUP_POSITION, "Ctrl+P - 设置局面");
        strings.put(LanguageKeys.SHORTCUT_EXIT, "Alt+F4 - 退出");

        strings.put(LanguageKeys.SHORTCUT_FLIP_BOARD, "Ctrl+F - 翻转棋盘");
        strings.put(LanguageKeys.SHORTCUT_COORDINATES, "Ctrl+Shift+C - 显示坐标");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_IN, "Ctrl+= - 放大");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_OUT, "Ctrl+- - 缩小");
        strings.put(LanguageKeys.SHORTCUT_ZOOM_RESET, "Ctrl+0 - 重置缩放");
        strings.put(LanguageKeys.SHORTCUT_TOGGLE_NOTATION, "H - 切换记谱");

        strings.put(LanguageKeys.SHORTCUT_NAV_PREV, "← - 上一步");
        strings.put(LanguageKeys.SHORTCUT_NAV_NEXT, "→ - 下一步");
        strings.put(LanguageKeys.SHORTCUT_NAV_FIRST, "↑ - 第一步");
        strings.put(LanguageKeys.SHORTCUT_NAV_LAST, "↓ - 最后一步");

        strings.put(LanguageKeys.SHORTCUT_ENGINE_MOVE, "Space - 引擎走棋");
        strings.put(LanguageKeys.SHORTCUT_ENGINE_ANALYZE, "Shift+Enter - 切换分析");
        strings.put(LanguageKeys.SHORTCUT_ENGINE_CONFIGURE, "Ctrl+Shift+E - 配置引擎");

        strings.put(LanguageKeys.SHORTCUT_OPEN_BROWSER, "Ctrl+B - 打开PGN浏览器");
        strings.put(LanguageKeys.SHORTCUT_REFRESH_BROWSER, "Ctrl+R - 刷新浏览器");
        strings.put(LanguageKeys.SHORTCUT_NEXT_GAME, "F11 - 下一局");
        strings.put(LanguageKeys.SHORTCUT_PREV_GAME, "Ctrl+F11 - 上一局");
        strings.put(LanguageKeys.SHORTCUT_NEXT_BROWSER, "Ctrl+Tab - 下一个浏览器");
        strings.put(LanguageKeys.SHORTCUT_PREV_BROWSER, "Ctrl+Shift+Tab - 上一个浏览器");
        strings.put(LanguageKeys.SHORTCUT_CLOSE_BROWSER, "Ctrl+W - 关闭浏览器");

        strings.put(LanguageKeys.SHORTCUT_CONNECT_DB, "Ctrl+D - 连接数据库");
        strings.put(LanguageKeys.SHORTCUT_IMPORT_DB, "Ctrl+I - 导入数据库");
        strings.put(LanguageKeys.SHORTCUT_SEARCH_DB, "Ctrl+Shift+F - 搜索数据库");

        strings.put(LanguageKeys.SHORTCUT_CLOSE_ALL_BROWSERS, "Ctrl+Shift+W - 关闭所有浏览器");
        strings.put(LanguageKeys.SHORTCUT_MINIMIZE_BROWSER, "最小化浏览器");
        strings.put(LanguageKeys.SHORTCUT_MAXIMIZE_BROWSER, "最大化浏览器");

        strings.put(LanguageKeys.FAILED_TOKENIZE_PGN, "无法对 PGN 进行分词");
        strings.put(LanguageKeys.ERROR_PARSING_MOVE, "解析走法时出错：%s");
        strings.put(LanguageKeys.ERROR_PARSING_PGN_TO_TREE, "将 PGN 解析为变体树时出错：%s");
    }

    private static StringBuilder createShortcutsContent() {
        StringBuilder shortcutsContent = new StringBuilder();
        shortcutsContent.append("═══════════════════════════════════════\n");
        shortcutsContent.append("📁 文件:\n");
        shortcutsContent.append("  Ctrl+N - 新对局\n");
        shortcutsContent.append("  Ctrl+O - 打开PGN\n");
        shortcutsContent.append("  Ctrl+S - 保存PGN\n");
        shortcutsContent.append("  Ctrl+E - 导出当前对局\n");
        shortcutsContent.append("  Ctrl+Shift+V - 从剪贴板导入\n");
        shortcutsContent.append("  Ctrl+P - 设置局面\n");
        shortcutsContent.append("  Alt+F4 - 退出\n");

        shortcutsContent.append("\n👁️ 视图:\n");
        shortcutsContent.append("  Ctrl+F - 翻转棋盘\n");
        shortcutsContent.append("  Ctrl+= - 放大\n");
        shortcutsContent.append("  Ctrl+- - 缩小\n");
        shortcutsContent.append("  Ctrl+0 - 重置缩放\n");
        shortcutsContent.append("  H - 切换记谱\n");

        shortcutsContent.append("\n🧭 导航:\n");
        shortcutsContent.append("  ← - 上一步\n");
        shortcutsContent.append("  → - 下一步\n");
        shortcutsContent.append("  ↑ - 第一步\n");
        shortcutsContent.append("  ↓ - 最后一步\n");

        shortcutsContent.append("\n⚙️ 引擎:\n");
        shortcutsContent.append("  Space - 引擎走棋\n");
        shortcutsContent.append("  Shift+Enter - 切换分析\n");
        shortcutsContent.append("  Ctrl+Shift+E - 配置引擎\n");

        shortcutsContent.append("\n📚 PGN/浏览器:\n");
        shortcutsContent.append("  Ctrl+B - 打开PGN浏览器\n");
        shortcutsContent.append("  Ctrl+R - 刷新浏览器\n");
        shortcutsContent.append("  F11 - 下一局\n");
        shortcutsContent.append("  Ctrl+F11 - 上一局\n");
        shortcutsContent.append("  Ctrl+Tab - 下一个浏览器\n");
        shortcutsContent.append("  Ctrl+Shift+Tab - 上一个浏览器\n");
        shortcutsContent.append("  Ctrl+W - 关闭浏览器\n");

        shortcutsContent.append("\n🗄️ 数据库:\n");
        shortcutsContent.append("  Ctrl+D - 连接数据库\n");
        shortcutsContent.append("  Ctrl+I - 导入数据库\n");
        shortcutsContent.append("  Ctrl+Shift+F - 搜索数据库\n");

        shortcutsContent.append("\n🪟 窗口:\n");
        shortcutsContent.append("  Ctrl+Shift+W - 关闭所有浏览器\n");

        shortcutsContent.append("\n═══════════════════════════════════════");
        return shortcutsContent;
    }

    @Override
    public String getCode() {
        return "zh";
    }

    @Override
    public String getDisplayName() {
        return "中文";
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