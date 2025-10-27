package net.codejava.utea.common.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.Role;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.RoleRepository;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.common.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepo;
	private final RoleRepository roleRepo;

	@Override
	public User save(User u) {
		return userRepo.save(u);
	}

	@Override
	public Optional<User> findById(Long id) {
		return userRepo.findById(id);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userRepo.findByEmailIgnoreCase(email);
	}

	@Override
	public boolean emailExists(String email, Long excludeId) {
		if (excludeId == null)
			return userRepo.existsByEmailIgnoreCase(email);
		return userRepo.existsByEmailIgnoreCaseAndIdNot(email, excludeId);
	}

	@Override
	public Page<User> search(String keyword, Pageable pageable) {
		if (keyword == null || keyword.isBlank())
			return userRepo.findAll(pageable);
		return userRepo.search(keyword.trim(), pageable);
	}

	@Override
	public void enable(Long id, boolean enabled) {
		userRepo.findById(id).ifPresent(u -> {
			u.setStatus(enabled ? "ACTIVE" : "LOCKED");
			userRepo.save(u);
		});
	}

	@Override
	public void assignRoles(Long userId, Set<String> roleCodes) {
		var user = userRepo.findById(userId).orElseThrow();
		var roles = roleRepo.findByCodeIn(roleCodes.stream().map(String::toUpperCase).collect(Collectors.toSet()));
		user.setRoles(roles.stream().collect(Collectors.toSet()));
		userRepo.save(user);
	}

	@Override
	public Set<String> getRoleCodes(Long userId) {
		return userRepo.findById(userId).orElseThrow().getRoles().stream().map(Role::getCode)
				.collect(Collectors.toSet());
	}
}
