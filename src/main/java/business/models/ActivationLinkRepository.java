package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivationLinkRepository extends JpaRepository<ActivationLink, Long> {
    ActivationLink findByToken(String token);
}
