package library.enrichment.core


data class BookData(
        val title: String? = null,
        val authors: List<String> = emptyList(),
        val numberOfPages: Int? = null
)