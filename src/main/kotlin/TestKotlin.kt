package org.jaqpot

import org.jaqpot.client.ModelApiClient

fun main() {
    val dataset = ModelApiClient(System.getenv("JAQPOT_API_KEY"), System.getenv("JAQPOT_API_SECRET"))
        .predictSync(
            1908,
            listOf(
                mapOf("X1" to "1", "X2" to "2", "X3" to "3", "X4" to "4"),
            )
        )
    println(dataset)
}
