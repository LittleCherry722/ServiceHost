package de.tkip.sbpm.application.miscellaneous

import com.typesafe.config.Config

object SystemProperties {

  def akkaRemotePort(implicit config: Config) = {
    config.getInt("akka.remote.netty.tcp.port")
  }

  def akkaRemoteHostname(implicit config: Config) = {
    config.getString("akka.remote.netty.tcp.hostname")
  }

  def akkaRemoteUrl(implicit config: Config) = {
    "@" + akkaRemoteHostname + ":" + akkaRemotePort
  }

  def sbpmPort(implicit config: Config) = {
    config.getInt("sbpm.rest.port")
  }

  def sbpmHostname(implicit config: Config) = {
    config.getString("sbpm.rest.hostname")
  }
}
