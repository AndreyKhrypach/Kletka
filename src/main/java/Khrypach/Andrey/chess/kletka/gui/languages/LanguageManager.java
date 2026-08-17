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

import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Менеджер локализации - синглтон
 */
public class LanguageManager {

    private static final Logger log = LoggerFactory.getLogger(LanguageManager.class);

    private static LanguageManager instance;

    @Getter
    private Language currentLanguage;
    private final Map<String, Language> languages = new HashMap<>();
    private final List<LanguageChangeListener> listeners = new ArrayList<>();

    private LanguageManager() {
        registerLanguage(new RuLanguage());
        registerLanguage(new EnLanguage());
        registerLanguage(new ZhLanguage());

        // Загружаем язык из настроек
        String savedLanguage = AppPreferences.getLanguage();
        currentLanguage = languages.getOrDefault(savedLanguage, languages.get("ru"));

        log.debug("language manager initialized");
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    private void registerLanguage(Language language) {
        languages.put(language.getCode(), language);
    }

    public void setLanguage(String code) {
        Language newLang = languages.get(code);
        if (newLang != null && newLang != currentLanguage) {
            currentLanguage = newLang;
            AppPreferences.saveLanguage(code); // ← сохраняем
            notifyListeners(); // если есть
        }
    }

    public List<Language> getAvailableLanguages() {
        return new ArrayList<>(languages.values());
    }

    public String get(String key) {
        return currentLanguage.get(key);
    }

    public String get(String key, Object... args) {
        return String.format(currentLanguage.get(key), args);
    }

    private void notifyListeners() {
        for (LanguageChangeListener listener : listeners) {
            listener.onLanguageChanged();
        }
    }

    public interface LanguageChangeListener {
        void onLanguageChanged();
    }
}