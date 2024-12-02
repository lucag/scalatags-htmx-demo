package com.rockthejvm.domain.errors

import zio.*
import zio.http.*

object ErrorMapper:
  import ServerExceptions.*

  extension [E <: Throwable, A](task: ZIO[Any, E, A])
    def defaultErrorsMappings: ZIO[Any, Response, A] =
      task.mapError {
        case AlreadyInUse(message)      => Response(
            status = Status.Conflict,
            body = Body.fromString(message)
          )
        case NotFound(message)          => Response(
            status = Status.NotFound,
            body = Body.fromString(message)
          )
        case BadRequest(message)        => Response(
            status = Status.BadRequest,
            body = Body.fromString(message)
          )
        case Unauthorized(message)      => Response(
            status = Status.Unauthorized,
            body = Body.fromString(message)
          )
        case DatabaseException(message) => Response(
            status = Status.InternalServerError,
            body = Body.fromString(message)
          )
        case _                          => Response(
            status = Status.InternalServerError
          )
      }
  end extension
end ErrorMapper

