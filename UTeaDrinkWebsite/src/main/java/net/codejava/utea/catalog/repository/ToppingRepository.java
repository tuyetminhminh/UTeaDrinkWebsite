//package net.codejava.utea.catalog.repository;
//
//import net.codejava.utea.catalog.entity.Topping;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface ToppingRepository extends JpaRepository<Topping, Long> {
//
//    // Truy vấn topping theo Shop ID
//    List<Topping> findByShopId(Long shopId);
//    List<Topping> findByShopIdAndStatus(Long shopId, String status);
//}

package net.codejava.utea.catalog.repository;

import net.codejava.utea.catalog.dto.ToppingRow;
import net.codejava.utea.catalog.entity.Topping;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {

    // Truy vấn topping theo Shop ID
    List<Topping> findByShopId(Long shopId);

    List<Topping> findByShopIdAndStatus(Long shopId, String status);

    // tìm kiếm + phân trang
    @EntityGraph(attributePaths = "shop")
    @Query("""
              select t from Topping t
              where (:shopId is null or t.shop.id = :shopId)
                and (:kw is null or lower(t.name) like lower(concat('%', :kw, '%')))
            """)
    Page<Topping> search(@Param("shopId") Long shopId,
                         @Param("kw") String kw,
                         Pageable pageable);

    @Query(value = """
        select new net.codejava.utea.catalog.dto.ToppingRow(
            t.id, t.name, t.price, t.status, s.id, s.name
        )
        from Topping t join t.shop s
        where (:shopId is null or s.id = :shopId)
          and (:kw is null or :kw = '' or lower(t.name) like lower(concat('%', :kw, '%')))
        """,
            countQuery = """
        select count(t)
        from Topping t join t.shop s
        where (:shopId is null or s.id = :shopId)
          and (:kw is null or :kw = '' or lower(t.name) like lower(concat('%', :kw, '%')))
        """)
    Page<ToppingRow> searchRows(@Param("shopId") Long shopId,
                                @Param("kw") String kw,
                                Pageable pageable);

    // Khi edit cần lấy entity + shop (để set form.shopId) → tránh lazy
    @EntityGraph(attributePaths = "shop")
    Optional<Topping> findById(Long id);
    // kiểm tra trùng tên trong 1 shop
    boolean existsByShopIdAndNameIgnoreCase(Long shopId, String name);

    boolean existsByShopIdAndNameIgnoreCaseAndIdNot(Long shopId, String name, Long excludeId);
}
