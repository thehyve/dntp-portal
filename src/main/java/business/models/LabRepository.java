/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRepository extends JpaRepository<Lab, Long> {

    Lab findByName(String lab);

    Lab findByNumber(Integer labNumber);

    List<Lab> findAllByActiveTrueOrderByNumberAsc();

    List<Lab> findAllByOrderByNumberAsc();

    Lab findOneByActiveTrue(Long id);

    List<Lab> findAllByOrderByNumberAsc(Set<Long> labIds);
}
