package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewPasswordRequestRepository extends JpaRepository<NewPasswordRequest, Long> {
    NewPasswordRequest findByToken(String token);
}
