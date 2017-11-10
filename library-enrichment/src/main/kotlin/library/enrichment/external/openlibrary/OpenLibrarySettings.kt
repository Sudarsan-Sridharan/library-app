package library.enrichment.external.openlibrary

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("openlibrary")
internal class OpenLibrarySettings {
    lateinit var url: String
}