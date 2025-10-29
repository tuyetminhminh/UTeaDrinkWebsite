//package net.codejava.utea.manager.repository;
//
//import net.codejava.utea.manager.entity.Shop;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface ShopRepository extends JpaRepository<Shop, Long> {
//
//    Optional<Shop> findByName(String name);
//
//    List<Shop> findByStatus(String status);
//
//    List<Shop> findByStatusOrderByNameAsc(String status);
//    // Ưu tiên shop đang mở
//    Optional<Shop> findFirstByStatusOrderByIdAsc(String status);
//
//    // Fallback: lấy shop đầu tiên bất kỳ
//    Optional<Shop> findFirstByOrderByIdAsc();
//}
//
package net.codejava.utea.manager.repository;

import net.codejava.utea.manager.dto.ShopDTO;
import net.codejava.utea.manager.entity.Shop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByName(String name);

    List<Shop> findByStatus(String status);

    List<Shop> findByStatusOrderByNameAsc(String status);
    // Ưu tiên shop đang mở
    Optional<Shop> findFirstByStatusOrderByIdAsc(String status);

    // Fallback: lấy shop đầu tiên bất kỳ
    Optional<Shop> findFirstByOrderByIdAsc();

    boolean existsByNameIgnoreCase(String name);
    boolean existsByPhone(String phone);
    boolean existsByAddressIgnoreCase(String address);

    // update (bỏ qua chính nó)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByPhoneAndIdNot(String phone, Long id);
    boolean existsByAddressIgnoreCaseAndIdNot(String address, Long id);
    @Query("""
        select new net.codejava.utea.manager.dto.ShopDTO(
            s.id, s.name, s.address, s.phone, s.status, s.createdAt, s.updatedAt,
            m.id, m.fullName
        )
        from Shop s
        left join ShopManager sm on sm.shop.id = s.id
        left join User m on sm.manager.id = m.id
        where (:q is null or lower(s.name) like lower(concat('%', :q, '%')))
        order by s.id desc
        """)
    Page<ShopDTO> findAllWithManager(String q, Pageable pageable);
}

