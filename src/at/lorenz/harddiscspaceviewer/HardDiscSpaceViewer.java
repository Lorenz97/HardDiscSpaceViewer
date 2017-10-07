package at.lorenz.harddiscspaceviewer;

import at.lorenz.console.SimpleConsole;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HardDiscSpaceViewer {

    private final SimpleConsole console;
    @SuppressWarnings("CanBeFinal") //intellij error
    private File currentFile;

    private HardDiscSpaceViewer() {
        currentFile = new File("/");

        console = new SimpleConsole();

        console.registerCommand("help", args -> {
            write("List of commands:");
            write(" - i - current directory");
            write(" - ls - list all elements");
            write(" - cd - navigate");
            write(" - help - lists this help");
            write(" - size - size");
        });
        console.registerCommand("ls", args -> cmdList());
        console.registerCommand("i", args -> write("You are in '" + currentFile.getAbsolutePath() + "'"));
        console.registerCommand("cd", args -> {
            if (args.length == 0) {
                write("cd <directory>");
                return;
            }
            String path = argsToString(args);
            if (path.equals("..")) {
                File parentFile = currentFile.getParentFile();
                if (parentFile != null) {
                    currentFile = parentFile;
                    write("Index moved to '" + currentFile.getAbsolutePath() + "'");
                } else {
                    write("Cannot go into parent directory");
                }
                return;
            }
            File[] files = currentFile.listFiles();
            if (files == null) {
                write("files == null");
                return;
            }
            for (File file : files) {
                if (!file.isDirectory()) continue;
                if (file.getName().equalsIgnoreCase(path)) {
                    currentFile = file;
                    write("Index moved to '" + currentFile.getAbsolutePath() + "'");
                    return;
                }
            }
            write("Directory '" + path + "' not found inside of '" + currentFile.getAbsolutePath() + "'");
        });
        console.registerCommand("i", args -> write("You are in '" + currentFile.getAbsolutePath() + "'"));
        console.registerCommand("stop", args -> {
            write("Stopping...");
            console.stop();
        });
        console.registerCommand("size", args -> new SizeCounter(currentFile));
        console.registerCommand("explorer", args -> {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + currentFile.getAbsolutePath());
            } catch (IOException e) {
                throw new InternalError(e);
            }
        });
        console.registerCommand("update", args -> SizeCounter.size.clear());
        console.onNoCommand(label -> write("Command '" + label + "' not found"));
        console.start();
    }

    public static void main(String[] args) {
        new HardDiscSpaceViewer();
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static String getSize(double length) {
        DecimalFormat format = new DecimalFormat("0.#");
        int i = 900;
        if (length < i) {
            return format.format(length) + " B";
        }
        length /= 1024;
        if (length < i) {
            return format.format(length) + " KB";
        }
        length /= 1024;
        if (length < i) {
            return format.format(length) + " MB";
        }
        length /= 1024;
        if (length < i) {
            return format.format(length) + " GB";
        }
        return "gb=" + format.format(length);
    }

    private void cmdList() {
        File[] files = currentFile.listFiles();
        write("Content of '" + currentFile.getAbsolutePath() + "'");
        if (files == null) {
            write("files == null");
            return;
        }
        Map<String, Long> directoriesMap = new HashMap<>();
        Map<String, Long> filesMap = new HashMap<>();
        long amountFileSize = 0;
        for (File file : files) {
            String name = file.getName();
            if (file.isDirectory()) {
                if (SizeCounter.size.containsKey(file.getAbsolutePath())) {
                    long size = SizeCounter.size.get(file.getAbsolutePath());
                    directoriesMap.put(name, size);
                } else {
                    directoriesMap.put(name, -1L);
                }
            } else if (file.isFile()) {
                long length = file.length();
                filesMap.put(name, length);
                amountFileSize += length;
            }
        }

        write("Directories: (" + directoriesMap.size() + ")");
        sortByValue(directoriesMap).forEach((name, size1) -> {
            if (size1 == -1) {
                write("  " + name + " - ?");
            } else {
                write("  " + name + " - " + getSize(size1));
            }
        });
        String s = amountFileSize == 0 ? "empty" : getSize(amountFileSize);
        write("Files: (" + filesMap.size() + ") - " + s);
        sortByValue(filesMap).forEach((name, size1) -> write("  " + name + " - " + getSize(size1)));
        if (filesMap.isEmpty() && directoriesMap.isEmpty()) {
            SizeCounter.size.put(currentFile.getAbsolutePath(), 0L);
        }
    }

    private void write(String format) {
        System.out.println(format);
    }


    private String argsToString(String[] args) {
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg);
            builder.append(" ");
        }
        String string = builder.toString();
        return string.substring(0, string.length() - 1);
    }
}
