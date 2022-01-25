package com.neginet.nameprocessor.batch;

import com.neginet.nameprocessor.domain.Person;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;

import java.util.Map;

@Configuration
@Slf4j
public class Reader {

    @SneakyThrows
    @Bean(name = "person-reader")
    @StepScope
    public FlatFileItemReader<Person> personReader(@Value("#{stepExecutionContext['fileName']}") String filename) {

        FlatFileItemReader<Person> reader = new FlatFileItemReader<>();
        reader.setResource(new UrlResource(filename));

        var personTokenizer = new DelimitedLineTokenizer() {
            {
                setNames("lastName", "name");
                setDelimiter(",");
            }
        };

        var lineMapper = new PatternMatchingCompositeLineMapper<Person>();
        lineMapper.setTokenizers(Map.of("*,*--*", personTokenizer));
        var setMappers = Map.of("*,*--*",
                (FieldSetMapper<Person>) fieldSet -> new Person(fieldSet.getValues()[1].trim().replaceAll("( -- [A-Z|a-z]*)", ""),
                        fieldSet.getValues()[0]));
        lineMapper.setFieldSetMappers(setMappers);
        reader.setLineMapper(lineMapper);

        return reader;
    }
}
