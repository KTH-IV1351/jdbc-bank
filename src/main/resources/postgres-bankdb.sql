CREATE TABLE "holder"
(
  "holder_id" SERIAL PRIMARY KEY,
  "name" VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE "account"
(
  "account_id" SERIAL PRIMARY KEY, -- This is the PK, which is the database id.
  "account_no" VARCHAR(10), -- This is the account number, which is the business id.
  "balance" INT,
  "holder_id" INT NOT NULL REFERENCES "holder" ON DELETE CASCADE
);
