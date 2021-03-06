package library.enrichment.external.openlibrary

import feign.Logger
import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("openlibrary")
class OpenLibrarySettings {
    lateinit var url: String
    lateinit var logLevel: Logger.Level
}