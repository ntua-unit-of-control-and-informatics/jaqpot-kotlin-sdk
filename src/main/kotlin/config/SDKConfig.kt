package org.jaqpot.config

object SDKConfig {
    @JvmStatic
    var host: String = "https://api.jaqpot.org"
        @Synchronized set
        @Synchronized get

    @JvmStatic
    var apiKey: String? = null
        @Synchronized set
        @Synchronized get

    @JvmStatic
    var apiSecret: String? = null
        @Synchronized set
        @Synchronized get
}
