package org.jaqpot

import org.jaqpot.client.JaqpotApiClient

fun main() {
    JaqpotApiClient("1", "2").predictSync(1, listOf(1, 2, 3))
}
