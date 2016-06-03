/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcerptEntryRepository extends JpaRepository<ExcerptEntry, Long> {

    Integer countByExcerptListId(Long excerptListId);

    Integer countBySelectedTrueAndExcerptListId(Long excerptListId);

    void deleteAllByExcerptListId(Long excerptListId);

}
