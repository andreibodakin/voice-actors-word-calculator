import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Парсер .docx-файла сценария для подсчёта количества слов по персонажам.
 * Предполагается структура:
 * 1. Строка с таймкодом: "цифра:цифра"
 * 2. Следующая строка — имя персонажа
 * 3. Следующая строка — реплика (считаем слова)
 */
public class DocxParse {

    public static final String DEFAULT_FILE_NAME = "Дьявольский судья 1.13.docx";
    public static final String INPUT_DIR = "files";

    // Слова/символы, которые не считаются при подсчёте
    private static final Set<String> EXCEPTION_WORDS = Set.of(
            "(з/к)", "(с/х)", "/", " ", ""
    );

    public static void main(String[] args) throws Exception {
        String fileName = args.length > 0 ? args[0] : DEFAULT_FILE_NAME;
        Path docxPath = Paths.get(INPUT_DIR, fileName);

        if (!Files.exists(docxPath)) {
            System.err.println("Файл не найден: " + docxPath.toAbsolutePath());
            return;
        }

        try {
            List<String> paragraphs = extractParagraphs(docxPath);
            Map<String, MovieCharacter> characterStats = parseScript(paragraphs);
            printResults(characterStats);
        } catch (Exception e) {
            System.err.println("Ошибка при обработке файла: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Извлекает все текстовые параграфы из document.xml внутри .docx-файла.
     */
    private static List<String> extractParagraphs(Path docxPath) throws Exception {
        try (FileSystem fs = FileSystems.newFileSystem(docxPath)) {
            Path documentXml = fs.getPath("/word/document.xml");
            try (XMLEventReader reader = XMLInputFactory.newInstance()
                    .createXMLEventReader(Files.newInputStream(documentXml))) {

                List<String> paragraphs = new ArrayList<>();
                StringBuilder currentParagraph = new StringBuilder();
                boolean inParagraph = false;

                while (reader.hasNext()) {
                    XMLEvent event = reader.nextEvent();

                    if (event.isStartElement()) {
                        StartElement startElement = event.asStartElement();
                        if ("p".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                            inParagraph = true;
                            currentParagraph.setLength(0); // очищаем
                        }
                    } else if (event.isCharacters() && inParagraph) {
                        currentParagraph.append(event.asCharacters().getData());
                    } else if (event.isEndElement()) {
                        EndElement endElement = event.asEndElement();
                        if ("p".equalsIgnoreCase(endElement.getName().getLocalPart())) {
                            inParagraph = false;
                            String text = currentParagraph.toString().trim();
                            if (!text.isEmpty()) {
                                paragraphs.add("<p>" + text + "</p>");
                            }
                        }
                    }
                }
                return paragraphs;
            }
        }
    }

    /**
     * Парсит список параграфов и возвращает статистику по персонажам.
     */
    private static Map<String, MovieCharacter> parseScript(List<String> paragraphs) {
        Map<String, MovieCharacter> characters = new HashMap<>();
        int phase = 1; // 1: ждём таймкод, 2: ждём имя, 3: считаем слова
        String currentCharacterName = null;

        for (String paragraph : paragraphs) {
            String cleanText = extractTextContent(paragraph);

            switch (phase) {
                case 1:
                    if (hasTimestampFormat(cleanText)) {
                        phase = 2;
                    }
                    break;

                case 2:
                    currentCharacterName = cleanText;
                    if (currentCharacterName != null && !currentCharacterName.isEmpty()) {
                        phase = 3;
                    } else {
                        phase = 1; // сброс, если имя пустое
                    }
                    break;

                case 3:
                    int wordCount = countWords(cleanText);
                    characters.computeIfAbsent(currentCharacterName, MovieCharacter::new)
                              .addWords(wordCount);
                    phase = 1; // возвращаемся к поиску таймкода
                    break;
            }
        }

        return characters;
    }

    /**
     * Удаляет теги <p> и </p> из строки.
     */
    private static String extractTextContent(String paragraphTag) {
        if (paragraphTag == null) return "";
        return paragraphTag.replaceAll("^<p>", "")
                           .replaceAll("</p>$", "")
                           .trim();
    }

    /**
     * Проверяет, соответствует ли строка формату "цифра:цифра".
     */
    private static boolean hasTimestampFormat(String text) {
        if (text == null || text.length() < 3) return false;
        int colonIndex = text.indexOf(':');
        if (colonIndex <= 0 || colonIndex >= text.length() - 1) return false;
        return Character.isDigit(text.charAt(colonIndex - 1)) &&
               Character.isDigit(text.charAt(colonIndex + 1));
    }

    /**
     * Подсчитывает количество слов в строке, исключая стоп-слова.
     */
    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;

        return Arrays.stream(text.trim().split("\\s+"))
                     .filter(word -> !EXCEPTION_WORDS.contains(word))
                     .collect(Collectors.toList())
                     .size();
    }

    /**
     * Выводит результаты в консоль.
     */
    private static void printResults(Map<String, MovieCharacter> stats) {
        if (stats.isEmpty()) {
            System.out.println("Ни один персонаж не найден.");
            return;
        }

        System.out.println("\n📊 Статистика по персонажам:");
        System.out.println("============================");
        stats.values().stream()
             .sorted((a, b) -> Integer.compare(b.getWords(), a.getWords())) // по убыванию
             .forEach(System.out::println);
    }
}

//Алгоритм
// первая строка - цифра + ":" + цифра
// вторая строка - имя
// третья строка - считаем слова по пробелам - исключения
