insert into users
  (id, login, pass_hash)
  values
  (1, 'user', '');

insert into projects
  (id, name)
  values
  (1, 'template');

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

insert into transitions
  (project_id, from_id, to_id, name)
  values
  (1, 1, 2, 'Тестировать');
insert into transitions
  (project_id, from_id, to_id, name)
  values
  (1, 2, 3, 'На доработку');
insert into transitions
  (project_id, from_id, to_id, name)
  values
  (1, 2, 4, 'Закрыть');
insert into transitions
  (project_id, from_id, to_id, name)
  values
  (1, 3, 2, 'Тестировать');

insert into priorities
  (id, project_id, order_num, name, color)
  values
  (1, 1, 1, 'Высокий', '#fed');
insert into priorities
  (id, project_id, order_num, name, color, is_default)
  values
  (2, 1, 2, 'Средний', '#ffb', true);
insert into priorities
  (id, project_id, order_num, name, color)
  values
  (3, 1, 3, 'Низкий', '#fbfbfb');

insert into reports
  (id, project_id, visible_id, name)
  values
  (1, 1, 1, 'Все баги');
