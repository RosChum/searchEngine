# SearchEngine
Приложение, которое индексирует web-страницы и осуществляет по ним быстрый поиск (поисковой движок). 
## Содержание:
 1. [Краткое описание проекта](#Description)
 2. [Пример работы](#Example)
 3. [Запуск приложения](#Setup)
 4. [Документация](#Documentation)
    1. [Используемый стек технологий](#Steck)
    2. [Описание структуры приложения](#Application-structure)
    3. [Подробное описание работы пакетов](#Description-Package)
 5. [Дополнительная информация](#Additional-information)
***
<a name="Description"></a>
### Краткое описание проекта 
Цель проекта упростить поиск необходимых страниц при ограниченном использовании ресурсов, например для локального поиска на определенных сайтах. 
Проект предназначен как для размещения на сервере, так и на локальном компьютере, ***при наличии JRE и локальной базы данных MySQL***.
Приложение осуществляет парсинг сайтов, лемитизирует текст на отдельные лемы, после чего индексирует лемы и по индексам осуществляет поиск страниц. 
Подробное описание проекта приведено в разделе [Документация](#Documentation)
***
<a name="Example"></a>
### Пример работы 
На стартовой странице отображается перечень сайтов, статус индексации, статистика индексации.

![start screen.png](/AssetsForReadMe/start%20screen.png)

Во вкладке **Management** предусмотрено две кнопки:<br>
**START INDEXING**- запуск индексации сайтов, размещенных в application.yaml<br>
**ADD/UPDATE** - добовляет / обновляет страницу сайта из перечня сайтов, размещенных в application.yaml
![Management](/AssetsForReadMe/Management.png)

Во вкладке **Serch** осуществляется непосредственно поиск. Имеется выпадающий список для возможности осуществления поиска на определенном сайте. 

![Serch](AssetsForReadMe/Search.png)
***
<a name="Setup"></a>
### Запуск приложения 
Jar архив запускается через коммандную строку 
```
java -jar SearchEngine-1.0-SNAPSHOT.jar
```

***
<a name="Documentation"></a>
<details>
<summary>
 
### Документация
<a name="Steck"></a>
</summary>

<details>
<summary>
 
#### Используемый стек технологий
</summary>
- Java 17<br>
- Spring Boot (v2.7.1)<br>
- Spring MVC<br>
- Spring Data<br>
- Lombok<br>
- MySql<br>
- Jsoup<br>
- Maven<br>
</details>
<details>
<summary>
 
#### Описание структуры приложения
</summary>

````
+- searchEngine
  +- src
  |      +- main
  |      |     +- java
  |      |       +- searchengine
  |      |         +- config
  |      |         |   +- MvcConfig.java
  |      |         |   +- Site.java
  |      |         |   +- SitesList.java
  |      |         +- controllers
  |      |         |   +- ApiController.java
  |      |         |   +- DefaultController.java
  |      |         +- dto
  |      |         |   +- searchModel
  |      |         |   |   +- DtoSearchPageInfo.java
  |      |         |   |   +- ResultSearch.java
  |      |         |   +- statistics
  |      |         |   |   +- DetailedStatisticsItem.java
  |      |         |   |   +- StatisticsData.java
  |      |         |   |   +- StatisticsResponse.java
  |      |         |   |   +- TotalStatistics.java
  |      |         |   +- StatusRequest.java
  |      |         +- model
  |      |         |   +- Index.java
  |      |         |   +- IndexingStatus.java
  |      |         |   +- Lemma.java
  |      |         |   +- Page.java
  |      |         |   +- Site.java
  |      |         +- repository
  |      |         |   +- IndexRepository.java
  |      |         |   +- LemmaRepository.java
  |      |         |   +- PageRepository.java
  |      |         |   +- SiteRepository.java
  |      |         +- services
  |      |         |   +- IndexingService.java
  |      |         |   +- IndexingServiceImpl.java
  |      |         |   +- StatisticsService.java
  |      |         |   +- StatisticsServiceImpl.java
  |      |         +- utility
  |      |         |   +- ApiExceptionHandler.java
  |      |         |   +- LemmaСonverter.java
  |      |         |   +- RequestResponseLoggerInterceptor.java
  |      |         |   +- SiteIndexing.java
  |      |         +- Application.java
  |      |         +- CommandLineRunnerImpl.java
  |      +- resources
  |          +- static/assets
  |          |   +- css
  |          |   +- fonts/Montserrat
  |          |   +- img/icons
  |          |   +- js
  |          |   +- plg
  |          +- templates
  |          |   +- index.html
  |          +- application.yaml
  |          +- logback-spring.xml
  +- AssetsForReadMe
  +- README.md
  +- application.yaml
  +- pom.xml
````
</details>





</details>




***
#### Подробное описание работы пакетов 
