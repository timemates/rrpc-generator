package app.timemate.rrpc.generator.plugin.api.communication

/**
 * Exception thrown when communication with the generator fails or produces an invalid response.
 */
public class CommunicationException(message: String) : Exception(message)