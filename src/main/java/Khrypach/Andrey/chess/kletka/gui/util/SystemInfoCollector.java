/*
 *
 *  * Copyright (c) 2024 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.gui.util;

import Khrypach.Andrey.chess.kletka.engine.UciEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Собирает информацию о системе для отчёта о проблеме.
 * Не включает персональные данные: только версии, ОС, архитектура.
 */
public class SystemInfoCollector {

    private static final Logger log = LoggerFactory.getLogger(SystemInfoCollector.class);

    /**
     * Собирает информацию о системе.
     *
     * @param appVersion  версия приложения
     * @param includeEngine включать ли информацию о движке
     * @return многострочная строка с информацией
     */
    public static String collect(String appVersion, boolean includeEngine) {
        log.debug("Collecting system info");
        StringBuilder sb = new StringBuilder();

        sb.append("### System Information\n\n");

        // ========== KLETKA ==========
        sb.append("- **Kletka version:** ").append(appVersion).append("\n");

        // ========== OS ==========
        String osName = System.getProperty("os.name", "unknown");
        String osVersion = System.getProperty("os.version", "unknown");
        String osArch = System.getProperty("os.arch", "unknown");
        sb.append("- **Operating System:** ")
                .append(osName).append(" ").append(osVersion)
                .append(" (").append(osArch).append(")\n");

        // ========== JAVA ==========
        String javaVersion = System.getProperty("java.version", "unknown");
        String javaVendor = System.getProperty("java.vendor", "unknown");
        sb.append("- **Java version:** ").append(javaVersion)
                .append(" (").append(javaVendor).append(")\n");

        // ========== LOCALE ==========
        Locale locale = Locale.getDefault();
        sb.append("- **Locale:** ").append(locale.toString()).append("\n");

        // ========== ENGINE ==========
        if (includeEngine) {
            UciEngineManager engineManager = UciEngineManager.getInstance();
            String engineVersion = null;
            if (engineManager != null && engineManager.isEngineRunning()) {
                engineVersion = engineManager.getEngineVersion();
            }

            if (engineVersion == null || engineVersion.isEmpty()) {
                sb.append("- **Engine:** not installed\n");
            } else {
                sb.append("- **Engine:** ").append(engineVersion).append("\n");
            }
        }

        // ========== TIMESTAMP ==========
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sb.append("- **Timestamp:** ").append(timestamp).append("\n");

        // ========== MEMORY ==========
        Runtime runtime = Runtime.getRuntime();
        long maxMemMB = runtime.maxMemory() / (1024 * 1024);
        sb.append("- **Max heap:** ").append(maxMemMB).append(" MB\n");

        sb.append("\n");
        return sb.toString();
    }
}
