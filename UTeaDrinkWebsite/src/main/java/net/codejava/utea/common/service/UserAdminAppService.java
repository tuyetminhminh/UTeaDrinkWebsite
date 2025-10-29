package net.codejava.utea.common.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.dto.UserForm;
import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserAdminAppService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    private Role mustRole(String code){
        return roleRepo.findByCode(code.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ: " + code));
    }

    @Transactional
    public User create(UserForm f){
        // unique
        if (userRepo.existsByEmailIgnoreCase(f.getEmail()))
            throw new IllegalArgumentException("Email đã tồn tại.");
        if (StringUtils.hasText(f.getUsername()) && userRepo.existsByUsername(f.getUsername()))
            throw new IllegalArgumentException("Username đã tồn tại.");

        var u = User.builder()
                .email(f.getEmail().trim())
                .username(StringUtils.hasText(f.getUsername()) ? f.getUsername().trim() : null)
                .fullName(f.getFullName())
                .status(f.getStatus())
                .passwordHash(f.getPassword()) // bạn đang NoOp → set thẳng
                .build();

        u.setRoles(Set.of(mustRole(f.getRoleCode())));
        try {
            return userRepo.save(u);
        } catch (DataIntegrityViolationException ex){
            throw new IllegalArgumentException("Dữ liệu không hợp lệ hoặc trùng lặp.", ex);
        }
    }

    @Transactional
    public User update(Long id, UserForm f, boolean isSelfAdmin){
        var u = userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

        // unique (exclude self)
        if (userRepo.existsByEmailIgnoreCaseAndIdNot(f.getEmail(), id))
            throw new IllegalArgumentException("Email đã tồn tại.");
        if (StringUtils.hasText(f.getUsername())
                && userRepo.existsByUsername(f.getUsername())
                && !f.getUsername().equalsIgnoreCase(u.getUsername()))
            throw new IllegalArgumentException("Username đã tồn tại.");

        // chặn tự hạ quyền admin
        String newRole = f.getRoleCode().toUpperCase();
        boolean wasAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
        if (isSelfAdmin && wasAdmin && !"ADMIN".equals(newRole))
            throw new IllegalArgumentException("Bạn không thể tự hạ quyền ADMIN của chính mình.");

        u.setEmail(f.getEmail().trim());
        u.setUsername(StringUtils.hasText(f.getUsername()) ? f.getUsername().trim() : null);
        u.setFullName(f.getFullName());
        u.setStatus(f.getStatus());

        if (StringUtils.hasText(f.getPassword())) {
            u.setPasswordHash(f.getPassword());
        }

        u.setRoles(new HashSet<>(List.of(mustRole(f.getRoleCode()))));
        try {
            return userRepo.save(u);
        } catch (DataIntegrityViolationException ex){
            throw new IllegalArgumentException("Dữ liệu không hợp lệ hoặc trùng lặp.", ex);
        }
    }
}
