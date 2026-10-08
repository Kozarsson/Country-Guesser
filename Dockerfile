FROM eclipse-temurin:17-jdk

ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

# Install Android command-line tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    unzip wget \
    && rm -rf /var/lib/apt/lists/*

RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdtools.zip \
    && mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools \
    && unzip -q /tmp/cmdtools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm /tmp/cmdtools.zip

# Accept licenses and install required SDK components
RUN yes | sdkmanager --licenses > /dev/null \
    && sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

WORKDIR /app

COPY . .

RUN sed -i 's/\r$//' gradlew && chmod +x ./gradlew

RUN ./gradlew --no-daemon lint test assembleDebug \
    && mkdir -p /output \
    && cp app/build/outputs/apk/debug/app-debug.apk /output/country-guesser-debug.apk

CMD ["sh", "-c", "ls -lh /output/country-guesser-debug.apk"]