package hello.itemservice.repository.jdbctemplate;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    /**
     * this.template = new JdbcTemplate(dataSource)`
     * `JdbcTemplate` 은 데이터소스( `dataSource` )가 필요하다.
     * `JdbcTemplateItemRepositoryV1()` 생성자를 보면 `dataSource` 를 의존 관계 주입 받고 생성자
     * 내부에서 `JdbcTemplate` 을 생성한다. 스프링에서는 `JdbcTemplate` 을 사용할 때 관례상 이 방법을 많이 사용한다.
     */

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item(item_name, price, quantity) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection ->
        {
            PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"id"});
            pstmt.setString(1, item.getItemName());
            pstmt.setInt(2, item.getPrice());
            pstmt.setInt(3, item.getQuantity());
            return pstmt;
        }, keyHolder);
        /**
         * template.update()` : 데이터를 변경할 때는 `update()` 를 사용하면 된다.
         * `INSERT` , `UPDATE` , `DELETE` SQL에 사용한다.
         * `template.update()` 의 반환 값은 `int` 인데, 영향 받은 로우 수를 반환한다.
         * 데이터를 저장할 때 PK 생성에 `identity` (auto increment) 방식을 사용하기 때문에, PK인 ID 값을 개발자가
         * 직접 지정하는 것이 아니라 비워두고 저장해야 한다. 그러면 데이터베이스가 PK인 ID를 대신 생성해준다.
         * 문제는 이렇게 데이터베이스가 대신 생성해주는 PK ID 값은 데이터베이스가 생성하기 때문에, 데이터베이스에
         * INSERT가 완료 되어야 생성된 PK ID 값을 확인할 수 있다.
         * `KeyHolder` 와 `connection.prepareStatement(sql, new String[]{"id"})` 를 사용해서 `id` 를
         * 지정해주면 `INSERT` 쿼리 실행 이후에 데이터베이스에서 생성된 ID 값을 조회할 수 있다.
         */
        long id = keyHolder.getKey().longValue();
        item.setId(id);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {

        String sql = "update item set item_name = ?, price = ?, quantity = ? where id = ?";
        template.update(updateParam.getItemName(),
                updateParam.getPrice(),
                updateParam.getQuantity(),
                itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {
        /**
         * 데이터를 하나 조회한다.`template.queryForObject()`
         * 결과 로우가 하나일 때 사용한다.
         * `RowMapper` 는 데이터베이스의 반환 결과인 `ResultSet` 을 객체로 변환한다.
         * 결과가 없으면 `EmptyResultDataAccessException` 예외가 발생한다.
         * 결과가 둘 이상이면 `IncorrectResultSizeDataAccessException` 예외가 발생한다.
         * `ItemRepository.findById()` 인터페이스는 결과가 없을 때 `Optional` 을 반환해야 한다. 따라서 결과가
         * 없으면 예외를 잡아서 `Optional.empty` 를 대신 반환하면 된다.
         */
        String sql = "select id, item_name, price, quantity from item where id = ?";
        try {
            Item item = template.queryForObject(sql, rowMapper(), id);
            return Optional.of(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        /**
         * template.query()`
         * 결과가 하나 이상일 때 사용한다.
         * `RowMapper` 는 데이터베이스의 반환 결과인 `ResultSet` 을 객체로 변환한다.
         * 결과가 없으면 빈 컬렉션을 반환한다.
         */
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "select id, item_name, price, quantity from item";

        if (StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
            boolean andFlag = false;
            List<Object> params = new ArrayList<>();
            if (StringUtils.hasText(itemName)) {
                sql += " item_name like concat('%', ?, '%')";
                andFlag = true;
                params.add(itemName);
            }

            if (maxPrice != null) {
                if (andFlag) {
                    sql += " and";
                }
                sql += " price <= ?";
                params.add(maxPrice);
            }
            log.info("sql={}", sql);
            return template.query(sql, rowMapper(), params.toArray());
        }
        return template.query(sql, rowMapper());
    }

    private RowMapper<Item> rowMapper() {
        return (rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        };
    }
}
