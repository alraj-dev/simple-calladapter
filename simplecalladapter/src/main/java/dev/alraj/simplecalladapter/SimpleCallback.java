package dev.alraj.simplecalladapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Callback to be given to [Simple.execute] and [Simple.enqueue]
 */
public interface SimpleCallback<R> {
    /**
     * Called function on success or failure, both data and exception in the same function
     * @param data [Response.body] after given [ResponseState], [State] conditional calls
     * @param exception Exception to be sent based on given condition
     * @param call the SimpleCall object of this request
     */
    void onResult(@Nullable R data, @Nullable Throwable exception, @NotNull SimpleCall<R> call);
}