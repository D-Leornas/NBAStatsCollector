# StatsCollector
Collects data straight from Nba.stats.com's REST API, parses the messages, and stores the statistics into a database in real time.

This project is run on Ubuntu through WSL. In order to have this project work on your system, you need to set an environmental variable DBSTRING to the database string that connects to your database and have Java 17 installed. Start the program at any point before the games start and it will run until the last game ends. The program can still function after the games start, but it will send many inserts to your database server at once which may cause it to not function correctly.
