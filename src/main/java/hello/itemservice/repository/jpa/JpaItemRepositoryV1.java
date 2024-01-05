package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
@Slf4j
public class JpaItemRepositoryV1 implements ItemRepository {

    private final EntityManager em;

    /**
     * `em.persist(item)` : JPA에서 객체를 테이블에 저장할 때는 엔티티 매니저가 제공하는 `persist()` 메서드
     * 를 사용하면 된다.
     * JPA가 만들어서 실행한 SQL을 보면 `id` 에 값이 빠져있는 것을 확인할 수 있다. PK 키 생성 전략을 `IDENTITY`
     * 로 사용했기 때문에 JPA가 이런 쿼리를 만들어서 실행한 것이다. 물론 쿼리 실행 이후에 `Item` 객체의 `id` 필드
     * 에 데이터베이스가 생성한 PK값이 들어가게 된다. (JPA가 INSERT SQL 실행 이후에 생성된 ID 결과를 받아서
     * 넣어준다)
     */
    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    /**
     * em.update()` 같은 메서드를 전혀 호출하지 않았다. 그런데 어떻게 UPDATE SQL이 실행되는 것일까?
     * JPA는 트랜잭션이 커밋되는 시점에, 변경된 엔티티 객체가 있는지 확인한다. 특정 엔티티 객체가 변경된 경우에
     * 는 UPDATE SQL을 실행한다.
     * JPA가 어떻게 변경된 엔티티 객체를 찾는지 명확하게 이해하려면 영속성 컨텍스트라는 JPA 내부 원리를 이해해
     * 야 한다. 이 부분은 JPA 기본편에서 자세히 다룬다. 지금은 트랜잭션 커밋 시점에 JPA가 변경된 엔티티 객체를
     * 찾아서 UPDATE SQL을 수행한다고 이해하면 된다.
     * 테스트의 경우 마지막에 트랜잭션이 롤백되기 때문에 JPA는 UPDATE SQL을 실행하지 않는다. 테스트에서
     * UPDATE SQL을 확인하려면 `@Commit` 을 붙이면 확인할 수 있다.
     */
    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item item = em.find(Item.class, itemId);
        item.setItemName(updateParam.getItemName());
        item.setPrice(updateParam.getPrice());
        item.setQuantity(updateParam.getQuantity());
    }

    /**
     * JPA에서 엔티티 객체를 PK를 기준으로 조회할 때는 `find()` 를 사용하고 조회 타입과, PK 값을 주면 된다. 그
     * 러면 JPA가 다음과 같은 조회 SQL을 만들어서 실행하고, 결과를 객체로 바로 변환해준다.
     */
    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String jpql = "select i from Item i";

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            jpql += " where";
        }

        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%',:itemName,'%')";
            andFlag = true;
        }

        if (maxPrice != null) {
            if (andFlag) {
                jpql += " and";
            }
            jpql += " i.price <= :maxPrice";
        }

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        return query.getResultList();
    }
}
