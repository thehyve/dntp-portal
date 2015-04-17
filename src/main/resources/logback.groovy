appender("stdout", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%thread] [%level] %logger{20} - %msg%n"
    }
}

//logger("org.springframework.batch", INFO)
//logger("org.springframework.batch", DEBUG)
//logger("org.springframework.jdbc", DEBUG)
logger("org.springframework.beans", INFO)
root(WARN, ["stdout"])