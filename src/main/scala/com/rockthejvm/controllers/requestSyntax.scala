package com.rockthejvm.controllers

import com.rockthejvm.domain.errors.ServerExceptions
import zio.ZIO
import zio.http.Request
import zio.json.*

object requestSyntax:

  import ServerExceptions.*

  extension (request: Request)
    def to[T: JsonDecoder]: ZIO[Any, BadRequest, T] =
      for
        body   <- request.body.asString.mapError: err =>
                    BadRequest(s"Failure getting request body: ${ err.getMessage }")
        result <- ZIO.fromEither(body.fromJson[T]).mapError: err =>
                    BadRequest(s"Failure parsing json: $err")
      yield result
