# SearchEngine
Приложение, которое индексирует web-страницы и осуществляет по ним быстрый поиск (поисковой движок). 
## Содержание:
 1. [Краткое описание проекта](#Description)
 2. [Пример работы](#Example)
 3. [Запуск приложения](#Setup)
 4. [Документация](#Documentation)
    1. [Используемый стек технологий](#Steck)
    2. [Описание структуры приложения](#Application-structure)
    3. [Подробное описание работы и устройства проекта](#Description-Package)
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
Для Windows Jar архив запускается через коммандную строку 
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
Ниже приведена схема проекта MVC, весть frontend размещени в resources. 

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
  |      |         |   +- FindMatchesSnippets.java
  |      |         |   +- LemmaСonverter.java
  |      |         |   +- HandlerInterceptorLog.java
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
  +- pom.xml
````
</details>


<details>
<summary>
 
#### Подробное описание работы и устройства проекта 
</summary>

В проекте содержаться пакеты config, controllers, dto, model, repository, services, utility и папка resources.<br>
Подробнее о каждом. 

##### Пакет config
Cодержит три класса MvcConfig, Site, SitesList.<br>
Класс MvcConfig является конфигурационным классом Spring Boot и содержит единственный переопределенный метод addInterceptors, который добавляет перехватчик RequestResponseLoggerInterceptor для сканирования классов в пакете controllers и записи в журнал поступающих запросов и результатов ответов (не содержание ответа).<br>
Класс Site предназначен для создания POJO объектов на основании данных, размещенных в разделе indexing-settings файла application.yaml.<br>
Класс SitesList создает список объектов Site.<br>


##### Пакет controllers
Cодержит два класса ApiController, DefaultController<br>
Класс ApiController является @RestController, возвращает данные в формате JSON. Содержит методы обрабатывающие get запросы на получение статистики (метод statistics), запуска индексации (метод startIndexing), остановки индексации (метод stopIndexing), поиска (метод search), а также post запрос на добавление/обновление страницы (метод indexPage)<dr>
Класс DefaultController является @Controller, возвращает HTML страницу index (стартовая страница).<br>

##### Пакет dto
Содержит dto (Data Transfer Objects) модели searchModel, statistics, StatusRequest. Модель DTO является шаблоном проектирования и предназначена для десереализации данных из базы данных в объект, передаваемый в @Controller для последующей передачи пользователю.<br>

##### Пакет model
Содержит POJO классы (за исключением ENUM класса IndexingStatus), аннотированные @Entity, тем самым обозначающие JPA (Java Persistence API) о создании и сохранении объектов в базе данных. Приведенные в проекте POJO классы имеют двунаправленные связи @OneToMany и @ManyToOne, связь @ManyToMany реализована через класс Index. Для более быстрого поиска классы Page и Lemma имеют индексацию по полям path и lemma, соответственно. 

##### Пакет repository
Содержит интерфейсы для взаимодействия с базой данных (формирования запросов к базе данных). Интерфейсы наследованы от JpaRepository, что позволяет использовать запросы из "коробки". Так же интерфейсы содержат кастомные JPQL (Java Persistence query language) запросы, помеченные аннотацией @Query. Отдельные запросы, вносящие изменения в базу данных, помечены аннотацией @Transactional для обеспечения атомарности выполнения запроса и аннотацией @Modifying(clearAutomatically = true), указывающей на модифицированный запрос с автоматической очисткой базового контекста сохранения после записи в базу данных.   

##### Пакет services
Содержит два интерфейса IndexingService и StatisticsService, а также два класса их реализации IndexingServiceImpl и StatisticsServiceImpl. Классы, имплементирующие соответствующий интерфейс, содержат основную бизнес-логику.<br> 
Класс IndexingServiceImpl имплементирован от интерфейса IndexingService. Данный класс реализует логику старта, остановки, обновления/добавления страницы или сайта индексации, также в данном классе реализована логика поиска и формирования итогового DTO объекта, возвращаемого в класс @RestController.

##### Пакет utility
Содержит пять классов ApiExceptionHandler,FindMatchesSnippets, LemmaСonverter, HandlerInterceptorLog, SiteIndexing.<br> 
Класс ApiExceptionHandler отлавливает Exceptions. Отдельно выделены Exceptions, содержащие HTTP статусы 404, 400, 405. Также данный класс обрабатывает все остальные исключения, возвращая HTTP статус 500. Подробное описание исключения, его StackTrace записывается в файл log.txt и выводится в консоль.<br> 
Класс FindMatchesSnippets имплементирован интерфейсом Callable для возможности создания сниппетов в многопоточной среде. Единственный метод call возвращает сниппеты для DTO объектов.<br> 
Класс LemmaСonverter имеет единственный мотод convertTextToLemmas, предназначенный для конвертации текста в леммы. Метод convertTextToLemmas возвращает список лемм, найденных в передаваемом методу тексте.<br> 
Класс HandlerInterceptorLog является классом перехватчиком на уровне @RestController. Данный класс имплементирован от интерфейса HandlerInterceptor и содержит переопределенные методы реализации preHandle(перехват на уровне поступления запроса), postHandle(перехват на уровне сформированного ответа Resonse), afterCompletion (перехват на уровне после отдачи ответа). Основная реализация методов заключается в логировании поступающих запросов, времени их отработки и возвращаемых ответов. <br> 
Класс SiteIndexing унаследован от RecursiveTask. Является реализаций ForkJoinPool. Данный класс представляет основную логику парсинга сайтов/страниц и запись получаемой информации в базу данных MySql. <br>

##### Директория resources
Папка resources содержит реализацию frontend (static/assets  и templates), а также файлы application.yaml и logback-spring.xml. <br> 
Файл application.yaml содержит:
- порт подключения;
- основные настройки подключения к базе данных MySql;
- настройки HTTP клиента;
- настройки выброса исключение, если страница не найдена (HTTP статус 404);
- настройки отключения показа статического контента(необходимо для реализации выброса исключения при HTTP статусе 404);
- настройки Jpa(указан диалект MySQLDialect, поведение Hibernate по умолчанию update (обновление базы данных), отображение запросов в консоли true);
- раздел indexing-settings содержит исходный перечень сайтов для индексации.<br>
Файл logback-spring.xml содержит настойки логирования. Логирование осуществляется как путем записи в файл, так и выводом в консоль. Уровень логирования выставлен на info.

##### Логирование
Логирование реализовано библиотекой Slf4j путем добавления аннотаций Lombok @Slf4j. 

##### Файл pom.xml
Является файлом для сборки Maven, содержит библиотеки зависимостей. 

</details>

</details>
