package com.unitedinternet.filestore.repository;

import com.unitedinternet.filestore.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByFullPath(String fullPath);

    /**
     * Be careful as we have 2 ways of validating regex matchings,
     * one in rdbms layer(see the below native query)
     * and another one in the service layer(@link com.unitedinternet.filestore.service.RegexFilesListener#handleFileOperationEvent())
     * They both have to match in applying regex specifications
     * @param regex the regex
     * @return
     */
    @Query(
            value = "SELECT * FROM FILES where regexp_like(FULL_PATH, ?, 'i');",
            nativeQuery = true)
    List<File> findAllFilesMatchingRegex(String regex);

}
