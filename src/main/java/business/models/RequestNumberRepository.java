package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestNumberRepository extends JpaRepository<RequestNumber, Long> {

    RequestNumber findByYear(Integer year);

}
