package org.pdzsoftware.moviereservationsystem.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    public ErrorResponse(HttpStatus status, String message, String path) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format(
                "{\"status\":%d, \"error\":\"%s\", \"message\":\"%s\", \"path\":\"%s\", \"timestamp\":\"%s\"}",
                status, error, message, path, timestamp
        );
    }
}
