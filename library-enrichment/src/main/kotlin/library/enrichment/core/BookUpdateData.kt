package library.enrichment.core


data class BookUpdateData(
        val title: String? = null,
        val authors: List<String>? = null,
        val numberOfPages: Int? = null
)