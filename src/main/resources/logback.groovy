/* from https://stackoverflow.com/questions/2602415/rolling-logback-logs-on-filesize-and-time */
appender("access_logger", RollingFileAppender) {
    file = "logs/dntp-access.log"
    rollingPolicy(TimeBasedRollingPolicy) {
        // daily rollover
        fileNamePattern = "logs/dntp-access.%d{yyyy-MM-dd}.%i.log"
        timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
            maxFileSize = "50MB"
        }
        // maxHistory = 30 // store max 30 days
    }
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%level\t%msg%n"
    }
}

appender("authorisation_logger", RollingFileAppender) {
    file = "logs/dntp-authorisation.log"
    rollingPolicy(TimeBasedRollingPolicy) {
        // daily rollover
        fileNamePattern = "logs/dntp-authorisation.%d{yyyy-MM-dd}.%i.log"
        timeBasedFileNamingAndTriggeringPolicy(SizeAndTimeBasedFNATP) {
            maxFileSize = "50MB"
        }
        // maxHistory = 30 // store max 30 days
    }
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%level\t%msg%n"
    }
}

appender("stdout", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d [%thread] [%level] %logger{20} - %msg%n"
    }
    /* from https://stackoverflow.com/questions/5998419/how-do-i-use-logback-groovy-file-to-log-trace-level-to-file-and-info-to-console */
    filter(ch.qos.logback.classic.filter.ThresholdFilter) {
        level = INFO
    }
}

//logger("org.springframework.batch", INFO)
//logger("org.springframework.batch", DEBUG)
//logger("org.springframework.jdbc", DEBUG)
logger("org.springframework.beans", INFO)
logger("business.controllers", INFO)
logger("business.security", INFO)
logger("business.security.CustomLoggingInterceptor", TRACE, ["access_logger"])
logger("business.security.CustomPermissionService", TRACE, ["authorisation_logger"])
logger("business.services", INFO)
logger("business", INFO)
//logger("org.springframework.security.access", DEBUG)
root(WARN, ["stdout"])