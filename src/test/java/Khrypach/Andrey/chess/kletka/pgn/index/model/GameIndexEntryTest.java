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

package Khrypach.Andrey.chess.kletka.pgn.index.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GameIndexEntry - Запись в индексе PGN файла")
class GameIndexEntryTest {

    // ============================================================
    // 1. ТЕСТЫ КОНСТРУКТОРА И BUILDER
    // ============================================================

    @Nested
    @DisplayName("Конструктор и Builder")
    class ConstructorTests {

        @Test
        @DisplayName("Должен создавать запись через builder с полными данными")
        void shouldCreateFullEntryViaBuilder() {
            // given/when
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(42)
                    .offset(1024L)
                    .length(256)
                    .version(3)
                    .deleted(false)
                    .hash(123456789)
                    .white("Magnus Carlsen")
                    .black("Hikaru Nakamura")
                    .eco("B52")
                    .result("1-0")
                    .year("2024")
                    .event("World Championship")
                    .site("London")
                    .opening("Sicilian Defense")
                    .variation("Najdorf")
                    .plyCount(67)
                    .build();

            // then
            assertThat(entry.getId()).isEqualTo(42);
            assertThat(entry.getOffset()).isEqualTo(1024L);
            assertThat(entry.getLength()).isEqualTo(256);
            assertThat(entry.getVersion()).isEqualTo(3);
            assertThat(entry.isDeleted()).isFalse();
            assertThat(entry.isActive()).isTrue();
            assertThat(entry.getHash()).isEqualTo(123456789);
            assertThat(entry.getWhite()).isEqualTo("Magnus Carlsen");
            assertThat(entry.getBlack()).isEqualTo("Hikaru Nakamura");
            assertThat(entry.getEco()).isEqualTo("B52");
            assertThat(entry.getResult()).isEqualTo("1-0");
            assertThat(entry.getYear()).isEqualTo("2024");
            assertThat(entry.getEvent()).isEqualTo("World Championship");
            assertThat(entry.getSite()).isEqualTo("London");
            assertThat(entry.getOpening()).isEqualTo("Sicilian Defense");
            assertThat(entry.getVariation()).isEqualTo("Najdorf");
            assertThat(entry.getPlyCount()).isEqualTo(67);
        }

        @Test
        @DisplayName("Должен создавать запись через конструктор по умолчанию")
        void shouldCreateEntryViaDefaultConstructor() {
            // given/when
            GameIndexEntry entry = new GameIndexEntry();
            entry.setId(1);
            entry.setWhite("Player");
            entry.setBlack("Opponent");

            // then
            assertThat(entry.getId()).isEqualTo(1);
            assertThat(entry.getWhite()).isEqualTo("Player");
            assertThat(entry.getBlack()).isEqualTo("Opponent");
            assertThat(entry.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Должен создавать запись через all-args конструктор")
        void shouldCreateEntryViaAllArgsConstructor() {
            // given/when
            GameIndexEntry entry = new GameIndexEntry(
                    1,      // id
                    100L,   // offset
                    200,    // length
                    2,      // version
                    false,  // deleted
                    12345,  // hash
                    "White", "Black", "A00", "1-0", "2024",
                    "Event", "Site", "Opening", "Variation", 45
            );

            // then
            assertThat(entry.getId()).isEqualTo(1);
            assertThat(entry.getOffset()).isEqualTo(100L);
            assertThat(entry.getLength()).isEqualTo(200);
            assertThat(entry.getVersion()).isEqualTo(2);
            assertThat(entry.isDeleted()).isFalse();
            assertThat(entry.getWhite()).isEqualTo("White");
            assertThat(entry.getBlack()).isEqualTo("Black");
        }
    }

    // ============================================================
    // 2. ТЕСТЫ isActive()
    // ============================================================

    @Nested
    @DisplayName("isActive() - Проверка активности записи")
    class IsActiveTests {

        @Test
        @DisplayName("Должен возвращать true для активной записи")
        void shouldReturnTrueForActiveEntry() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .deleted(false)
                    .build();

            // then
            assertThat(entry.isActive()).isTrue();
        }

        @Test
        @DisplayName("Должен возвращать false для удаленной записи")
        void shouldReturnFalseForDeletedEntry() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .deleted(true)
                    .build();

            // then
            assertThat(entry.isActive()).isFalse();
        }
    }

