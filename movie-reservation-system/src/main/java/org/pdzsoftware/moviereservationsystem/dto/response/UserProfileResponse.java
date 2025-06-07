package org.pdzsoftware.moviereservationsystem.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.pdzsoftware.moviereservationsystem.enums.UserRole;

@Getter
@Setter
public class UserProfileResponse {
    private Long id;
    private String userName;
    private String email;
    private UserRole role;

    public UserProfileResponse(Long id, String userName, String email, UserRole role) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.role = role;
    }
}
