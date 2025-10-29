package net.codejava.utea.common.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.Address;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.AddressRepository;
import net.codejava.utea.common.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository repo;

    @Override
    public Address save(Address a) { return repo.save(a); }

    @Override
    public void delete(Long id, Long ownerId) {
        Address a = repo.findById(id).orElseThrow();
        if (!a.getUser().getId().equals(ownerId)) throw new RuntimeException("Không có quyền xóa");
        repo.delete(a);
    }

    @Override
    public List<Address> listOf(User user) {
        return repo.findByUserOrderByIsDefaultDescIdDesc(user);
    }

    @Override
    public Optional<Address> findById(Long id) { return repo.findById(id); }

    @Transactional
    @Override
    public void markDefault(Long id, Long ownerId) {
        Address pick = repo.findById(id).orElseThrow();
        if (!pick.getUser().getId().equals(ownerId)) throw new RuntimeException("Không có quyền");

        // bỏ default cũ
        List<Address> all = repo.findByUserOrderByIsDefaultDescIdDesc(pick.getUser());
        for (Address a : all) { if (a.isDefault()) { a.setDefault(false); repo.save(a); } }
        // set mới
        pick.setDefault(true);
        repo.save(pick);
    }
}