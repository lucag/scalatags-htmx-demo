package com.rockthejvm.db

import com.rockthejvm.domain.config.AppConfig
import zio.ZLayer

case class DbConfig(jdbcUrl: String):
  val connectionInitSql = "PRAGMA foreign_keys = ON"

object DbConfig:
  val live: ZLayer[AppConfig, Nothing, DbConfig] =
    ZLayer.fromFunction[AppConfig => DbConfig] {
      appConfig => DbConfig(appConfig.db.url)
    }
