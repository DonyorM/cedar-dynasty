FROM public.ecr.aws/docker/library/clojure:openjdk-11-tools-deps-slim-bullseye AS build
WORKDIR /app

COPY deps.edn deps.edn
RUN clojure -A:build:dev -M -e ::ok   # preload and cache dependencies, only reruns if deps.edn changes

COPY .git .git
COPY shadow-cljs.edn shadow-cljs.edn
COPY src src
COPY src-build src-build
COPY src-dev src-dev
COPY resources resources

CMD clj -A:dev -X dev/-main