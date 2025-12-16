# Postgres Setup

`docker-compose` mounts this directory into `/docker-entrypoint-initdb.d`, so `init.sql` will be executed automatically
whenever the database starts with an empty data volume.
Administrator accounts are **not** created automatically.
To add an initial admin user:

1. Generate a bcrypt hash for a strong password.
   Example on Unix:
   ```bash
   htpasswd -nbBC 10 "" strongpassword | tr -d ':\n'
   ```
2. Insert the account using `seed_admin.sql`:
   ```bash
   psql -U cyber -d cyber -f seed_admin.sql
   ```
   Replace `<bcrypt-hash>` in the file with the generated value.


file seed_admin must be after init.sql in the postgres directory, so it is executed after the database is created.