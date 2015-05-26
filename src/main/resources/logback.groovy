appender("stdout", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%thread] [%level] %logger{20} - %msg%n"
    }
}

//logger("org.springframework.batch", INFO)
//logger("org.springframework.batch", DEBUG)
//logger("org.springframework.jdbc", DEBUG)
logger("org.springframework.beans", INFO)
logger("business.controllers", INFO)
logger("business.security", INFO)
logger("business.services", INFO)
//logger("org.springframework.security.access", DEBUG)
root(WARN, ["stdout"])