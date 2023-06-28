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

### Краткое описание проекта<a name="Description"></a>
Цель проекта упростить поиск необходимых страниц при ограниченном использовании ресурсов, например для локального поиска на определенных сайтах. 
Проект предназначен как для размещения на сервере, так и на локальном компьютере, ***при наличии JRE и локальной базы данных MySQL***.
Приложение осуществляет парсинг сайтов, лемитизирует текст на отдельные лемы, после чего индексирует лемы и по индексам осуществляет поиск страниц. 
Подробное описание проекта приведено в разделе [Документация](#Documentation)
***
### Пример работы<a name="Example"></a>
На стартовой странице отображается перечень сайтов, статус индексации, статистика индексации.

![start screen.png](/AssetsForReadMe/start%20screen.png)

Во вкладке **Management** предусмотрено две кнопки:<br>
**START INDEXING**- запуск индексации сайтов, размещенных в application.yaml<br>
**ADD/UPDATE** - добовляет / обновляет страницу сайта из перечня сайтов, размещенных в application.yaml
![Management](/AssetsForReadMe/Management.png)

Во вкладке **Serch** осуществляется непосредственно поиск. Имеется выпадающий список для возможности осуществления поиска на определенном сайте. 

![Serch](AssetsForReadMe/Search.png)
***
### Запуск приложения<a name="Setup"></a>
Jar архив запускается через коммандную строку 
```
java -jar SearchEngine-1.0-SNAPSHOT.jar
```


***
### Документация<a name="Documentation"></a>
#### Используемый стек технологий<a name="Steck"></a>

***
#### Описание структуры приложения<a name="Application-structure"></a>

***
#### Подробное описание работы пакетов<a name="Description-Package"></a>
