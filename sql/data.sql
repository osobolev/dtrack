insert into users
  (id, login, pass_hash)
  values
  (1, 'user', '');

insert into projects
  (id, name)
  values
  (1, 'Template');

insert into user_access
  (project_id, user_id)
  values
  (1, 1);

insert into states
  (id, project_id, order_num, name, is_default)
  values
  (1, 1, 1, 'Новый', true);
insert into states
  (id, project_id, order_num, name)
  values
  (2, 1, 2, 'Тестирование');
insert into states
  (id, project_id, order_num, name)
  values
  (3, 1, 3, 'Доработка');
insert into states
  (id, project_id, order_num, name)
  values
  (4, 1, 4, 'Закрыт');

insert into priorities
  (id, project_id, order_num, name)
  values
  (1, 1, 1, 'Высокий');
insert into priorities
  (id, project_id, order_num, name, is_default)
  values
  (2, 1, 2, 'Средний', true);
insert into priorities
  (id, project_id, order_num, name)
  values
  (3, 1, 3, 'Низкий');
