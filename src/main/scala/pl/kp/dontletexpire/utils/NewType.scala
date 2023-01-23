package pl.kp.dontletexpire.utils

import caliban.schema.{ArgBuilder, Schema}
import cats.Monoid
import cats.syntax.either.*
import doobie.{Get, Meta, Put}
import io.circe.{Decoder, Encoder}

trait NewType[T] extends Typeclasses[T]:
  opaque type Type = T
  inline def apply(t: T): Type            = t
  extension (t: Type) inline def value: T = t
  given CanEqual[Type, Type]              = CanEqual.derived

trait Typeclasses[T]:
  self: NewType[T] =>
  // below given instances are needed for doobie, circe and caliban
  // doobie
  given (using m: Meta[T]): Meta[self.Type]            = m.asInstanceOf[Meta[self.Type]]
  given (using m: Meta[List[T]]): Meta[Set[self.Type]] = m.imap(_.toSet)(_.toList).asInstanceOf[Meta[Set[self.Type]]]

  // circe
  given (using e: Encoder[T]): Encoder[self.Type] = e.asInstanceOf[Encoder[self.Type]]
  given (using d: Decoder[T]): Decoder[self.Type] = d.asInstanceOf[Decoder[self.Type]]

  // caliban
  given (using a: ArgBuilder[T]): ArgBuilder[self.Type]   = a.asInstanceOf[ArgBuilder[self.Type]]
  given (using s: Schema[Any, T]): Schema[Any, self.Type] = s.asInstanceOf[Schema[Any, self.Type]]

end Typeclasses
