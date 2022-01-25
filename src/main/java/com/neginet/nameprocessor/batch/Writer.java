package com.neginet.nameprocessor.batch;

import com.neginet.nameprocessor.domain.NameStructures;
import com.neginet.nameprocessor.domain.Person;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class Writer {

    @Value("${modified-names}")
    private int modifiedNamesSize;

    @Bean(name = "person-writer")
    public ItemWriter<Person> personWriter() {
        return list -> list.stream()
                .parallel()
                .limit(modifiedNamesSize)
                .filter(person -> NameStructures.modifiedNames.size() < modifiedNamesSize)
                .filter(person -> !NameStructures.modifiedNames.containsKey(person.getName()) && !NameStructures.modifiedNames.containsValue(person.getLastName()))
                .peek(person -> NameStructures.modifiedNames.put(person.getName(), person.getLastName()))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public void write(String fileName, String formattedString) {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(formattedString);
        writer.close();
    }
}
