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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoGenerator {

    private static final Logger log = LoggerFactory.getLogger(LogoGenerator.class);
    private static final double BASE_WIDTH = 400;
    private static final double BASE_HEIGHT = 300;

    // ========== ОСНОВНОЙ МЕТОД ==========

    /**
     * Создает полный логотип с китом, фонтаном и надписями
     */
    public static Group createLogo(double width, double height) {
        double scale = Math.min(width / BASE_WIDTH, height / BASE_HEIGHT);
        Group root = new Group();
        root.setScaleX(scale);
        root.setScaleY(scale);

        Color outlineColor = Color.rgb(10, 40, 90);

        Group whaleGroup = new Group();
        whaleGroup.setTranslateY(20);

        // -----------------------------------------------------
        // 1. ФИГУРА КИТА (SVGPath)
        // -----------------------------------------------------
        String whaleSvgData =
                "M 365 135 " +
                        "C 345 105, 295 95, 240 100 " +
                        "C 180 105, 120 125, 80 130 " +
                        "C 55 125, 45 105, 40 85 " +
                        "C 30 60, 15 40, 0 35 " +
                        "C 15 55, 25 75, 40 85 " +
                        "C 25 95, 15 105, 0 110 " +
                        "C 15 100, 30 90, 40 85 " +
                        "C 55 105, 85 170, 140 215 " +
                        "C 195 240, 260 230, 310 205 " +
                        "C 350 185, 375 165, 365 135 Z";

        SVGPath whaleFigure = new SVGPath();
        whaleFigure.setContent(whaleSvgData);

        // -----------------------------------------------------
        // 2. ШАХМАТНАЯ СЕТКА (Спина кита)
        // -----------------------------------------------------
        Group checkerGroup = createCheckerPattern(400, 300, 18);
        checkerGroup.setClip(whaleFigure);
        whaleGroup.getChildren().add(checkerGroup);

        // -----------------------------------------------------
        // 3. КОНТУР И ЛИЦО
        // -----------------------------------------------------
        SVGPath whaleOutline = new SVGPath();
        whaleOutline.setContent(whaleSvgData);
        whaleOutline.setStroke(outlineColor);
        whaleOutline.setStrokeWidth(3.0);
        whaleOutline.setFill(null);
        whaleGroup.getChildren().add(whaleOutline);

        Circle eyeWhite = new Circle(335, 135, 12);
        eyeWhite.setFill(Color.WHITE);
        eyeWhite.setStroke(outlineColor);
        eyeWhite.setStrokeWidth(1.5);
        whaleGroup.getChildren().add(eyeWhite);

        Circle pupil = new Circle(337, 132, 7);
        pupil.setFill(Color.rgb(20, 40, 80));
        whaleGroup.getChildren().add(pupil);

        Circle eyeShine = new Circle(339, 129, 3.5);
        eyeShine.setFill(Color.rgb(255, 255, 255, 0.9));
        whaleGroup.getChildren().add(eyeShine);

        Path brow = new Path();
        brow.setStroke(outlineColor);
        brow.setStrokeWidth(3.5);
        brow.setStrokeLineCap(StrokeLineCap.ROUND);
        brow.setFill(null);
        brow.getElements().addAll(
                new MoveTo(322, 124),
                new QuadCurveTo(335, 118, 348, 125)
        );
        whaleGroup.getChildren().add(brow);

        Arc smile = new Arc();
        smile.setCenterX(328);
        smile.setCenterY(165);
        smile.setRadiusX(25);
        smile.setRadiusY(10);
        smile.setStartAngle(180);
        smile.setLength(180);
        smile.setType(ArcType.OPEN);
        smile.setStroke(outlineColor);
        smile.setStrokeWidth(2.5);
        smile.setFill(null);
        whaleGroup.getChildren().add(smile);

        // -----------------------------------------------------
        // 4. МОЩНЫЙ ФОНТАН ВОДЫ НА СПИНЕ
        // -----------------------------------------------------
        double fountainX = 170;
        double fountainY = 105;

        Color waterDark = Color.rgb(100, 180, 240, 0.7);
        Color waterLight = Color.rgb(200, 235, 255, 0.8);
        Color waterWhite = Color.rgb(255, 255, 255, 0.6);

        Path mainStream = new Path();
        mainStream.setStroke(waterLight);
        mainStream.setStrokeWidth(12.0);
        mainStream.setStrokeLineCap(StrokeLineCap.ROUND);
        mainStream.getElements().addAll(
                new MoveTo(fountainX, fountainY + 5),
                new QuadCurveTo(150, 60, 170, 35)
        );
        whaleGroup.getChildren().add(mainStream);

        Path secStream = new Path();
        secStream.setStroke(waterDark);
        secStream.setStrokeWidth(8.0);
        secStream.setStrokeLineCap(StrokeLineCap.ROUND);
        secStream.getElements().addAll(
                new MoveTo(fountainX - 5, fountainY + 12),
                new QuadCurveTo(180, 80, 185, 45)
        );
        whaleGroup.getChildren().add(secStream);

        for (int i = 0; i < 35; i++) {
            double angle = -160 + Math.random() * 140;
            double dist = 15 + Math.random() * 60;
            double x = fountainX + Math.cos(Math.toRadians(angle)) * dist;
            double y = fountainY + Math.sin(Math.toRadians(angle)) * dist - 20;
            double r = 2 + Math.random() * 8;
            Circle drop = new Circle(x, y, r);
            if (Math.random() > 0.5) {
                drop.setFill(waterWhite);
            } else {
                drop.setFill(waterLight);
            }
            whaleGroup.getChildren().add(drop);
        }

        // -----------------------------------------------------
        // 5. ГЕРБ: КОРОЛЬ В ФОНТАНЕ (ЗАГРУЗКА ИЗ PNG)
        // -----------------------------------------------------
        double centerX = 170;
        double centerY = 45;

        ImageView kingView = null;
        try {
            java.io.InputStream kingStream = LogoGenerator.class.getResourceAsStream("/images/pieces/bK.png");
            if (kingStream != null) {
                Image kingImage = new Image(kingStream);
                if (!kingImage.isError()) {
                    kingView = new ImageView(kingImage);
                    kingView.setFitWidth(45);
                    kingView.setFitHeight(45);
                    kingView.setPreserveRatio(true);
                    kingView.setLayoutX(centerX - 22.5);
                    kingView.setLayoutY(centerY - 22.5);

                    DropShadow ks = new DropShadow();
                    ks.setColor(Color.BLACK);
                    ks.setRadius(5);
                    kingView.setEffect(ks);

                    whaleGroup.getChildren().add(kingView);
                }
            }
        } catch (Exception e) {
            log.trace("Не удалось загрузить короля: {}", e.getMessage());
        }

        if (kingView == null || kingView.getImage() == null || kingView.getImage().isError()) {
            log.trace("Draw king by JavaFX");
            drawKing(whaleGroup, centerX, centerY, 2.8, Color.BLACK, Color.rgb(40, 40, 40));
        }

        root.getChildren().add(whaleGroup);

        // -----------------------------------------------------
        // 6. НАДПИСИ (ЛОКАЛИЗОВАННЫЕ)
        // -----------------------------------------------------
        LanguageManager lang = LanguageManager.getInstance();

        DropShadow textShadow = new DropShadow();
        textShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        textShadow.setOffsetX(0);
        textShadow.setOffsetY(2);
        textShadow.setRadius(2);

        double leftX = 105.0;
        double rightX = 235.0;

        // ========== НАЗВАНИЕ ПРОГРАММЫ ==========
        String titleText = lang.get(LanguageKeys.LOGO_TITLE);
        // Для русского языка оставляем "КЛЕТКА", для других - "KLETKA"
        // Это уже учтено в языковых файлах
        Text labelCenter = new Text(titleText);
        labelCenter.setFont(Font.font("Arial Rounded MT Bold", FontWeight.BOLD, 22));
        labelCenter.setFill(Color.rgb(255, 255, 255));
        double tw = labelCenter.getLayoutBounds().getWidth();
        labelCenter.setX(170 - tw / 2);
        labelCenter.setY(25);
        labelCenter.setEffect(textShadow);
        root.getChildren().add(labelCenter);

        // ========== ПЕРВАЯ ЧАСТЬ ПОДПИСИ ==========
        String line1 = lang.get(LanguageKeys.LOGO_SUBTITLE_LINE1);
        double startY = 15;
        double endY = 110;

        for (int i = 0; i < line1.length(); i++) {
            double t = (double) i / (line1.length() - 1);
            double y = startY + t * (endY - startY);

            Text letter = new Text(String.valueOf(line1.charAt(i)));
            letter.setFont(Font.font("Arial Rounded MT Bold", FontWeight.BOLD, 11));
            letter.setFill(Color.rgb(235, 235, 235));
            letter.setX(leftX - letter.getLayoutBounds().getWidth() / 2);
            letter.setY(y);
            letter.setEffect(textShadow);
            root.getChildren().add(letter);
        }

        // ========== ВТОРАЯ ЧАСТЬ ПОДПИСИ ==========
        String line2 = lang.get(LanguageKeys.LOGO_SUBTITLE_LINE2);
        for (int i = 0; i < line2.length(); i++) {
            double t = (double) i / (line2.length() - 1);
            double y = startY + t * (endY - startY);

            Text letter = new Text(String.valueOf(line2.charAt(i)));
            letter.setFont(Font.font("Arial Rounded MT Bold", FontWeight.BOLD, 11));
            letter.setFill(Color.rgb(235, 235, 235));
            letter.setX(rightX - letter.getLayoutBounds().getWidth() / 2);
            letter.setY(y);
            letter.setEffect(textShadow);
            root.getChildren().add(letter);
        }

        return root;
    }

    /**
     * Рисует короля средствами JavaFX (запасной вариант)
     */
    private static void drawKing(Group root, double cx, double cy, double scale, Color fillColor, Color strokeColor) {
        Path cross = new Path();
        cross.setStroke(strokeColor);
        cross.setStrokeWidth(1.2 * scale);
        cross.setStrokeLineCap(StrokeLineCap.ROUND);
        cross.getElements().addAll(
                new MoveTo(cx, cy - 4 * scale),
                new LineTo(cx, cy + 4 * scale),
                new MoveTo(cx - 4 * scale, cy),
                new LineTo(cx + 4 * scale, cy)
        );
        root.getChildren().add(cross);

        Path crown = new Path();
        crown.setFill(fillColor);
        crown.setStroke(strokeColor);
        crown.setStrokeWidth(scale);
        crown.getElements().addAll(
                new MoveTo(cx - 8 * scale, cy + 2 * scale),
                new LineTo(cx - 6 * scale, cy - 4 * scale),
                new LineTo(cx - 2 * scale, cy - 2 * scale),
                new LineTo(cx, cy - 6 * scale),
                new LineTo(cx + 2 * scale, cy - 2 * scale),
                new LineTo(cx + 6 * scale, cy - 4 * scale),
                new LineTo(cx + 8 * scale, cy + 2 * scale),
                new ClosePath()
        );
        root.getChildren().add(crown);

        Circle body = new Circle(cx, cy + 8 * scale, 6 * scale);
        body.setFill(fillColor);
        body.setStroke(strokeColor);
        body.setStrokeWidth(scale);
        root.getChildren().add(body);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Создает шахматный узор
     */
    private static Group createCheckerPattern(double width, double height, double cellSize) {
        Group group = new Group();
        Color goldColor = Color.rgb(218, 165, 32);
        Color brownColor = Color.rgb(139, 69, 19);

        int cols = (int) Math.ceil(width / cellSize);
        int rows = (int) Math.ceil(height / cellSize);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Rectangle cell = new Rectangle(cellSize, cellSize);
                cell.setFill((row + col) % 2 == 0 ? goldColor : brownColor);
                cell.setX(col * cellSize);
                cell.setY(row * cellSize);
                group.getChildren().add(cell);
            }
        }

        return group;
    }
}