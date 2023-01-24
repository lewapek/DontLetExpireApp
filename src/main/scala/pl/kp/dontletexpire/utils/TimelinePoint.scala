package pl.kp.dontletexpire.utils

import caliban.CalibanError.ExecutionError
import caliban.Value.IntValue.LongNumber
import caliban.Value.StringValue
import caliban.schema.{ArgBuilder, Schema}
import cats.syntax.either.*
import com.sun.jdi.LongValue
import doobie.Meta
import doobie.postgres.implicits.*
import pl.kp.dontletexpire.errors.AppError
import pl.kp.dontletexpire.errors.AppError.ValidationError
import pl.kp.dontletexpire.utils.TimelinePoint.fullFormatter
import zio.UIO

import java.time.*
import java.time.format.DateTimeFormatter
import scala.annotation.targetName
import scala.concurrent.duration.*
import scala.util.Try

// accuracy used up to millis - may lose nanos
case class TimelinePoint private (instant: Instant):
  @targetName("isBefore")
  def <(that: TimelinePoint): Boolean = instant.isBefore(that.instant)
  @targetName("isAfter")
  def >(that: TimelinePoint): Boolean = instant.isAfter(that.instant)
  @targetName("isBeforeOrEqual")
  def <=(that: TimelinePoint): Boolean = !(>(that))
  @targetName("isAfterOrEqual")
  def >=(that: TimelinePoint): Boolean = !(<(that))

  @targetName("minus")
  def -(that: TimelinePoint): FiniteDuration = (epochMillis - that.epochMillis).millis

  def plusDuration(duration: FiniteDuration): TimelinePoint  = TimelinePoint(instant.plusNanos(duration.toNanos))
  def minusDuration(duration: FiniteDuration): TimelinePoint = TimelinePoint(instant.minusNanos(duration.toNanos))

  def epochMillis: Long = instant.toEpochMilli

  def show: String =
    fullFormatter.format(LocalDateTime.ofInstant(instant, ZoneOffset.UTC))
end TimelinePoint

object TimelinePoint:
  def apply(i: Instant): TimelinePoint             = new TimelinePoint(i)
  def fromEpochMillis(millis: Long): TimelinePoint = apply(Instant.ofEpochMilli(millis))

  val now: UIO[TimelinePoint] = zio.Clock.instant.map(TimelinePoint.apply)

  private val fullFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS")

  private val orderedFormatters = List(
    parseDateTime(fullFormatter),
    parseDateTime(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
    parseDateTime(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
    parseDate(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
  )

  private def parseDateTime(formatter: DateTimeFormatter)(string: String): Try[LocalDateTime] = Try(
    LocalDateTime.parse(string, formatter)
  )

  private def parseDate(formatter: DateTimeFormatter)(string: String): Try[LocalDateTime] = Try(
    LocalDate.parse(string, formatter).atTime(LocalTime.MIN)
  )

  private def tryAllFormatters(s: String): Either[ValidationError, LocalDateTime] =
    orderedFormatters.foldLeft(AppError.validation("No date formatter found").asLeft[LocalDateTime]) {
      case (value: Right[ValidationError, LocalDateTime], _) => value
      case (_, parse) =>
        parse(s).toEither
          .leftMap(throwable => AppError.validation(s"Couldn't parse date. Reason: $throwable"))
    }
  end tryAllFormatters

  def fromString(s: String): Either[ValidationError, TimelinePoint] =
    tryAllFormatters(s).map(localDateTime => TimelinePoint(Instant.from(localDateTime.atOffset(ZoneOffset.UTC))))

  given schema: Schema[Any, TimelinePoint] = Schema.stringSchema.contramap(_.show)
  given argBuilder: ArgBuilder[TimelinePoint] =
    case string: StringValue => fromString(string.value).leftMap(e => ExecutionError(e.msg))
    case long: LongNumber    => fromEpochMillis(long.value).asRight
    case other               => ExecutionError(s"Can't build LocalDate from $other").asLeft
  end argBuilder

  given Meta[TimelinePoint] = Meta[Instant].imap(TimelinePoint.apply)(_.instant)

  given Ordering[TimelinePoint] = Ordering[Instant].on(_.instant)
end TimelinePoint
