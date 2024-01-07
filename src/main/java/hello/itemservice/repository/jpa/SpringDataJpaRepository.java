package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataJpaRepository extends JpaRepository<Item, Long> {
    /**
     * 스프링 데이터 JPA가 제공하는 `JpaRepository` 인터페이스를 인터페이스 상속 받으면 기본적인 CRUD 기능
     * 을 사용할 수 있다.
     * 그런데 이름으로 검색하거나, 가격으로 검색하는 기능은 공통으로 제공할 수 있는 기능이 아니다. 따라서 쿼리 메
     * 서드 기능을 사용하거나 `@Query` 를 사용해서 직접 쿼리를 실행하면 된다
     */

    List<Item> findByItemNameLike(String itemName);

    List<Item> findByPriceLessThanEqual(Integer price);

    List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);


    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findByItem(@Param("itemName") String itemName, @Param("price") Integer price);
}
