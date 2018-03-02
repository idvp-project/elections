package com.idvp.elections.utils

/**
  * @author Oleg Zinoviev
  * @since 01.03.18.
  */
object Assert {
    def notNull[T <: AnyRef](value: T, parameter: String = null): T = {
        if (value == null) {
            if (parameter == null) {
                throw new IllegalStateException("Parameter value cannot be null")
            } else {
                throw new IllegalArgumentException(s"$parameter value cannot be null")
            }
        }

        value
    }

    def notEmpty[T <: CharSequence](value: T, parameter: String = null): T = {
        if (value == null || value.length() == 0) {
            if (parameter == null) {
                throw new IllegalStateException("Parameter value cannot be null")
            } else {
                throw new IllegalArgumentException(s"$parameter value cannot be null")
            }
        }

        value
    }
}
