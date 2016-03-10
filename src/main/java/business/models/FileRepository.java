/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import business.models.File.AttachmentType;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByType(AttachmentType type);
}
