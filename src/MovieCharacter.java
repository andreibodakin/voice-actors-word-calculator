import java.util.Objects;

/**
 * Модель персонажа из сценария.
 * Хранит имя и общее количество произнесённых слов.
 */
public class MovieCharacter {
    private String name;
    private int words;

    /**
     * Создаёт персонажа с именем и нулевым счётчиком слов.
     */
    public MovieCharacter(String name) {
        this.name = Objects.requireNonNull(name, "Имя персонажа не может быть null");
        this.words = 0;
    }

    /**
     * Добавляет указанное количество слов к счётчику персонажа.
     */
    public void addWords(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Количество слов не может быть отрицательным");
        }
        this.words += count;
    }

    // Геттеры

    public String getName() {
        return name;
    }

    public int getWords() {
        return words;
    }

    // equals & hashCode — только для сравнения по имени

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MovieCharacter that = (MovieCharacter) obj;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name
