CREATE TABLE holder
(
  holder_id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) UNIQUE NOT NULL,
  PRIMARY KEY (holder_id)
);

CREATE TABLE account
(
  account_id INT NOT NULL AUTO_INCREMENT, -- This is the PK, which is the database id.
  account_no VARCHAR(10), -- This is the account number, which is the business id.
  balance INT,
  holder_id INT NOT NULL REFERENCES holder ON DELETE CASCADE,
  PRIMARY KEY(account_id)
);
