package dev.alraj.simplecalladapter

/**
 * Some conditional states of response, to give corresponding Exception and null for response data.
 */
enum class DefaultConditions {
    /**
     * when response body is null, gives [NullDataException]
     */
    NULL_RESPONSE,

    /**
     * when response is a collection or array, and is empty gives, [EmptyListException]
     */
    EMPTY_LIST
}