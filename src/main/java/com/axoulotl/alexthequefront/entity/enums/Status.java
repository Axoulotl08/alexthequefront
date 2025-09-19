package com.axoulotl.alexthequefront.entity.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Status {
    TO_START(0),
    IN_PROGRESS(1),
    DONE(2),
    STOPPED(3),
    COMPLETE(4);

    private final Integer numStatus;
    private static final Map<Integer, Status> mapOfStatusById = new HashMap<>();

    Status(Integer numStatus) {
        this.numStatus = numStatus;
    }

    public Integer getNumStatus(){
        return numStatus;
    }

    static {
        Arrays.stream(Status.values()).forEach(status -> mapOfStatusById.put(status.getNumStatus(), status));
    }

    public static Status getStatusFromInt(Integer statusId){
        return mapOfStatusById.get(statusId);
    }
}
