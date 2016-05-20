CREATE TABLE "chapter" (
"id" BIGINT IDENTITY,
"label" VARCHAR(64) NOT NULL);

ALTER TABLE "chapter" ADD CONSTRAINT "chapter-unique-label" UNIQUE ("label");

CREATE TABLE "tag" (
"id" BIGINT IDENTITY,
"label" VARCHAR(64) NOT NULL);

ALTER TABLE "tag" ADD CONSTRAINT "tag-unique-label" UNIQUE ("label");

CREATE TABLE "user" (
"id" BIGINT IDENTITY,
"firstname" VARCHAR(64) NOT NULL,
"lastname" VARCHAR(64) NOT NULL,
"email" VARCHAR(128) NOT NULL,
"phone" VARCHAR(20),
"passwd" VARCHAR(128),
"failed-logins" SMALLINT NOT NULL DEFAULT 0,
"roles" VARCHAR(256),
"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
"modified" TIMESTAMP NOT NULL AS CURRENT_TIMESTAMP());

ALTER TABLE "user" ADD CONSTRAINT "user-unique-email" UNIQUE ("email");

INSERT INTO "user" ("firstname", "lastname", "email", "phone", "passwd", "roles")
  VALUES ('Martina', 'Kalovská', 'm.kalovska@didaktis.cz', '',
'$s0$f0801$rK7k1fqX+anvoVBb4QhZxQ==$Fnn1W8bxgNQgh5u6H5bF7uZWk40l32gOJoF8OdniS04=', 'admin');
INSERT INTO "user"  ("firstname", "lastname", "email", "phone", "passwd", "roles")
  VALUES ('Karel', 'Miarka', 'karel.miarka@seznam.cz', '702 573 669',
'$s0$f0801$rK7k1fqX+anvoVBb4QhZxQ==$Fnn1W8bxgNQgh5u6H5bF7uZWk40l32gOJoF8OdniS04=', 'admin');

CREATE TABLE "imagionary" (
"id" BIGINT IDENTITY,
"chapter-id" BIGINT NOT NULL,
"word" VARCHAR(32) NOT NULL,
"img-filename" VARCHAR(32) NOT NULL,
"copyright" VARCHAR(64) NOT NULL,
"explanation" VARCHAR(128) NOT NULL,
"notes" VARCHAR(256),
"syllables" TINYINT,
"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
"modified" TIMESTAMP NOT NULL AS CURRENT_TIMESTAMP());

ALTER TABLE "imagionary" ADD CONSTRAINT "imagionary-unique-word" UNIQUE ("word");

CREATE TABLE "imagionary-tag" (
"id" BIGINT IDENTITY,
"imagionary-id" BIGINT NOT NULL,
"tag-id" BIGINT NOT NULL,
"created" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP());

ALTER TABLE "imagionary-tag" ADD CONSTRAINT "fk-imagionary-tag-to-img"
  FOREIGN KEY ("imagionary-id") REFERENCES "imagionary" ("id");
ALTER TABLE "imagionary-tag" ADD CONSTRAINT "fk-imagionary-tag-to-tag"
  FOREIGN KEY ("tag-id") REFERENCES "tag" ("id");
