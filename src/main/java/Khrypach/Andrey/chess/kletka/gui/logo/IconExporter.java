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

package Khrypach.Andrey.chess.kletka.gui.logo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IconExporter extends Application {

    private static final Logger log = LoggerFactory.getLogger(IconExporter.class);
    private static final String RESOURCES_PATH = "src/main/resources/icons/";

    public static void main(String[] args) {
        log.info("Запуск экспорта иконок...");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            createDirectories();
            exportAllIcons();

            log.info("✅ Экспорт иконок успешно завершен!");
            log.info("📁 Иконки сохранены в: {}", RESOURCES_PATH);

            Platform.exit();

        } catch (Exception e) {
            log.error("❌ Ошибка: {}", e.getMessage(), e);
            Platform.exit();
        }
    }

    private static void createDirectories() throws IOException {
        String[] dirs = {
                RESOURCES_PATH + "windows/",
                RESOURCES_PATH + "macos/",
                RESOURCES_PATH + "linux/16x16/apps/",
                RESOURCES_PATH + "linux/22x22/apps/",
                RESOURCES_PATH + "linux/24x24/apps/",
                RESOURCES_PATH + "linux/32x32/apps/",
                RESOURCES_PATH + "linux/48x48/apps/",
                RESOURCES_PATH + "linux/64x64/apps/",
                RESOURCES_PATH + "linux/128x128/apps/",
                RESOURCES_PATH + "linux/256x256/apps/",
                RESOURCES_PATH + "linux/512x512/apps/",
                RESOURCES_PATH + "linux/1024x1024/apps/",
                RESOURCES_PATH + "linux/scalable/apps/"
        };

        for (String dir : dirs) {
            Path path = Paths.get(dir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.debug("📁 Создана: {}", dir);
            }
        }
    }

    private static void exportAllIcons() {
        log.info("Начинаю экспорт...");

        exportPngIcon(16);
        exportPngIcon(22);
        exportPngIcon(24);
        exportPngIcon(32);
        exportPngIcon(48);
        exportPngIcon(64);
        exportPngIcon(128);
        exportPngIcon(256);
        exportPngIcon(512);
        exportPngIcon(1024);

        createIcoFromPng();
        createIcnsFromPng();
        exportSvg();

        log.info("Экспорт завершен!");
    }

    private static void exportPngIcon(int size) {
        try {
            Group logo = LogoGenerator.createLogo(size, size);

            Scene scene = new Scene(logo, size, size);
            WritableImage image = new WritableImage(size, size);
            logo.snapshot(null, image);

            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            String windowsPath = RESOURCES_PATH + "windows/icon_" + size + "x" + size + ".png";
            File windowsFile = new File(windowsPath);
            ImageIO.write(bufferedImage, "png", windowsFile);

            String macosPath = RESOURCES_PATH + "macos/icon_" + size + "x" + size + ".png";
            File macosFile = new File(macosPath);
            ImageIO.write(bufferedImage, "png", macosFile);

            String linuxPath = RESOURCES_PATH + "linux/" + size + "x" + size + "/apps/kletka.png";
            File linuxFile = new File(linuxPath);
            linuxFile.getParentFile().mkdirs();
            ImageIO.write(bufferedImage, "png", linuxFile);

            log.debug("✅ PNG: {}x{}", size, size);

        } catch (Exception e) {
            log.error("❌ Ошибка PNG {}x{}: {}", size, size, e.getMessage());
        }
    }

    private static void createIcoFromPng() {
        try {
            log.info("Создаю ICO для Windows...");

            int[] icoSizes = {16, 24, 32, 48, 64, 128, 256};
            List<BufferedImage> images = new ArrayList<>();

            for (int size : icoSizes) {
                String pngPath = RESOURCES_PATH + "windows/icon_" + size + "x" + size + ".png";
                File pngFile = new File(pngPath);

                if (pngFile.exists()) {
                    BufferedImage image = ImageIO.read(pngFile);
                    if (image != null) {
                        images.add(image);
                        log.debug("  Добавлен размер {}x{} для ICO", size, size);
                    }
                }
            }

            if (!images.isEmpty()) {
                String icoPath = RESOURCES_PATH + "windows/kletka.ico";
                ICOEncoder.write(images, new File(icoPath));
                log.info("✅ ICO создан: {}", icoPath);
            }

        } catch (Exception e) {
            log.error("❌ Ошибка создания ICO: {}", e.getMessage());
        }
    }

    private static void createIcnsFromPng() {
        try {
            log.info("Создаю ICNS для macOS...");

            String iconsetPath = RESOURCES_PATH + "macos/kletka.iconset/";
            File iconsetDir = new File(iconsetPath);
            if (!iconsetDir.exists()) {
                iconsetDir.mkdirs();
            }

            String[][] macSizes = {
                    {"16", "icon_16x16.png"},
                    {"32", "icon_32x32.png"},
                    {"128", "icon_128x128.png"},
                    {"256", "icon_256x256.png"},
                    {"512", "icon_512x512.png"},
                    {"1024", "icon_1024x1024.png"}
            };

            for (String[] sizeInfo : macSizes) {
                String size = sizeInfo[0];
                String fileName = sizeInfo[1];

                String sourcePath = RESOURCES_PATH + "macos/" + fileName;
                String destPath = iconsetPath + "icon_" + size + "x" + size + ".png";

                File sourceFile = new File(sourcePath);
                if (sourceFile.exists()) {
                    Files.copy(sourceFile.toPath(), Paths.get(destPath),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    log.debug("  Копирован {}x{} для ICNS", size, size);

                    if (Integer.parseInt(size) <= 512) {
                        String destPath2x = iconsetPath + "icon_" + size + "x" + size + "@2x.png";
                        Files.copy(sourceFile.toPath(), Paths.get(destPath2x),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            "iconutil", "-c", "icns", iconsetPath
                    );
                    pb.directory(new File(RESOURCES_PATH + "macos/"));
                    Process process = pb.start();
                    int exitCode = process.waitFor();

                    if (exitCode == 0) {
                        log.info("✅ ICNS создан: {}", RESOURCES_PATH + "macos/kletka.icns");
                    } else {
                        log.warn("⚠️ iconutil завершился с кодом: {}", exitCode);
                        log.warn("⚠️ ICNS нужно создать вручную");
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Не удалось запустить iconutil: {}", e.getMessage());
                    log.warn("⚠️ Создайте ICNS вручную командой:");
                    log.warn("   iconutil -c icns {}", iconsetPath);
                }
            } else {
                log.info("⚠️ Для создания ICNS нужна macOS");
                log.info("⚠️ Файлы для ICNS подготовлены в: {}", iconsetPath);
                log.info("⚠️ На macOS выполните: iconutil -c icns {}", iconsetPath);
            }

        } catch (Exception e) {
            log.error("❌ Ошибка создания ICNS: {}", e.getMessage(), e);
        }
    }

    private static void exportSvg() {
        try {
            String svgPath = RESOURCES_PATH + "linux/scalable/apps/kletka.svg";

            String svgContent = generateSvgContent();
            Files.write(Paths.get(svgPath), svgContent.getBytes());
            log.info("✅ SVG: {}", svgPath);
        } catch (Exception e) {
            log.error("❌ Ошибка SVG: {}", e.getMessage());
        }
    }

    private static String generateSvgContent() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300">
                    <rect width="400" height="300" fill="#1a1a2e" rx="20"/>
                    <text x="200" y="280" text-anchor="middle" font-size="24"
                          fill="white" font-weight="bold">Kletka Chess</text>
                </svg>
                """;

    }
}