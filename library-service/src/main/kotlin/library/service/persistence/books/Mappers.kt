package library.service.persistence.books

import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.states.Available
import library.service.business.books.domain.states.Borrowed
import library.service.business.books.domain.types.BookId
import library.service.business.books.domain.types.Borrower
import library.service.business.books.domain.types.Isbn13
import library.service.business.books.domain.types.Title
import library.service.common.Mapper
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Component
class BookToDocumentMapper : Mapper<Book, BookDocument> {

    override fun map(source: Book): BookDocument {
        return BookDocument(
                id = UUID.randomUUID(),
                isbn = "${source.isbn}",
                title = "${source.title}",
                borrowed = null
        )
    }

}

@Component
class BookRecordToDocumentMapper : Mapper<BookRecord, BookDocument> {

    override fun map(source: BookRecord): BookDocument {
        val bookState = source.state
        return BookDocument(
                id = source.id.toUuid(),
                isbn = "${source.book.isbn}",
                title = "${source.book.title}",
                borrowed = when (bookState) {
                    is Available -> null
                    is Borrowed -> BorrowedState(
                            by = "${bookState.by}",
                            on = "${bookState.on.withOffsetSameInstant(ZoneOffset.UTC)}"
                    )
                }
        )
    }

}

@Component
class BookDocumentToRecordMapper : Mapper<BookDocument, BookRecord> {

    override fun map(source: BookDocument): BookRecord {
        val borrowed = source.borrowed
        return BookRecord(
                id = BookId(source.id),
                book = Book(
                        isbn = Isbn13(source.isbn),
                        title = Title(source.title)
                ),
                initialState = when (borrowed) {
                    null -> Available
                    else -> Borrowed(
                            by = Borrower(borrowed.by),
                            on = OffsetDateTime.parse(borrowed.on)
                    )
                }
        )
    }

}