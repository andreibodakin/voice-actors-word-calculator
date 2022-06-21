import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Parser {
    public static void main(String[] args) throws IOException {

        System.out.println(args[0].toString());
        System.out.println(args[1].toString());

        System.out.println("hello Java");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        Path currentRelativePath = Paths.get("");
        String str = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current absolute path is: " + str);

        System.out.println(str + "/src/" + args[0].toString());

        List<String> lines = Files.readAllLines(Paths.get(str + "/src/" + args[0].toString()), UTF_8);

        for (String s : lines) {
            System.out.println(s);
        }





//        try (Stream<Path> paths = Files.walk(Paths.get(str))) {
//            paths
//                    .filter(Files::isRegularFile)
//                    .forEach(System.out::println);
//        }



    }
}
