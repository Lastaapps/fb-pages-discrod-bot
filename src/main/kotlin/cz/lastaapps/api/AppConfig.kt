package cz.lastaapps.api

data class AppConfig(
    val setupMode: Boolean,
    val facebook: Facebook,
    val discord: Discord,
    val server: Server,
    val databaseFileName: String,
    val adminToken: String,
    val intervalSec: Int,
) {
    data class Facebook(
        val appID: String,
        val configID: String,
        val appSecret: String,
        val redirectURL: String,
    )

    data class Discord(
        val token: String,
    )

    data class Server(
        val host: String,
        val port: Int,
        val endpointPublic: String,
        val endpointOAuth: String,
        val hostURL: String,
    )

    companion object {
        fun fromEnv() =
            AppConfig(
                setupMode = bool("SETUP_MODE"),
                facebook =
                    Facebook(
                        appID = str("FACEBOOK_APP_ID"),
                        configID = str("FACEBOOK_CONFIG_ID"),
                        appSecret = str("FACEBOOK_APP_SECRET"),
                        redirectURL = str("FACEBOOK_REDIRECT_URL"),
                    ),
                discord =
                    Discord(
                        token = str("DISCORD_BOT_TOKEN"),
                    ),
                server =
                    Server(
                        host = str("SERVER_HOST"),
                        port = int("SERVER_PORT"),
                        endpointPublic = str("SERVER_ENDPOINT_PUBLIC").withSlash(),
                        endpointOAuth = str("SERVER_ENDPOINT_OAUTH").withSlash(),
                        hostURL = str("SERVER_HOST_URL"),
                    ),
                databaseFileName = str("DATABASE_FILENAME"),
                adminToken = str("ADMIN_TOKEN"),
                intervalSec = int("INTERVAL_SEC"),
            )

        private fun key(key: String) = "FB_DC_API_$key"

        private fun str(key: String) = System.getenv(key(key)).also { check(it.isNotBlank()) { "The env var $key cannot be blank" } }

        private fun int(key: String) = str(key).toInt()

        private fun bool(key: String) = str(key).toBoolean()

        private fun String.withSlash() = if (this.startsWith("/")) this else "/$this"
    }
}
