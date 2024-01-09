package org.example.database

import kotlinx.coroutines.sync.Mutex

class Entry {

  // ACID, also usage of mutex instead of @Synchronized to limit it in coroutine scope

  // Moved into entry to give finer granularity
  private val _mutex: Mutex = Mutex()
  val mutex: Mutex
    get() = _mutex

  // data, i.e. amount in banking
  private var _value: ULong = 0u

  var value: ULong
    get() = _value
    set(value) { _value = value }
}