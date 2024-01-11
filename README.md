# StatsCollector
Collects data straight from Nba.stats.com's REST API, parses the messages, and stores the statistics into a database in real time.

This project runs on Ubuntu through WSL on my system. In order to have this project work on your system, you need to set up a database with at least the player_game_stats and games tables set up, an environmental variable DBSTRING to the database string that connects to your database, and have Java 17 installed. Start the program at any point before the games start and it will run until the last game ends. The program can still function if you start it after the games start. Efforts have been set forth to try to minimize stress on the database when trying to catch up to live, but it still may not function correctly.
