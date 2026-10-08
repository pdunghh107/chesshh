package com.pdunghh.shared.persistent;

import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import com.pdunghh.shared.security.RequestContext;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class UserAuditableEntity extends DateAuditableEntity {

    @CreatedBy
    @Column(updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    private UUID lastModifiedBy;

    @Override
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        this.createdBy = RequestContext.getUserId();
        this.lastModifiedBy = RequestContext.getUserId();
    }

    @Override
    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        this.lastModifiedBy = RequestContext.getUserId();
    }
}
