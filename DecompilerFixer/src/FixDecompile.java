import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Eugene on 20.12.2014.
 */
public class FixDecompile {
    private static final Pattern PATTERN = Pattern.compile("private static final \\w+\\s([\\u0410-\\u044fA-Za-z0-9_]+)\\s=\\s(.{4,100})?;");

    public static void main(String[] args) throws Exception {
        File f = new File("E:\\java-projects\\L2WT\\game\\src\\l2next\\scripts\\");
        processFile(f);

    }

    private static void processFile(File file) throws Exception {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files)
                processFile(f);
        } else {
            fixFile(file);
//            fixFileStrings(file);
        }
    }

    private static void fixFile(File file) throws Exception {
        Path path = file.toPath();
        String s = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));

        Map<String, String> map = new HashMap<>();
        Matcher matcher = PATTERN.matcher(s);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }

        StringBuilder sb = new StringBuilder();
        LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));

        String line;
        while ((line = lineNumberReader.readLine()) != null) {
            if (line.contains("static") || line.contains("import")) {
                sb.append(line).append('\n');
            } else {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    line = line.replace(" " + entry.getValue() + " ", " " + entry.getKey() + " ");
                    line = line.replace(" " + entry.getValue() + ",", " " + entry.getKey() + ",");
                    line = line.replace("(" + entry.getValue() + ")", "(" + entry.getKey() + ")");
                }
                sb.append(line).append('\n');
            }
        }

        lineNumberReader.close();
        if (map.size() > 0) {
            System.out.println("Fix file: " + path + ". Fixed " + map.size() + " patterns.");
            Files.delete(path);
            Files.write(path, sb.toString().getBytes());
        }
    }

    private static void fixFileStrings(File file) throws Exception {
        Path path = file.toPath();
        LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));

        StringBuilder fileBuilder = new StringBuilder();

        String line;
        while ((line = lineNumberReader.readLine()) != null) {
            if (line.contains("\\u")) {
                char[] chars = line.toCharArray();
                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] == '\\' && chars[i + 1] == 'u') {
                        char u1 = chars[i + 2];
                        char u2 = chars[i + 3];
                        char u3 = chars[i + 4];
                        char u4 = chars[i + 5];

                        int ui = Integer.parseInt(new String(new char[]{u1, u2, u3, u4}), 16);
                        char c = (char) ui;
                        sb.append(c);
                        i += 5;
                    } else
                        sb.append(chars[i]);
                }
                fileBuilder.append(sb).append('\n');
            } else
                fileBuilder.append(line).append('\n');
        }
        lineNumberReader.close();

        Files.delete(path);
        Files.write(path, fileBuilder.toString().getBytes(Charset.forName("UTF-8")));
    }
}
