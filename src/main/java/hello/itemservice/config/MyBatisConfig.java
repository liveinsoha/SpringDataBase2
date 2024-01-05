package hello.itemservice.config;


import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.mybatis.ItemMapper;
import hello.itemservice.repository.mybatis.MyBatisRepository;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MyBatisConfig {

    private final ItemMapper itemMapper;
    /**
     * `MyBatisConfig` 는 `ItemMapper` 를 주입받고, 필요한 의존관계를 만든다.
     */

    @Bean
    public ItemRepository itemRepository() {
        return new MyBatisRepository(itemMapper);
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }
}
