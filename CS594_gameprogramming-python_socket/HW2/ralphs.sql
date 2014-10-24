create database ralphs;
use ralphs;
drop table if exists users;
create table users (
    id              integer auto_increment primary key,
    user_name      varchar(255),
    user_password       varchar(255) 
   
);
insert into users values(1,'swaril','abcd');