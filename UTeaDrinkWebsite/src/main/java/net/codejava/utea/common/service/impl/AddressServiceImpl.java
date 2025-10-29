package net.codejava.utea.common.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.common.entity.Address;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.AddressRepository;
import net.codejava.utea.common.service.AddressService;
import net.codejava.utea.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository repo;
    private final OrderRepository orderRepo;

    @Override
    public Address save(Address a) { return repo.save(a); }

    @Override
    public void delete(Long id, Long ownerId) {
        Address a = repo.findById(id).orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));
        
        // Kiểm tra quyền sở hữu
        if (!a.getUser().getId().equals(ownerId)) {
            throw new RuntimeException("Không có quyền xóa địa chỉ này");
        }
        
        // ✅ KIỂM TRA: Không cho xóa địa chỉ đang được sử dụng trong đơn hàng
        if (orderRepo.existsByShippingAddressId(id)) {
            throw new RuntimeException("Không thể xóa địa chỉ đã được sử dụng trong đơn hàng");
        }
        
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