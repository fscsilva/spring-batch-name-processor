package com.neginet.nameprocessor.batch;

import com.neginet.nameprocessor.domain.NameStructures;
import com.neginet.nameprocessor.domain.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@Slf4j
public class Processor {

    @Bean(name = "person-processor")
    public ItemProcessor<Person, Person> personProcessor() {
        return person -> {
            validateName(person);
            validateLastName(person);
            validateFullName(person);
            return person;
        };
    }

    private void validateName(Person person) {
        var nameCount =  NameStructures.names.get(person.getName());
        if (Objects.nonNull(nameCount)) {
            NameStructures.names.put(person.getName(), nameCount + 1);
        } else {
            NameStructures.names.put(person.getName(), 1);
        }
    }

    private void validateFullName(Person person) {
        var fullName = person.getName() + person.getLastName();
        var nameCount =  NameStructures.fullNames.get(fullName);
        if (Objects.nonNull(nameCount)) {
            NameStructures.fullNames.put(fullName, nameCount + 1);
        } else {
            NameStructures.fullNames.put(fullName, 1);
        }
    }

    private void validateLastName(Person person) {
        var lastNameCount =  NameStructures.lastNames.get(person.getLastName());
        if (Objects.nonNull(lastNameCount)) {
            NameStructures.lastNames.put(person.getLastName(), lastNameCount + 1);
        } else {
            NameStructures.lastNames.put(person.getLastName(), 1);
        }
    }
}
