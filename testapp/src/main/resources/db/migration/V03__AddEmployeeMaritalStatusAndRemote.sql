alter table t_employee add column marital_status varchar(20) not null default 'Single';
alter table t_employee add column remote boolean not null default false;

create index idx_e_marital_status on t_employee(marital_status);
create index idx_e_remote on t_employee(remote);
