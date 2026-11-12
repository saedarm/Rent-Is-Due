# Builds the server and the browser game, then runs them on a small Java image.
FROM gradle:8.14-jdk21 AS build
WORKDIR /src
COPY . .
RUN gradle --no-daemon server:installDist web:gdx_teavm_web_js_build

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/server/build/install/server ./server
COPY --from=build /src/web/build/dist/js/webapp ./webapp
ENV WEB_DIR=/app/webapp PORT=8080
EXPOSE 8080
CMD ["./server/bin/server"]
