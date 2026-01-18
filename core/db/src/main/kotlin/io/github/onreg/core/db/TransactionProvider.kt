package io.github.onreg.core.db

public interface TransactionProvider {
    public fun <T> run(block: suspend () -> T): T
}
