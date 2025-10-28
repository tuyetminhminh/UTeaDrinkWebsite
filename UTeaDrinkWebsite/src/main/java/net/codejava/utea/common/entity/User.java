//package net.codejava.utea.common.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//@Table(
//        name = "users",
//        indexes = @Index(name = "ix_user_email", columnList = "email")
//)
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true, length = 150)
//    private String email;
//
//    @Column(unique = true, length = 100)
//    private String username; // có thể null, vẫn login bằng email được
//
//    @Column(name = "password_hash", nullable = false, length = 255)
//    private String passwordHash;
//
//    @Column(name = "full_name", columnDefinition = "NVARCHAR(200)")
//    private String fullName;
//
//    @Column(length = 20)
//    @Builder.Default
//    private String status = "ACTIVE"; // ACTIVE | LOCKED | PENDING
//
//    @Column(name = "created_at")
//    @Builder.Default
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    /* ✅ Roles: EAGER để tránh LazyInitializationException khi login */
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "user_roles",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id")
//    )
//    @Builder.Default
//    private Set<Role> roles = new HashSet<>();
//
//    /* Addresses */
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    @Builder.Default
//    private Set<Address> addresses = new HashSet<>();
//}

package net.codejava.utea.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "ix_user_email", columnList = "email"),
        @Index(name = "ix_user_phone", columnList = "phone")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(unique = true, length = 100)
    private String username; // có thể null, vẫn login bằng email được

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(200)")
    private String fullName;

    @Column(length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE | LOCKED | PENDING

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /* ✅ Roles: EAGER để tránh LazyInitializationException khi login */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /* Addresses */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Address> addresses = new HashSet<>();
}
