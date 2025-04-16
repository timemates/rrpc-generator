package app.timemate.rrpc.generator.plugin.api.communication

public sealed interface GPMessage<TSignal : GPSignal> {
    public val id: SignalId
    public val signal: TSignal
}