CREATEDATABASE
my_mtcg_db;
\c
my_mtcg_db;

SET
timezone='CET';


CREATE TABLE users
(
    user_id   VARCHAR(255) PRIMARY KEY,
    username  VARCHAR(50) UNIQUE NOT NULL,
    password  VARCHAR(255)       NOT NULL,
    coins     INT     DEFAULT 20 CHECK (coins >= 0),
    eloRating INT     DEFAULT 100,
    isAdmin   boolean DEFAULT false
);


CREATE TABLE access_token
(
    token_id        SERIAL PRIMARY KEY,
    user_fk         VARCHAR(255) REFERENCES users (user_id) ON DELETE CASCADE,
    token_name      VARCHAR(255) UNIQUE NOT NULL,
    token_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE userdata
(
    userdata_id SERIAL PRIMARY KEY,
    user_fk     VARCHAR(255) REFERENCES users (user_id) ON DELETE CASCADE,
    name        VARCHAR(255),
    bio         VARCHAR(255) DEFAULT 'Hello there, I am playing MTCG :)',
    image       VARCHAR(255) DEFAULT '\'
);


CREATE TABLE cards
(
    card_id     VARCHAR(255) PRIMARY KEY,
    name        VARCHAR(255),
    damage      INT CHECK (damage > 0),
    elementType VARCHAR(255) CHECK (elementType IN ('water', 'fire', 'normal')),
    cardType    VARCHAR(255) CHECK (cardType IN ('spell', 'monster'))
);


CREATE TABLE packages
(
    orderid    SERIAL UNIQUE,
    package_id VARCHAR(255) PRIMARY KEY,
    price      INT     DEFAULT 5 CHECK (price > 0),
    sold       BOOLEAN DEFAULT false
);


CREATE TABLE cards_packages
(
    card_fk    VARCHAR(255),
    package_fk VARCHAR(255),
    FOREIGN KEY (card_fk) REFERENCES cards (card_id),
    FOREIGN KEY (package_fk) REFERENCES packages (package_id),
    PRIMARY KEY (card_fk, package_fk)
);


CREATE TABLE user_cards
(
    user_fk VARCHAR(255),
    card_fk VARCHAR(255),
    inDeck  BOOLEAN DEFAULT false,
    FOREIGN KEY (card_fk) REFERENCES cards (card_id),
    FOREIGN KEY (user_fk) REFERENCES users (user_id),
    PRIMARY KEY (user_fk, card_fk)
);


CREATE TABLE trades
(
    trade_id       VARCHAR(255) PRIMARY KEY,
    user_fk        VARCHAR(255),
    card_fk        VARCHAR(255),
    FOREIGN KEY (card_fk) REFERENCES cards (card_id),
    FOREIGN KEY (user_fk) REFERENCES users (user_id),
    expectedType   VARCHAR(255),
    expectedDamage VARCHAR(255)
);


CREATE TABLE battles
(
    battle_id   VARCHAR(255) PRIMARY KEY,
    player_a_fk VARCHAR(255),
    FOREIGN KEY (player_a_fk) REFERENCES users (user_id),
    player_b_fk VARCHAR(255),
    FOREIGN KEY (player_b_fk) REFERENCES users (user_id),
    winner_fk   VARCHAR(255),
    FOREIGN KEY (winner_fk) REFERENCES users (user_id),
    start_time  TIMESTAMP,
    status      VARCHAR(255) CHECK (status IN ('waiting', 'active', 'completed'))
);


CREATE TABLE battle_logs
(
    log_id    SERIAL PRIMARY KEY,
    battle_fk VARCHAR(255),
    FOREIGN KEY (battle_fk) REFERENCES battles (battle_id),
    log_entry TEXT
);


CREATE TABLE wheelOfFortune
(
    wheel_id   SERIAL PRIMARY KEY,
    user_fk    VARCHAR(255),
    FOREIGN KEY (user_fk) REFERENCES users (user_id),
    wheel_time DATE DEFAULT CURRENT_DATE
);

/*Ausführen bevor curl gestartet wird*/
DELETE
FROM wheelOfFortune;
DELETE
FROM user_cards;
DELETE
FROM cards_packages;
DELETE
FROM trades;
DELETE
FROM battle_logs;
DELETE
FROM battles;
DELETE
FROM users;
DELETE
FROM access_token;
DELETE
FROM userdata;
DELETE
FROM cards;
DELETE
FROM packages;



