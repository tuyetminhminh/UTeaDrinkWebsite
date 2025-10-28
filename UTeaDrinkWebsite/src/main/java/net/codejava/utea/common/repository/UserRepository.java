//package net.codejava.utea.common.repository;
//
//import net.codejava.utea.common.entity.User;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.*;
//import org.springframework.data.repository.query.Param;
//
//import java.util.Optional;
//
//public interface UserRepository extends JpaRepository<User, Long> {
//
//    Optional<User> findByEmailIgnoreCase(String email);
//
//    default Optional<User> findByLoginId(String loginId){
//        // cho phép nhập email hoặc username
//        var byEmail = findByEmailIgnoreCase(loginId);
//        return byEmail.isPresent() ? byEmail : findByUsername(loginId);
//    }
//
//    default Optional<User> findByEmailOrUsername(String principal) {
//        // Ưu tiên email nếu có '@'
//        if (principal != null && principal.contains("@")) {
//            return findByEmail(principal);
//        }
//        // nếu không, thử username; nếu thất bại, fallback email
//        return findByUsername(principal).or(() -> findByEmail(principal));
//    }
//
//    boolean existsByEmailIgnoreCase(String email);
//    boolean existsByUsername(String username);
//
//    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long excludeId);
//
//    @Query("""
//           SELECT u FROM User u
//           WHERE lower(u.email) LIKE lower(concat('%', :kw, '%'))
//              OR lower(u.fullName) LIKE lower(concat('%', :kw, '%'))
//           """)
//    Page<User> search(@Param("kw") String keyword, Pageable pageable);
//
//    @EntityGraph(attributePaths = "roles") // load kèm roles để tránh lazy khi cần
//    Optional<User> findByEmail(String email);
//
//    // Dùng cho chat (lấy admin đầu tiên theo role)
//    Optional<User> findFirstByRoles_CodeOrderByIdAsc(String roleCode);
//
//    // (nếu cần) lấy theo username hiển thị
//    Optional<User> findByUsername(String username);
//}

package net.codejava.utea.common.repository;

import net.codejava.utea.chat.dto.UserModerationRow;
import net.codejava.utea.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    default Optional<User> findByLoginId(String loginId) {
        // cho phép nhập email hoặc username
        var byEmail = findByEmailIgnoreCase(loginId);
        return byEmail.isPresent() ? byEmail : findByUsername(loginId);
    }

    default Optional<User> findByEmailOrUsername(String principal) {
        // Ưu tiên email nếu có '@'
        if (principal != null && principal.contains("@")) {
            return findByEmail(principal);
        }
        // nếu không, thử username; nếu thất bại, fallback email
        return findByUsername(principal).or(() -> findByEmail(principal));
    }

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long excludeId);

    @Query("""
            SELECT u FROM User u
            WHERE lower(u.email) LIKE lower(concat('%', :kw, '%'))
               OR lower(u.fullName) LIKE lower(concat('%', :kw, '%'))
            """)
    Page<User> search(@Param("kw") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "roles") // load kèm roles để tránh lazy khi cần
    Optional<User> findByEmail(String email);

    // Dùng cho chat (lấy admin đầu tiên theo role)
    Optional<User> findFirstByRoles_CodeOrderByIdAsc(String roleCode);

    // (nếu cần) lấy theo username hiển thị
    Optional<User> findByUsername(String username);

    @Query("""
        select distinct u
        from User u
        join u.roles r
        where r.id = :roleId
          and (
            lower(u.fullName) like lower(concat('%', :kw, '%'))
            or lower(u.email)   like lower(concat('%', :kw, '%'))
            or (u.username is not null and lower(u.username) like lower(concat('%', :kw, '%')))
          )
        """)
    List<User> searchByRoleAndKeyword(@Param("roleId") Long roleId,
                                      @Param("kw") String keyword);

    List<User> findTop20ByOrderByIdDesc();

    @Query("""
        SELECT new net.codejava.utea.chat.dto.UserModerationRow(
            u.id, u.email, u.fullName, cb.bannedUntil
        )
        FROM User u LEFT JOIN ChatBan cb ON u.id = cb.user.id 
        WHERE :kw IS NULL OR :kw = ''
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%'))
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%'))
    """)
    Page<UserModerationRow> searchForModeration(String kw, Pageable pageable);
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

}
