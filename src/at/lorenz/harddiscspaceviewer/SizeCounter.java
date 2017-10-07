package at.lorenz.harddiscspaceviewer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class SizeCounter {

    public static final Map<String, Long> size = new HashMap<>();

    private int amountFiles = 0;
    private int amountDirectories = 0;
    private long amountSize = 0;

    public SizeCounter(File file) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printStatus();
            }
        }, 300, 300);
        new Thread(() -> {
            long start = System.currentTimeMillis();
            count(file);
            timer.cancel();
            long stop = System.currentTimeMillis();
            printStatus();
            System.out.println("DONE. (" + new DecimalFormat("#,##0").format(stop - start) + "ms)");
        }).start();
    }

    private void printStatus() {
        System.out.println("");
        DecimalFormat format = new DecimalFormat("#,##0");
        System.out.println("files: " + format.format(amountFiles));
        System.out.println("directories: " + format.format(amountDirectories));
        System.out.println("size: " + HardDiscSpaceViewer.getSize(amountSize));
    }

    private long count(File file) {
        if (file.isFile()) {
            amountFiles++;
            long length = file.length();
            amountSize += length;
            return length;
        }
        if (file.isDirectory()) {
            amountDirectories++;
            File[] files = file.listFiles();
            if (files == null) {
//                System.out.println("no perms: " + file.getAbsolutePath());
                return 0;
            }
            long current = 0;
            for (File child : files) {
                current += count(child);
            }
            size.put(file.getAbsolutePath(), current);
            return current;
        }
        throw new InternalError("Neither file or directory: " + file.getAbsolutePath());
    }

//    public static void main(String[] args) {
//        new SizeCounter();
//    }

//    class SmartFile {
//
//        private final File file;
//
//        SmartFile(File file) {
//            this.file = file;
//        }
//
//        SmartFile(String name) {
//            this(new File(name));
//        }
//
//        SmartFile getChild(String name) {
//            File[] files = file.listFiles();
//            if (files == null) {
//                return null;
//            }
//            for (File file1 : files) {
//                if (file1.getName().equals(name)) {
//                    return new SmartFile(file1);
//                }
//            }
//            return null;
//        }
//
//        public String getPath() {
//            return file.getAbsolutePath();
//        }
//    }
}
