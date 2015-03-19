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

        // Take the values into the representation
        return new ProfileRepresentation();
    }

    @RequestMapping(value = "/profile", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileRepresentation postOwnProfile(Principal user) {
        // Authenticate user

        // Validate posted data (password requirements, no html tags, etc)

        // Update its profile

        // Return the updated profile
        // FIXME probably we should return an error message in case validation fails
        return new ProfileRepresentation();
    }
}
