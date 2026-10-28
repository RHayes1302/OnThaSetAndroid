package com.hayesenterprisesllc.onthaset.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    // Flexible offset formatter: accepts "+00", "+0000", "+00:00", "Z", optional fractions.
    private val flexible: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .optionalStart().appendLiteral('T').optionalEnd()
        .optionalStart().appendLiteral(' ').optionalEnd()
        .appendPattern("HH:mm")
        .optionalStart().appendPattern(":ss").optionalEnd()
        .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
        .optionalStart().appendOffset("+HH:MM", "Z").optionalEnd()
        .optionalStart().appendOffset("+HHMM", "Z").optionalEnd()
        .optionalStart().appendOffset("+HH", "Z").optionalEnd()
        .toFormatter(Locale.US)

    /** Mirrors the custom JSONDecoder date strategy in SupabaseManager.swift. */
    fun parseSupabaseDate(raw: String?): Instant? {
        if (raw.isNullOrBlank()) return null
        val s = raw.trim()
        runCatching { return OffsetDateTime.parse(s).toInstant() }
        runCatching { return Instant.parse(s) }
        runCatching {
            val parsed = flexible.parseBest(s, OffsetDateTime::from, LocalDateTime::from)
            return when (parsed) {
                is OffsetDateTime -> parsed.toInstant()
                is LocalDateTime -> parsed.toInstant(ZoneOffset.UTC)
                else -> null
            }
        }
        runCatching { return LocalDate.parse(s.take(10)).atStartOfDay(ZoneOffset.UTC).toInstant() }
        return null
    }

    /** ISO8601 with internet date-time (no fractional seconds), UTC. */
    fun iso(instant: Instant): String =
        DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(instant.truncatedTo(ChronoUnit.SECONDS).atOffset(ZoneOffset.UTC))

    /**
     * Cutoff date — events before this are considered expired.
     * Weekdays: 24 hours ago. Saturday/Sunday: pushed back to Friday 8am ET.
     */
    fun eventCutoff(now: Instant = Instant.now()): Instant {
        val et = ZoneId.of("America/New_York")
        val nowET = now.atZone(et)
        val todayAt8 = nowET.withHour(8).withMinute(0).withSecond(0).withNano(0)
        val base = if (nowET.isBefore(todayAt8)) todayAt8.minusDays(1) else todayAt8
        return when (now.atZone(zone).dayOfWeek) {
            DayOfWeek.SUNDAY -> base.minusDays(2).toInstant()
            DayOfWeek.SATURDAY -> base.minusDays(1).toInstant()
            else -> now.minus(24, ChronoUnit.HOURS)
        }
    }

    private fun fmt(pattern: String) = DateTimeFormatter.ofPattern(pattern, Locale.US)

    /** `.formatted(date: .abbreviated, time: .shortened)` → "Oct 28, 2026 at 8:30 AM" */
    fun abbreviatedWithTime(i: Instant): String = fmt("MMM d, yyyy 'at' h:mm a").format(i.atZone(zone))

    /** `.formatted(date: .long, time: .shortened)` → "October 28, 2026 at 8:30 AM" */
    fun longWithTime(i: Instant): String = fmt("MMMM d, yyyy 'at' h:mm a").format(i.atZone(zone))

    /** `.formatted(date: .abbreviated, time: .omitted)` → "Oct 28, 2026" */
    fun abbreviated(i: Instant): String = fmt("MMM d, yyyy").format(i.atZone(zone))

    /** DateFormatter .long date → "October 28, 2026" */
    fun longDate(i: Instant): String = fmt("MMMM d, yyyy").format(i.atZone(zone))

    /** `.dateStyle = .medium, .timeStyle = .short` */
    fun mediumWithTime(i: Instant): String = fmt("MMM d, yyyy, h:mm a").format(i.atZone(zone))

    fun time(i: Instant): String = fmt("h:mm a").format(i.atZone(zone))
    fun monthShort(i: Instant): String = fmt("MMM").format(i.atZone(zone)).uppercase()
    fun dayOfMonth(i: Instant): String = fmt("d").format(i.atZone(zone))
    fun weekdayShort(i: Instant): String = fmt("EEE").format(i.atZone(zone))
    fun monthYear(d: LocalDate): String = fmt("MMMM yyyy").format(d)

    fun localDate(i: Instant): LocalDate = i.atZone(zone).toLocalDate()

    fun isSameDay(a: Instant, b: LocalDate): Boolean = localDate(a) == b

    fun combine(date: LocalDate, hour: Int, minute: Int): Instant =
        ZonedDateTime.of(date, java.time.LocalTime.of(hour, minute), zone).toInstant()
}
