# SearchEngine
Приложение, которое индексирует web-страницы и осуществляет по ним быстрый поиск (поисковой движок). 
## Содержание:
 1. [Краткое описание проекта](#Description)
 2. [Пример работы](#Example)
 3. [Запуск приложения](#Setup)
 4. [Документация](#Documentation)
    1. [Используемый стек технологий](#Steck)]
    2. [Описание структуры приложения](#Application structure)
    3. [Подробное описание работы пакетов](#Description Package)
 5. [Дополнительная информация](#Additional information)
***

###Краткое описание проекта<a name="Description"></a>
Цель проекта упростить поиск необходимых страниц при ограниченном использовании ресурсов, например для локального поиска на определенных сайтах. 
Проект предназначен как для размещения на сервере, так и на локальном компьютере, ***при наличии JRE и локальной базы данных MySQL***.
Приложение осуществляет парсинг сайтов, лемитизирует текст на отдельные лемы, после чего индексирует лемы и по индексам осуществляет поиск страниц. 
Подробное описание проекта приведено в разделе [Документация](#Documentation)
***
###Пример работы<a name="Example"></a>
На стартовой странице отображается перечень сайтов, статус индексации, статистика индексации.

![Стартовая страница](#.\serchEngine\start screen.png)



***
