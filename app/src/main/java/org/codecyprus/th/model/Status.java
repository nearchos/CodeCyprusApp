package org.codecyprus.th.model;

public enum Status {
    OK, ERROR;

    public boolean isOk() {
        return this == OK;
    }

    public boolean isError() {
        return this == ERROR;
    }
}
