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

package Khrypach.Andrey.chess.kletka.gui.dialogs;

import Khrypach.Andrey.chess.kletka.gui.KletkaGui;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.util.SystemInfoCollector;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Диалог отправки отчёта о проблеме на GitHub.
 * Использует URL-prefill: открывает страницу новой issue с предзаполненным текстом.
 */
public class ReportProblemDialog {

    private static final Logger log = LoggerFactory.getLogger(ReportProblemDialog.class);
    private static final String GITHUB_ISSUE_URL = "https://github.com/AndreyKhrypach/Kletka/issues/new";
    private static final int MAX_URL_LENGTH = 8000;

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Stage ownerStage;
    private final String appVersion;

    // UI
    private Stage dialogStage;
    private ToggleGroup typeGroup;
    private RadioButton bugRadio;
    private RadioButton featureRadio;
    private RadioButton performanceRadio;
    private RadioButton uiRadio;
    private RadioButton documentationRadio;
    private RadioButton otherRadio;
    private TextArea descriptionArea;
    private CheckBox includeSystemInfoCheck;

    public ReportProblemDialog(Stage ownerStage, String appVersion) {
        this.ownerStage = ownerStage;
        this.appVersion = appVersion;
    }

    public void showAndWait() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        if (ownerStage != null) {
            dialogStage.initOwner(ownerStage);
        }
        dialogStage.setTitle(lang.get(LanguageKeys.REPORT_TITLE));
        dialogStage.setResizable(true);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // ========== HEADER ==========
        Label headerLabel = new Label(lang.get(LanguageKeys.REPORT_HEADER));
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // ========== TYPE SELECTION ==========
        Label typeLabel = new Label(lang.get(LanguageKeys.REPORT_TYPE_LABEL));
        typeLabel.setStyle("-fx-font-weight: bold;");

        typeGroup = new ToggleGroup();

        bugRadio = createTypeRadio(lang.get(LanguageKeys.REPORT_TYPE_BUG), true);
        featureRadio = createTypeRadio(lang.get(LanguageKeys.REPORT_TYPE_FEATURE), false);
        performanceRadio = createTypeRadio(lang.get(LanguageKeys.REPORT_TYPE_PERFORMANCE), false);
        uiRadio = createTypeRadio(lang.get(LanguageKeys.REPORT_TYPE_UI), false);
        documentationRadio = createTypeRadio(lang.get(LanguageKeys.REPORT_TYPE_DOCUMENTATION), false);
        otherRadio = createTypeRadio(lang.get(LanguageKeys.REPORT_TYPE_OTHER), false);

        VBox typesBox = new VBox(5,
                bugRadio, featureRadio, performanceRadio,
                uiRadio, documentationRadio, otherRadio);
        typesBox.setPadding(new Insets(0, 0, 0, 15));

        // ========== DESCRIPTION ==========
        Label descLabel = new Label(lang.get(LanguageKeys.REPORT_DESCRIPTION_LABEL));
        descLabel.setStyle("-fx-font-weight: bold;");

        descriptionArea = new TextArea();
        descriptionArea.setPromptText(lang.get(LanguageKeys.REPORT_DESCRIPTION_PROMPT));
        descriptionArea.setPrefRowCount(8);
        descriptionArea.setWrapText(true);
        VBox.setVgrow(descriptionArea, Priority.ALWAYS);

        // ========== SYSTEM INFO CHECKBOX ==========
        includeSystemInfoCheck = new CheckBox(lang.get(LanguageKeys.REPORT_INCLUDE_SYSTEM_INFO));
        includeSystemInfoCheck.setSelected(true);

        // ========== PRIVACY NOTICE ==========
        VBox privacyBox = new VBox(3);
        privacyBox.setPadding(new Insets(8));
        privacyBox.setStyle(
                "-fx-background-color: #e8f4f8; " +
                        "-fx-border-color: #b0d4e0; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;"
        );

        Label privacyLabel = new Label("ℹ️ " + lang.get(LanguageKeys.REPORT_PRIVACY_NOTICE));
        privacyLabel.setWrapText(true);
        privacyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2c5a6b;");

        Hyperlink privacyLink = new Hyperlink(lang.get(LanguageKeys.REPORT_PRIVACY_LINK));
        privacyLink.setStyle("-fx-font-size: 11px;");
        privacyLink.setOnAction(e -> openUrl("https://andreykhrypach.github.io/Kletka/privacy.html"));

        privacyBox.getChildren().addAll(privacyLabel, privacyLink);

        // ========== BUTTONS ==========
        Button copyButton = new Button(lang.get(LanguageKeys.REPORT_BUTTON_COPY));
        copyButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-weight: bold;");
        copyButton.setOnAction(e -> copyToClipboard());

