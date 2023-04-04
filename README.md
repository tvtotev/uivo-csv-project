Csv Parser project
------------------

This is a CMD Java application to
extract and represents data which are exported in CSV format from the Secondary Market page of UIVO site:
``https://www.iuvo-group.com/en/market/#/p0=secondary_market_pub_page;p2=undefined;lang=en_US``

Following 3 representations are implemented:

1. Препоръчва инвестиция в кредита с най-висока доходност, при който минималната сума за инвестиция е 10 лв или евро и не е в закъснение
2. Сортира списъка по матуритет на кредитите спрямо оставащите брой вноски и типа им
3. Показва средната доходност на кредитите за всеки от оригинаторите

---------

* Interest rate е доходността на годишна база
* Броят на оставащите вноски е колона Term – числото извън скобите
* Типът на вноските е в колона Instalment type – обикновено на всеки 7 или 30 дни (7 days / 30 days)
* Закъснението е в колона Status. Current означава, че няма закъснение.

---------

Snapshot/Sample with a data is in ``src/main/resources/csv/Loans.csv``
This is the default location for the CSV file location
Default CSV separator is a 'TAB' char

To build application is required:
* maven
* java 8+

How to build
----------------------------------------------------------------------------
mvn clean install

How to print usage:
----------------------------------------------------------------------------
``java -jar target/csv-project-0.1-SNAPSHOT-jar-with-dependencies.jar --help``

How to specify custom CSV file location & CSV separator
----------------------------------------------------------------------------
``java -jar target/csv-project-0.1-SNAPSHOT-jar-with-dependencies.jar -file=src/main/resources/csv/Loans.csv -separator=,``

Note: The only way to be used "TAB" char as separator is to skip ``-separator`` parameter.
Then "TAB" will be used as default separator
``java -jar target/csv-project-0.1-SNAPSHOT-jar-with-dependencies.jar -file=src/main/resources/csv/Loans.csv``

Note: If is is used and unproper separator, the following error will be shown:
``Exception in thread "main" java.sql.SQLException: Wrong number of columns in line: 1 Columns read: 1 Columns expected: 13``

How to specify exact representation
----------------------------------------------------------------------------
To print a representation  (any of from the listed 3 representations above),
just add its number as argument.

To print any of them, execute following samples:

Representation 1: Препоръчва инвестиция в кредита с най-висока доходност, при който минималната сума за инвестиция е 10 лв или евро и не е в закъснение

``java -jar target/csv-project-0.1-SNAPSHOT-jar-with-dependencies.jar 1``

Representation 1: Сортира списъка по матуритет на кредитите спрямо оставащите брой вноски и типа им

``java -jar target/csv-project-0.1-SNAPSHOT-jar-with-dependencies.jar 2``

Representation 1: Показва средната доходност на кредитите за всеки от оригинаторите

``java -jar target/csv-project-0.1-SNAPSHOT-jar-with-dependencies.jar 3``

To print all of them at once, execute

``java -jar target/csv-project-0.1-SNAPSHOT-jar-with-dependencies.jar``

or

``java -jar target/csv-project-0.1-SNAPSHOT-jar-with-dependencies.jar 0``
