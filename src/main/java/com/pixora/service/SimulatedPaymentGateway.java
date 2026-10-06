package com.pixora.service;

public final class SimulatedPaymentGateway {
    public enum Result { SUCCESS, DECLINED, UNAVAILABLE }

    public Result authorize(String simulationMode) {
        if ("DECLINE".equalsIgnoreCase(simulationMode)) return Result.DECLINED;
        if ("UNAVAILABLE".equalsIgnoreCase(simulationMode)) return Result.UNAVAILABLE;
        return Result.SUCCESS;
    }

    public Result refund(String simulationMode) {
        return authorize(simulationMode);
    }
}
