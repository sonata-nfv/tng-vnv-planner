-- create database planner;

create table if not exists test_plan
(
    id            bigint  not null
        constraint test_plan_pkey
            primary key,
    description   varchar(255),
    index         integer not null,
    nsd           oid,
    package_id    varchar(255),
    service_uuid  varchar(255),
    status        varchar(255),
    test_uuid     varchar(255),
    testd         oid,
    uuid          varchar(255),
    test_suite_id bigint  not null
        constraint fkr3dynl20c9b59u0277wm3n9t9
            references test_suite
);

-- alter table test_plan
--     owner to sonatatest;

create table if not exists test_suite
(
    id   bigint not null
    constraint test_suite_pkey
    primary key,
    uuid varchar(255)
    );

-- alter table test_suite
--     owner to sonatatest;