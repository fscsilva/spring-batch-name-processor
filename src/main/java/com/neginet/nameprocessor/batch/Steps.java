package com.neginet.nameprocessor.batch;

import com.neginet.nameprocessor.domain.NameStructures;
import com.neginet.nameprocessor.domain.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class Steps {

    private final Writer writer;

    @Value("${batch.chunk-size}")
    private int chunkSize;

    @Value("${file.result-name}")
    private String filename;

    @Bean
    @Qualifier("processDataStep")
    public Step processDataStep(final StepBuilderFactory stepBuilderFactory,
                                @Qualifier("person-reader")final FlatFileItemReader<Person> reader,
                                @Qualifier("person-processor") ItemProcessor<Person, Person> processor,
                                @Qualifier("person-writer") ItemWriter<Person> writer) {
        return stepBuilderFactory.get("processData")
                .<Person, Person>chunk(chunkSize)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .build();
    }

    @Bean
    @Qualifier("resultStep")
    public Step resultStep(final StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("resultStep")
                .tasklet((stepContribution, chunkContext) -> {

                    buildNameResults();
                    var changedNames = changeNames();

                    var result = String.format("\nThe names cardinality for full, last, and first names:" +
                                    "\nFull names : %s \nLast names : %s \nFirst names : %s \nThe most common last names are: \n%s\n" +
                                    "The most common first names are: \n%s \nModified names are: \n%s\n",
                            NameStructures.fullNames.size(), NameStructures.lastNames.size(), NameStructures.names.size(),
                            NameStructures.lastNames.entrySet().stream()
                                    .limit(10)
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                            LinkedHashMap::new)),
                            NameStructures.names.entrySet().stream()
                                    .limit(10)
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                            LinkedHashMap::new)),
                            changedNames);

                    log.info(result);
                    writer.write(filename, result);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private List<String> changeNames() {
        var newValues = Arrays.asList(NameStructures.modifiedNames.values().toArray());
        var newKeys = NameStructures.modifiedNames.keySet().toArray();
        Collections.rotate(newValues, 1);
        return IntStream.range(0, newKeys.length).boxed()
                .collect(Collectors.toMap(i -> newKeys[i], newValues::get))
                .entrySet()
                .stream()
                .map(entry -> entry.getValue()+" "+entry.getKey())
                .collect(Collectors.toList());
    }

    private void buildNameResults() {
        NameStructures.names = NameStructures.names.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        NameStructures.lastNames = NameStructures.lastNames.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        NameStructures.fullNames = NameStructures.fullNames.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
    }
}