    // ============================================================
    // 3. ТЕСТЫ markDeleted()
    // ============================================================

    @Nested
    @DisplayName("markDeleted() - Создание маркера удаления")
    class MarkDeletedTests {

        @Test
        @DisplayName("Должен создавать новую запись с deleted=true")
        void shouldCreateDeletedMarker() {
            // given
            GameIndexEntry original = GameIndexEntry.builder()
                    .id(1)
                    .offset(100L)
                    .length(200)
                    .version(5)
                    .deleted(false)
                    .hash(12345)
                    .white("White")
                    .black("Black")
                    .eco("A00")
                    .result("1-0")
                    .year("2024")
                    .event("Event")
                    .site("Site")
                    .opening("Opening")
                    .variation("Variation")
                    .plyCount(45)
                    .build();

            // when
            GameIndexEntry deleted = original.markDeleted();

            // then
            assertThat(deleted.getId()).isEqualTo(original.getId());
            assertThat(deleted.getOffset()).isEqualTo(original.getOffset());
            assertThat(deleted.getLength()).isEqualTo(original.getLength());
            assertThat(deleted.getVersion()).isEqualTo(original.getVersion() + 1);
            assertThat(deleted.isDeleted()).isTrue();
            assertThat(deleted.getHash()).isEqualTo(original.getHash());
            assertThat(deleted.getWhite()).isEqualTo(original.getWhite());
            assertThat(deleted.getBlack()).isEqualTo(original.getBlack());
            assertThat(deleted.getEco()).isEqualTo(original.getEco());
            assertThat(deleted.getResult()).isEqualTo(original.getResult());
            assertThat(deleted.getYear()).isEqualTo(original.getYear());
            assertThat(deleted.getEvent()).isEqualTo(original.getEvent());
            assertThat(deleted.getSite()).isEqualTo(original.getSite());
            assertThat(deleted.getOpening()).isEqualTo(original.getOpening());
            assertThat(deleted.getVariation()).isEqualTo(original.getVariation());
            assertThat(deleted.getPlyCount()).isEqualTo(original.getPlyCount());
        }

        @Test
        @DisplayName("Должен увеличивать версию при каждом удалении")
        void shouldIncrementVersionOnEachDelete() {
            // given
            GameIndexEntry original = GameIndexEntry.builder()
                    .id(1)
                    .version(1)
                    .deleted(false)
                    .build();

            // when
            GameIndexEntry deleted1 = original.markDeleted();
            GameIndexEntry deleted2 = deleted1.markDeleted();

            // then
            assertThat(deleted1.getVersion()).isEqualTo(2);
            assertThat(deleted2.getVersion()).isEqualTo(3);
        }
    }

    // ============================================================
    // 4. ТЕСТЫ equals() И hashCode()
    // ============================================================

    @Nested
    @DisplayName("equals() и hashCode()")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Должен считать записи равными по id")
        void shouldBeEqualById() {
            // given
            GameIndexEntry entry1 = GameIndexEntry.builder()
                    .id(1)
                    .white("Player A")
                    .black("Player B")
                    .build();

            GameIndexEntry entry2 = GameIndexEntry.builder()
                    .id(1)
                    .white("Player C")
                    .black("Player D")
                    .build();

            // then
            assertThat(entry1).isEqualTo(entry2);
            assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
        }

        @Test
        @DisplayName("Должен считать записи разными при разных id")
        void shouldBeDifferentWithDifferentId() {
            // given
            GameIndexEntry entry1 = GameIndexEntry.builder()
                    .id(1)
                    .white("Player A")
                    .build();

            GameIndexEntry entry2 = GameIndexEntry.builder()
                    .id(2)
                    .white("Player A")
                    .build();

            // then
            assertThat(entry1).isNotEqualTo(entry2);
        }

