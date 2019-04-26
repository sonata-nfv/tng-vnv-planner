-- create database planner;

create table if not exists test_plan
(
    id            bigint  not null
        constraint test_plan_pkey
            primary key,
    index         integer not null,
    package_id    varchar(255),
    uuid          varchar(255),
    service_uuid  varchar(255),
    test_uuid     varchar(255),
    description   varchar(255),
    status        varchar(255)
);

-- alter table test_plan
--     owner to sonatatest;

