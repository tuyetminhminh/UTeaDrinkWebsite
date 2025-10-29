package net.codejava.utea.common.base;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;

@MappedSuperclass
@Getter
@Setter
public abstract class Auditable {
	@Column(name = "created_at")
	protected LocalDateTime createdAt;
	@Column(name = "updated_at")
	protected LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		createdAt = LocalDateTime.now();
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