        @Test
        @DisplayName("Должен корректно сравнивать с null")
        void shouldHandleNullComparison() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .build();

            // then
            assertThat(entry).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Должен корректно сравнивать с другим типом")
        void shouldHandleDifferentTypeComparison() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .build();

            // then
            assertThat(entry).isNotEqualTo("not an entry");
        }

        @Test
        @DisplayName("Должен быть симметричным")
        void shouldBeSymmetric() {
            // given
            GameIndexEntry entry1 = GameIndexEntry.builder().id(1).build();
            GameIndexEntry entry2 = GameIndexEntry.builder().id(1).build();

            // then
            assertThat(entry1.equals(entry2)).isTrue();
            assertThat(entry2.equals(entry1)).isTrue();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ toString()
    // ============================================================

    @Nested
    @DisplayName("toString() - Строковое представление")
    class ToStringTests {

        @Test
        @DisplayName("Должен возвращать читаемое строковое представление")
        void shouldReturnReadableString() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(1024L)
                    .length(256)
                    .version(3)
                    .deleted(false)
                    .white("Carlsen")
                    .black("Nakamura")
                    .build();

            // when
            String str = entry.toString();

            // then
            assertThat(str).contains("id=1");
            assertThat(str).contains("offset=1024");
            assertThat(str).contains("length=256");
            assertThat(str).contains("version=3");
            assertThat(str).contains("deleted=false");
            assertThat(str).contains("Carlsen vs Nakamura");
        }

        @Test
        @DisplayName("Должен показывать deleted=true для удаленных записей")
        void shouldShowDeletedStatus() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .deleted(true)
                    .white("Player")
                    .black("Opponent")
                    .build();

            // when
            String str = entry.toString();

            // then
            assertThat(str).contains("deleted=true");
        }
    }

    // ============================================================
    // 6. ТЕСТЫ СЕРИАЛИЗАЦИИ
    // ============================================================

    @Nested
    @DisplayName("Сериализация")
    class SerializationTests {

        @Test
        @DisplayName("Должен быть Serializable")
        void shouldBeSerializable() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .white("Test")
                    .black("Serialization")
                    .build();

            // then
            assertThat(entry).isInstanceOf(java.io.Serializable.class);
        }

        @Test
        @DisplayName("Должен иметь serialVersionUID")
        void shouldHaveSerialVersionUID() throws Exception {
            // when - используем рефлексию для доступа к статическому полю
            java.lang.reflect.Field field = GameIndexEntry.class.getDeclaredField("serialVersionUID");
            field.setAccessible(true);
            long serialVersionUID = (Long) field.get(null);

            // then
            assertThat(serialVersionUID).isEqualTo(1L);
        }

        @Test
        @DisplayName("Должен корректно сериализоваться и десериализоваться")
        void shouldSerializeAndDeserialize() throws Exception {
            // given
            GameIndexEntry original = GameIndexEntry.builder()
                    .id(42)
                    .offset(1024L)
                    .length(256)
                    .version(3)
                    .deleted(false)
                    .hash(12345)
                    .white("Magnus Carlsen")
                    .black("Hikaru Nakamura")
                    .eco("B52")
                    .result("1-0")
                    .year("2024")
                    .event("World Championship")
                    .site("London")
                    .opening("Sicilian Defense")
                    .variation("Najdorf")
                    .plyCount(67)
                    .build();

            // when - сериализация
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos)) {
                oos.writeObject(original);
            }
            byte[] serializedData = baos.toByteArray();

            // when - десериализация
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(serializedData);
            GameIndexEntry deserialized;
            try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais)) {
                deserialized = (GameIndexEntry) ois.readObject();
            }

            // then
            assertThat(deserialized).isNotNull();
            assertThat(deserialized.getId()).isEqualTo(original.getId());
            assertThat(deserialized.getOffset()).isEqualTo(original.getOffset());
            assertThat(deserialized.getLength()).isEqualTo(original.getLength());
            assertThat(deserialized.getVersion()).isEqualTo(original.getVersion());
            assertThat(deserialized.isDeleted()).isEqualTo(original.isDeleted());
            assertThat(deserialized.getHash()).isEqualTo(original.getHash());
            assertThat(deserialized.getWhite()).isEqualTo(original.getWhite());
            assertThat(deserialized.getBlack()).isEqualTo(original.getBlack());
            assertThat(deserialized.getEco()).isEqualTo(original.getEco());
            assertThat(deserialized.getResult()).isEqualTo(original.getResult());
            assertThat(deserialized.getYear()).isEqualTo(original.getYear());
            assertThat(deserialized.getEvent()).isEqualTo(original.getEvent());
            assertThat(deserialized.getSite()).isEqualTo(original.getSite());
            assertThat(deserialized.getOpening()).isEqualTo(original.getOpening());
            assertThat(deserialized.getVariation()).isEqualTo(original.getVariation());
            assertThat(deserialized.getPlyCount()).isEqualTo(original.getPlyCount());
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ГЕТТЕРОВ И СЕТТЕРОВ (через Lombok)
    // ============================================================

    @Nested
    @DisplayName("Геттеры и сеттеры (Lombok)")
    class GetterSetterTests {

        @Test
        @DisplayName("Должен корректно устанавливать и получать все поля")
        void shouldSetAndGetAllFields() {
            // given
            GameIndexEntry entry = createEntry();

            // then
            assertThat(entry.getId()).isEqualTo(999);
            assertThat(entry.getOffset()).isEqualTo(8888L);
            assertThat(entry.getLength()).isEqualTo(777);
            assertThat(entry.getVersion()).isEqualTo(6);
            assertThat(entry.isDeleted()).isTrue();
            assertThat(entry.getHash()).isEqualTo(55555);
            assertThat(entry.getWhite()).isEqualTo("White Player");
            assertThat(entry.getBlack()).isEqualTo("Black Player");
            assertThat(entry.getEco()).isEqualTo("E20");
            assertThat(entry.getResult()).isEqualTo("1/2-1/2");
            assertThat(entry.getYear()).isEqualTo("2023");
            assertThat(entry.getEvent()).isEqualTo("Tournament");
            assertThat(entry.getSite()).isEqualTo("Venue");
            assertThat(entry.getOpening()).isEqualTo("Opening");
            assertThat(entry.getVariation()).isEqualTo("Variation");
            assertThat(entry.getPlyCount()).isEqualTo(50);
        }
    }

    private static GameIndexEntry createEntry() {
        GameIndexEntry entry = new GameIndexEntry();

        // when
        entry.setId(999);
        entry.setOffset(8888L);
        entry.setLength(777);
        entry.setVersion(6);
        entry.setDeleted(true);
        entry.setHash(55555);
        entry.setWhite("White Player");
        entry.setBlack("Black Player");
        entry.setEco("E20");
        entry.setResult("1/2-1/2");
        entry.setYear("2023");
        entry.setEvent("Tournament");
        entry.setSite("Venue");
        entry.setOpening("Opening");
        entry.setVariation("Variation");
        entry.setPlyCount(50);
        return entry;
    }

    // ============================================================
    // 8. ТЕСТЫ ГРАНИЧНЫХ СЛУЧАЕВ
    // ============================================================

    @Nested
    @DisplayName("Граничные случаи")
    class EdgeCasesTests {

        @Test
        @DisplayName("Должен обрабатывать отрицательные значения")
        void shouldHandleNegativeValues() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(-1)
                    .offset(-100L)
                    .length(-50)
                    .version(-1)
                    .build();

            // then
            assertThat(entry.getId()).isEqualTo(-1);
            assertThat(entry.getOffset()).isEqualTo(-100L);
            assertThat(entry.getLength()).isEqualTo(-50);
            assertThat(entry.getVersion()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Должен обрабатывать null строки")
        void shouldHandleNullStrings() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .white(null)
                    .black(null)
                    .eco(null)
                    .result(null)
                    .build();

            // then
            assertThat(entry.getWhite()).isNull();
            assertThat(entry.getBlack()).isNull();
            assertThat(entry.getEco()).isNull();
            assertThat(entry.getResult()).isNull();
        }

        @Test
        @DisplayName("Должен обрабатывать пустые строки")
        void shouldHandleEmptyStrings() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .white("")
                    .black("")
                    .build();

            // then
            assertThat(entry.getWhite()).isEmpty();
            assertThat(entry.getBlack()).isEmpty();
        }

        @Test
        @DisplayName("Должен обрабатывать большие значения")
        void shouldHandleLargeValues() {
            // given
            long largeOffset = Long.MAX_VALUE;
            int largeLength = Integer.MAX_VALUE;
            int largeVersion = Integer.MAX_VALUE;
            int largeHash = Integer.MAX_VALUE;

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(Integer.MAX_VALUE)
                    .offset(largeOffset)
                    .length(largeLength)
                    .version(largeVersion)
                    .hash(largeHash)
                    .plyCount(Integer.MAX_VALUE)
                    .build();

            // then
            assertThat(entry.getId()).isEqualTo(Integer.MAX_VALUE);
            assertThat(entry.getOffset()).isEqualTo(Long.MAX_VALUE);
            assertThat(entry.getLength()).isEqualTo(Integer.MAX_VALUE);
            assertThat(entry.getVersion()).isEqualTo(Integer.MAX_VALUE);
            assertThat(entry.getHash()).isEqualTo(Integer.MAX_VALUE);
            assertThat(entry.getPlyCount()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    // ============================================================
    // 9. ТЕСТЫ ДЛЯ РАБОТЫ В КОЛЛЕКЦИЯХ
    // ============================================================

    @Nested
    @DisplayName("Работа в коллекциях")
    class CollectionTests {

        @Test
        @DisplayName("Должен корректно работать в HashSet")
        void shouldWorkInHashSet() {
            // given
            GameIndexEntry entry1 = GameIndexEntry.builder().id(1).build();
            GameIndexEntry entry2 = GameIndexEntry.builder().id(1).build(); // тот же id
            GameIndexEntry entry3 = GameIndexEntry.builder().id(2).build();

            // when
            java.util.HashSet<GameIndexEntry> set = new java.util.HashSet<>();
            set.add(entry1);
            set.add(entry2);
            set.add(entry3);

            // then
            assertThat(set).hasSize(2);
            assertThat(set).contains(entry1);
            assertThat(set).contains(entry3);
        }

        @Test
        @DisplayName("Должен корректно работать в HashMap")
        void shouldWorkInHashMap() {
            // given
            GameIndexEntry entry1 = GameIndexEntry.builder().id(1).build();
            GameIndexEntry entry2 = GameIndexEntry.builder().id(1).build(); // тот же id

            // when
            java.util.Map<GameIndexEntry, String> map = new java.util.HashMap<>();
            map.put(entry1, "value1");
            map.put(entry2, "value2");

            // then
            assertThat(map).hasSize(1);
            assertThat(map.get(entry1)).isEqualTo("value2");
        }

        @Test
        @DisplayName("Должен поддерживать сортировку по id")
        void shouldSupportSortingById() {
            // given
            GameIndexEntry entry1 = GameIndexEntry.builder().id(3).build();
            GameIndexEntry entry2 = GameIndexEntry.builder().id(1).build();
            GameIndexEntry entry3 = GameIndexEntry.builder().id(2).build();

            java.util.List<GameIndexEntry> list = new java.util.ArrayList<>();
            list.add(entry1);
            list.add(entry2);
            list.add(entry3);

            // when
            list.sort(java.util.Comparator.comparingInt(GameIndexEntry::getId));

            // then
            assertThat(list).containsExactly(entry2, entry3, entry1);
        }
    }
}