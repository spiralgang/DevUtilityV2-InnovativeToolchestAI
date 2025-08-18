package com.sgneuronlabs.devutilityandroidv2.ai

class AITrainingSetManager {
    private val trainingSets = mutableListOf<TrainingSet>()

    /**
     * Adds a new training set with a system prompt and expected response.
     * @param prompt The system prompt.
     * @param expectedResponse The expected AI response.
     */
    fun addTrainingSet(prompt: String, expectedResponse: String) {
        trainingSets.add(TrainingSet(prompt, expectedResponse))
    }

    /**
     * Retrieves the expected response for a given prompt.
     * @param prompt The system prompt.
     * @return The expected response, or null if not found.
     */
    fun getExpectedResponse(prompt: String): String? {
        return trainingSets.find { it.prompt == prompt }?.expectedResponse
    }

    /**
     * Updates the training sets based on user feedback.
     * @param prompt The prompt to update.
     * @param newResponse The new expected response.
     */
    fun updateTrainingSet(prompt: String, newResponse: String) {
        val index = trainingSets.indexOfFirst { it.prompt == prompt }
        if (index != -1) {
            trainingSets[index] = TrainingSet(prompt, newResponse)
        }
    }
}

data class TrainingSet(val prompt: String, val expectedResponse: String)