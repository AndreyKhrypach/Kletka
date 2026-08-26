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

import Khrypach.Andrey.chess.kletka.gui.board.BoardTheme;
import Khrypach.Andrey.chess.kletka.gui.board.BoardSizeController;
import Khrypach.Andrey.chess.kletka.gui.languages.Language;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Диалог глобальных настроек программы
 */
public class PreferencesDialog {

    private static final Logger log = LoggerFactory.getLogger(PreferencesDialog.class);

    private final Stage ownerStage;
    private final LanguageManager lang = LanguageManager.getInstance();

    // Элементы управления
    private ComboBox<String> languageCombo;
    private ComboBox<String> themeCombo;
    private Slider boardSizeSlider;
    private CheckBox coordinatesCheck;
    private TextField saveDirectoryField;
    private TextField enginePathField;
    private CheckBox flipBoardCheck;
    private Label configPathLabel;

    public PreferencesDialog(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    public void showAndWait() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(lang.get(PREFERENCES_TITLE));
        dialog.setHeaderText(lang.get(PREFERENCES_HEADER));

        ButtonType saveButton = new ButtonType(lang.get(PREFERENCES_SAVE), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType(lang.get(PREFERENCES_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType resetButton = new ButtonType(lang.get(PREFERENCES_RESET), ButtonBar.ButtonData.LEFT);

        dialog.getDialogPane().getButtonTypes().addAll(saveButton, resetButton, cancelButton);

        // Создаем содержимое
        VBox content = createContent();
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(650);

        // Обработка кнопок
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButton) {
                savePreferences();
            } else if (buttonType == resetButton) {
                resetPreferences();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private VBox createContent() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // ========== ЯЗЫК ==========
        GridPane languageGrid = createGridPane();
        languageCombo = new ComboBox<>();
        List<Language> languages = LanguageManager.getInstance().getAvailableLanguages();
        for (Language lang : languages) {
            languageCombo.getItems().add(lang.getDisplayName());
        }
        String currentLang = AppPreferences.getLanguage();
        for (Language lang : languages) {
            if (lang.getCode().equals(currentLang)) {
                languageCombo.setValue(lang.getDisplayName());
                break;
            }
        }
        if (languageCombo.getValue() == null && !languageCombo.getItems().isEmpty()) {
            languageCombo.setValue(languageCombo.getItems().get(0));
        }

        languageGrid.add(new Label(lang.get(PREFERENCES_LANGUAGE) + ":"), 0, 0);
        languageGrid.add(languageCombo, 1, 0);
        root.getChildren().add(languageGrid);

        // ========== ТЕМА ДОСКИ ==========
        GridPane themeGrid = createGridPane();
        themeCombo = new ComboBox<>();
        for (BoardTheme.Theme theme : BoardTheme.THEMES) {
            themeCombo.getItems().add(theme.name());
        }
        int currentThemeIndex = AppPreferences.getBoardThemeIndex();
        if (currentThemeIndex >= 0 && currentThemeIndex < BoardTheme.THEMES.length) {
            themeCombo.setValue(BoardTheme.THEMES[currentThemeIndex].name());
        } else {
            themeCombo.setValue(BoardTheme.THEMES[0].name());
        }

        themeGrid.add(new Label(lang.get(PREFERENCES_BOARD_THEME) + ":"), 0, 0);
        themeGrid.add(themeCombo, 1, 0);
        root.getChildren().add(themeGrid);

        // ========== РАЗМЕР ДОСКИ ==========
        GridPane sizeGrid = createGridPane();
        int currentSize = AppPreferences.getTileSize();
        boardSizeSlider = new Slider(
                BoardSizeController.MIN_TILE_SIZE,
                BoardSizeController.MAX_TILE_SIZE,
                currentSize
        );
        boardSizeSlider.setMajorTickUnit(BoardSizeController.STEP_SIZE * 2);
        boardSizeSlider.setMinorTickCount(1);
        boardSizeSlider.setShowTickLabels(true);
        boardSizeSlider.setShowTickMarks(true);
        boardSizeSlider.setSnapToTicks(true);

        Label sizeLabel = new Label(currentSize + "px");
        boardSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int size = newVal.intValue();
            sizeLabel.setText(size + "px");
        });

        HBox sizeBox = new HBox(10);
        sizeBox.getChildren().addAll(boardSizeSlider, sizeLabel);

        sizeGrid.add(new Label(lang.get(PREFERENCES_BOARD_SIZE) + ":"), 0, 0);
        sizeGrid.add(sizeBox, 1, 0);
        root.getChildren().add(sizeGrid);

        // ========== КООРДИНАТЫ ==========
        GridPane coordGrid = createGridPane();
        coordinatesCheck = new CheckBox(lang.get(PREFERENCES_COORDINATES));
        coordinatesCheck.setSelected(AppPreferences.isShowCoordinates());
        coordGrid.add(coordinatesCheck, 1, 0);
        root.getChildren().add(coordGrid);

        // ========== ПЕРЕВОРОТ ДОСКИ ==========
        GridPane flipGrid = createGridPane();
        flipBoardCheck = new CheckBox(lang.get(PREFERENCES_FLIP_BOARD));
        flipBoardCheck.setSelected(AppPreferences.isBoardFlipped());
        flipGrid.add(flipBoardCheck, 1, 0);
        root.getChildren().add(flipGrid);

        // ========== ДИРЕКТОРИЯ СОХРАНЕНИЯ (BASES) ==========
        GridPane saveGrid = createGridPane();
        saveDirectoryField = new TextField(AppPreferences.getSaveDirectory());
        saveDirectoryField.setEditable(false);
        saveDirectoryField.setPrefWidth(350);

        Button browseSaveButton = new Button(lang.get(PREFERENCES_BROWSE));
        browseSaveButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(lang.get(PREFERENCES_SELECT_SAVE_DIR));
            File currentDir = new File(saveDirectoryField.getText());
            if (currentDir.exists() && currentDir.isDirectory()) {
                chooser.setInitialDirectory(currentDir);
            }
            File selected = chooser.showDialog(ownerStage);
            if (selected != null) {
                saveDirectoryField.setText(selected.getAbsolutePath());
            }
        });

