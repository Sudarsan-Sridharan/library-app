package library.service.persistence.books

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "books")
data class BookDocument(
        @Id val id: UUID,
        val isbn: String,
        val title: String,
        val borrowed: BorrowedState?
)

data class BorrowedState(
        val by: String,
        val on: String
)