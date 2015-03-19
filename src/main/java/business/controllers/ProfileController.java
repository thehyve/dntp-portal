package business.controllers;

import business.representation.ProfileRepresentation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@RestController
public class ProfileController {

    @RequestMapping(value = "/profile", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileRepresentation getOwnProfile(Principal user) {
        // Authenticate user?

        // Query its profile

        // Return the representation
        return new ProfileRepresentation();
    }

    @RequestMapping(value = "/profile", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileRepresentation putOwnProfile(Principal user) {
        // Authenticate user

        // Validate data (password requirements, no html tags, etc)

        // Update its profile

        // Return the updated profile
        // FIXME probably we should return an error message in case validation fails
        return new ProfileRepresentation();
    }

    @RequestMapping(value = "/profile/password", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void changePassword(Principal user) {
        // Authenticate user (including old password)

        // Validate data (password requirements)

        // Update its profile

        // FIXME probably we should return an error message in case validation fails
    }
}
