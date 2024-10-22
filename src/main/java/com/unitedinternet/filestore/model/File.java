package com.unitedinternet.filestore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Table(name = "files", indexes = @Index(columnList = "fullPath", name = "fullPathIndex", unique = true))
@Entity
public class File {

    @Id
    @GeneratedValue
    private Long id;

    private String path;

    private String name;

    private String fullPath;

    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    public File() {
    }

    public File(String path, String name, String fullPath, AccessType accessType, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.path = path;
        this.name = name;
        this.fullPath = fullPath;
        this.accessType = accessType;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
     }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
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
        return Objects.equals(id, file.id) && Objects.equals(path, file.path) && Objects.equals(name, file.name) && Objects.equals(fullPath, file.fullPath) && Objects.equals(accessType, file.accessType) && Objects.equals(createdDate, file.createdDate) && Objects.equals(updatedDate, file.updatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, name);
    }

    public static class Builder {

        private String path;

        private String name;

        private String fullPath;

        private AccessType accessType;

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

        public Builder fullPath (String fullPath) {
            this.fullPath = fullPath;
            return this;
        }

        public Builder accessType (AccessType accessType) {
            this.accessType = accessType;
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
            return new File(path, name, fullPath, accessType, createdDate, updatedDate);
        }

    }

}
