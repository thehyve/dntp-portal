package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PathologyItemRepository extends JpaRepository<PathologyItem, Long> {

    Long countByLabRequestId(Long labRequestId);
}
