package org.jaqpot

import org.jaqpot.client.JaqpotApiClient

suspend fun main() {
    JaqpotApiClient("1", "2").predictSync(1, listOf(1, 2, 3))
}
