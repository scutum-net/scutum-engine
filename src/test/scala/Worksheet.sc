import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

val d1 = LocalDateTime.of(2017, 1, 1, 0, 0, 0)
val d2 = LocalDateTime.now()


// 0-10, 11-21, 22-63

val ms = d1.until(d2, ChronoUnit.NANOS) / 1000000

val id = ms << 22 | 2049 | 3

id >> 22

d1.plusNanos(ms * 1000000)