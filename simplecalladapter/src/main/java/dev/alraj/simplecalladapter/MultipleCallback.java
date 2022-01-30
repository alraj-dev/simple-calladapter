package dev.alraj.simplecalladapter;

import java.util.List;

/**
 * Callback for MultipleCall
 */
public interface MultipleCallback<R> {
    /**
     * Result callback which returns the results of all the given [SimpleCall] calls to [MultipleCall]
     * Gives list of data, exception, and SimpleCalls of [SimpleCallback]. and the MultipleCall object used.
     */
    void onResult(List<R> data, List<Throwable> exceptions, List<SimpleCall<R>> calls, MultipleCall<R> call);
}