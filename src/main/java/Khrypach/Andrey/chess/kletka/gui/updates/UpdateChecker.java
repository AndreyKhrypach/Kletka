/*
 *
 *  * Copyright (c) 2025 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.gui.updates;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Проверка обновлений Kletka через GitHub Releases API.
 * Один запрос — либо при запуске (авто), либо по кнопке (ручная).
 */
public final class UpdateChecker {
    private static final Logger log = LoggerFactory.getLogger(UpdateChecker.class);

    private static final String API_URL =
            "https://api.github.com/repos/AndreyKhrypach/Kletka/releases/latest";
    private static final String RELEASES_URL =
            "https://github.com/AndreyKhrypach/Kletka/releases/latest";
    private static final int TIMEOUT_MS = 5000;

    private UpdateChecker() {}

    public enum Status {
        UP_TO_DATE,       // новой версии нет
        UPDATE_AVAILABLE, // есть новая версия
        ERROR             // ошибка (нет интернета, timeout, парсинг и т.д.)
    }

    public record Result(Status status, String version, String errorMessage) {
        public boolean hasUpdate() { return status == Status.UPDATE_AVAILABLE; }
        public boolean isError()   { return status == Status.ERROR; }
        public boolean isUpToDate(){ return status == Status.UP_TO_DATE; }
    }

    /** URL страницы релизов (для открытия по клику на иконку). */
    public static String releasesUrl() {
        return RELEASES_URL;
    }

    /**
     * Автопроверка при запуске.
     * Тихо: при ошибке — только лог, пользователь ничего не видит.
     */
    public static void checkAsync(Consumer<Result> onResult) {
        runAsync(onResult);
    }

    /**
     * Ручная проверка из меню.
     * Отличается от авто только семантикой на стороне вызывающего
     * (UI покажет ошибку/диалоги), а логика та же.
     */
    public static void checkManualAsync(Consumer<Result> onResult) {
        runAsync(onResult);
    }

    private static void runAsync(Consumer<Result> onResult) {
        new Thread(() -> {
            Result result = checkSync();
            if (onResult != null) {
                Platform.runLater(() -> onResult.accept(result));
            }
        }, "update-checker").start();
    }

    private static Result checkSync() {
        String currentVersion = readCurrentVersion();
        if (currentVersion == null) {
            log.debug("Update check: cannot read current version");
            return new Result(Status.ERROR, null, "Cannot read current version");
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Kletka-UpdateChecker");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            int code = connection.getResponseCode();
            if (code != 200) {
                log.debug("Update check: HTTP {}", code);
                return new Result(Status.ERROR, null, "HTTP " + code);
            }

            String json = readBody(connection);
            if (json == null || json.isEmpty()) {
                return new Result(Status.ERROR, null, "Empty response");
            }

            String latestVersion = extractTagName(json);
            if (latestVersion == null) {
                return new Result(Status.ERROR, null, "Cannot parse response");
            }
            if (latestVersion.startsWith("v")) {
                latestVersion = latestVersion.substring(1);
            }

            if (isNewer(latestVersion, currentVersion)) {
                log.info("Update available: {} -> {}", currentVersion, latestVersion);
                return new Result(Status.UPDATE_AVAILABLE, latestVersion, null);
            }

            log.debug("Update check: up to date ({})", currentVersion);
            return new Result(Status.UP_TO_DATE, currentVersion, null);

        } catch (java.net.SocketTimeoutException e) {
            log.debug("Update check: timeout");
            return new Result(Status.ERROR, null, "Timeout");
        } catch (java.io.IOException e) {
            log.debug("Update check: I/O error: {}", e.getMessage());
            return new Result(Status.ERROR, null, e.getMessage());
        } catch (Exception e) {
            log.debug("Update check: unexpected error: {}", e.getMessage());
            return new Result(Status.ERROR, null, e.getMessage());
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String readBody(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (Exception e) {
            log.debug("Update check: cannot read body: {}", e.getMessage());
            return null;
        }
    }

    private static String extractTagName(String json) {
        try {
            String key = "\"tag_name\":";
            int i = json.indexOf(key);
            if (i < 0) return null;
            int start = json.indexOf('"', i + key.length());
            if (start < 0) return null;
            int end = json.indexOf('"', start + 1);
            if (end < 0) return null;
            return json.substring(start + 1, end);
        } catch (Exception e) {
            return null;
        }
    }

    private static String readCurrentVersion() {
        try {
            java.util.Properties props = new java.util.Properties();
            try (java.io.InputStream is = UpdateChecker.class
                    .getResourceAsStream("/version.properties")) {
                if (is == null) return null;
                props.load(is);
                return props.getProperty("version");
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isNewer(String latest, String current) {
        if (latest == null || current == null) return false;
        try {
            String[] l = latest.split("\\.");
            String[] c = current.split("\\.");
            int max = Math.max(l.length, c.length);
            for (int i = 0; i < max; i++) {
                int lv = i < l.length ? Integer.parseInt(l[i]) : 0;
                int cv = i < c.length ? Integer.parseInt(c[i]) : 0;
                if (lv > cv) return true;
                if (lv < cv) return false;
            }
        } catch (NumberFormatException e) {
            log.debug("Version parse error: {} vs {}", latest, current);
        }
        return false;
    }
}
