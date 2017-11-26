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
        val bookDataSets = dataSources.map { it.getBookData(isbn) }.filterNotNull()

        // TODO how to determine best available data?

        val bestTitle = bookDataSets
                .mapNotNull { it.title }
                .firstOrNull { it.isNotBlank() }
        val bestAuthors = bookDataSets
                .map { it.authors }
                .firstOrNull { it.isNotEmpty() }
        val bestNumberOfPages = bookDataSets
                .mapNotNull { it.numberOfPages }
                .firstOrNull { it > 0 }

        library.updateBook(bookId, BookUpdateData(
                title = bestTitle,
                authors = bestAuthors,
                numberOfPages = bestNumberOfPages
        ))
    }

}