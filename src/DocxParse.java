
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import javax.xml.namespace.QName;

public class DocxParse {
    public static final String FILE_NAME = "Дьявольский судья 1.13.docx";

    public static void main(String args[]) throws Exception {

        Path currentRelativePath1 = Paths.get("");
        String strFile = currentRelativePath1.toAbsolutePath().toString() + "/files/" + FILE_NAME;

        Path source = Paths.get(strFile);

        FileSystem fs = FileSystems.newFileSystem(source);

        Path document = fs.getPath("/word/document.xml");

        XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(Files.newInputStream(document));

        StringBuffer content = new StringBuffer();

        String contentSearched = "the content we are searching for";

        boolean inParagraph = false;
        String paragraphText = "";
        while (reader.hasNext()) {
            XMLEvent event = (XMLEvent) reader.next();
            if (event.isStartElement()) {
                StartElement startElement = (StartElement) event;
                QName startElementName = startElement.getName();
                if (startElementName.getLocalPart().equalsIgnoreCase("p")) { //start element of paragraph
                    inParagraph = true;
                    content.append("<p>");
                    paragraphText = "";
                }
            } else if (event.isCharacters() && inParagraph) { //characters in elements of this paragraph
                String characters = event.asCharacters().getData();
                paragraphText += characters; // can be splitted into different run elements
            } else if (event.isEndElement() && inParagraph) {
                EndElement endElement = (EndElement) event;
                QName endElementName = endElement.getName();
                if (endElementName.getLocalPart().equalsIgnoreCase("p")) { //end element of paragraph
                    inParagraph = false;
                    content.append(paragraphText);
                    content.append("</p>\r\n");
                    //here you can check the paragraphText and exit the while if you found what you are searching for
                    if (paragraphText.contains(contentSearched)) break;
                }
            }
        }

        String tmp = content.toString();
        String[] tmpArr = tmp.split("\n");
        List<String> al = new ArrayList<String>();
        al = Arrays.asList(tmpArr);

        HashMap<String, Integer> actors = new HashMap<>();
        HashSet<String> exceptionWords = new HashSet<>();

        exceptionWords.add("(з/к)");
        exceptionWords.add("(с/х)");
        exceptionWords.add("<p>");
        exceptionWords.add("</p>");
        exceptionWords.add(" ");
        exceptionWords.add("/");

        int phase = 1;
        String tmpName = "";
        int wordsNumber = 0;
        String strTrim = "";

//        int counter = 0;
//        for (String s : al) {
//            System.out.println(s);
//            System.out.println(counter++);
//        }
//        counter = 0;

        for (String s : al) {
            if (phase == 1 && s.contains(":")) {
                int i = s.indexOf(":");
                if (Character.isDigit(s.charAt(i - 1)) && Character.isDigit(s.charAt(i + 1))) {
                    phase = 2;
                }
            } else if (phase == 2 && !s.contains(":")) {
                tmpName = s.substring(s.indexOf(">") + 1, s.lastIndexOf("<"));
                phase = 3;
            } else if (phase == 3) {
                strTrim = s.trim();
                wordsNumber = Stream.of(strTrim.substring(3, strTrim.length() - 4).replaceAll("\\s+", " ").split(" "))
                        .filter(str -> !exceptionWords.contains(str))
                        .collect(Collectors.toList()).size();

//                System.out.println("Stream: ");
//                Stream.of(strTrim.substring(3, strTrim.length() - 4).replaceAll("\\s+", " ").split(" "))
//                        .filter(str -> !exceptionWords.contains(str))
//                        .forEach(str -> System.out.println(str));

//                System.out.println(counter++);

                update(actors, tmpName, wordsNumber);
                phase = 1;
            }
        }
        actors.entrySet().forEach(entry -> {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        });

//        for (HashSet<String, Integer> ch : actors)
//            System.out.println(ch.getName() + "\t" + ch.getWords());

        fs.close();
    }

    static void update(HashMap<String, Integer> one, String name, int words) {

        if (one.containsKey(name)) {
            Integer tmp = one.get(name);
            one.put(name, tmp + words);
        } else {
            one.put(name, words);
        }
    }
}

//Алгоритм
// первая строка - цифра + ":" + цифра
// вторая строка - имя
// третья строка - считаем слова по пробелам - исключения

