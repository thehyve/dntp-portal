package business.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import business.models.PathologyItemRepository;

@Service
public class PathologyItemService {

    @Autowired
    PathologyItemRepository pathologyItemRepository;

    @Cacheable("pathologycount")
    public Long getPathologyCountCached(Long labRequestId) {
        return pathologyItemRepository.countByLabRequestId(labRequestId);
    }

}
