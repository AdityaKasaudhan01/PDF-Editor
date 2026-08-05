# PDF Word Editor - Setup Guide for Windows

## Prerequisites

You need to install the following tools on your Windows machine:

## 1. Install Java 21 (JDK)

**Option A: Using Chocolatey (Recommended)**
```powershell
# Install Chocolatey if you don't have it
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Java 21
choco install openjdk21
```

**Option B: Manual Download**
1. Go to https://adoptium.net/temurin/releases/?version=21
2. Download "OpenJDK 21 LTS" for Windows x64 (MSI installer)
3. Run the installer
4. Add Java to PATH:
   - Search "Edit environment variables" in Windows
   - Add `C:\Program Files\Eclipse Adoptium\jdk-21.*\bin` to PATH

**Verify installation:**
```powershell
java -version
javac -version
```

## 2. Install Maven

**Option A: Using Chocolatey**
```powershell
choco install maven
```

**Option B: Manual Download**
1. Go to https://maven.apache.org/download.cgi
2. Download "Binary zip archive" (e.g., apache-maven-3.9.6-bin.zip)
3. Extract to `C:\Program Files\Apache\maven`
4. Add to PATH:
   - `C:\Program Files\Apache\maven\apache-maven-3.9.6\bin`
5. Set environment variable:
   - `MAVEN_HOME` = `C:\Program Files\Apache\maven\apache-maven-3.9.6`

**Verify installation:**
```powershell
mvn -version
```

## 3. Install Node.js 20+ (LTS)

**Option A: Using Chocolatey**
```powershell
choco install nodejs-lts
```

**Option B: Manual Download**
1. Go to https://nodejs.org/en/download/
2. Download "Windows Installer (.msi)" for LTS version (20.x or 22.x)
3. Run the installer (check "Add to PATH" during installation)
4. Restart your terminal/VS Code

**Verify installation:**
```powershell
node -version
npm -version
```

## 4. Clone the Repository (if not already done)

```powershell
git clone <repository-url>
cd pdf-word-editor
```

## 5. Run the Backend

```powershell
cd 2026-08-04\files-mentioned-by-the-user-you\outputs\pdf-word-editor-phase1\backend
mvn spring-boot:run
```

The backend will start at `http://localhost:8080`

## 6. Run the Frontend (in a new terminal)

```powershell
cd 2026-08-04\files-mentioned-by-the-user-you\outputs\pdf-word-editor-phase1\frontend
npm install
npm run dev
```

The frontend will start at `http://localhost:5173`

## Alternative: Use the Project's Batch Files (Recommended)

If you're having trouble with PATH, you can use these commands directly:

### Backend
```powershell
cd "2026-08-04\files-mentioned-by-the-user-you\outputs\pdf-word-editor-phase1\backend"

# Use full path to Maven
& "C:\Program Files\Apache\maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
```

### Frontend
```powershell
cd "2026-08-04\files-mentioned-by-the-user-you\outputs\pdf-word-editor-phase1\frontend"

# Use full path to Node
& "C:\Program Files\nodejs\npm.cmd" run dev
```

## Troubleshooting

### "mvn is not recognized"
- Ensure Maven's `bin` folder is in your PATH
- Restart your terminal/VS Code after installation
- Try using the full path as shown above

### "vite is not recognized"
- Ensure Node.js is installed
- Run `npm install` in the frontend directory first
- Try using `npx vite` instead of `npm run dev`

### "java is not recognized"
- Ensure JDK 21 is installed
- Ensure `JAVA_HOME` environment variable is set
- Restart your terminal after installation

## Quick Setup Script (PowerShell)

Run this as Administrator to install everything:

```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072

# Install Chocolatey
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Install Java 21, Maven, and Node.js
choco install openjdk21 maven nodejs-lts -y

# Verify installations
java -version
mvn -version
node -version
npm -version
```

After running this script, restart your terminal and try the commands again.
