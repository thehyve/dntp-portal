/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRepository extends JpaRepository<Lab, Long> {

    Lab findByName(String lab);
    
    Lab findByNumber(Integer labNumber);
    
    List<Lab> findAllByActiveTrue();
    
    Lab findOneByActiveTrue(Long id);
}
