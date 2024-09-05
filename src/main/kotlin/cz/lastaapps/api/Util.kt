package cz.lastaapps.api

import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

// 2024-09-03T17:00:00+0300
private val facebookTimestampParser =
    DateTimeComponents.Format {
        year()
        char('-')
        monthNumber(padding = Padding.ZERO)
        char('-')
        dayOfMonth(padding = Padding.ZERO)
        char('T')
        hour(padding = Padding.ZERO)
        char(':')
        minute(padding = Padding.ZERO)
        char(':')
        second(padding = Padding.ZERO)
        offsetHours(padding = Padding.ZERO)
        offsetMinutesOfHour(padding = Padding.ZERO)
    }

// yes, I don't like this either, but I'm to lazy to do it properly
fun String.createdTimeToInstant() = Instant.parse(this, facebookTimestampParser)

fun String.idToFacebookURL() = "https://www.facebook.com/$this"

fun isFBLink(link: String) =
    link.startsWith("https://l.facebook.com") or
        link.startsWith("https://lm.facebook.com")

fun Instant.formatDateTime(timeZone: TimeZone) =
    this
        .toLocalDateTime(timeZone)
        .format(
            LocalDateTime.Format {
                dayOfMonth(padding = Padding.NONE)
                char('.')
                char(' ')
                monthNumber(padding = Padding.NONE)
                char('.')
                char(' ')
                hour(padding = Padding.NONE)
                char(':')
                minute(padding = Padding.ZERO)
            },
        )

// inspired by DefaultFormatter
object TimeStampFormatter : MessageStringFormatter {
    override fun formatMessage(
        severity: Severity?,
        tag: Tag?,
        message: Message,
    ): String {
        val sb = StringBuilder()

        sb.append('[')
        sb.append(Instant.fromEpochSeconds(Clock.System.now().epochSeconds))
        sb.append("]: ")

        if (severity != null) sb.append(formatSeverity(severity)).append(" ")
        if (tag != null && tag.tag.isNotEmpty()) sb.append(formatTag(tag)).append(" ")
        sb.append(message.message)

        return sb.toString()
    }
}
