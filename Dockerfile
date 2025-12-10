# Use Maven with JDK 17 as the base image (SHAFT compatibility with JDK 17 is stable)
FROM maven:3.9.6-eclipse-temurin-21

# Set the working directory in the container
WORKDIR /Shaft-Automation

# Copy the test execution script
COPY scripts/run_tests.sh /Shaft-Automation/run_tests.sh

# Make the script executable
RUN chmod +x /Shaft-Automation/run_tests.sh

# Install essential tools and update package lists
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    curl \
    unzip \
    gnupg \
    vim \
    nano \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Install necessary dependencies
RUN apt-get update && apt-get install -y wget curl unzip gnupg && \
	    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | gpg --dearmor -o /usr/share/keyrings/google-chrome.gpg && \
	    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/google-chrome.gpg] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list && \
	    apt-get update && apt-get install -y google-chrome-stable

# Fetch Chrome version
	RUN CHROME_VERSION=$(google-chrome --version | grep -oE '[0-9.]+' | head -1 | awk -F. '{print $1}') && \
	    echo "Chrome version detected: $CHROME_VERSION"

# Fetch ChromeDriver version based on Chrome version
RUN CHROMEDRIVER_VERSION=$(curl -s "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_$CHROME_VERSION") && \
	    echo "ChromeDriver version detected: $CHROMEDRIVER_VERSION"


# Clean up unnecessary files
	RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# Set Chrome environment variables (used by SHAFT for locating Chrome and ChromeDriver)
ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROME_PATH=/usr/bin/google-chrome

# Install additional SHAFT dependencies (adjust if more are required for your setup)
RUN apt-get update && apt-get install -y --no-install-recommends \
    default-jre \
    default-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Verify installations (optional for debugging)
RUN java -version && \
    mvn -version && \
    google-chrome --version

# Copy project files into the container
COPY . /Shaft-Automation

# Install Allure CLI for report generation
#RUN curl -sL https://github.com/allure-framework/allure2/releases/download/2.21.0/allure-2.21.0.tgz | tar -xz -C /opt/ && \
 #   ln -s /opt/allure-2.21.0/bin/allure /usr/local/bin/allure

# Verify Allure installation
#RUN allure --version

# Environment variable to specify the test group
ENV TEST_GROUP=Shipping

# Default command to run tests
CMD ["sh", "/Shaft-Automation/run_tests.sh"]
