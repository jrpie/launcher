package de.jrpie.android.launcher.preferences.list

@Suppress("unused")
enum class AppNameFormat {
    DEFAULT {
        override fun format(input: String): String {
            return input
        }
    },
    UPPERCASE {
        override fun format(input: String): String {
            return input.uppercase()
        }
    },
    LOWERCASE {
        override fun format(input: String): String {
            return input.lowercase()
        }
    };

    abstract fun format(input: String): String
}