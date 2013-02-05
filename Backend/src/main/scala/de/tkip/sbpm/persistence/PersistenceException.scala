package de.tkip.sbpm.persistence

class PersistenceException(msg: String, inner: Throwable = null) extends Exception(msg, inner) {

}

class EntityNotFoundException(msg: String, msgArgs: Any*) extends PersistenceException(msg.format(msgArgs: _*)) {

}