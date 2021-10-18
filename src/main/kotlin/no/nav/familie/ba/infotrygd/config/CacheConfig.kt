package no.nav.familie.ba.infotrygd.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class  CacheConfig {

    @Bean
    fun perioderCacheManager(): CacheManager {
        val caffeine = Caffeine
            .newBuilder()
            .initialCapacity(100)
            .maximumSize(200000)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .recordStats()
        val caffeineCacheManager = CaffeineCacheManager()
        caffeineCacheManager.setCaffeine(caffeine)
        return caffeineCacheManager
    }


    @Bean
    @Primary
    fun personerCacheManager(): CacheManager {
        val caffeine = Caffeine
            .newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.DAYS)
            .recordStats()
        val caffeineCacheManager = CaffeineCacheManager()
        caffeineCacheManager.setCaffeine(caffeine)
        return caffeineCacheManager
    }
}