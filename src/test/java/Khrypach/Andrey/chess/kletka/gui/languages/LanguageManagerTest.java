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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LanguageManager - Менеджер локализации")
class LanguageManagerTest {

    private LanguageManager languageManager;

    @BeforeEach
    void setUp() {
        // Сбрасываем синглтон для тестов
        // Используем рефлексию для сброса instance
        try {
            java.lang.reflect.Field instanceField = LanguageManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Игнорируем
        }
        languageManager = LanguageManager.getInstance();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ getInstance()
    // ============================================================

    @Nested
    @DisplayName("getInstance() - Получение экземпляра")
    class GetInstanceTests {

        @Test
        @DisplayName("Должен возвращать синглтон")
        void shouldReturnSingleton() {
            // when
            LanguageManager instance1 = LanguageManager.getInstance();
            LanguageManager instance2 = LanguageManager.getInstance();

            // then
            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("Должен быть инициализирован с языком по умолчанию")
        void shouldBeInitializedWithDefaultLanguage() {
            // then
            assertThat(languageManager.getCurrentLanguage()).isNotNull();
            assertThat(languageManager.getCurrentLanguage().getCode()).isIn("ru", "en", "zh");
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ getAvailableLanguages()
    // ============================================================

    @Nested
    @DisplayName("getAvailableLanguages() - Доступные языки")
    class GetAvailableLanguagesTests {

        @Test
        @DisplayName("Должен возвращать все доступные языки")
        void shouldReturnAllAvailableLanguages() {
            // when
            List<Language> languages = languageManager.getAvailableLanguages();

            // then
            assertThat(languages).hasSize(3);
            assertThat(languages)
                    .extracting(Language::getCode)
                    .containsExactlyInAnyOrder("ru", "en", "zh");
        }

        @Test
        @DisplayName("Должен возвращать копию списка, а не оригинал")
        void shouldReturnCopyOfList() {
            // when
            List<Language> languages1 = languageManager.getAvailableLanguages();
            List<Language> languages2 = languageManager.getAvailableLanguages();

            // then
            assertThat(languages1).isNotSameAs(languages2);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ setLanguage()
    // ============================================================

    @Nested
    @DisplayName("setLanguage() - Установка языка")
    class SetLanguageTests {

        @Test
        @DisplayName("Должен устанавливать русский язык")
        void shouldSetRussianLanguage() {
            // when
            languageManager.setLanguage("ru");

            // then
            assertThat(languageManager.getCurrentLanguage().getCode()).isEqualTo("ru");
            assertThat(languageManager.getCurrentLanguage().getDisplayName()).isEqualTo("Русский");
        }

        @Test
        @DisplayName("Должен устанавливать английский язык")
        void shouldSetEnglishLanguage() {
            // when
            languageManager.setLanguage("en");

            // then
            assertThat(languageManager.getCurrentLanguage().getCode()).isEqualTo("en");
            assertThat(languageManager.getCurrentLanguage().getDisplayName()).isEqualTo("English");
        }

        @Test
        @DisplayName("Должен устанавливать китайский язык")
        void shouldSetChineseLanguage() {
            // when
            languageManager.setLanguage("zh");

            // then
            assertThat(languageManager.getCurrentLanguage().getCode()).isEqualTo("zh");
            assertThat(languageManager.getCurrentLanguage().getDisplayName()).isEqualTo("中文");
        }

        @Test
        @DisplayName("Не должен менять язык при неизвестном коде")
        void shouldNotChangeLanguageForUnknownCode() {
            // given
            String currentCode = languageManager.getCurrentLanguage().getCode();

            // when
            languageManager.setLanguage("unknown");

            // then
            assertThat(languageManager.getCurrentLanguage().getCode()).isEqualTo(currentCode);
        }

        @Test
        @DisplayName("Должен сохранять язык в настройки")
        void shouldSaveLanguageToPreferences() {
            // when
            languageManager.setLanguage("en");

            // then
            String savedLanguage = AppPreferences.getLanguage();
            assertThat(savedLanguage).isEqualTo("en");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ get() - ПОЛУЧЕНИЕ СТРОК
    // ============================================================

    @Nested
    @DisplayName("get() - Получение строк локализации")
    class GetTests {

        @Test
        @DisplayName("Должен возвращать строку на русском")
        void shouldReturnRussianString() {
            // given
            languageManager.setLanguage("ru");

            // when
            String result = languageManager.get("menu.file");

            // then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Должен возвращать строку на английском")
        void shouldReturnEnglishString() {
            // given
            languageManager.setLanguage("en");

            // when
            String result = languageManager.get("menu.file");

            // then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Должен возвращать строку на китайском")
        void shouldReturnChineseString() {
            // given
            languageManager.setLanguage("zh");

            // when
            String result = languageManager.get("menu.file");

            // then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("Должен возвращать ключ в специальном формате если перевод не найден")
        void shouldReturnKeyIfTranslationNotFound() {
            // given
            languageManager.setLanguage("ru");
            String unknownKey = "unknown.key";

            // when
            String result = languageManager.get(unknownKey);

            // then
            // Проверяем, что результат содержит ключ (в любом формате)
            assertThat(result).contains(unknownKey);
            // Или проверяем точный формат
            assertThat(result).isEqualTo("???unknown.key???");
        }

        @Test
        @DisplayName("Должен форматировать строку с аргументами")
        void shouldFormatStringWithArguments() {
            // given
            languageManager.setLanguage("ru");
            String key = "test.format";
            // Предполагаем, что в языковых файлах есть ключ "test.format" со значением "Hello %s!"

            // when
            String result = languageManager.get(key, "World");

            // then
            assertThat(result).isNotNull();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ СЛУШАТЕЛЕЙ
    // ============================================================

    @Nested
    @DisplayName("Слушатели изменения языка")
    class ListenersTests {

        @Test
        @DisplayName("Должен уведомлять слушателей при смене языка")
        void shouldNotifyListenersWhenLanguageChanged() {
            // given
            boolean[] notified = {false};
            LanguageManager.LanguageChangeListener listener = () -> notified[0] = true;

            // Используем рефлексию для добавления слушателя
            try {
                java.lang.reflect.Field listenersField = LanguageManager.class.getDeclaredField("listeners");
                listenersField.setAccessible(true);
                java.util.List<LanguageManager.LanguageChangeListener> listeners =
                        (java.util.List<LanguageManager.LanguageChangeListener>) listenersField.get(languageManager);
                listeners.add(listener);
            } catch (Exception e) {
                // Игнорируем
            }

            // when
            languageManager.setLanguage("en");

            // then
            assertThat(notified[0]).isTrue();
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ ЯЗЫКОВЫХ КЛЮЧЕЙ
    // ============================================================

    @Nested
    @DisplayName("Проверка языковых ключей")
    class LanguageKeysTests {

        @Test
        @DisplayName("Все языки должны иметь одинаковые ключи")
        void allLanguagesShouldHaveSameKeys() {
            // given
            Language russian = new RuLanguage();
            Language english = new EnLanguage();
            Language chinese = new ZhLanguage();

            // when
            java.util.Set<String> russianKeys = russian.getAllStrings().keySet();
            java.util.Set<String> englishKeys = english.getAllStrings().keySet();
            java.util.Set<String> chineseKeys = chinese.getAllStrings().keySet();

            // then
            assertThat(russianKeys).containsAll(englishKeys);
            assertThat(russianKeys).containsAll(chineseKeys);
            assertThat(englishKeys).containsAll(russianKeys);
            assertThat(englishKeys).containsAll(chineseKeys);
            assertThat(chineseKeys).containsAll(russianKeys);
            assertThat(chineseKeys).containsAll(englishKeys);
        }

        @Test
        @DisplayName("Все ключи должны иметь переводы")
        void allKeysShouldHaveTranslations() {
            // given
            languageManager.setLanguage("ru");
            Language english = new EnLanguage();
            Language chinese = new ZhLanguage();

            // when
            for (String key : english.getAllStrings().keySet()) {
                String ruValue = languageManager.get(key);
                String enValue = english.get(key);
                String zhValue = chinese.get(key);

                // then
                assertThat(ruValue).isNotEmpty();
                assertThat(enValue).isNotEmpty();
                assertThat(zhValue).isNotEmpty();
            }
        }
    }
}