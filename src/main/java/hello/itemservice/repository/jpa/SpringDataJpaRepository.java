package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataJpaRepository extends JpaRepository<Item, Long> {

    List<Item> findByItemNameLike(String itemName);

    List<Item> findByPriceLessThanEqual(Integer maxPrice);

    List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer maxPrice);


    @Query("select i from Item i where i.itemName like :itemName and i.price <= :maxPrice")
    List<Item> findByItem(@Param("itemName") String itemName, @Param("maxPrice") Integer maxPrice);
}
