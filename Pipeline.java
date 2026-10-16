public class Pipeline {

    private final Source<String> source;
    private final Stage<String, LogRecord> stage;
    private final Sink<LogRecord> sink;

    public Pipeline(
            Source<String> source,
            Stage<String, LogRecord> stage,
            Sink<LogRecord> sink) {
        this.source = source;
        this.stage = stage;
        this.sink = sink;
    }

    public void run() {
        source.produce(line -> {
            try {
                stage.process(line, sink::consume);
            } catch (StageException e) {
                throw new RuntimeException("Pipeline hatasi", e);
            }
        });
    }
}