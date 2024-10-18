package com.unitedinternet.filestore.repository;

import com.unitedinternet.filestore.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.util.Collection;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByFullPath(String fullPath);

    @Query(
            value = "SELECT * FROM FILES where regexp_like(NAME, ?, 'i');",
            nativeQuery = true)
    List<File> findAllFilesMatchingRegex(String regex);

}
