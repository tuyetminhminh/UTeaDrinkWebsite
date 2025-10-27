package net.codejava.utea.common.service;

import net.codejava.utea.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface UserService {
    User save(User u);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean emailExists(String email, Long excludeId);

    Page<User> search(String keyword, Pageable pageable); // tìm theo tên/email
    void enable(Long id, boolean enabled);

    // phân quyền cơ bản
    void assignRoles(Long userId, Set<String> roleCodes);   // ["ADMIN","CUSTOMER"]
    Set<String> getRoleCodes(Long userId);
}
