### Run Local
```
mvn spring-boot:run
```
### Tests
Unit Testing
```
mvn test
```

### application.yml

We can find inside /resources folder an application.yml file as default

The Yml files contains the main properties used in our application

- modified-names: quantity of N for the modified list of names 
  
  default: (25)

- file.path: path where the file(s) will be read 

  default: classpath*:/batch/*.txt

- file.result-name: name of the file with all the results  
  
  default: result.txt

- batch.chunk-size: quantity of names read per batch 
  
  default: (200)
  
- batch.partitioner-size: quantity of files to be read in the same folder 
  
  default: 10

- executor.pool-size: quantity of threads to be use per batch 
  
  default: 5

### Run with different arguments

change quantity of modified names
```
mvn spring-boot:run -Dmodified-names=N
```

change path of file(s) to be read
```
mvn spring-boot:run -Dfile.result-name=classpath*:/other-folder/*.txt
```

change name of result file
```
mvn spring-boot:run -Dfile.result-name=other-name.txt
```

change batch size
```
mvn spring-boot:run -Dbatch.chunk-size=N
```

change quantity of files to be read
```
mvn spring-boot:run -Dbatch.partitioner-size=N
```

change quantity of threads 
```
mvn spring-boot:run -Dexecutor.pool-size=N
```
### Run mixing different arguments 

```
mvn spring-boot:run -Dmodified-names=25 -Dfile.result-name=classpath*:/batch/*.txt -Dexecutor.pool-size=5 -Dfile.result-name=result.txt -Dbatch.chunk-size=200 -Dbatch.partitioner-size=10
```