package com.neginet.nameprocessor.batch.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class BatchConfig {

    @Value("${file.path}")
    private String filePath;

    @Value("${executor.pool-size}")
    private int poolSize;

    @Value("${batch.partitioner-size}")
    private int partitionerSize;

    @Bean("masterJob")
    public Job masterJob(final JobBuilderFactory jobBuilderFactory,
                         final @Qualifier("masterStep") Step masterStep,
                         final @Qualifier("resultStep") Step resultStep) {
        return jobBuilderFactory
                .get("masterJob")
                .start(masterStep)
                .next(resultStep)
                .build();
    }

    @Bean
    @Qualifier("masterStep")
    public Step masterStep(final StepBuilderFactory stepBuilderFactory,
                           final @Qualifier("processDataStep") Step processData) {
        return stepBuilderFactory.get("masterStep")
                .partitioner("processData", partitioner())
                .step(processData)
                .taskExecutor(taskExecutor())
                .build();
    }

    @SneakyThrows
    @Bean
    public Partitioner partitioner() {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources = resolver.getResources(filePath);
        partitioner.setResources(resources);
        partitioner.partition(partitionerSize);
        return partitioner;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(poolSize);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

}
