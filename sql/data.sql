insert into states (code, name) values ('new', 'Новый');
insert into states (code, name) values ('testing', 'Тестирование');
insert into states (code, name) values ('rework', 'Доработка');
insert into states (code, name) values ('closed', 'Закрыт');

insert into priorities (code, name, color) values ('high', 'Высокий', '#fed');
insert into priorities (code, name, color) values ('mid', 'Средний', '#ffb');
insert into priorities (code, name, color) values ('low', 'Низкий', '#fbfbfb');

insert into users (id, login, pass_hash) values (1, 'user', '');

insert into projects (id, name) values (1, 'template');

insert into user_access (project_id, user_id) values (1, 1);

insert into project_states (project_id, code, order_num) values (1, 'new', 1);
insert into project_states (project_id, code, order_num) values (1, 'testing', 2);
insert into project_states (project_id, code, order_num) values (1, 'rework', 3);
insert into project_states (project_id, code, order_num) values (1, 'closed', 4);
update project_states set is_default = (code = 'new');

insert into transitions (project_id, code_from, code_to, name) values (1, 'new', 'testing', 'Тестировать');
insert into transitions (project_id, code_from, code_to, name) values (1, 'testing', 'rework', 'На доработку');
insert into transitions (project_id, code_from, code_to, name) values (1, 'testing', 'closed', 'Закрыть');
insert into transitions (project_id, code_from, code_to, name) values (1, 'rework', 'testing', 'Тестировать');

insert into project_priorities (project_id, code, order_num) values (1, 'high', 1);
insert into project_priorities (project_id, code, order_num) values (1, 'mid', 2);
insert into project_priorities (project_id, code, order_num) values (1, 'low', 3);
update project_priorities set is_default = (code = 'mid');

insert into reports (id, project_id, visible_id, name) values (1, 1, 1, 'Все баги');
