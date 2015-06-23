CREATE USER 'ema'@'%' IDENTIFIED BY 'password';

create database ema;

grant all on ema.* to 'ema'@'%';

flush privileges;