        Button cancelButton = new Button(lang.get(LanguageKeys.REPORT_BUTTON_CANCEL));
        cancelButton.setStyle("-fx-background-color: #999; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> dialogStage.close());

        Button githubButton = new Button(lang.get(LanguageKeys.REPORT_BUTTON_GITHUB));
        githubButton.setStyle(
                "-fx-background-color: #2e8b57; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 8 20 8 20;"
        );
        githubButton.setOnAction(e -> openOnGitHub());

        HBox buttonBox = new HBox(10, copyButton, cancelButton, githubButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(copyButton, Priority.NEVER);
        HBox.setHgrow(cancelButton, Priority.NEVER);
        HBox.setHgrow(githubButton, Priority.NEVER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottomBox = new HBox(10, copyButton, spacer, cancelButton, githubButton);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        // ========== LAYOUT ==========
        root.getChildren().addAll(
                headerLabel,
                new Separator(),
                typeLabel,
                typesBox,
                new Separator(),
                descLabel,
                descriptionArea,
                includeSystemInfoCheck,
                privacyBox,
                bottomBox
        );

        Scene scene = new Scene(root, 600, 650);
        dialogStage.setScene(scene);
        dialogStage.setMinWidth(500);
        dialogStage.setMinHeight(500);

        dialogStage.setOnShown(e -> {
            dialogStage.toFront();
            descriptionArea.requestFocus();
        });

        dialogStage.showAndWait();
    }

    private RadioButton createTypeRadio(String text, boolean selected) {
        RadioButton radio = new RadioButton(text);
        radio.setToggleGroup(typeGroup);
        radio.setSelected(selected);
        radio.setStyle("-fx-font-size: 13px;");
        return radio;
    }

    // ========== ACTIONS ==========

    private void openOnGitHub() {
        String description = descriptionArea.getText();
        if (description == null || description.trim().isEmpty()) {
            showError(lang.get(LanguageKeys.REPORT_ERROR_EMPTY_DESCRIPTION));
            return;
        }

        // ========== WARNING DIALOG ==========
        Alert warning = new Alert(Alert.AlertType.INFORMATION);
        warning.setTitle(lang.get(LanguageKeys.REPORT_WARNING_TITLE));
        warning.setHeaderText(lang.get(LanguageKeys.REPORT_WARNING_HEADER));
        warning.setContentText(lang.get(LanguageKeys.REPORT_WARNING_CONTENT));
        warning.getDialogPane().setPrefWidth(500);

        ButtonType continueButton = new ButtonType(
                lang.get(LanguageKeys.REPORT_WARNING_CONTINUE),
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelButton = new ButtonType(
                lang.get(LanguageKeys.REPORT_BUTTON_CANCEL),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        warning.getButtonTypes().setAll(continueButton, cancelButton);

        ButtonType choice = warning.showAndWait().orElse(cancelButton);
        if (choice != continueButton) {
            return;
        }

        // ========== BUILD URL ==========
        String title = buildTitle();
        String body = buildBody(description);

        String url;
        try {
            url = GITHUB_ISSUE_URL +
                    "?title=" + encode(title) +
                    "&body=" + encode(body) +
                    "&labels=" + encode(getLabelForSelectedType());
        } catch (Exception e) {
            log.error("Failed to build GitHub URL", e);
            showError(lang.get(LanguageKeys.REPORT_OPEN_BROWSER_ERROR));
            return;
        }

        if (url.length() > MAX_URL_LENGTH) {
            showError(lang.get(LanguageKeys.REPORT_ERROR_URL_TOO_LONG));
            return;
        }

        log.info("Opening GitHub issue: URL length = {}", url.length());

        openUrl(url);
        dialogStage.close();
    }

    private void copyToClipboard() {
        String description = descriptionArea.getText();
        if (description == null || description.trim().isEmpty()) {
            showError(lang.get(LanguageKeys.REPORT_ERROR_EMPTY_DESCRIPTION));
            return;
        }

        String title = buildTitle();
        String body = buildBody(description);

        String fullText = "Title: " + title + "\n\n" + body;

        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(fullText);
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle(lang.get(LanguageKeys.NOTIFICATION_INFO));
        info.setHeaderText(null);
        info.setContentText(lang.get(LanguageKeys.REPORT_COPIED_TO_CLIPBOARD));
        info.showAndWait();
    }

    // ========== BUILDING ==========

    private String buildTitle() {
        String prefix = getSelectedTypePrefix();
        String firstLine = descriptionArea.getText().split("\n", 2)[0].trim();

        if (firstLine.length() > 80) {
            firstLine = firstLine.substring(0, 77) + "...";
        }

        return prefix + " " + firstLine;
    }

    private String buildBody(String description) {
        StringBuilder sb = new StringBuilder();

        sb.append("## ").append(lang.get(LanguageKeys.REPORT_DESCRIPTION_LABEL)).append("\n\n");
        sb.append(description).append("\n\n");

        if (includeSystemInfoCheck.isSelected()) {
            sb.append(SystemInfoCollector.collect(appVersion, true));
        }

        sb.append("---\n");
        sb.append("*Reported from Kletka*\n");

        return sb.toString();
    }

    private String getSelectedTypePrefix() {
        if (bugRadio.isSelected()) return "[Bug]";
        if (featureRadio.isSelected()) return "[Feature]";
        if (performanceRadio.isSelected()) return "[Performance]";
        if (uiRadio.isSelected()) return "[UI]";
        if (documentationRadio.isSelected()) return "[Docs]";
        return "[Other]";
    }

    private String getLabelForSelectedType() {
        if (bugRadio.isSelected()) return "bug";
        if (featureRadio.isSelected()) return "enhancement";
        if (performanceRadio.isSelected()) return "performance";
        if (uiRadio.isSelected()) return "ui";
        if (documentationRadio.isSelected()) return "documentation";
        return "question";
    }

    // ========== UTILS ==========

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private void openUrl(String url) {
        try {
            HostServices hs = KletkaGui.hostServices();
            if (hs != null) {
                hs.showDocument(url);
            } else {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            log.error("Failed to open URL: {}", url, e);
            showError(lang.get(LanguageKeys.REPORT_OPEN_BROWSER_ERROR));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lang.get(LanguageKeys.NOTIFICATION_ERROR));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
