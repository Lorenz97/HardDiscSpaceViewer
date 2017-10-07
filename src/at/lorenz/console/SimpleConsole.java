package at.lorenz.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class SimpleConsole {
    private boolean running = false;
    private final Map<String, SimpleCommand> commands = new HashMap<>();
    private Consumer<String> noCommand = s -> {};

    public void start() {
        running = true;
        while (running) {
            InputStream inputStream = System.in;
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String line;
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                throw new InternalError(e);
            }
            String[] split = line.split(Pattern.quote(" "));
            String label = split[0];
            String[] args = new String[split.length - 1];
            System.arraycopy(split, 1, args, 0, args.length);
            catchCommand(label, args);
        }
    }

    private void catchCommand(String label, String[] args) {
        for (String name : commands.keySet()) {
            if (name.equalsIgnoreCase(label)) {
                SimpleCommand command = commands.get(name);
                command.execute(args);
                return;
            }
        }
        noCommand.accept(label);
    }

    public void stop() {
        running = false;
    }

    public void onNoCommand(Consumer<String> label) {
        noCommand = label;
    }

    public void registerCommand(String label, SimpleCommand command) {
        commands.put(label, command);
    }
}
