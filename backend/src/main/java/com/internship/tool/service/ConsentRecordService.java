package com.internship.tool.service;

import com.internship.tool.entity.ConsentStatus;
import com.internship.tool.service.dto.ConsentRecordRequest;
import com.internship.tool.service.dto.ConsentRecordResponse;
import com.internship.tool.service.dto.StatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ConsentRecordService {

    Page<ConsentRecordResponse> getAll(Pageable pageable);
    ConsentRecordResponse       getById(Long id);
    ConsentRecordResponse       create(ConsentRecordRequest request);
    ConsentRecordResponse       update(Long id,
                                       ConsentRecordRequest request);
    void                        delete(Long id);
    Page<ConsentRecordResponse> search(String query,
                                       Pageable pageable);
    Page<ConsentRecordResponse> filterByStatus(
                                    ConsentStatus status,
                                    Pageable pageable);
    StatsResponse               getStats();
    void                        evictAllCaches();
}