        HBox saveBox = new HBox(10);
        saveBox.getChildren().addAll(saveDirectoryField, browseSaveButton);

        saveGrid.add(new Label(lang.get(PREFERENCES_SAVE_DIRECTORY) + ":"), 0, 0);
        saveGrid.add(saveBox, 1, 0);
        root.getChildren().add(saveGrid);

        // ========== ПУТЬ К ДВИЖКУ ==========
        GridPane engineGrid = createGridPane();
        enginePathField = new TextField();
        String enginePath = AppPreferences.getEnginePath();
        if (enginePath != null && !enginePath.isEmpty()) {
            enginePathField.setText(enginePath);
        }
        enginePathField.setEditable(false);
        enginePathField.setPrefWidth(350);

        Button browseEngineButton = new Button(lang.get(PREFERENCES_BROWSE));
        browseEngineButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(lang.get(PREFERENCES_SELECT_ENGINE));

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                chooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Executable files", "*.exe")
                );
            } else {
                chooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("All files", "*.*")
                );
            }

            File currentFile = new File(enginePathField.getText());
            if (currentFile.exists()) {
                chooser.setInitialDirectory(currentFile.getParentFile());
            }
            File selected = chooser.showOpenDialog(ownerStage);
            if (selected != null) {
                enginePathField.setText(selected.getAbsolutePath());
            }
        });

        HBox engineBox = new HBox(10);
        engineBox.getChildren().addAll(enginePathField, browseEngineButton);

        engineGrid.add(new Label(lang.get(PREFERENCES_ENGINE) + ":"), 0, 0);
        engineGrid.add(engineBox, 1, 0);
        root.getChildren().add(engineGrid);

        // ========== ИНФОРМАЦИЯ О ФАЙЛЕ НАСТРОЕК ==========
        Separator separator = new Separator();
        root.getChildren().add(separator);

        GridPane infoGrid = createGridPane();
        configPathLabel = new Label();
        configPathLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        configPathLabel.setText(lang.get(PREFERENCES_CONFIG_PATH) + ": " + AppPreferences.getConfigFilePath());

        // Кнопка "Открыть папку" для файла настроек
        Button openConfigFolderButton = new Button(lang.get(PREFERENCES_OPEN_FOLDER));
        openConfigFolderButton.setOnAction(e -> {
            try {
                File configFile = new File(AppPreferences.getConfigFilePath());
                File parentDir = configFile.getParentFile();
                if (parentDir != null && parentDir.exists()) {
                    java.awt.Desktop.getDesktop().open(parentDir);
                }
            } catch (Exception ex) {
                log.error("Failed to open config folder", ex);
                showError(lang.get(PREFERENCES_OPEN_FOLDER_ERROR));
            }
        });

        HBox infoBox = new HBox(10);
        infoBox.getChildren().addAll(configPathLabel, openConfigFolderButton);

        infoGrid.add(infoBox, 0, 0);
        root.getChildren().add(infoGrid);

        return root;
    }

    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(5, 0, 5, 0));
        return grid;
    }

    private void savePreferences() {
        boolean languageChanged = false;
        String oldLanguageCode = AppPreferences.getLanguage();
        String newLanguageCode;
        String languageDisplayName = "";

        // Сохраняем язык
        String selectedLanguage = languageCombo.getValue();
        if (selectedLanguage != null) {
            List<Language> languages = LanguageManager.getInstance().getAvailableLanguages();
            for (Language lang : languages) {
                if (lang.getDisplayName().equals(selectedLanguage)) {
                    newLanguageCode = lang.getCode();
                    languageDisplayName = lang.getDisplayName();
                    if (!newLanguageCode.equals(oldLanguageCode)) {
                        languageChanged = true;
                    }
                    AppPreferences.saveLanguage(newLanguageCode);
                    LanguageManager.getInstance().setLanguage(newLanguageCode);
                    break;
                }
            }
        }

        // Сохраняем тему доски
        String selectedTheme = themeCombo.getValue();
        if (selectedTheme != null) {
            for (int i = 0; i < BoardTheme.THEMES.length; i++) {
                if (BoardTheme.THEMES[i].name().equals(selectedTheme)) {
                    AppPreferences.saveBoardTheme(i);
                    break;
                }
            }
        }

        // Сохраняем размер доски
        int newSize = (int) boardSizeSlider.getValue();
        AppPreferences.saveTileSize(newSize);

        // Сохраняем координаты
        AppPreferences.saveShowCoordinates(coordinatesCheck.isSelected());

        // Сохраняем переворот
        AppPreferences.saveBoardFlipped(flipBoardCheck.isSelected());

        // Сохраняем директорию
        String saveDir = saveDirectoryField.getText();
        if (saveDir != null && !saveDir.isEmpty()) {
            AppPreferences.saveSaveDirectory(saveDir);
        }

        // Сохраняем путь к движку
        String enginePath = enginePathField.getText();
        if (enginePath != null && !enginePath.isEmpty()) {
            AppPreferences.saveEnginePath(enginePath);
        }

        log.info("Preferences saved");

        // ========== ПОКАЗЫВАЕМ СООБЩЕНИЕ О ЯЗЫКЕ ЕСЛИ ОН ИЗМЕНИЛСЯ ==========
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (languageChanged) {
            alert.setTitle(lang.get(LANG_CHANGE_TITLE));
            alert.setHeaderText(String.format(lang.get(LANG_CHANGE_HEADER), languageDisplayName));
            alert.setContentText(lang.get(LANG_CHANGE_CONTENT));
            alert.getButtonTypes().setAll(new ButtonType(lang.get(LANG_CHANGE_BUTTON_OK)));
        } else {
            // Если язык не менялся - показываем стандартное сообщение о сохранении
            alert.setTitle(lang.get(NOTIFICATION_INFO));
            alert.setHeaderText(null);
            alert.setContentText(lang.get(PREFERENCES_SAVED));
        }
        alert.showAndWait();
    }

    private void resetPreferences() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lang.get(PREFERENCES_RESET_TITLE));
        confirm.setHeaderText(lang.get(PREFERENCES_RESET_HEADER));
        confirm.setContentText(lang.get(PREFERENCES_RESET_CONTENT));

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            AppPreferences.resetAllPreferences();

            // Обновляем UI
            List<Language> languages = LanguageManager.getInstance().getAvailableLanguages();
            if (!languages.isEmpty()) {
                languageCombo.setValue(languages.get(0).getDisplayName());
            }
            themeCombo.setValue(BoardTheme.THEMES[0].name());
            boardSizeSlider.setValue(BoardSizeController.DEFAULT_TILE_SIZE);
            coordinatesCheck.setSelected(true);
            flipBoardCheck.setSelected(false);
            saveDirectoryField.setText(AppPreferences.getBasesDirectory().toString());
            enginePathField.setText("");
            configPathLabel.setText(lang.get(PREFERENCES_CONFIG_PATH) + ": " + AppPreferences.getConfigFilePath());

            log.info("Preferences reset to defaults");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(lang.get(NOTIFICATION_INFO));
            alert.setHeaderText(null);
            alert.setContentText(lang.get(PREFERENCES_RESET_SUCCESS));
            alert.showAndWait();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lang.get(NOTIFICATION_ERROR));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}