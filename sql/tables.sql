CREATE TABLE users (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  login TEXT NOT NULL,
  pass_hash BYTEA NOT NULL
);

CREATE TABLE projects (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
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
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  order_num INT NOT NULL,
  name TEXT NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (project_id, order_num)
);

CREATE TABLE priorities (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  order_num INT NOT NULL,
  name TEXT NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (project_id, order_num)
);

CREATE TABLE bugs (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
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
  full_text TEXT
);

CREATE TABLE bug_attachments (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  bug_id INT NOT NULL REFERENCES bugs (id) ON DELETE CASCADE,
  file_name TEXT NOT NULL,
  file_content BYTEA NOT NULL,
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE bug_changes (
  id INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  bug_id INT NOT NULL REFERENCES bugs (id) ON DELETE CASCADE,
  old_assigned_user_id INT REFERENCES users (id),
  new_assigned_user_id INT REFERENCES users (id),
  old_state_id INT REFERENCES states (id),
  new_state_id INT REFERENCES states (id),
  old_priority_id INT REFERENCES priorities (id),
  new_priority_id INT REFERENCES priorities (id),
  old_short_text TEXT,
  new_short_text TEXT,
  old_full_text TEXT,
  new_full_text TEXT,
  old_attachment_id INT REFERENCES bug_attachments (id),
  new_attachment_id INT REFERENCES bug_attachments (id),
  comment_text TEXT,
  user_id INT NOT NULL REFERENCES users (id),
  ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
