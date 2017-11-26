package library.enrichment.core

interface Library {

    fun getIsbnOfBook(bookId: String): String // TODO value types for book ID and ISBN

    fun updateBookTitle(bookId: String, title: String) // TODO value type for book ID
    fun updateBookAuthors(bookId: String, authors: List<String>) // TODO value type for book ID
    fun updateBookNumberOfPages(bookId: String, numberOfPages: Int) // TODO value type for book ID

}