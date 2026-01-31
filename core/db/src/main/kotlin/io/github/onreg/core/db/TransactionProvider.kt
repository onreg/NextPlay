package io.github.onreg.core.db

public interface TransactionProvider {
    public suspend fun <T> run(block: suspend () -> T): T
}
