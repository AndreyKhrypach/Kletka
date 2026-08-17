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

package Khrypach.Andrey.chess.kletka.gui.dialogs;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DonateDialog {

    private static final String PAYPAL_EMAIL = "pacik78@gmail.com";
    private static final String BITCOIN_ADDRESS = "15HGcdu67yy3pMb2roANr2GAJZDPefB1HS";

    private final LanguageManager lang = LanguageManager.getInstance();

    private final Stage stage;

    public DonateDialog(Stage owner) {
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(lang.get(LanguageKeys.DONATE_TITLE));
        stage.setResizable(false);
        stage.setWidth(480);
        stage.setHeight(580);
    }

    public void showAndWait() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // Заголовок
        Label titleLabel = new Label(lang.get(LanguageKeys.DONATE_HEADER));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label descLabel = new Label(lang.get(LanguageKeys.DONATE_DESCRIPTION));
        descLabel.setStyle("-fx-font-size: 13px;");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        Separator separator = new Separator();

        // ===== PayPal =====
        VBox paypalBox = createPayPalSection();

        // ===== Bitcoin =====
        VBox bitcoinBox = createBitcoinSection();

        Separator separator2 = new Separator();

        // Подсказка
        Label hintLabel = new Label(lang.get(LanguageKeys.DONATE_HINT));
        hintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        hintLabel.setWrapText(true);
        hintLabel.setAlignment(Pos.CENTER);

        // Кнопка закрытия
        Button closeButton = new Button(lang.get(LanguageKeys.DONATE_CLOSE));
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 8 30 8 30;");
        closeButton.setOnAction(e -> stage.close());

        root.getChildren().addAll(
                titleLabel,
                descLabel,
                separator,
                paypalBox,
                bitcoinBox,
                separator2,
                hintLabel,
                closeButton
        );

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private VBox createPayPalSection() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Label paypalLabel = new Label(lang.get(LanguageKeys.DONATE_PAYPAL));
        paypalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label paypalAddress = new Label(PAYPAL_EMAIL);
        paypalAddress.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-text-fill: #0066cc;");
        paypalAddress.setPadding(new Insets(0, 0, 0, 10));

        HBox paypalRow = new HBox(10);
        paypalRow.setAlignment(Pos.CENTER_LEFT);
        paypalRow.getChildren().add(paypalAddress);

        Button copyPaypalBtn = new Button(lang.get(LanguageKeys.DONATE_COPY));
        copyPaypalBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(PAYPAL_EMAIL);
            clipboard.setContent(content);
            showToast(lang.get(LanguageKeys.DONATE_TOAST_COPIED_EMAIL));
        });

        Button openPaypalBtn = new Button(lang.get(LanguageKeys.DONATE_OPEN));
        openPaypalBtn.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(
                        java.net.URI.create("https://www.paypal.com/paypalme/")
                );
            } catch (Exception ex) {
                showToast(lang.get(LanguageKeys.DONATE_TOAST_OPEN_BROWSER));
            }
        });

        HBox paypalButtons = new HBox(8);
        paypalButtons.getChildren().addAll(copyPaypalBtn, openPaypalBtn);

        box.getChildren().addAll(paypalLabel, paypalRow, paypalButtons);
        return box;
    }

    private VBox createBitcoinSection() {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Label btcLabel = new Label(lang.get(LanguageKeys.DONATE_BITCOIN));
        btcLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label btcAddress = new Label(BITCOIN_ADDRESS);
        btcAddress.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-text-fill: #f7931a;");
        btcAddress.setPadding(new Insets(0, 0, 0, 10));
        btcAddress.setWrapText(true);

        HBox btcRow = new HBox(10);
        btcRow.setAlignment(Pos.CENTER_LEFT);
        btcRow.getChildren().add(btcAddress);

        Button copyBtcBtn = new Button(lang.get(LanguageKeys.DONATE_COPY));
        copyBtcBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(BITCOIN_ADDRESS);
            clipboard.setContent(content);
            showToast(lang.get(LanguageKeys.DONATE_TOAST_COPIED_BITCOIN));
        });

        Button qrBtn = new Button(lang.get(LanguageKeys.DONATE_QR));
        qrBtn.setOnAction(e -> showQrCodeDialog());

        HBox btcButtons = new HBox(8);
        btcButtons.getChildren().addAll(copyBtcBtn, qrBtn);

        box.getChildren().addAll(btcLabel, btcRow, btcButtons);
        return box;
    }

    private void showQrCodeDialog() {
        try {
            // Генерируем QR-код для Bitcoin адреса
            String bitcoinUri = "bitcoin:" + BITCOIN_ADDRESS;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(bitcoinUri, BarcodeFormat.QR_CODE, 300, 300);

            // Конвертируем в JavaFX Image
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            Image qrImage = new Image(new ByteArrayInputStream(baos.toByteArray()));

            // Показываем диалог с QR-кодом
            Stage qrStage = new Stage();
            qrStage.initModality(Modality.APPLICATION_MODAL);
            qrStage.initOwner(stage);
            qrStage.setTitle(lang.get(LanguageKeys.DONATE_QR_TITLE));
            qrStage.setResizable(false);

            VBox qrRoot = new VBox(15);
            qrRoot.setPadding(new Insets(20));
            qrRoot.setAlignment(Pos.CENTER);

            Label qrTitle = new Label(String.format("%s %s ", lang.get(LanguageKeys.DONATE_BITCOIN), lang.get(LanguageKeys.DONATE_QR)));
            qrTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            ImageView qrView = new ImageView(qrImage);
            qrView.setFitWidth(250);
            qrView.setFitHeight(250);

            Label qrAddress = new Label(BITCOIN_ADDRESS);
            qrAddress.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-text-fill: #f7931a;");
            qrAddress.setWrapText(true);
            qrAddress.setMaxWidth(300);

            Label qrHint = new Label(lang.get(LanguageKeys.DONATE_QR_HINT));
            qrHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

            Button qrCloseBtn = new Button(lang.get(LanguageKeys.DONATE_CLOSE));
            qrCloseBtn.setOnAction(e -> qrStage.close());

            qrRoot.getChildren().addAll(qrTitle, qrView, qrAddress, qrHint, qrCloseBtn);

            Scene qrScene = new Scene(qrRoot);
            qrStage.setScene(qrScene);
            qrStage.showAndWait();

        } catch (WriterException | IOException e) {
            showToast(lang.get(LanguageKeys.DONATE_TOAST_QR_ERROR));
        }
    }

    private void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(LanguageKeys.NOTIFICATION_INFO));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}