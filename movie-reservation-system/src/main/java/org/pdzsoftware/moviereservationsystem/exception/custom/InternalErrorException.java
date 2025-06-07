package org.pdzsoftware.moviereservationsystem.exception.custom;

import org.springframework.http.HttpStatus;

public class InternalErrorException extends ApiException {
    public InternalErrorException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
