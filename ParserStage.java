
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserStage implements Stage<String, LogRecord> {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd/MMM/yyyy:HH:mm:ss Z",
                    Locale.ENGLISH
            );

    private static final Pattern CLF_PATTERN = Pattern.compile(
            "^(\\S+)\\s+\\S+\\s+\\S+\\s+\\[([^]]+)]\\s+"
                    + "\"(\\S+)\\s+(.+?)\\s+HTTP/\\d\\.\\d\"\\s+"
                    + "(\\d{3})\\s+(\\d+|-)\\s*"
                    + "(?:\"([^\"]*)\"\\s+\"([^\"]*)\")?\\s*$"
    );

    private long skippedCount = 0;

    public long getSkippedCount() {
        return skippedCount;
    }

    @Override
    public void process(String input, Emitter<LogRecord> out)
            throws StageException {

        Matcher matcher = CLF_PATTERN.matcher(input);

        if (!matcher.matches()) {
            skippedCount++;
            return;
        }

        try {
            Instant timestamp = TIMESTAMP_FORMAT.parse(
                    matcher.group(2),
                    Instant::from
            );

            String clientIp = matcher.group(1);
            String method = matcher.group(3);
            String path = matcher.group(4);
            int status = Integer.parseInt(matcher.group(5));

            String bytesText = matcher.group(6);
            long bytes = bytesText.equals("-")
                    ? 0L
                    : Long.parseLong(bytesText);

            String userAgent = matcher.group(8);
            if (userAgent == null) {
                userAgent = "";
            }

            LogRecord record = new LogRecord(
                    timestamp,
                    clientIp,
                    method,
                    path,
                    status,
                    bytes,
                    userAgent,
                    Map.of(),
                    input
            );

            out.emit(record);

        } catch (DateTimeParseException | NumberFormatException e) {
            skippedCount++;
        }
    }
}
