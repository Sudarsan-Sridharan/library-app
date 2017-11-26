package library.enrichment.core

interface Library {
    fun getIsbnOfBook(bookId: String): String // TODO value types for book ID and ISBN
    fun updateBook(bookId: String, data: BookUpdateData) // TODO value type for book ID
}