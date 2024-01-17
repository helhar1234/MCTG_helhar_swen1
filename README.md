# MCTG - Monster Card Trading Game

Welcome to the Monster Card Trading Game (MCTG) repository! This repository contains the source code and assets for a
trading card game where players can collect, trade, and battle with monster cards. It was developed as part of the SWEN1
course at FH TECHNIKUM WIEN.

## About the Game

MCTG is a fun and engaging card game that allows players to:

- Collect monster cards.
- Build their decks and customize their strategies.
- Challenge other players to epic card battles.
- Trade cards with other players to complete their collections.

## Getting Started

To get started with the game, follow these steps:

1. Open a new Maven Project in IntelliJ IDEA (my Version: 2023.2.4)

2. Clone this repository into your project:

   ```bash
   https://github.com/helhar1234/MCTG_helhar_swen1.git

3. Start Docker and fetch the needed Database Container and start it
    ```bash
   docker pull helhar1234/my_mtcg_db:1.0
   docker run -d --name my_mtcg_container -p 5432:5432 helhar1234/my_mtcg_db:1.0

4. Connect to Database in IntelliJ (see connection data in databas/Database.java file)
5. Run the sql Script sql/mtcg-script.sql to create the database
6. Start the Application (Main.java)
7. Run the CURL Script


