package org.pdzsoftware.moviereservationsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    private static final String PASSWORD_MESSAGE = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, and one number";

    @Email(message = "Email must be a valid address")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @Pattern(
            regexp = PASSWORD_REGEX,
            message = PASSWORD_MESSAGE
    )
    private String password;
}
