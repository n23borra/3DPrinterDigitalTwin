-- Seed script for creating an administrator account
-- Generate a bcrypt hash for the password and replace <bcrypt-hash> below.
-- Example (Unix): htpasswd -nbBC 10 "" strongpassword | tr -d ':\n'

-- fichier: postgres/seed_admin.sql
INSERT INTO users (username, email, password_hash, role)
VALUES ('admin', 'admin@cyber.com', '$2y$10$6crrFQ2Jrl3Tekr7PdcJv.7M3ay8cns29J7UpG06kgRt.ldgdz9JW', 'ADMIN');
INSERT INTO users (username, email, password_hash, role)
VALUES ('superadmin', 'superadmin@cyber.com', '$2y$10$6crrFQ2Jrl3Tekr7PdcJv.7M3ay8cns29J7UpG06kgRt.ldgdz9JW',
        'SUPER_ADMIN');
INSERT INTO users (username, email, password_hash, role)
VALUES ('Nathan', 'nathan.bidouille@gmail.com', '$2y$10$6crrFQ2Jrl3Tekr7PdcJv.7M3ay8cns29J7UpG06kgRt.ldgdz9JW',
        'USER');