package com.example.qrhunterapp_t11;

/**
 * Interface for implementing callback. The onComplete method for queries is asynchronous, so
 * this ensures that the work in onComplete is finished before continuing the program
 *
 * @author afra
 * @reference <a href="https://stackoverflow.com/a/52128102">Information on implementing callback</a>
 */
public interface Callback {
    void dataValid(boolean valid);
}
