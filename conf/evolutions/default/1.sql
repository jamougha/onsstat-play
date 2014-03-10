# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table test (
  id                        bigint not null,
  value                     varchar(255),
  constraint pk_test primary key (id))
;

create sequence test_seq;




# --- !Downs

drop table if exists test cascade;

drop sequence if exists test_seq;

