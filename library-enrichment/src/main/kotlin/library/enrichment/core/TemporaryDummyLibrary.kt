package library.enrichment.core

import org.springframework.stereotype.Component

@Component
class TemporaryDummyLibrary : Library {

    override fun getIsbnOfBook(bookId: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateBook(bookId: String, data: BookData) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}