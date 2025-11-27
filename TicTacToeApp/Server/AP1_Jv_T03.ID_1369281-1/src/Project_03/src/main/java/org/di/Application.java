package org.di;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = {
        "org.di",
        "org.web",
        "org.web.utils",
        "org.domain",
        "org.datasource",
        "org.domain.service",
        "org.domain.service.impl",
        "org.datasource.repository",
        "org.datasource.repository.impl"
})
public class Application {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);

        checkBean(context, "authController");
        checkBean(context, "gameController");
        checkBean(context, "gameServiceImplWithRepository");
        checkBean(context, "gameToDtoMapper");
        checkBean(context, "dtoToGameMapper");

        // ПРОВЕРКА РЕПОЗИТОРИЯ
        try {
            Object repo = context.getBean("inMemoryGameRepositoryImpl");
            System.out.println("✅ GameRepository создан: " + repo.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("❌ GameRepository НЕ создан: " + e.getMessage());
        }
    }

    private static void checkBean(ApplicationContext context, String beanName) {
        try {
            Object bean = context.getBean(beanName);
            System.out.println("✅ " + beanName + " создан: " + bean.getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("❌ " + beanName + " НЕ создан: " + e.getMessage());

            // ДЛЯ gameService ПРОВЕРЯЕМ АВТОМАТИЧЕСКОЕ ИМЯ
            if ("gameService".equals(beanName)) {
                try {
                    Object beanByAutoName = context.getBean("gameServiceImplWithRepository");
                    System.out.println("🔍 GameService создан под именем: gameServiceImplWithRepository");
                } catch (Exception e2) {
                    System.out.println("🔍 GameService не найден под автоматическим именем");
                }
            }
        }
    }
}