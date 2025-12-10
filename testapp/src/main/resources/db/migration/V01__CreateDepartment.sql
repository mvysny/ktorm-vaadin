create table t_department
(
    id       int          not null primary key auto_increment,
    name     varchar(128) not null,
    location varchar(128) not null
);
create unique index idx_department_name ON t_department(name);
create index idx_department_location ON t_department(location);
