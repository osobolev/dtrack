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

insert into reports (project_id, visible_id, name, simple_query) values (1, 1, 'Все баги', 'true');
insert into reports (project_id, visible_id, name, simple_query) values (1, 2, 'Мои задачи на тестирование', 'b.assigned_user_id = $user or (b.assigned_user_id is null and b.state_code = "testing")');
insert into reports (project_id, visible_id, name, simple_query) values (1, 3, 'Мои задачи на разработку', 'b.assigned_user_id = $user or (b.assigned_user_id is null and b.state_code in ("new", "rework"))');
insert into reports (project_id, visible_id, name, simple_query) values (1, 4, 'Все активные', 'b.state_code <> "closed"');
insert into reports (project_id, visible_id, name, simple_query) values (1, 5, 'Все зввершенные', 'b.state_code = "closed"');
insert into reports (project_id, visible_id, name, json_query) values (1, 6, 'Мои + остальные активные', '["b.assigned_user_id = $user", "(b.assigned_user_id <> $user or b.assigned_user_id is null) and b.state_code <> ''closed''"]');
