CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  login TEXT NOT NULL,
  pass_hash BYTEA NOT NULL
);

CREATE TABLE projects (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  description TEXT,
  last_visible_bug_id INT NOT NULL DEFAULT 0
);

CREATE TABLE user_access (
  user_id INT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
    UNIQUE (user_id, project_id)
);

CREATE TABLE states (
  code TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE priorities (
  code TEXT NOT NULL PRIMARY KEY,
  name TEXT NOT NULL,
  color TEXT NOT NULL
);

CREATE TABLE project_states (
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  code TEXT NOT NULL REFERENCES states (code),
  order_num INT NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (project_id, code),
    UNIQUE (project_id, order_num)
);

CREATE TABLE project_priorities (
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  code TEXT NOT NULL REFERENCES priorities (code),
  order_num INT NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (project_id, code),
    UNIQUE (project_id, order_num)
);

CREATE TABLE transitions (
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  code_from TEXT NOT NULL,
  code_to TEXT NOT NULL,
  name TEXT NOT NULL,
    UNIQUE (project_id, code_from, code_to),
    FOREIGN KEY (project_id, code_from) REFERENCES project_states (project_id, code) ON DELETE CASCADE,
    FOREIGN KEY (project_id, code_to) REFERENCES project_states (project_id, code) ON DELETE CASCADE
);

CREATE TABLE reports (
  id SERIAL PRIMARY KEY,
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  visible_id INT NOT NULL,
  name TEXT NOT NULL,
  simple_query TEXT,
  json_query TEXT,
    UNIQUE (project_id, visible_id)
);

CREATE TABLE user_states (
  project_id INT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  user_id INT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  default_state_code TEXT NOT NULL,
    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id, default_state_code) REFERENCES project_states (project_id, code) ON DELETE CASCADE
);

CREATE TABLE user_reports (
  user_id INT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  report_id INT NOT NULL REFERENCES reports (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, report_id)
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
  state_code TEXT NOT NULL REFERENCES states (code),
  priority_code TEXT NOT NULL REFERENCES priorities (code),
  short_text TEXT NOT NULL,
  full_text TEXT NOT NULL,
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
  change_id INT NOT NULL PRIMARY KEY REFERENCES changes (id) ON DELETE CASCADE,
  old_assigned_user_id INT REFERENCES users (id),
  new_assigned_user_id INT REFERENCES users (id),
  old_state_code TEXT REFERENCES states (code),
  new_state_code TEXT REFERENCES states (code),
  old_priority_code TEXT REFERENCES priorities (code),
  new_priority_code TEXT REFERENCES priorities (code),
  old_short_text TEXT,
  new_short_text TEXT,
  old_full_text TEXT,
  new_full_text TEXT
);

CREATE TABLE changes_files (
  change_id INT NOT NULL PRIMARY KEY REFERENCES changes (id) ON DELETE CASCADE,
  old_attachment_id INT REFERENCES bug_attachments (id),
  new_attachment_id INT REFERENCES bug_attachments (id)
);

CREATE TABLE changes_comments (
  change_id INT NOT NULL PRIMARY KEY REFERENCES changes (id) ON DELETE CASCADE,
  comment_text TEXT NOT NULL,
  delete_ts TIMESTAMP,
  delete_user_id INT REFERENCES users (id)
);

CREATE TABLE comment_attachments (
  id SERIAL PRIMARY KEY,
  change_id INT NOT NULL REFERENCES changes (id) ON DELETE CASCADE,
  file_name TEXT NOT NULL,
  file_content BYTEA NOT NULL
);
