package com.example.restservice.serviceSignalR;

/**
 * SignalRMessage
 */
public class SignalRMessage {

    public String target;
    public Object[] arguments;

    public SignalRMessage(String target, Object[] arguments) {
        this.target = target;
        this.arguments = arguments;
    }
}