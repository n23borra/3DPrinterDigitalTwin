# CyberAssessment

CyberAssessment is a risk management platform built with a Spring Boot backend and a Vite/React frontend. The
application helps organisations map their assets, evaluate cyber risks and plan remediation actions in line with
ISO&nbsp;27002 recommendations.

For a full description in French see [`todo/software.txt`](todo/software.txt).

## Quick start with Docker Compose

1. Create a `.env` file **in the project root** with at least `JWT_SECRET`.
   Optionally set `MOSP_API_KEY=<your token>` to refresh the reference JSON
   files and provide `MAIL_USERNAME` and `MAIL_PASSWORD` with the Gmail account
   credentials used to send emails. The API reads `CORS_ORIGINS` as a comma
   separated list (default: `http://localhost:3000`) to determine which origins
   may access it via CORS.

2. Build and launch all services:
   ```bash
   docker compose up --build
   ```
   This will start PostgreSQL, the Spring Boot API server and the Nginx container serving the React application.



## Implementation on a Server
This section explains how to deploy **CyberAssessment** on a remote Ubuntu server using **Docker** and **Docker Compose**.  
The following example assumes you already have SSH access to the server.

### 1. Install prerequisites

Make sure Git and Docker are installed on the server:
```bash
git --version
docker --version
docker-compose --version
```

If any of these commands return an error, install them using the following commands:

```bash
sudo apt update
sudo apt install git docker.io
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu noble stable" \
  | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER
sudo reboot
sudo systemctl enable docker
sudo systemctl start docker
```

### 2. Generate SSH key for GitHub access
First, you need to generate an SSH key on the server if you haven't done so already:

```bash
ssh-keygen -t ed25519 -C "your_github_email@example.com"
```
Press enter to accept the default file location and set a passphrase if desired.
After generating the key, display the public key with the following command:

```bash
cat ~/.ssh/id_ed25519.pub
```
Copy the key (start with ssh-ed25519 and finish with your email address)
Then, add the generated public key to your GitHub account under **Settings > SSH and GPG keys > New SSH key**.

Try the SSH connection to GitHub to ensure everything is set up correctly:

```bash
ssh -T git@github.com
```
You should see a success message indicating that you have successfully authenticated.

### 3. Clone the CyberAssessment repository
On GitHub, on the CyberAssessment repo page, click the Code button > SSH tab, you will see a URL like this:

```bash
git clone git@github.com:yourUser/CyberAssessment.git
```
This should create a folder with the project name. Check with :

```bash
ls
cd your_repo_name
ls -la
```

### 4. Build and run with Docker Compose

Do the following steps to after updating `CORS_ORIGINS:` in `docker-compose.yml` and `VITE_API_URL` in `frontend/.env` file with the required environment variables:
```bash
docker compose down
docker compose build backend frontend
docker compose up -d
```

After these steps, the application should be accessible via your server's IP address or domain name.

You can you start the application with:
```bash
docker compose up --build -d
```

This launches all containers in the background:

PostgreSQL database
Spring Boot API
Nginx serving the React frontend


## Running tests

Backend unit tests can be executed with Maven:

```bash
cd backend
./mvnw test
```

## Import scripts

The following helper scripts can populate the database:

* `scripts/import_faith_excel.py` – import the FAITH Gold Table from `data/` into PostgreSQL.
* `scripts/import_local_json.py` – load the JSON files located under `data/database`.
* `scripts/update_json_from_mosp.py` – refresh those JSON files by downloading the
  latest English reference data from the MOSP API (requires `MOSP_API_KEY`). The
  script skips French entries and avoids creating duplicate files.

Both scripts rely on the database settings used in `docker-compose.yml`.

The `local_json_importer` service in `docker-compose.yml` automatically runs
`import_local_json.py` once PostgreSQL is ready. If `MOSP_API_KEY` is present,
it first calls `update_json_from_mosp.py` to refresh the JSON files before
loading them into PostgreSQL, so no manual installation is required.

## API URL configuration

The application uses the `VITE_API_URL` environment variable to know where the
backend API is located. Copy `.env.example` to `.env` and edit the value if the
default does not suit your environment:

```bash
cp .env.example .env
# then modify VITE_API_URL as needed
```

During local development it should usually remain set to
`http://localhost:8080/api`. When deploying to production, set the variable to
the URL of your API server.
