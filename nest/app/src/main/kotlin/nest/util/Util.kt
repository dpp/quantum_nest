package nest.util

import arrow.core.Option
import arrow.core.Some
import arrow.core.None


public object Helpers {
        public fun <A>tryIt(fn: () -> A): Box<A> {
                return try {
                        Full(fn())
                } catch (e: Exception) {
                        Failure(e.toString(), Some(e), None)
                }
        }
}

public sealed class Box<out T> {
        public fun <S>map(fn: (T) -> S) : Box<S> {
              return  when (this) {
                is Full -> Full(fn(value))
                is Empty -> Empty
                is Failure -> this 
                }
        }

        public fun <S>flatMap(fn: (T) -> Box<S>): Box<S> {
                              return  when (this) {
                is Full -> fn(value)
                is Empty -> Empty
                is Failure -> this
                }
        }

        public fun toOption(): Option<T> {
                return when (this) {
                        is Full -> Some(value)
                        else -> None
                }
        }

        public fun toList(): List<T> {
                return when(this) {
                        is Full -> listOf(value)
                        else -> listOf()
                }
        }
}

public object Empty : Box<Nothing>() {}

public data class Full<out T>(val value: T) : Box<T>() {}

public data class Failure(
                val msg: String,
                val stack: Option<Exception>,
                val chain: Option<Failure>
) : Box<Nothing>() {}
