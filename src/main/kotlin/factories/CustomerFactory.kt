package factories

import domain.entities.Customer
import domain.entities.Position

object CustomerFactory {
    fun fromCsvRow(row: List<String>): Customer {
        val id = row[0].toInt()
        val x = row[1].toDouble()
        val y = row[2].toDouble()

        val position = Position(x, y)
        return Customer(id, position)
    }
}
