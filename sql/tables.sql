CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  login TEXT NOT NULL,
  pass_hash BYTEA NOT NULL
);

CREATE TABLE projects (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  description TEXT,
  last_visible_id INT NOT NULL DEFAULT 0
);

CREATE TABLE user_access (
  user_id INT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    UNIQUE (user_id, project_id)
);

CREATE TABLE states (
  id SERIAL PRIMARY KEY,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  order_num INT NOT NULL,
  name TEXT NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (project_id, order_num)
);

CREATE TABLE transitions (
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  from_id INT NOT NULL REFERENCES states (id) ON DELETE CASCADE,
  to_id INT NOT NULL REFERENCES states (id) ON DELETE CASCADE,
  name TEXT NOT NULL,
    UNIQUE (project_id, from_id, to_id)
);

CREATE TABLE priorities (
  id SERIAL PRIMARY KEY,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  order_num INT NOT NULL,
  name TEXT NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (project_id, order_num)
);

CREATE TABLE reports (
  id SERIAL PRIMARY KEY,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  visible_id INT NOT NULL,
  name TEXT NOT NULL,
  sql_query TEXT, -- order: visible_id, created, modified, state_id->order_num, priority_id->order_num, 
    UNIQUE (project_id, visible_id)
);

CREATE TABLE bugs (
  id SERIAL PRIMARY KEY,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  visible_id INT NOT NULL,
  created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  create_user_id INT NOT NULL REFERENCES users (id),
  modify_user_id INT NOT NULL REFERENCES users (id),
  assigned_user_id INT REFERENCES users (id),
  state_id INT NOT NULL REFERENCES states (id),
  priority_id INT NOT NULL REFERENCES priorities (id),
  short_text TEXT,
  full_text TEXT,
    UNIQUE (project_id, visible_id)
);

CREATE TABLE bug_attachments (
  id SERIAL PRIMARY KEY,
  bug_id INT NOT NULL REFERENCES bugs (id) ON DELETE CASCADE,
  file_name TEXT NOT NULL,
  file_content BYTEA NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE changes (
  id SERIAL PRIMARY KEY,
  bug_id INT NOT NULL REFERENCES bugs (id) ON DELETE CASCADE,
  user_id INT NOT NULL REFERENCES users (id),
  ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE changes_fields (
  change_id INT NOT NULL REFERENCES changes (id) ON DELETE CASCADE,
  old_assigned_user_id INT REFERENCES users (id),
  new_assigned_user_id INT REFERENCES users (id),
  old_state_id INT REFERENCES states (id),
  new_state_id INT REFERENCES states (id),
  old_priority_id INT REFERENCES priorities (id),
  new_priority_id INT REFERENCES priorities (id),
  old_short_text TEXT,
  new_short_text TEXT,
  old_full_text TEXT,
  new_full_text TEXT
);

CREATE TABLE changes_files (
  change_id INT NOT NULL REFERENCES changes (id) ON DELETE CASCADE,
  old_attachment_id INT REFERENCES bug_attachments (id),
  new_attachment_id INT REFERENCES bug_attachments (id)
);

CREATE TABLE changes_comments (
  change_id INT NOT NULL REFERENCES changes (id) ON DELETE CASCADE,
  comment_text TEXT,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE comment_attachments (
  id SERIAL PRIMARY KEY,
  change_id INT NOT NULL REFERENCES changes (id) ON DELETE CASCADE,
  file_name TEXT NOT NULL,
  file_content BYTEA NOT NULL
);
