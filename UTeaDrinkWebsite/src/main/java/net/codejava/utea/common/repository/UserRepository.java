package net.codejava.utea.common.repository;

import net.codejava.utea.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	
    Optional<User> findByEmailIgnoreCase(String email);

    default Optional<User> findByLoginId(String loginId){
        // cho phép nhập email hoặc username
        var byEmail = findByEmailIgnoreCase(loginId);
        return byEmail.isPresent() ? byEmail : findByUsername(loginId);
    }
    
    default Optional<User> findByEmailOrUsername(String principal) {
        System.out.println("DEBUG Repository: Tìm kiếm với principal: " + principal);
        
        // Ưu tiên email nếu có '@'
        if (principal != null && principal.contains("@")) {
            System.out.println("DEBUG Repository: Tìm kiếm theo email: " + principal);
            return findByEmail(principal);
        }
        
        // nếu không, thử username trước
        System.out.println("DEBUG Repository: Tìm kiếm theo username: " + principal);
        Optional<User> byUsername = findByUsername(principal);
        if (byUsername.isPresent()) {
            System.out.println("DEBUG Repository: Tìm thấy user theo username");
            return byUsername;
        }
        
        // nếu không tìm thấy username, thử email
        System.out.println("DEBUG Repository: Không tìm thấy username, thử email");
        return findByEmail(principal);
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
}
