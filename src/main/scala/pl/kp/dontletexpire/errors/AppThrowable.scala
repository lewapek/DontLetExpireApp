package pl.kp.dontletexpire.errors

class AppThrowable(msg: String) extends Throwable:
  override def getMessage: String = msg
end AppThrowable

object AppThrowable:
  def create(msg: String, maybeCause: Option[Throwable] = None): AppThrowable =
    val appThrowable = new AppThrowable(msg)
    maybeCause.fold(ifEmpty = appThrowable) { cause =>
      appThrowable.initCause(cause)
      appThrowable
    }
  end create
end AppThrowable
