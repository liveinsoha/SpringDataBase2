package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JdbcTemplate은 이런 문제를 보완하기 위해 `NamedParameterJdbcTemplate` 라는 이름을 지정해서 파라미터를
 * 바인딩 하는 기능을 제공한다.
 */
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        /**
         * JdbcTemplateItemRepositoryV2` 는 `ItemRepository` 인터페이스를 구현했다.
         * `this.template = new NamedParameterJdbcTemplate(dataSource)`
         * `NamedParameterJdbcTemplate` 도 내부에 `dataSource` 가 필요하다.
         * `JdbcTemplateItemRepositoryV2` 생성자를 보면 의존관계 주입은 `dataSource` 를 받고 내부에서
         * `NamedParameterJdbcTemplate` 을 생성해서 가지고 있다. 스프링에서는 `JdbcTemplate` 관련 기능
         * 을 사용할 때 관례상 이 방법을 많이 사용한다.
         */
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 이름 지정 파라미터
     * 파라미터를 전달하려면 `Map` 처럼 `key` , `value` 데이터 구조를 만들어서 전달해야 한다.
     * 여기서 `key` 는 `:파리이터이름` 으로 지정한, 파라미터의 이름이고 , `value` 는 해당 파라미터의 값이 된다.
     */

    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price, quantity) values (:itemName, :price, :quantity)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(item);
        template.update(sql, params, keyHolder);

        long id = keyHolder.getKey().longValue();
        item.setId(id);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {

        String sql = "update item set item_name = :itemName, price = :price, quantity = :quantity where id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity())
                .addValue("id", itemId);

        template.update(sql, params);
    }

    @Override
    public Optional<Item> findById(Long id) {

        String sql = "select id, item_name, price, quantity from item where id = :id";

        try {
            Map<String, Object> params = Map.of("id", id);
            Item item = template.queryForObject(sql, params, itemRowMapper());
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(cond);

        String sql = "select id, item_name, price, quantity from item";

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
            boolean andFlag = false;

            if (StringUtils.hasText(itemName)) {
                sql += " item_name like concat('%', :itemName, '%')";
                andFlag = true;
            }

            if (maxPrice != null) {
                if (andFlag) {
                    sql += " and";
                }
                sql += " price <= :maxPrice";
            }

            return template.query(sql, params, itemRowMapper());
        }
        return template.query(sql, itemRowMapper());
    }

    private BeanPropertyRowMapper<Item> itemRowMapper() {
        return new BeanPropertyRowMapper<>(Item.class);
        /**
         * `BeanPropertyRowMapper` 는 `ResultSet` 의 결과를 받아서 자바빈 규약에 맞추어 데이터를 변환한다.
         * 예를 들어서 데이터베이스에서 조회한 결과가 `select id, price` 라고 하면 다음과 같은 코드를 작성해준다. (실제
         * 로는 리플렉션 같은 기능을 사용한다.)
         * Item item = new Item();
         * item.setId(rs.getLong("id"));
         * item.setPrice(rs.getInt("price"));
         * 데이터베이스에서 조회한 결과 이름을 기반으로 `setId()` , `setPrice()` 처럼 자바빈 프로퍼티 규약에 맞춘 메서드
         * 를 호출하는 것이다.
         */
    }
}
