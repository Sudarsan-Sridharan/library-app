package library.service.api.books

import com.nhaarman.mockito_kotlin.given
import library.service.business.books.BookCollection
import library.service.business.books.domain.BookRecord
import library.service.business.books.domain.composites.Book
import library.service.business.books.domain.states.Borrowed
import library.service.business.books.domain.types.BookId
import library.service.business.books.domain.types.Borrower
import library.service.business.books.domain.types.Isbn13
import library.service.business.books.domain.types.Title
import library.service.business.books.exceptions.BookAlreadyBorrowedException
import library.service.business.books.exceptions.BookAlreadyReturnedException
import library.service.business.books.exceptions.BookNotFoundException
import library.service.common.correlation.CorrelationIdHolder
import library.service.security.UserContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import utils.classification.IntegrationTest
import utils.document
import java.time.OffsetDateTime


@IntegrationTest
@ExtendWith(SpringExtension::class)
@WebMvcTest(BooksController::class, secure = false)
@AutoConfigureRestDocs("build/generated-snippets/books")
internal class BooksControllerDocTest {

    @SpyBean lateinit var userContext: UserContext
    @SpyBean lateinit var correlationIdHolder: CorrelationIdHolder
    @SpyBean lateinit var bookResourceAssembler: BookResourceAssembler
    @MockBean lateinit var bookCollection: BookCollection

    @Autowired lateinit var mvc: MockMvc

    // POST on /api/books

    @Test fun `post book - created`() {
        val createdBook = availableBook()
        given { bookCollection.addBook(createdBook.book) }.willReturn(createdBook)

        val request = post("/api/books")
                .contentType("application/json")
                .content("""
                    {
                        "isbn": "${createdBook.book.isbn}",
                        "title": "${createdBook.book.title}"
                    }
                """)
        mvc.perform(request)
                .andExpect(status().isCreated)
                .andExpect(content().contentType("application/hal+json;charset=UTF-8"))
                .andDo(document("postBook-created"))
    }

    @Test fun `post book - bad request`() {
        val request = post("/api/books")
                .contentType("application/json")
                .content(""" {} """)
        mvc.perform(request)
                .andExpect(status().isBadRequest)
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andDo(document("error-example"))
    }

    // GET on /api/books

    @Test fun `getting all books - 0 books`() {
        given { bookCollection.getAllBooks() }.willReturn(emptyList())
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk)
                .andDo(document("getAllBooks-0Books"))
    }

    @Test fun `getting all books - 2 books`() {
        given { bookCollection.getAllBooks() }.willReturn(listOf(availableBook(), borrowedBook()))
        mvc.perform(get("/api/books"))
                .andExpect(status().isOk)
                .andDo(document("getAllBooks-2Books"))
    }

    // GET on /api/books/{id}

    @Test fun `getting book by ID - found available`() {
        val book = availableBook()
        given { bookCollection.getBook(book.id) }.willReturn(book)
        mvc.perform(get("/api/books/${book.id}"))
                .andExpect(status().isOk)
                .andDo(document("getBookById-foundAvailable"))
    }

    @Test fun `getting book by ID - found borrowed`() {
        val book = borrowedBook()
        given { bookCollection.getBook(book.id) }.willReturn(book)
        mvc.perform(get("/api/books/${book.id}"))
                .andExpect(status().isOk)
                .andDo(document("getBookById-foundBorrowed"))
    }

    @Test fun `getting book by ID - not found`() {
        val id = BookId.generate()
        given { bookCollection.getBook(id) }.willThrow(BookNotFoundException(id))
        mvc.perform(get("/api/books/$id"))
                .andExpect(status().isNotFound)
                .andDo(document("getBookById-notFound"))
    }

    // DELETE on /api/books/{id}

    @Test fun `deleting book by ID - found`() {
        val id = BookId.generate()
        mvc.perform(delete("/api/books/$id"))
                .andExpect(status().isNoContent)
                .andDo(document("deleteBookById-found"))
    }

    @Test fun `deleting book by ID - not found`() {
        val id = BookId.generate()
        given { bookCollection.removeBook(id) }.willThrow(BookNotFoundException(id))
        mvc.perform(delete("/api/books/$id"))
                .andExpect(status().isNotFound)
                .andDo(document("deleteBookById-notFound"))
    }

    // POST on /api/books/{id}/borrow

    @Test fun `borrowing book by ID - found available`() {
        val book = borrowedBook()
        val borrower = (book.state as Borrowed).by
        given { bookCollection.borrowBook(book.id, borrower) }.willReturn(book)

        val request = post("/api/books/${book.id}/borrow")
                .contentType("application/json")
                .content(""" { "borrower": "$borrower" } """)
        mvc.perform(request)
                .andExpect(status().isOk)
                .andDo(document("borrowBookById-foundAvailable"))
    }

    @Test fun `borrowing book by ID - found already borrowed`() {
        val id = BookId.generate()
        val borrower = borrower()
        given { bookCollection.borrowBook(id, borrower) }.willThrow(BookAlreadyBorrowedException(id))

        val request = post("/api/books/$id/borrow")
                .contentType("application/json")
                .content(""" { "borrower": "$borrower" } """)
        mvc.perform(request)
                .andExpect(status().isConflict)
                .andDo(document("borrowBookById-foundAlreadyBorrowed"))
    }

    @Test fun `borrowing book by ID - not found`() {
        val id = BookId.generate()
        val borrower = borrower()
        given { bookCollection.borrowBook(id, borrower) }.willThrow(BookNotFoundException(id))

        val request = post("/api/books/$id/borrow")
                .contentType("application/json")
                .content(""" { "borrower": "$borrower" } """)
        mvc.perform(request)
                .andExpect(status().isNotFound)
                .andDo(document("borrowBookById-notFound"))
    }

    // POST on /api/books/{id}/return

    @Test fun `returning book by ID - found borrowed`() {
        val book = availableBook()
        given { bookCollection.returnBook(book.id) }.willReturn(book)

        mvc.perform(post("/api/books/${book.id}/return"))
                .andExpect(status().isOk)
                .andDo(document("returnBookById-foundBorrowed"))
    }

    @Test fun `returning book by ID - found already borrowed`() {
        val id = BookId.generate()
        given { bookCollection.returnBook(id) }.willThrow(BookAlreadyReturnedException(id))

        mvc.perform(post("/api/books/$id/return"))
                .andExpect(status().isConflict)
                .andDo(document("returnBookById-foundAlreadyReturned"))
    }

    @Test fun `returning book by ID - not found`() {
        val id = BookId.generate()
        given { bookCollection.returnBook(id) }.willThrow(BookNotFoundException(id))

        mvc.perform(post("/api/books/$id/return"))
                .andExpect(status().isNotFound)
                .andDo(document("returnBookById-notFound"))
    }

    // utility methods

    private fun borrowedBook(id: BookId = BookId.generate()): BookRecord {
        val borrowedBy = borrower()
        val borrowedOn = OffsetDateTime.parse("2017-08-21T12:34:56.789Z")
        return availableBook(id).borrow(borrowedBy, borrowedOn)
    }

    private fun borrower() = Borrower("slu")

    private fun availableBook(id: BookId = BookId.generate()): BookRecord {
        val isbn = Isbn13("9780132350882")
        val title = Title("Clean Code: A Handbook of Agile Software Craftsmanship")
        val book = Book(isbn, title)
        return BookRecord(id, book)
    }

}