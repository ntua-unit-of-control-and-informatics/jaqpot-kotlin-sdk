package org.jaqpot

import org.jaqpot.client.ModelApiClient

fun main() {
    ModelApiClient("1", "2").predictSync(1, listOf(1, 2, 3))
}
