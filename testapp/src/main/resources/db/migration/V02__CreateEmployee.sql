create table t_employee
(
    id            int          not null primary key auto_increment,
    name          varchar(128) not null,
    job           varchar(128) not null,
    manager_id    int null,
    hire_date     date         not null,
    salary        bigint       not null,
    department_id int          not null
);

create index idx_e_name on t_employee(name);
create index idx_e_job on t_employee(job);
create index idx_e_manager on t_employee(manager_id);
create index idx_e_hire_date on t_employee(hire_date);
create index idx_e_salary on t_employee(salary);
create index idx_e_depid on t_employee(department_id);
