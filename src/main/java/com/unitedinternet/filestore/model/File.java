package com.unitedinternet.filestore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Table(name = "files")
@Entity
public class File {

    @Id
    @GeneratedValue
    private Long id;

    private String path;

    private String name;

    private String systemPath;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    public File(String path, String name, String systemPath, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.path = path;
        this.name = name;
        this.systemPath = systemPath;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemPath() {
        return systemPath;
    }

    public void setSystemPath(String systemPath) {
        this.systemPath = systemPath;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(id, file.id) && Objects.equals(path, file.path) && Objects.equals(name, file.name) && Objects.equals(systemPath, file.systemPath) && Objects.equals(createdDate, file.createdDate) && Objects.equals(updatedDate, file.updatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, name);
    }

    public static class Builder {

        private String path;

        private String name;

        private String systemPath;

        private LocalDateTime createdDate;

        private LocalDateTime updatedDate;

        public Builder path (String path) {
            this.path = path;
            return this;
        }

        public Builder name (String name) {
            this.name = name;
            return this;
        }

        public Builder systemPath (String systemPath) {
            this.systemPath = systemPath;
            return this;
        }

        public Builder createdDate (LocalDateTime createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Builder updatedDate (LocalDateTime updatedDate) {
            this.updatedDate = updatedDate;
            return this;
        }

        public File build() {
            return new File(path, name, systemPath, createdDate, updatedDate);
        }

    }

}
