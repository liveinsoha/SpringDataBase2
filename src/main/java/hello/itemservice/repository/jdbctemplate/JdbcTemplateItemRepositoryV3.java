package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JdbcTemplate은 이런 문제를 보완하기 위해 `NamedParameterJdbcTemplate` 라는 이름을 지정해서 파라미터를
 * 바인딩 하는 기능을 제공한다.
 */
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
        /**
         * withTableName` : 데이터를 저장할 테이블 명을 지정한다.
         * `usingGeneratedKeyColumns` : `key` 를 생성하는 PK 컬럼 명을 지정한다.
         * `usingColumns` : INSERT SQL에 사용할 컬럼을 지정한다. 특정 값만 저장하고 싶을 때 사용한다. 생략할 수
         * 있다.
         * `SimpleJdbcInsert` 는 생성 시점에 데이터베이스 테이블의 메타 데이터를 조회한다. 따라서 어떤 컬럼이 있는지 확
         * 인 할 수 있으므로 `usingColumns` 을 생략할 수 있다. 만약 특정 컬럼만 지정해서 저장하고 싶다면 `usingColumns`
         * 를 사용하면 된다.
         */
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("item")
                .usingGeneratedKeyColumns("id");
    }

    /**
     * 이름 지정 파라미터
     * 파라미터를 전달하려면 `Map` 처럼 `key` , `value` 데이터 구조를 만들어서 전달해야 한다.
     * 여기서 `key` 는 `:파리이터이름` 으로 지정한, 파라미터의 이름이고 , `value` 는 해당 파라미터의 값이 된다.
     */

    @Override
    public Item save(Item item) {
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(item);
        Number key = jdbcInsert.executeAndReturnKey(params);
        item.setId(key.longValue());
        return item;
        /**
         * `jdbcInsert.executeAndReturnKey(param)` 을 사용해서 INSERT SQL을 실행하고, 생성된 키 값도 매우 편
         * 리하게 조회할 수 있다.
         */
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
