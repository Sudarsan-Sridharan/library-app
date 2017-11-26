package library.enrichment.core

import library.enrichment.common.logging.logger
import library.enrichment.core.events.BookAddedEvent
import org.springframework.stereotype.Component

@Component
class BookEventProcessor(
        private val dataSources: List<BookDataSource>,
        private val library: Library
) {

    private val log = BookEventProcessor::class.logger()

    fun bookWasAdded(event: BookAddedEvent) {

        log.info("processing book added event: {}", event)

        val bookId = event.bookId

        val isbn = library.getIsbnOfBook(bookId) // TODO what happens if book is no longer there?
        val bookDataSets = dataSources.mapNotNull {
            log.debug("looking up book data using {}", it)
            it.getBookData(isbn)
        }

        if (bookDataSets.isNotEmpty()) {
            updateTitle(bookId, bookDataSets)
            updateAuthors(bookId, bookDataSets)
            updateNumberOfPages(bookId, bookDataSets)
        }

    }

    private fun updateTitle(bookId: String, dataSets: Iterable<BookData>) {
        dataSets.mapNotNull { it.title }
                .firstOrNull { it.isNotBlank() }
                ?.let { library.updateBookTitle(bookId, it) }
    }

    private fun updateAuthors(bookId: String, dataSets: Iterable<BookData>) {
        dataSets.map { it.authors }
                .firstOrNull { it.isNotEmpty() }
                ?.let { library.updateBookAuthors(bookId, it) }
    }

    private fun updateNumberOfPages(bookId: String, dataSets: Iterable<BookData>) {
        dataSets.mapNotNull { it.numberOfPages }
                .firstOrNull { it > 0 }
                ?.let { library.updateBookNumberOfPages(bookId, it) }
    }

}