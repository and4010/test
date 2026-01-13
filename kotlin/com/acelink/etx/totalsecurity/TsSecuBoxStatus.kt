package com.acelink.etx.totalsecurity

enum class TsSecuBoxStatus private constructor(val value: Int) {
    UNKNOWN(-1),
    SUCCESS(0),
    FORMAT_ERROR(1),
    INVALID_COMMAND(2),
    INVALID_PASSWORD(3),
    CONNECTION_FAILED(21),
    OPTION_ERROR(22),
    DESCRIBE_ERROR(23),
    UNAUTHORIZED_ERROR(24),
    SETUP_ERROR(25),
    PLAY_ERROR(26),
    PROFILE_NOT_MATCH(27),
    EMPTY(28),
    DEVICE_INTERNAL_ERROR(29);

    override fun toString(): String {
        when (this) {
            UNKNOWN -> return "UNKNOWN"
            SUCCESS -> return "SUCCESS"
            FORMAT_ERROR -> return "FORMAT ERROR"
            INVALID_COMMAND -> return "INVALID COMMAND"
            INVALID_PASSWORD -> return "INVALID PASSWORD"
            CONNECTION_FAILED -> return "CONNECTION FAILED"
            OPTION_ERROR -> return "OPTION ERROR"
            DESCRIBE_ERROR -> return "DESCRIBE ERROR"
            UNAUTHORIZED_ERROR -> return "UNAUTHORIZED ERROR"
            SETUP_ERROR -> return "SETUP ERROR"
            PLAY_ERROR -> return "PLAY ERROR"
            PROFILE_NOT_MATCH -> return "PROFILE NOT MATCH"
            EMPTY -> return "EMPTY"
            DEVICE_INTERNAL_ERROR -> return "DEVICE INTERNAL ERROR"
            else -> return "UNKNOWN"
        }
    }

    companion object {

        fun fromValue(value: Int): TsSecuBoxStatus {
            val var1 = values()
            val var2 = var1.size

            for (var3 in 0 until var2) {
                val status = var1[var3]
                if (status.value == value) {
                    return status
                }
            }

            return UNKNOWN
        }
    }
}