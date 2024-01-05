package hello.itemservice;

import hello.itemservice.config.*;
import hello.itemservice.repository.ItemRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import javax.xml.crypto.Data;

/**
 * `@Import(MemoryConfig.class)` : 앞서 설정한 `MemoryConfig` 를 설정 파일로 사용한다.
 * `scanBasePackages = "hello.itemservice.web"` : 여기서는 컨트롤러만 컴포넌트 스캔을 사용하고,
 * 나머지는 직접 수동 등록한다. 그래서 컴포넌트 스캔 경로를 `hello.itemservice.web` 하위로 지정했다.
 * `@Profile("local")` : 특정 프로필의 경우에만 해당 스프링 빈을 등록한다. 여기서는 `local` 이라는 이름의
 * 프로필이 사용되는 경우에만 `testDataInit` 이라는 스프링 빈을 등록한다. 이 빈은 앞서 본 것인데, 편의상 초
 * 기 데이터를 만들어서 저장하는 빈이다
 */
//@Import(MemoryConfig.class)
//@Import(JdbcTemplateV3Config.class)
//@Import(MyBatisConfig.class)
@Import(JpaConfig.class)
@SpringBootApplication(scanBasePackages = "hello.itemservice.web")
public class ItemServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemServiceApplication.class, args);
    }

    @Bean
    @Profile("local")
    public TestDataInit testDataInit(ItemRepository itemRepository) {
        return new TestDataInit(itemRepository);
    }

    /**
     * 프로필
     * 스프링은 로딩 시점에 `application.properties` 의 `spring.profiles.active` 속성을 읽어서 프로필로 사용한다.
     * 이 프로필은 로컬(나의 PC), 운영 환경, 테스트 실행 등등 다양한 환경에 따라서 다른 설정을 할 때 사용하는 정보이다.
     * 예를 들어서 로컬PC에서는 로컬 PC에 설치된 데이터베이스에 접근해야 하고, 운영 환경에서는 운영 데이터베이스에
     * 접근해야 한다면 서로 설정 정보가 달라야 한다. 심지어 환경에 따라서 다른 스프링 빈을 등록해야 할 수 도 있다. 프로
     * 필을 사용하면 이런 문제를 깔끔하게 해결할 수 있다.
     */

    /*@Bean
    @Profile("test")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }*/
    /**
     * 스프링 부트는 개발자에게 정말 많은 편리함을 제공하는데, 임베디드 데이터베이스에 대한 설정도 기본으로 제공한다.
     * 스프링 부트는 데이터베이스에 대한 별다른 설정이 없으면 임베디드 데이터베이스를 사용한다.
     * 이렇게 하면 데이터베이스에 접근하는 모든 설정 정보가 사라지게 된다.
     * 이렇게 별다른 정보가 없으면 스프링 부트는 임베디드 모드로 접근하는 데이터소스( `DataSource` )를 만들어서 제공한
     * 다. 바로 앞서 우리가 직접 만든 데이터소스와 비슷하다 생각하면 된다.
     * @Transactional이 붙어있는 테스트 덕분에..?
     */

}
