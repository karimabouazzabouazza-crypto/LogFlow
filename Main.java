
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Kullanim: java Main <log-dosyasi>");
            return;
        }

        Source<String> source =
                new FileLineSource(Path.of(args[0]));

        Stage<String, LogRecord> stage =
                new ParserStage();

        Sink<LogRecord> sink =
                new Sink<LogRecord>() {
                    @Override
                    public void consume(LogRecord record) {
                        System.out.println(record);
                    }
                };

        Pipeline pipeline =
                new Pipeline(source, stage, sink);

        pipeline.run();
    }
}
