package business.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import business.models.File.AttachmentType;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByType(AttachmentType type);
}
