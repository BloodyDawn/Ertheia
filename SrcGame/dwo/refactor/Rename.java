package dwo.refactor;

import java.io.File;

public class Rename {
    public static void main(String[] args) {
        File dir = new File("/home/alf/Desktop/dist/game/data/stats/npc/data");

        File[] files = dir.listFiles();
        for (File f : files) {
            String file_name = f.getName();

            file_name = file_name.replaceAll("\\s", "");

            String[] parts = file_name.split("-");
            while (parts[0].startsWith("0")) {
                parts[0] = parts[0].substring(1);
            }

            while (parts[1].startsWith("0")) {
                parts[1] = parts[1].substring(1);
            }

            file_name = parts[0] + "-" + parts[1];
            f.renameTo(new File(dir, file_name));
        }
    }
}
