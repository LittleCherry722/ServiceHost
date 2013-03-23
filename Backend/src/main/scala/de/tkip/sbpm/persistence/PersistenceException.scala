/*
 * S-BPM Groupware v1.2
 *
 * http://www.tk.informatik.tu-darmstadt.de/
 *
 * Copyright 2013 Telecooperation Group @ TU Darmstadt
 * Contact: Stephan.Borgert@cs.tu-darmstadt.de
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.tkip.sbpm.persistence

/**
 * Exception for errors occured on persistence layer.
 */
class PersistenceException(msg: String, inner: Throwable = null) extends Exception(msg, inner)

/**
 * Exception if a required entity was not found in the database, e.g. on update. 
 */
class EntityNotFoundException(msg: String, msgArgs: Any*) extends PersistenceException(msg.format(msgArgs: _*))